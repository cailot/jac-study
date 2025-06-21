package hyung.jin.seo.jae.service.impl;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hyung.jin.seo.jae.dto.CycleDTO;
import hyung.jin.seo.jae.dto.StudentAccount;
import hyung.jin.seo.jae.model.Student;
import hyung.jin.seo.jae.model.User;
import hyung.jin.seo.jae.service.CycleService;
import hyung.jin.seo.jae.service.LoginActivityService;
import hyung.jin.seo.jae.service.StudentAccountService;
import hyung.jin.seo.jae.utils.JaeConstants;
import hyung.jin.seo.jae.utils.JaeUtils;
import hyung.jin.seo.jae.repository.EnrolmentRepository;
import hyung.jin.seo.jae.repository.StudentRepository;
import hyung.jin.seo.jae.repository.UserRepository;

@Service
public class StudentAccountServiceImpl implements StudentAccountService {

	private static final Logger logger = LoggerFactory.getLogger(StudentAccountServiceImpl.class);

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EnrolmentRepository enrolmentRepository;

	@Autowired
	private ConfigurableApplicationContext applicationContext;

	@Autowired
	private CycleService cycleService;

	@Autowired
	private LoginActivityService loginActivityService;

	@Autowired
	private HttpSession session;

	private List<CycleDTO> cycles;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			logger.debug("Attempting login for username: {}", username);
			boolean isStudent = username.length() == 8;
			
			if (isStudent) { // normal case for student
				logger.debug("Processing student login...");
				Object[] result = studentRepository.checkStudentAccount(Long.parseLong(username));
				
				if (result != null && result.length > 0) {
					logger.debug("Found student account in database");
					Object[] obj = (Object[]) result[0];
					StudentAccount account = new StudentAccount(obj);
					
					// Check enrolment is valid if student is active
					int currentYear = getYear();
					int currentWeek = getWeek();
					logger.debug("Checking enrollment for Year: {}, Week: {}", currentYear, currentWeek);

					////////////////////////////////////////////////////////////////////////////////
					// if currentWeek is first week, then check previous year last week
					// if(currentWeek==1) {
					// 	currentYear = currentYear - 1;
					// 	currentWeek = cycleService.lastAcademicWeek(currentYear);
					// 	logger.debug("First week detected, checking previous year. New Year: {}, Week: {}", currentYear, currentWeek);				
					// }
					///////////////////////////////////////////////////////////////////////////////

					// Check if student is enrolled in any class for current week
					List<Object[]> enrols = enrolmentRepository.checkEnrolmentTime(Long.parseLong(username), currentYear, currentWeek);
					if (enrols == null || enrols.isEmpty()) {
						// No enrolment
						logger.debug("No valid enrollment found for student");
						account.setEnabled(JaeConstants.INACTIVE);
						throw new DisabledException("User enrolment is not valid");
					}
					logger.debug("Valid enrollment found for student");
					for(Object[] enrol : enrols){
						String enrolId = enrol[0].toString();
						String enrolGrade = enrol[1].toString();
						// Set the grade in the account authorities
						account.setGrade(enrolGrade);
						logger.debug("Enrolled Id - {} and Grade - {}", enrolId, enrolGrade);
					}
					
					String fromWhere = (String) session.getAttribute("referer");
					// keep login entry if user is connected from login page
					if(fromWhere!=null && fromWhere.contains(JaeConstants.CONNECTED_FROM)) {
						logger.debug("Saving login activity for connected student");
						// add login activity
						loginActivityService.saveLoginActivity(Long.parseLong(username));
					}
					logger.debug("Student login successful");
					return account;
				} else {
					logger.debug("Student not found in database");
					throw new UsernameNotFoundException("Student not found");
				}
			} else { // admin case
				logger.debug("Processing admin login...");
				Object[] result = userRepository.checkUserAccount(username);
				if (result != null && result.length > 0) {
					logger.debug("Admin login successful");
					Object[] obj = (Object[]) result[0];
					User account = new User(obj);                
					return account;
				} else {
					logger.debug("Admin not found in database");
					throw new UsernameNotFoundException("Admin not found");
				}
			}
		} catch (Exception e) {
			logger.error("Login failed for username: {}. Error: {}", username, e.getMessage());
			throw new UsernameNotFoundException("User: " + username + " was not found in the database", e);
		}
	}

	@Override
	@Transactional
	public void updatePassword(Long id, String password) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(password);
		try{
			studentRepository.updatePassword(id, encodedPassword);
		}catch(Exception e){
			System.out.println("No student found");
		}	
	}

	@Override
	public Student getStudent(Long id) {
		Student std = null;
		try{
			std = studentRepository.findById(id).get();
		}catch(Exception e){
			System.out.println("No student found");
		}
		// studentRepository.findById(id).get();	
		return std;
	}

	// get current year
	private int getYear(){
		if(cycles==null) {
			cycles = (List<CycleDTO>) applicationContext.getBean(JaeConstants.ACADEMIC_CYCLES);
		}
		int year = 0;
		for(CycleDTO dto: cycles) {
			String startDate = dto.getStartDate();
			String endDate = dto.getEndDate();
			try {
				if(JaeUtils.checkIfTodayBelongTo(startDate, endDate)) {
					year =  Integer.parseInt(dto.getYear());
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return year;
	}

	// get current week
	public int getWeek(){
		LocalDate today = LocalDate.now();
		int currentYear = today.getYear();
		int academicYear = getYear();
		int weeks = 0;
		String academicDate = "";
		String vacationStartDate = "";
		String vacationEndDate = "";
		// bring academic start date
		for(CycleDTO dto : cycles){
			if(dto.getYear().equals(Integer.toString(academicYear))){
				academicDate = dto.getStartDate();
				vacationStartDate = dto.getVacationStartDate();
				vacationEndDate = dto.getVacationEndDate();
				break;
			}
		}
		LocalDate academicStart = LocalDate.parse(academicDate);
		LocalDate vacationStart = LocalDate.parse(vacationStartDate);
		LocalDate vacationEnd = LocalDate.parse(vacationEndDate);
		
		if(currentYear==academicYear) { // from June to December
			// compare today's date with vacation start date
			if(today.isBefore(vacationStart)) { // simply calculate weeks
				weeks = (int) ChronoUnit.WEEKS.between(academicStart, today);
			}else { // set weeks as xmas week
				weeks = (int) ChronoUnit.WEEKS.between(academicStart, vacationStart) - 1;
			}
		}else { // from January to June
			// simply calculate since last year starting date - 3 weeks (xmas holidays)
			// compare today's date with vacation end date
			if(today.isBefore(vacationEnd)) { // until vacation start date
				weeks = (int) ChronoUnit.WEEKS.between(academicStart, vacationStart) - 1;
			}else{
				weeks = (int) ChronoUnit.WEEKS.between(academicStart, today) - 3; // 3 weeks for xmas holidays
			}		
		}
		return (weeks+1); // calculation must start from 1 not 0
	}


}

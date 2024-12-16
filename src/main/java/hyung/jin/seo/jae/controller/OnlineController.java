package hyung.jin.seo.jae.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import hyung.jin.seo.jae.dto.OnlineSessionDTO;
import hyung.jin.seo.jae.model.OnlineActivity;
import hyung.jin.seo.jae.model.Student;
import hyung.jin.seo.jae.service.CodeService;
import hyung.jin.seo.jae.service.CycleService;
import hyung.jin.seo.jae.service.EnrolmentService;
import hyung.jin.seo.jae.service.LoginActivityService;
import hyung.jin.seo.jae.service.OnlineActivityService;
import hyung.jin.seo.jae.service.OnlineSessionService;
import hyung.jin.seo.jae.service.StudentService;
import hyung.jin.seo.jae.utils.JaeConstants;
import hyung.jin.seo.jae.utils.JaeUtils;

@Controller
@RequestMapping("elearning")
public class OnlineController {

	@Autowired
	private EnrolmentService enrolmentService;

	@Autowired
	private OnlineSessionService onlineSessionService;

	@Autowired
	private StudentService studentService;

	@Autowired
	private CycleService cycleService;

	@Autowired
	private CodeService codeService;

	@Autowired
	private LoginActivityService loginActivityService;

	@Autowired
	private OnlineActivityService onlineActivityService;

	// get online course url
	@GetMapping("/getLive/{id}/{year}/{week}")
	@ResponseBody
	public List<OnlineSessionDTO> getOnlineLive(@PathVariable("id") long id, @PathVariable("year") int year, @PathVariable("week") int week) {	
		// 1. get clazzId via Enrolment with parameters - studentId, year, week, online
		Long clazzId = enrolmentService.findClazzId4OnlineSession(id, year, week);
		// 2. get OnlineSession by clazzId, set (week-1)
		List<OnlineSessionDTO> dtos = onlineSessionService.findSessionByClazzNWeek(clazzId, week-1);
		// 4. return OnlineSessionDTO
		return dtos;
	}

	// get online course url
	@GetMapping("/getRecord/{id}/{year}/{week}/{set}")
	@ResponseBody
	public List<OnlineSessionDTO> getOnlineRecorded(@PathVariable("id") long id, @PathVariable("year") int year, @PathVariable("week") int week, @PathVariable("set") int set) {	
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		// if week is first week of academic year, check student's register date is more than a month.
		////////////////////////////////////////////////////////////////////////////////////////////////
		if(week == JaeConstants.FIRST_WEEK){
			Student std = studentService.getStudent(id);
			LocalDate regDate = std.getRegisterDate();
			// check if regDate is less than last month compared with today
			LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
			if (regDate.isBefore(oneMonthAgo)) {
				// regDate is more than one month ago which means existing student so show last week of previous grade unless grade = TT8
				// check grade is TT or not
				String grade = std.getGrade(); // TT8's code is 12
				// TT8 needs last week on TT8
				if ((JaeConstants.TT6_CODE.equals(grade))||(JaeConstants.TT8_CODE.equals(grade))) {
					// get last week of last year
					int lastWeek = cycleService.lastAcademicWeek(year-1);
					// get OnlineSession by grade, year, week, for example> 2024, 50th week
					List<OnlineSessionDTO> dtos = onlineSessionService.getOnlineSessionByGradeNSetNYear(grade, lastWeek, year-1);
					return dtos;
				}else{
					// get last week of previous grade
					int lastWeek = cycleService.lastAcademicWeek(year-1);
					// get previous grade
					String previousGrade = codeService.getPreviousGrade(grade);
					// check if previous grade = 0, it means no need to show
					if(JaeConstants.NO_PREVIOUS_GRADE.equals(previousGrade)){
						return new ArrayList(); // return empty OnlineSessionDTO to avoid NullPointerException
					}else{
						// get OnlineSession by previous grade, last year, last week, for example> 2024, 50th week
						List<OnlineSessionDTO> dtos = onlineSessionService.getOnlineSessionByGradeNSetNYear(previousGrade, lastWeek, year-1);
						return dtos;
					}
				}

			} else {
				// regDate is within the last month, it means new student so no need to show 49th week of previous grade
				System.out.println("Registration date is within the last month.");
				return new ArrayList(); // return empty OnlineSessionDTO to avoid NullPointerException
			}
		}
		// 1. get clazzId via Enrolment with parameters - studentId, year, week, online
		Long clazzId = enrolmentService.findClazzId4OnlineSession(id, year, week);
		// 2. get OnlineSession by clazzId, set (week-1)
		List<OnlineSessionDTO> dtos = onlineSessionService.findSessionByClazzNWeek(clazzId, set);
		// 4. return OnlineSessionDTO
		return dtos;
	}


	// keep start time for online watching
	@GetMapping("/startWatch/{studentId}/{id}")	
	@ResponseBody
	public ResponseEntity<String> saveStartWatch(@PathVariable("studentId") long studentId, @PathVariable("id") long id) {	
		// print out student id and week with timestamp
		System.out.println(">>>>>>> Start time - Student ID: " + studentId + " ID: " + id + " Time: " + System.currentTimeMillis());
		OnlineActivity activity = onlineActivityService.getOnlineActivity(studentId, id);
		if(activity == null){
			// save online activity
			onlineActivityService.addOnlineActivity(studentId, id);
		}else{ 
			// if status = 2 (completed), then no need to keep track again
			if(activity.getStatus() == JaeConstants.STATUS_COMPLETED){
				return ResponseEntity.ok("Already completed");
			}
			// update online activity
			LocalDateTime now = LocalDateTime.now();
			activity.setStartDateTime(now);
			activity.setStatus(JaeConstants.STATUS_PROCESSING);
			activity.setEndDateTime(null);
			onlineActivityService.updateOnlineActivity(activity, activity.getId());
		}
		return ResponseEntity.ok("Start log enters successfully in the server");
	}
	
	// keep end time for online watching
	@GetMapping("/endWatch/{studentId}/{id}")
	@ResponseBody
	public ResponseEntity<String> saveEndWatch(@PathVariable("studentId") long studentId, @PathVariable("id") long id) {	
		// print out student id and week with timestamp
		System.out.println(">>>>>>> End time - Student ID: " + studentId + " ID: " + id + " Time: " + System.currentTimeMillis());
		OnlineActivity activity = onlineActivityService.getOnlineActivity(studentId, id);
		if(activity != null && activity.getStatus() != JaeConstants.STATUS_COMPLETED){
			// update online activity
			LocalDateTime now = LocalDateTime.now();
			activity.setEndDateTime(now);
			// check if watching is completed or not
			long watching = JaeUtils.calculateDurationInMinutes(activity.getStartDateTime(), activity.getEndDateTime());
			long lecture = JaeUtils.calculateDurationInMinutes(activity.getOnlineSession().getStartTime(), activity.getOnlineSession().getEndTime());
			if(isCompleted(lecture, watching)){
				activity.setStatus(JaeConstants.STATUS_COMPLETED);
			}
			onlineActivityService.updateOnlineActivity(activity, activity.getId());
		}
		return ResponseEntity.ok("End log enters successfully in the server");
	}

	// store login activity
	@GetMapping("/checkLogin/{studentId}")
	public ResponseEntity<String> storeLoginActivity(@PathVariable("studentId") long studentId) {	
		loginActivityService.saveLoginActivity(studentId);
		return ResponseEntity.ok("End log enters successfully in the server");
	}

	// check if watching is completed or not
	private boolean isCompleted(long lecture, long watching){
		// if watching time is more than 80% of lecture time, then it is completed
		if(watching > lecture * 0.8){
			return true;
		}else{
			return false;
		}
	}

}

package hyung.jin.seo.jae.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hyung.jin.seo.jae.dto.ExtraworkDTO;
import hyung.jin.seo.jae.dto.HomeworkDTO;
import hyung.jin.seo.jae.dto.HomeworkScheduleDTO;
import hyung.jin.seo.jae.dto.PracticeAnswerDTO;
import hyung.jin.seo.jae.dto.PracticeDTO;
import hyung.jin.seo.jae.dto.PracticeScheduleDTO;
import hyung.jin.seo.jae.dto.SimpleBasketDTO;
import hyung.jin.seo.jae.dto.StudentPracticeDTO;
import hyung.jin.seo.jae.dto.StudentTestDTO;
import hyung.jin.seo.jae.dto.TestAnswerDTO;
import hyung.jin.seo.jae.dto.TestDTO;
import hyung.jin.seo.jae.dto.TestScheduleDTO;
import hyung.jin.seo.jae.model.Extrawork;
import hyung.jin.seo.jae.model.ExtraworkProgress;
import hyung.jin.seo.jae.model.Homework;
import hyung.jin.seo.jae.model.HomeworkProgress;
import hyung.jin.seo.jae.model.HomeworkSchedule;
import hyung.jin.seo.jae.model.Practice;
import hyung.jin.seo.jae.model.PracticeAnswer;
import hyung.jin.seo.jae.model.PracticeSchedule;
import hyung.jin.seo.jae.model.StudentPractice;
import hyung.jin.seo.jae.model.StudentTest;
import hyung.jin.seo.jae.model.Test;
import hyung.jin.seo.jae.model.TestAnswer;
import hyung.jin.seo.jae.model.TestAnswerItem;
import hyung.jin.seo.jae.repository.ExtraworkProgressRepository;
import hyung.jin.seo.jae.repository.ExtraworkRepository;
import hyung.jin.seo.jae.repository.HomeworkProgressRepository;
import hyung.jin.seo.jae.repository.HomeworkRepository;
import hyung.jin.seo.jae.repository.HomeworkScheduleRepository;
import hyung.jin.seo.jae.repository.PracticeAnswerRepository;
import hyung.jin.seo.jae.repository.PracticeRepository;
import hyung.jin.seo.jae.repository.PracticeScheduleRepository;
import hyung.jin.seo.jae.repository.StudentPracticeRepository;
import hyung.jin.seo.jae.repository.StudentTestRepository;
import hyung.jin.seo.jae.repository.TestAnswerRepository;
import hyung.jin.seo.jae.repository.TestRepository;
import hyung.jin.seo.jae.repository.TestScheduleRepository;
import hyung.jin.seo.jae.service.ConnectedService;

@Service
public class ConnectedServiceImpl implements ConnectedService {
	
	@Autowired
	private HomeworkRepository homeworkRepository;

	@Autowired
	private ExtraworkRepository extraworkRepository;

	@Autowired
	private PracticeRepository practiceRepository;

	@Autowired
	private PracticeAnswerRepository practiceAnswerRepository;

	@Autowired
	private StudentPracticeRepository studentPracticeRepository;

	@Autowired
	private TestRepository testRepository;

	@Autowired
	private TestAnswerRepository testAnswerRepository;

	@Autowired
	private StudentTestRepository studentTestRepository;

	@Autowired
	private PracticeScheduleRepository practiceScheduleRepository;

	@Autowired
	private TestScheduleRepository testScheduleRepository;

	@Autowired
	private HomeworkScheduleRepository homeworkScheduleRepository;

	@Autowired
	private HomeworkProgressRepository homeworkProgressRepository;

	@Autowired
	private ExtraworkProgressRepository extraworkProgressRepository;

	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	@Override
	public List<Homework> allHomeworks() {
		List<Homework> dtos = new ArrayList<>();
		try{
			dtos = homeworkRepository.findAll();
		}catch(Exception e){
			System.out.println("No Homework found");
		}
		return dtos;
	}

	@Override
	public List<Extrawork> allExtraworks() {
		List<Extrawork> dtos = new ArrayList<>();
		try{
			dtos = extraworkRepository.findAll();
		}catch(Exception e){
			System.out.println("No Extrawork found");
		}
		return dtos;
	}

	@Override
	public List<Practice> allPractices() {
		List<Practice> dtos = new ArrayList<>();
		try{
			dtos = practiceRepository.findAll();
		}catch(Exception e){
			System.out.println("No Practice found");
		}
		return dtos;
	}

	@Override
	public List<Test> allTests() {
		List<Test> dtos = new ArrayList<>();
		try{
			dtos = testRepository.findAll();
		}catch(Exception e){
			System.out.println("No Test found");
		}
		return dtos;
	}

	@Override
	public List<HomeworkSchedule> allHomeworkSchedules() {
		List<HomeworkSchedule> dtos = new ArrayList<>();
		try{
			dtos = homeworkScheduleRepository.findAll();
		}catch(Exception e){
			System.out.println("No Homework Schedule found");
		}
		return dtos;
	}

	@Override
	public List<PracticeSchedule> allPracticeSchedules() {
		List<PracticeSchedule> dtos = new ArrayList<>();
		try{
			dtos = practiceScheduleRepository.findAll();
		}catch(Exception e){
			System.out.println("No Practice Schedule found");
		}
		return dtos;
	}

	@Override
	public Homework getHomework(Long id) {
		Optional<Homework> work = homeworkRepository.findById(id);
		if(!work.isPresent()) return null;
		return work.get();
	}

	@Override
	public Extrawork getExtrawork(Long id) {
		Optional<Extrawork> work = extraworkRepository.findById(id);
		if(!work.isPresent()) return null;
		return work.get();
	}

	@Override
	public Practice getPractice(Long id) {
		Optional<Practice> work = practiceRepository.findById(id);
		if(!work.isPresent()) return null;
		return work.get();
	}

	@Override
	public PracticeDTO getPracticeInfo(Long id) {
		PracticeDTO dto = practiceRepository.getPracticeById(id);
		return dto;
	}

	@Override
	public PracticeAnswer getPracticeAnswer(Long id) {
		Optional<PracticeAnswer> answer = practiceAnswerRepository.findById(id);
		if(!answer.isPresent()) return null;
		return answer.get();
	}

	@Override
	public StudentPractice getStudentPractice(Long id) {
		Optional<StudentPractice> sp = studentPracticeRepository.findById(id);
		if(!sp.isPresent()) return null;
		return sp.get();
	}

	@Override
	public Test getTest(Long id) {
		Optional<Test> work = testRepository.findById(id);
		if(!work.isPresent()) return null;
		return work.get();
	}

	@Override
	public TestDTO getTestInfo(Long id) {
		TestDTO dto = testRepository.getTestById(id);
		return dto;
	}

	@Override
	public TestAnswer getTestAnswer(Long id) {
		Optional<TestAnswer> test = testAnswerRepository.findById(id);
		if(!test.isPresent()) return null;
		return test.get();
	}

	@Override
	public StudentTest getStudentTest(Long id) {
		Optional<StudentTest> test = studentTestRepository.findById(id);
		if(!test.isPresent()) return null;
		return test.get();
	}

	@Override
	public HomeworkSchedule getHomeworkSchedule(Long id) {
		Optional<HomeworkSchedule> schedule = homeworkScheduleRepository.findById(id);
		if(!schedule.isPresent()) return null;
		return schedule.get();
	}

	@Override
	public HomeworkProgress getHomeworkProgress(Long id) {
		Optional<HomeworkProgress> progress = homeworkProgressRepository.findById(id);
		if(!progress.isPresent()) return null;
		return progress.get();
	}

	@Override
	public ExtraworkProgress getExtraworkProgress(Long id) {
		Optional<ExtraworkProgress> progress = extraworkProgressRepository.findById(id);
		if(!progress.isPresent()) return null;
		return progress.get();
	}

	@Override
	public PracticeSchedule getPracticeSchedule(Long id) {
		Optional<PracticeSchedule> test = practiceScheduleRepository.findById(id);
		if(!test.isPresent()) return null;
		return test.get();
	}

	// @SuppressWarnings("null")
	// @Override
	// @Transactional
	// public Homework addHomework(Homework work) {
	// 	Homework home = homeworkRepository.save(work);
	// 	return home;
	// }

	// @SuppressWarnings("null")
	// @Override
	// @Transactional
	// public Extrawork addExtrawork(Extrawork work) {
	// 	Extrawork extra = extraworkRepository.save(work);
	// 	return extra;
	// }

	// @SuppressWarnings("null")
	// @Override
	// @Transactional
	// public Practice addPractice(Practice practice) {
	// 	Practice prac = practiceRepository.save(practice);
	// 	return prac;
	// }

	// @SuppressWarnings("null")
	// @Override
	// @Transactional
	// public PracticeAnswer addPracticeAnswer(PracticeAnswer ans) {
	// 	PracticeAnswer answer = practiceAnswerRepository.save(ans);
	// 	return answer;
	// }

	@SuppressAjWarnings("null")
	@Override
	@Transactional
	public StudentPractice addStudentPractice(StudentPractice crs) {
		StudentPractice sp = studentPracticeRepository.save(crs);
		return sp;
	}

	// @SuppressAjWarnings("null")
	// @Override
	// @Transactional
	// public Test addTest(Test crs) {
	// 	Test test = testRepository.save(crs);
	// 	return test;
	// }

	// @SuppressAjWarnings("null")
	// @Override
	// @Transactional
	// public TestAnswer addTestAnswer(TestAnswer crs) {
	// 	TestAnswer answer = testAnswerRepository.save(crs);
	// 	return answer;
	// }

	@SuppressAjWarnings("null")
	@Override
	@Transactional
	public StudentTest addStudentTest(StudentTest crs) {
		StudentTest test = studentTestRepository.save(crs);
		return test;
	}

	// @SuppressAjWarnings("null")
	// @Override
	// @Transactional
	// public HomeworkSchedule addHomeworkSchedule(HomeworkSchedule schedule) {
	// 	HomeworkSchedule home = homeworkScheduleRepository.save(schedule);
	// 	return home;
	// }

	@SuppressAjWarnings("null")
	@Override
	@Transactional
	public HomeworkProgress addHomeworkProgress(HomeworkProgress progress) {
		HomeworkProgress home = homeworkProgressRepository.save(progress);
		return home;
	}

	@SuppressAjWarnings("null")
	@Override
	@Transactional
	public ExtraworkProgress addExtraworkProgress(ExtraworkProgress progress) {
		ExtraworkProgress extra = extraworkProgressRepository.save(progress);
		return extra;
	}

	// @SuppressAjWarnings("null")
	// @Override
	// @Transactional
	// public PracticeSchedule addPracticeSchedule(PracticeSchedule ps) {
	// 	PracticeSchedule schedule = practiceScheduleRepository.save(ps);
	// 	return schedule;
	// }

	// @Override
	// @Transactional
	// public Homework updateHomework(Homework newWork, Long id) {
	// 	// search by getId
	// 	Homework existing = homeworkRepository.findById(id).get();
    //     // Update info
    //     String newVideoPath = StringUtils.defaultString(newWork.getVideoPath());
    //     // if(StringUtils.isNotBlank(newVideoPath)){
    //     	existing.setVideoPath(newVideoPath);
    //     // }
	// 	String newPdfPath = StringUtils.defaultString(newWork.getPdfPath());
    //     // if(StringUtils.isNotBlank(newPdfPath)){
    //     	existing.setPdfPath(newPdfPath);
    //     // }
    //     String newInfo = StringUtils.defaultString(newWork.getInfo());
    //     // if(StringUtils.isNotBlank(newInfo)){
    //     	existing.setInfo(newInfo);
    //     // }
	// 	int newWeek = newWork.getWeek();
	// 	existing.setWeek(newWeek);
	// 	// int newYear = newWork.getYear();
	// 	// existing.setYear(newYear);
	// 	boolean newActive = newWork.isActive();
	// 	existing.setActive(newActive);
    //     // update the existing record
    //     Homework updated = homeworkRepository.save(existing);
    //     return updated;
	// }

	// @Override
	// @Transactional
	// public Extrawork updateExtrawork(Extrawork newWork, Long id) {
	// 	// search by getId
	// 	Extrawork existing = extraworkRepository.findById(id).get();
    //     // Update info
    //     String newVideoPath = StringUtils.defaultString(newWork.getVideoPath());
    //     // if(StringUtils.isNotBlank(newVideoPath)){
    //     	existing.setVideoPath(newVideoPath);
    //     // }
	// 	String newPdfPath = StringUtils.defaultString(newWork.getPdfPath());
    //     // if(StringUtils.isNotBlank(newPdfPath)){
    //     	existing.setPdfPath(newPdfPath);
    //     // }
    //     String newName = StringUtils.defaultString(newWork.getName());
    //     // if(StringUtils.isNotBlank(newName)){
    //     	existing.setName(newName);
    //     // }
	// 	boolean newActive = newWork.isActive();
	// 	existing.setActive(newActive);
    //     // update the existing record
    //     Extrawork updated = extraworkRepository.save(existing);
    //     return updated;
	// }

	// @Override
	// @Transactional
	// public Practice updatePractice(Practice newWork, Long id) {
	// 	// search by getId
	// 	Practice existing = practiceRepository.findById(id).get();
    //     // Update info
    //     String newPdfPath = StringUtils.defaultString(newWork.getPdfPath());
    //     // if(StringUtils.isNotBlank(newPdfPath)){
    //     	existing.setPdfPath(newPdfPath);
    //     // }
	// 	String newInfo = StringUtils.defaultString(newWork.getInfo());
    //     // if(StringUtils.isNotBlank(newInfo)){
    //     	existing.setInfo(newInfo);
    //     // }
	// 	int newVolume = newWork.getVolume();
	// 	existing.setVolume(newVolume);
	// 	// int newCount = newWork.getQuestionCount();
	// 	// existing.setQuestionCount(newCount);
	// 	boolean newActive = newWork.isActive();
	// 	existing.setActive(newActive);
    //     // update the existing record
    //     Practice updated = practiceRepository.save(existing);
	// 	return updated;
	// }

	// @Override
	// @Transactional
	// public PracticeAnswer updatePracticeAnswer(PracticeAnswer newWork, Long id) {
	// 	// search by getId
	// 	PracticeAnswer existing = practiceAnswerRepository.findById(id).get();
	// 	// update info
	// 	String newVideoPath = StringUtils.defaultString(newWork.getVideoPath());
    //     // if(StringUtils.isNotBlank(newVideoPath)){
    //     	existing.setVideoPath(newVideoPath);
    //     // }
	// 	String newPdfPath = StringUtils.defaultString(newWork.getPdfPath());
    //     // if(StringUtils.isNotBlank(newPdfPath)){
    //     	existing.setPdfPath(newPdfPath);
    //     // }
	// 	List newAns = newWork.getAnswers();
	// 	existing.setAnswers(newAns);
	// 	// update the existing record
	// 	PracticeAnswer updated = practiceAnswerRepository.save(existing);
	// 	return updated;	
	// }

	// @Override
	// @Transactional
	// public StudentPractice updateStudentPractice(StudentPractice newWork, Long id) {
	// 	// search by getId
	// 	StudentPractice existing = studentPracticeRepository.findById(id).get();
	// 	// update info
	// 	double newScore = newWork.getScore();
	// 	existing.setScore(newScore);
	// 	List newAnswers = newWork.getAnswers();
	// 	existing.setAnswers(newAnswers);
	// 	// update the existing record
	// 	StudentPractice updated = studentPracticeRepository.save(existing);
	// 	return updated;
	// }

	// @Override
	// @Transactional
	// public Test updateTest(Test newWork, Long id) {
	// 	// search by getId
	// 	Test existing = testRepository.findById(id).get();
    //     // Update info
    //     String newPdfPath = StringUtils.defaultString(newWork.getPdfPath());
    //     existing.setPdfPath(newPdfPath);
    //     String newInfo = StringUtils.defaultString(newWork.getInfo());
    //     existing.setInfo(newInfo);
    //     int newVolume = newWork.getVolume();
	// 	existing.setVolume(newVolume);
	// 	boolean newActive = newWork.isActive();
	// 	existing.setActive(newActive);
    //     Test updated = testRepository.save(existing);
	// 	return updated;
	// }

	// @Override
	// @Transactional
	// public TestAnswer updateTestAnswer(TestAnswer newWork, Long id) {
	// 	// search by getId
	// 	TestAnswer existing = testAnswerRepository.findById(id).get();
	// 	// update info
	// 	String newVideoPath = StringUtils.defaultString(newWork.getVideoPath());
    //     existing.setVideoPath(newVideoPath);
    //     String newPdfPath = StringUtils.defaultString(newWork.getPdfPath());
    //     existing.setPdfPath(newPdfPath);
    //     List newAns = newWork.getAnswers();
	// 	existing.setAnswers(newAns);
	// 	// update the existing record
	// 	TestAnswer updated = testAnswerRepository.save(existing);
	// 	return updated;	
	// }

	// @Override
	// @Transactional
	// public StudentTest updateStudentTest(StudentTest newWork, Long id) {
	// 	// search by getId
	// 	StudentTest existing = studentTestRepository.findById(id).get();
	// 	// update info
	// 	double newScore = newWork.getScore();
	// 	existing.setScore(newScore);
	// 	List newAnswers = newWork.getAnswers();
	// 	existing.setAnswers(newAnswers);
	// 	// update the existing record
	// 	StudentTest updated = studentTestRepository.save(existing);
	// 	return updated;
	// }

	// @Override
	// @Transactional
	// public HomeworkSchedule updateHomeworkSchedule(HomeworkSchedule schedule, Long id) {
	// 	HomeworkSchedule existing = homeworkScheduleRepository.findById(id).get();
	// 	// update info
	// 	LocalDateTime newFrom = schedule.getFromDatetime();
	// 	existing.setFromDatetime(newFrom);
	// 	LocalDateTime newTo = schedule.getToDatetime();
	// 	existing.setToDatetime(newTo);
	// 	boolean newActive = schedule.isActive();
	// 	existing.setActive(newActive);
	// 	String newInfo = schedule.getInfo();
	// 	existing.setInfo(newInfo);
	// 	int newSubjectDisplay = schedule.getSubjectDisplay();
	// 	existing.setSubjectDisplay(newSubjectDisplay);
	// 	int newAnswerDisplay = schedule.getAnswerDisplay();
	// 	existing.setAnswerDisplay(newAnswerDisplay);
	// 	String newGrade = schedule.getGrade();
	// 	existing.setGrade(newGrade);
	// 	String newSubject = schedule.getSubject();
	// 	existing.setSubject(newSubject);
	// 	// update the existing record
	// 	HomeworkSchedule updated = homeworkScheduleRepository.save(existing);
	// 	return updated;
	// }

	@Override
	@Transactional
	public HomeworkProgress updateHomeworkProgressPercentage(Long id, int percentage) {
		HomeworkProgress existing = homeworkProgressRepository.findById(id).get();
		// update percentage
		existing.setPercentage(percentage);
		// update registerDate
		existing.setRegisterDate(LocalDate.now());
		// update the existing record
		HomeworkProgress updated = homeworkProgressRepository.save(existing);
		return updated;
	}

	@Override
	@Transactional
	public ExtraworkProgress updateExtraworkProgressPercentage(Long id, int percentage) {
		ExtraworkProgress existing = extraworkProgressRepository.findById(id).get();
		// update percentage
		existing.setPercentage(percentage);
		// update registerDate
		existing.setRegisterDate(LocalDate.now());
		// update the existing record
		ExtraworkProgress updated = extraworkProgressRepository.save(existing);
		return updated;
	}

	// @Override
	// @Transactional
	// public PracticeSchedule updatePracticeSchedule(PracticeSchedule newWork, Long id) {
	// 	// search by getId
	// 	PracticeSchedule existing = practiceScheduleRepository.findById(id).get();
	// 	// update info
	// 	int newYear = newWork.getYear();
	// 	existing.setYear(newYear);
	// 	int newWeek = newWork.getWeek();
	// 	existing.setWeek(newWeek);
	// 	boolean newActive = newWork.isActive();
	// 	existing.setActive(newActive);
	// 	String newInfo = newWork.getInfo();
	// 	existing.setInfo(newInfo);
	// 	Set<Practice> newPracs = newWork.getPractices();
	// 	existing.setPractices(newPracs);
	// 	// update the existing record
	// 	PracticeSchedule updated = practiceScheduleRepository.save(existing);
	// 	return updated;	
	// }

	// @Override
	// @Transactional
	// public void deleteHomework(Long id) {
	// 	try{
	// 	    homeworkRepository.deleteById(id);
    //     }catch(org.springframework.dao.EmptyResultDataAccessException e){
    //         System.out.println("Nothing to delete");
    //     }
	// }

	// @Override
	// @Transactional
	// public void deletePractice(Long id) {
	// 	try{
	// 	    practiceRepository.deleteById(id);
    //     }catch(org.springframework.dao.EmptyResultDataAccessException e){
    //         System.out.println("Nothing to delete");
    //     }
	// }

	// @Override
	// @Transactional
	// public void deleteExtrawork(Long id) {
	// 	try{
	// 	    extraworkRepository.deleteById(id);
    //     }catch(org.springframework.dao.EmptyResultDataAccessException e){
    //         System.out.println("Nothing to delete");
    //     }
	// }

	@Override
	@Transactional
	public void deleteStudentPractice(Long studentId, Long practiceId) {
		try{
			studentPracticeRepository.deleteByStudentIdAndPracticeId(studentId, practiceId);
		}catch(Exception e){
			System.out.println("No StudentPractice found");
		}
	}

	// @Override
	// @Transactional
	// public void deleteTest(Long id) {
	// 	try{
	// 		testRepository.deleteById(id);
	// 	}catch(Exception e){
	// 		System.out.println("Nothing to delete");
	// 	}
	// }

	// @Override
	// @Transactional
	// public void deleteStudentTest(Long studentId, Long testId) {
	// 	try{
	// 		studentTestRepository.deleteById(testId);
	// 	}catch(Exception e){
	// 		System.out.println("Nothing to delete");
	// 	}
	// }

	// @Override
	// @Transactional
	// public void deleteHomeworkSchedule(Long id) {
	// 	try{
	// 		homeworkScheduleRepository.deleteById(id);
	// 	}catch(Exception e){
	// 		System.out.println("Nothing to delete");
	// 	}
	// }

	// @Override
	// @Transactional
	// public void deletePracticeSchedule(Long id) {
	// 	PracticeSchedule practiceSchedule = practiceScheduleRepository.findById(id).orElse(null);
	// 	if (practiceSchedule != null) {
	// 		// Retrieve the associated practices
	// 		Set<Practice> practices = practiceSchedule.getPractices();		
	// 		// Remove the associations between PracticeSchedule and Practice entities
	// 		practiceSchedule.setPractices(new LinkedHashSet<>());
	// 		practiceScheduleRepository.save(practiceSchedule); // Update to remove associations
	// 		// Now you can safely delete the PracticeSchedule record
	// 		practiceScheduleRepository.delete(practiceSchedule);
	// 	} else {
	// 		// Handle the case where the PracticeSchedule record doesn't exist
	// 	}

	// }

	@Override
	public HomeworkDTO getHomeworkInfo(long subject, int week) {
		HomeworkDTO dto = null;
		try{
			dto = homeworkRepository.findHomework(subject, week);
		}catch(Exception e){
			System.out.println("No Homework found");
		}
		return dto;
	}

	@Override
	public ExtraworkDTO getExtraworkInfo(int subject, int year, int week) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getExtraworkkInfo'");
	}

	@Override
	public PracticeDTO getPracticeInfo(int type, String grade, int volume) {
		PracticeDTO dto = null;
		try{
			dto = practiceRepository.findPractice(type, grade, volume);
		}catch(Exception e){
			System.out.println("No Practice found");
		}
		return dto;
	}

	@Override
	public TestDTO getTestInfo(int type, String grade, int volume) {
		TestDTO dto = null;
		try{
			dto = testRepository.findTest(type, grade, volume);
		}catch(Exception e){
			System.out.println("No Test found");
		}
		return dto;
	}

	@Override
	public List<HomeworkDTO> listHomework(long subject, String grade, int week) {
		List<HomeworkDTO> dtos = new ArrayList<>();
		try{
			dtos = homeworkRepository.filterHomeworkBySubjectNGradeNWeek(subject, grade, week);
		}catch(Exception e){
			System.out.println("No Homework found");
		}
		return dtos;
	}

	@Override
	public long getHomeworkIdByWeek(long subject, String grade, int week){
		long id = 0L;
		try{
			id = homeworkRepository.findHomeworkIdBySubjectNGradeNWeek(subject, grade, week);
		}catch(Exception e){
			System.out.println("No Homework found");
		}
		return id;
	}


	@Override
	public List<ExtraworkDTO> listExtrawork(String grade) {
		List<ExtraworkDTO> dtos = new ArrayList<>();
		try{
			dtos = extraworkRepository.filterExtraworkByGrade(grade);
		}catch(Exception e){
			System.out.println("No Homework found");
		}
		return dtos;
	}

	@Override
	public List<PracticeDTO> listPractice(int type, String grade, int volume) {
		List<PracticeDTO> dtos = new ArrayList<>();
		try{
			dtos = practiceRepository.filterPracticeByTypeNGradeNVolume(type, grade, volume);
		}catch(Exception e){
			System.out.println("No Practice found");
		}
		return dtos;
	}

	@Override
	public List<TestDTO> listTest(int type, String grade, int volume) {
		List<TestDTO> dtos = new ArrayList<>();
		try{
			dtos = testRepository.filterTestByTypeNGradeNVolume(type, grade, volume);
		}catch(Exception e){
			System.out.println("No Test found");
		}
		return dtos;
	}

	@Override
	public List<HomeworkScheduleDTO> listHomeworkSchedule(LocalDateTime from, LocalDateTime to) {
		List<HomeworkScheduleDTO> dtos = new ArrayList<>();
		try{
			dtos = homeworkScheduleRepository.filterHomeworkScheduleByYear(from, to);
		}catch(Exception e){
			System.out.println("No Homework Schedule found");
		}
		return dtos;
	}

	// @Override
	// public List<PracticeScheduleDTO> listPracticeSchedule(int year, int week) {
	// 	List<PracticeScheduleDTO> dtos = new ArrayList<>();
	// 	try{
	// 		dtos = practiceScheduleRepository.filterPracticeScheduleByYearNWeek(year, week);
	// 	}catch(Exception e){
	// 		System.out.println("No Practice Schedule found");
	// 	}
	// 	return dtos;
	// }

	@Override
	public List<SimpleBasketDTO> loadExtrawork(String grade) {
		List<Object[]> objects = new ArrayList<>();
		try{
			objects = extraworkRepository.summaryExtrawork(grade);
		}catch(Exception e){
			System.out.println("No Extrawork found");
		}
		List<SimpleBasketDTO> dtos = new ArrayList<>();
		for(Object[] object : objects){
			SimpleBasketDTO dto = new SimpleBasketDTO(object);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public List<SimpleBasketDTO> loadPractice(int type, int grade) {
		List<Object[]> objects = new ArrayList<>();
		try{
			objects = practiceRepository.summaryPractice(type, grade);
		}catch(Exception e){
			System.out.println("No Practice found");
		}
		List<SimpleBasketDTO> dtos = new ArrayList<>();
		for(Object[] object : objects){
			SimpleBasketDTO dto = new SimpleBasketDTO(object);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public List<SimpleBasketDTO> loadTest(int type, int grade) {
		List<Object[]> objects = new ArrayList<>();
		try{
			objects = testRepository.summaryTest(type, grade);
		}catch(Exception e){
			System.out.println("No Test found");
		}
		List<SimpleBasketDTO> dtos = new ArrayList<>();
		for(Object[] object : objects){
			SimpleBasketDTO dto = new SimpleBasketDTO(object);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public List<Integer> getAnswersByPractice(Long practiceId) {
		Optional<PracticeAnswer> answer = practiceAnswerRepository.findByPracticeId(practiceId);
		if(answer.isPresent()){
			return answer.get().getAnswers();
		}else{
			return null;
		}
	}

	@Override
	public List<Integer> getStudentPracticeAnswer(Long studentId, Long practionId) {
		Optional<StudentPractice> sp = studentPracticeRepository.findByStudentIdAndPracticeId(studentId, practionId);
		if(sp.isPresent()){
			return sp.get().getAnswers();
		}else{
			return null;
		}
	}

	@Override
	public List<TestAnswerItem> getAnswersByTest(Long testId) {
		Optional<TestAnswer> answer = testAnswerRepository.findByTestId(testId);
		if(answer.isPresent()){
			return answer.get().getAnswers();
		}else{
			return null;
		}
	}

	@Override
	public List<Integer> getStudentTestAnswer(Long studentId, Long testId) {
		Optional<StudentTest> answer = studentTestRepository.findByStudentIdAndTestId(studentId, testId);
		if(answer.isPresent()){
			return answer.get().getAnswers();
		}else{
			return null;
		}
	}

	@Override
	public int getPracticeAnswerCount(Long practiceId) {
		Optional<PracticeAnswer> answer = practiceAnswerRepository.findByPracticeId(practiceId);
		if(answer.isPresent()){
			List<Integer> answers =  answer.get().getAnswers();
			if((answers != null) && (answers.size()>0)) return answers.get(0);
		}
		return 0;
	}

	@Override
	public int getPracticeAnswerCountPerQuestion(Long practiceId) {
		int count = 0;
		try{
			count = practiceAnswerRepository.getAnswerCountByPractice(practiceId);
		}catch(Exception e){
			System.out.println("No PracticeAnswer found");
		}
		return count;
	}

	@Override
	public int getTestAnswerCount(Long testId) {
		Optional<TestAnswer> answer = testAnswerRepository.findByTestId(testId);
		if(answer.isPresent()){
			List<TestAnswerItem> answers =  answer.get().getAnswers();
			if((answers != null) && (answers.size()>0)) return answers.size();
		}
		return 0;
	}

	@Override
	public int getTestAnswerCountPerQuestion(Long testId) {
		int count = 0;
		try{
			count = testAnswerRepository.getAnswerCountByTest(testId);
		}catch(Exception e){
			System.out.println("No TestAnswer found");
		}
		return count;
	}

	@Override
	public boolean isStudentPracticeExist(Long studentId, Long practiceId) {
		Optional<StudentPractice> sp = studentPracticeRepository.findByStudentIdAndPracticeId(studentId, practiceId);
		if(sp.isPresent()){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean isStudentTestExist(Long studentId, Long testId) {
		Optional<StudentTest> st = studentTestRepository.findByStudentIdAndTestId(studentId, testId);
		if(st.isPresent()){
			return true;
		}else{
			return false;
		}
	}

	@Override
	public PracticeAnswerDTO findPracticeAnswerByPractice(Long id) {
		PracticeAnswerDTO dto = null;
		try{
			PracticeAnswer answer = practiceAnswerRepository.findPracticeAnswerByPractice(id);
			if(answer!=null){
				dto = new PracticeAnswerDTO(answer);
			}
		}catch(Exception e){
			System.out.println("No PracticeAnswer found");
		}
		return dto;
	}

	@Override
	public StudentPracticeDTO findStudentPracticeByStudentNPractice(Long studentId, Long practiceId) {
		StudentPracticeDTO dto = null;
		try{
			dto = studentPracticeRepository.findStudentPractice(studentId, practiceId);
		}catch(Exception e){
			System.out.println("No StudentPractice found");
		}
		return dto;	
	}

	@Override
	public TestAnswerDTO findTestAnswerByTest(Long id) {
		TestAnswerDTO dto = null;
		try{
			TestAnswer answer = testAnswerRepository.findTestAnswerByTest(id);
			if(answer!=null){
				dto = new TestAnswerDTO(answer);
			}
		}catch(Exception e){
			System.out.println("No TestAnswer found");
		}
		return dto;
	}

	@Override
	public StudentTestDTO findStudentTestByStudentNTest(Long studentId, Long testId, String from, String to) {
		StudentTestDTO dto = null;
		// convert String to LocalDate
		LocalDate fromDate = LocalDate.parse(from, DATE_FORMATTER);
		LocalDate toDate = LocalDate.parse(to, DATE_FORMATTER);			
		try{
			dto =  studentTestRepository.findStudentTest(studentId, testId, fromDate, toDate);
		}catch(Exception e){
			System.out.println("No StudentTest found");
		}
		return dto;
	}

	@Override
	public StudentTestDTO getStudentTest(Long studentId, Long testTypeId, String grade, int volume, String from, String to) {
		StudentTestDTO dto = null;
		// convert String to LocalDate
		LocalDate fromDate = LocalDate.parse(from, DATE_FORMATTER);
		LocalDate toDate = LocalDate.parse(to, DATE_FORMATTER);			
		try{
			dto =  studentTestRepository.findStudentTestResult(studentId, testTypeId, grade, volume, fromDate, toDate);
		}catch(Exception e){
			System.out.println("No StudentTest found");
		}
		return dto;
	}

	@Override
	public String getRegDateforStudentTest(Long studentId, Long test, String from, String to) {
		String regString = "";
		// convert String to LocalDate
		LocalDate fromDate = LocalDate.parse(from, DATE_FORMATTER);
		LocalDate toDate = LocalDate.parse(to, DATE_FORMATTER);
		try{
			LocalDate regDate = studentTestRepository.getRegisterDateByStudentIdAndTest(studentId, test, fromDate, toDate);
			regString = regDate.toString();
		}catch(Exception e){
			System.out.println("No StudentTest found");
		}
		return regString;
	}

	@Override
	public HomeworkScheduleDTO getHomeworkScheduleBySubjectAndGrade(String subject, String grade, LocalDateTime now) {
		List<HomeworkScheduleDTO> result = homeworkScheduleRepository.getHomeworkScheduleBySubjectAndGrade(subject, grade, now, PageRequest.of(0, 1));
        return result.isEmpty() ? null : result.get(0);
	}

	@Override
	public HomeworkProgress getHomeworkProgressByStudentNHomework(Long studentId, Long homeworkId) {
		HomeworkProgress progress = null;
		try{
			progress =  homeworkProgressRepository.findHomeworkProgressByStudentAndHomework(studentId, homeworkId);
		}catch(Exception e){
			System.out.println("No Homework Progress found");
		}
		return progress;
	}

	@Override
	public ExtraworkProgress getExtraworkProgressByStudentNHomework(Long studentId, Long extraworkId) {
		ExtraworkProgress progress = null;
		try{
			progress =  extraworkProgressRepository.findExtraworkProgressByStudentAndHomework(studentId, extraworkId);
		}catch(Exception e){
			System.out.println("No Extrawork Progress found");
		}
		return progress;
	}

	@Override
	public int getHomeworkProgressPercentage(Long studentId, Long homeworkId) {
		int percentage = 0;
		try{
			percentage = homeworkProgressRepository.getPercentageByStudentAndHomework(studentId, homeworkId);
		}catch(Exception e){
			System.out.println("No Homework Progress found");
		}
		return percentage;
	}

	@Override
	public int getExtraworkProgressPercentage(Long studentId, Long extraworkId) {
		int percentage = 0;
		try{
			percentage = extraworkProgressRepository.getPercentageByStudentAndHomework(studentId, extraworkId);
		}catch(Exception e){
			System.out.println("No Extrawork Progress found");
		}
		return percentage;
	}

	@Override
	public List<PracticeScheduleDTO> checkPracticeSchedule(String practiceGroup, String grade, LocalDateTime now) {
		List<PracticeScheduleDTO> dtos = new ArrayList<>();
		try{
			dtos = practiceScheduleRepository.getPracticeScheduleByGroupAndGrade(practiceGroup, grade, now);
		}catch(Exception e){
			System.out.println("No Practice Schedule found");
		}
		return dtos;
	}

	@Override
	public List<TestScheduleDTO> checkTestSchedule(String testGroup, String grade, LocalDateTime now) {
		List<TestScheduleDTO> dtos = new ArrayList<>();
		try{
			dtos = testScheduleRepository.getTestScheduleByGroupAndGrade(testGroup, grade, now);
		}catch(Exception e){
			System.out.println("No Test Schedule found");
		}
		return dtos;
	}

	@Override
	public List<PracticeDTO> getPracticeInfoByGroup(int practiceGroup, String grade, int week) {
		List<PracticeDTO> dtos = new ArrayList<>();
		try{
			dtos = practiceRepository.getPracticeByGroupNGradeNWeek(practiceGroup, grade, week);
		}catch(Exception e){
			System.out.println("No Practice found");
		}
		return dtos;
	}

	@Override
	public List<TestDTO> getTestInfoByGroup(int testGroup, String grade, int week) {
		List<TestDTO> dtos = new ArrayList<>();
		try{
			dtos = testRepository.getTestByGroupNGradeNWeek(testGroup, grade, week);
		}catch(Exception e){
			System.out.println("No Test found");
		}
		return dtos;
	}

}

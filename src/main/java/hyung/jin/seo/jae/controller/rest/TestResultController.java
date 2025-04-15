package hyung.jin.seo.jae.controller.rest;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hyung.jin.seo.jae.dto.BranchDTO;
import hyung.jin.seo.jae.dto.CycleDTO;
import hyung.jin.seo.jae.dto.StudentDTO;
import hyung.jin.seo.jae.dto.StudentTestDTO;
import hyung.jin.seo.jae.dto.TestDTO;
import hyung.jin.seo.jae.dto.TestResultHistoryDTO;
import hyung.jin.seo.jae.dto.TestScheduleDTO;
import hyung.jin.seo.jae.model.Branch;
import hyung.jin.seo.jae.model.Student;
import hyung.jin.seo.jae.model.Test;
import hyung.jin.seo.jae.model.TestAnswerItem;
import hyung.jin.seo.jae.model.TestSchedule;
import hyung.jin.seo.jae.service.CodeService;
import hyung.jin.seo.jae.service.ConnectedService;
import hyung.jin.seo.jae.service.CycleService;
import hyung.jin.seo.jae.service.PdfService;
import hyung.jin.seo.jae.service.StudentService;
import hyung.jin.seo.jae.utils.JaeConstants;
import hyung.jin.seo.jae.utils.JaeUtils;

@RestController
@RequestMapping("result")
public class TestResultController {

	@Autowired
	private PdfService pdfService;

	@Autowired
	private ConnectedService connectedService;

	@Autowired
	private CycleService cycleService;

	@Autowired
	private CodeService codeService;

	@Autowired
	private StudentService studentService;

	@GetMapping("/download-pdf/{id}")
	public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
		System.out.println("Download PDF with ID: " + id);

		// Correctly instantiate HttpHeaders
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
		ContentDisposition contentDisposition = ContentDisposition.builder("attachment").filename("TestResult.pdf").build();
 		headers.setContentDisposition(contentDisposition);

		// get latest test result by student id
		List<StudentTestDTO> studentTests = connectedService.getLatestStudentTest(id);

		if (studentTests == null || studentTests.isEmpty()) {
			byte[] pdfData = pdfService.generateEmptyTestResult(id);
			return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
		}

		// Set Map to store ingredients
		Map<String, Object> data = new HashMap<>();
		// add student info
		Student std = studentService.getStudent(id);
		data.put(JaeConstants.STUDENT_INFO, new StudentDTO(std));
		// add test result info
		data.put(JaeConstants.TEST_RESULT_INFO, studentTests);
		// add test group info
		long tempTestId = ((StudentTestDTO)(studentTests.get(0))).getTestId();
		int testGroup = connectedService.getTestGroup(tempTestId);
		String testGrade = connectedService.getTestGrade(tempTestId);
		String testGroupName = JaeUtils.getTestGroup(testGroup);
		data.put(JaeConstants.TEST_GROUP_INFO, testGroupName);
		// add branch info
		BranchDTO branch = codeService.getBranch(std.getBranch());
		data.put(JaeConstants.BRANCH_INFO, branch);
		// add test volume info
		int volume = connectedService.getTestVolume(tempTestId);
		String volumeName = "";
		if(testGroup == 1 || testGroup == 2) { // if test group is 1 or 2, then add volume info
			if(volume == 1) {
				volumeName = "Volume 1";
			}else if(volume == 2) {
				volumeName = "Volume 2";
			}else if(volume == 3) {
				volumeName = "Volume 3";
			}else if(volume == 4) {
				volumeName = "Volume 4";
			}else if(volume == 5) {
				volumeName = "Volume 5";
			}
		}else{
			if(volume == 36) {
				volumeName = "SIM 1";
			}else if(volume == 37) {
				volumeName = "SIM 2";
			}else if(volume == 38) {
				volumeName = "SIM 3";
			}else if(volume == 39) {
				volumeName = "SIM 4";
			}else if(volume == 40) {
				volumeName = "SIM 5";
			}else {
				volumeName = volume + " week";
			}
		}
		data.put(JaeConstants.VOLUME_INFO, volumeName);

		int currentYear = cycleService.academicYear();
		CycleDTO cycle = cycleService.listCycles(currentYear);
		String startCycle = cycle.getStartDate();
		String endCycle = cycle.getEndDate();

		// set test title info
		List<String> testTitles = new ArrayList<>();
		// set test answer total count info
		List<Integer> testAnswerTotalCount = new ArrayList<>();
		// set student answer correct count info
		List<Integer> studentAnswerCorrectCount = new ArrayList<>();
		// set student score
		List<Double> studentScores = new ArrayList<>();
		// set average score
		List<Double> averageScores = new ArrayList<>();
		// set highest score
		List<Double> highestScores = new ArrayList<>();
		// set lowest score
		List<Double> lowestScores = new ArrayList<>();
		// set student answer
		List<List<Integer>> studentAnswers = new ArrayList<>();
		// set test answer
		List<List<TestAnswerItem>> testAnswers = new ArrayList<>();
		// set result history
		List<List<TestResultHistoryDTO>> histories = new ArrayList<>();
		for(StudentTestDTO studentTest : studentTests) {		
			// add test title info
			long testId = studentTest.getTestId();
			String testTypeName = connectedService.getTestTypeName(testId);
			testTitles.add(testTypeName);
			// get test answer total count info
			int testAnswerCount = connectedService.getTestAnswerCount(testId);
			testAnswerTotalCount.add(testAnswerCount);
			// get student answer correct count info
			List<Integer> answers = connectedService.getStudentTestAnswer(id, testId, cycle.getStartDate(), cycle.getEndDate());	
			List<TestAnswerItem> testAnswerItems = connectedService.getAnswersByTest(testId);
			int correctCount = JaeUtils.countTestScore(answers, testAnswerItems);
			studentAnswerCorrectCount.add(correctCount);
			studentAnswers.add(answers);
			testAnswers.add(testAnswerItems);
			// get average, hightest, lowest score
			double studentScore = studentTest.getScore();
			double averageScore = connectedService.getAverageScoreByTest(studentTest.getTestId(), startCycle, endCycle);
			double highestScore = connectedService.getHighestScoreByTest(studentTest.getTestId(), startCycle, endCycle);
			double lowestScore = connectedService.getLowestScoreByTest(studentTest.getTestId(), startCycle, endCycle);
			studentScores.add(studentScore);
			averageScores.add(averageScore);
			highestScores.add(highestScore);
			lowestScores.add(lowestScore);



			// get test result history
			List<TestResultHistoryDTO> history = new ArrayList<>();
			
			int weekCount = 0;	
			if(testGroup == 1 || testGroup == 2 || testGroup == 5) { // Mega, Revision or Mock
				weekCount = 5;
			}else if(testGroup == 3){ // Edu - max 24
				weekCount = 24;
			}else{ // Acer - max 40
				weekCount = 40;
			}	
			// get test result history
			for(int i=1; i<=weekCount; i++){
				TestScheduleDTO testSchedule = connectedService.getMostRecentTestSchedule(testGroup+"", testGrade, i+"");
				if(testSchedule == null) {
					continue;
				}
				String from = testSchedule.getFrom(); // ex> 01/04/2025, 17:38
				String dateOnlyFrom = from.substring(0, 10); // Extracts the first 10 characters ex> 01/04/2025
				try {
					from = JaeUtils.convertToyyyyMMddFormat(dateOnlyFrom);
				} catch (ParseException e) {
					from = "2100-01-01";
					e.printStackTrace();
				} // ex> 2025-04-01

				String to = testSchedule.getTo();
				String dateOnlyTo = to.substring(0, 10);
				try {
					to = JaeUtils.convertToyyyyMMddFormat(dateOnlyTo);
				} catch (ParseException e) {
					to = "2100-01-01";
					e.printStackTrace();
				}
				// add more buffer before and after....
				double average = connectedService.getAverageScoreByTest(studentTest.getTestId(), from, to);
				TestResultHistoryDTO testHistory = new TestResultHistoryDTO();
				testHistory.setTestNo(i);
				testHistory.setAverage((int)(average));
				// student score
				StudentTestDTO studentTestHistory = connectedService.findStudentTestByStudentNTest(id, testId, from, to);
				testHistory.setStudentScore(studentTestHistory==null ? 0 : (int)(studentTestHistory.getScore()));
				history.add(testHistory);
			}
			histories.add(history);
				
			// get branch info

			

		}
		// add test title info
		data.put(JaeConstants.TEST_TITLE_INFO, testTitles);
		// add test answer total count info
		data.put(JaeConstants.TEST_ANSWER_TOTAL_COUNT, testAnswerTotalCount);
		// add student answer correct count info
		data.put(JaeConstants.STUDENT_ANSWER_CORRECT_COUNT, studentAnswerCorrectCount);
		// add student score
		data.put(JaeConstants.STUDENT_SCORE, studentScores);
		// add average score
		data.put(JaeConstants.TEST_AVERAGE_SCORE, averageScores);
		// add highest score
		data.put(JaeConstants.TEST_HIGHEST_SCORE, highestScores);
		// add lowest score
		data.put(JaeConstants.TEST_LOWEST_SCORE, lowestScores);
		// add student answer
		data.put(JaeConstants.STUDENT_ANSWERS, studentAnswers);
		// add test answers
		data.put(JaeConstants.TEST_ANSWERS, testAnswers);
		// add test result history
		data.put(JaeConstants.TEST_RESULT_HISTORY, histories);


		/*
		// assume we have the following data
		Long studentId = 11301580L;
		Long testId = 6L;
		// 1. student object
		StudentDTO student = new StudentDTO();
		student.setFirstName("John");
		student.setLastName("Doe");
		student.setId(studentId.toString());
		student.setGrade("2");
		student.setBranch("13");
		// 2 StudentTestDTO
		int currentYear = cycleService.academicYear();
		CycleDTO cycle = cycleService.listCycles(currentYear);
		StudentTestDTO test = connectedService.findStudentTestByStudentNTest(studentId, testId, cycle.getStartDate(), cycle.getEndDate());
		// 3. student test answer
		List<Integer> answers = connectedService.getStudentTestAnswer(studentId, 6L, cycle.getStartDate(), cycle.getEndDate());	
		test.setAnswers(answers);
		// 4. test answer collection using testanswer id
		List<TestAnswerItem> testAnswerItems = connectedService.getAnswersByTest(testId);
		// 5. get average, hightest, lowest score
		double studentScore = test.getScore();
		double averageScore = connectedService.getAverageScoreByTest(testId, cycle.getStartDate(), cycle.getEndDate());
		double highestScore = connectedService.getHighestScoreByTest(testId, cycle.getStartDate(), cycle.getEndDate());
		double lowestScore = connectedService.getLowestScoreByTest(testId, cycle.getStartDate(), cycle.getEndDate());
		// 6. get branch info
		String branchCode = student.getBranch();
		BranchDTO branch = codeService.getBranch(branchCode);
		String branchName = branch.getName();
		String branchPhone = branch.getPhone();
		// 7. get histogram data - for example : Higher
		String histogramData = connectedService.getScoreCategory(studentScore, testId, cycle.getStartDate(), cycle.getEndDate());
		// 8. get test result history
		List<TestResultHistoryDTO> histories = new ArrayList<>();
		List<TestDTO> tests = connectedService.getTestInfoByType(testId, cycle.getStartDate(), cycle.getEndDate());
		for (TestDTO t : tests) {
			TestResultHistoryDTO history = new TestResultHistoryDTO();
			history.setTestNo(Integer.parseInt(t.getId()));
			StudentTestDTO studentTest = connectedService.findStudentTestByStudentNTest(studentId, Long.parseLong(t.getId()), cycle.getStartDate(), cycle.getEndDate());
			if(studentTest != null) {
				history.setStudentScore((int)(studentTest.getScore()));
			}			
			history.setAverage((int)(t.getAverage()));
			histories.add(history);
		}
		*/






		// Student student = studentService.getStudent(id);
		// if (student == null) {
		// 	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		// }


		// byte[] pdfData = pdfService.dummyPdf(new StudentDTO(student));

		byte[] pdfData = pdfService.generateTestResult(data);
		// Log the PDF size (important for debugging)
		System.out.println("Generated PDF size: " + (pdfData != null ? pdfData.length : "null"));

		if (pdfData == null || pdfData.length == 0) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
	}

}

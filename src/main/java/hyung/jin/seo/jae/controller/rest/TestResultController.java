package hyung.jin.seo.jae.controller.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hyung.jin.seo.jae.dto.BranchDTO;
import hyung.jin.seo.jae.dto.CycleDTO;
import hyung.jin.seo.jae.dto.StudentDTO;
import hyung.jin.seo.jae.dto.StudentTestDTO;
import hyung.jin.seo.jae.dto.TestDTO;
import hyung.jin.seo.jae.dto.TestResultHistoryDTO;
import hyung.jin.seo.jae.model.TestAnswerItem;
import hyung.jin.seo.jae.service.CodeService;
import hyung.jin.seo.jae.service.ConnectedService;
import hyung.jin.seo.jae.service.CycleService;
import hyung.jin.seo.jae.service.PdfService;

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

	@GetMapping("/download-pdf")
	public ResponseEntity<byte[]> downloadPdf() {
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






		byte[] pdfData = pdfService.dummyPdf(student);

		// Log the PDF size (important for debugging)
		System.out.println("Generated PDF size: " + (pdfData != null ? pdfData.length : "null"));

		if (pdfData == null || pdfData.length == 0) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}

        // Correctly instantiate HttpHeaders
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        
		 // Use builder() instead of attachment()
		 ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
		 .filename("TestResult.pdf")
		 .build();
 		headers.setContentDisposition(contentDisposition);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfData);
	}

}

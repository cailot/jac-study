package hyung.jin.seo.jae.service;

import java.util.Map;

import hyung.jin.seo.jae.dto.StudentDTO;

public interface PdfService {

	// generate empty test result pdf file
	byte[] generateEmptyTestResult(Long studentId);

	// generate test result pdf file
	byte[] generateTestResult(Map<String, Object> data);



	byte[] dummyPdf(StudentDTO student);

	byte[] generateAssessmentPdf(Map<String, Object> data);

	void generateTestPdf(Map<String, Object> data);

}

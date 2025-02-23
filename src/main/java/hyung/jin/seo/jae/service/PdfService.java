package hyung.jin.seo.jae.service;

import java.util.Map;

import hyung.jin.seo.jae.dto.StudentDTO;

public interface PdfService {

	// generate invoice pdf file
	// void generateInvoicePdf(String name, Map<String, Object> data);

	byte[] dummyPdf(StudentDTO student);

	byte[] generateAssessmentPdf(Map<String, Object> data);

	void generateTestPdf(Map<String, Object> data);

	// byte[] generateReceiptPdf(Map<String, Object> data);
}

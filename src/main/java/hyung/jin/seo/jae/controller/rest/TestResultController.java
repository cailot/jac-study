package hyung.jin.seo.jae.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hyung.jin.seo.jae.dto.CycleDTO;
import hyung.jin.seo.jae.dto.StudentDTO;
import hyung.jin.seo.jae.dto.StudentTestDTO;
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

	@GetMapping("/download-pdf")
	public ResponseEntity<byte[]> downloadPdf() {
		// 1. student object
		StudentDTO student = new StudentDTO();
		student.setFirstName("John");
		student.setLastName("Doe");
		student.setId("11301580");
		student.setGrade("2");
		// 2 StudentTestDTO
		int currentYear = cycleService.academicYear();
		CycleDTO cycle = cycleService.listCycles(currentYear);
		StudentTestDTO test = connectedService.findStudentTestByStudentNTest(Long.parseLong(student.getId()), 6L, cycle.getStartDate(), cycle.getEndDate());


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

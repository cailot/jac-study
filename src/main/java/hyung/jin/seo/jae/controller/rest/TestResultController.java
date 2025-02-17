package hyung.jin.seo.jae.controller.rest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import hyung.jin.seo.jae.dto.AssessmentAnswerDTO;
import hyung.jin.seo.jae.dto.AssessmentDTO;
import hyung.jin.seo.jae.dto.GuestStudentAssessmentDTO;
import hyung.jin.seo.jae.dto.GuestStudentDTO;
import hyung.jin.seo.jae.model.Assessment;
import hyung.jin.seo.jae.model.AssessmentAnswer;
import hyung.jin.seo.jae.model.AssessmentAnswerItem;
import hyung.jin.seo.jae.model.Grade;
import hyung.jin.seo.jae.model.GuestStudent;
import hyung.jin.seo.jae.model.GuestStudentAssessment;
import hyung.jin.seo.jae.model.Subject;
import hyung.jin.seo.jae.service.AssessmentService;
import hyung.jin.seo.jae.service.CodeService;
import hyung.jin.seo.jae.service.EmailService;
import hyung.jin.seo.jae.service.PdfService;
import hyung.jin.seo.jae.utils.JaeConstants;
import hyung.jin.seo.jae.utils.JaeUtils;

@RestController
@RequestMapping("result")
public class TestResultController {

	@Autowired
	private PdfService pdfService;

	@GetMapping("/download-pdf")
	public ResponseEntity<byte[]> downloadPdf() {
		byte[] pdfData = pdfService.dummyPdf();

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

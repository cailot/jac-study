package hyung.jin.seo.jae.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

@Controller
@RequestMapping("assessment")
public class AssessmentController {

	@Autowired
	private CodeService codeService;

	@Autowired
	private AssessmentService assessmentService;

	@Autowired
	private PdfService pdfService;

	@Autowired
	private EmailService emailService;

	@Value("${spring.sender.assessment}")
    private String assessmentName;

	// register test
	@PostMapping("/addAssessment")
	@ResponseBody
	public AssessmentDTO registerAssessment(@RequestBody AssessmentDTO formData) {
		// 1. create barebone
		Assessment work = formData.convertToAssessment();
		// 2. set active to true as default
		work.setActive(true);
		// 3. set Grade & Subject
		Grade grade = codeService.getGrade(Long.parseLong(formData.getGrade()));
		Subject subject = codeService.getSubject(formData.getSubject());
		// 4. associate Grade & Subject
		work.setGrade(grade);
		work.setSubject(subject);
		// 5. register Assessment
		Assessment added = assessmentService.addAssessment(work);
		// 6. return dto
		AssessmentDTO dto = new AssessmentDTO(added);
		return dto;
	}

	// register guest student
	@PostMapping("/addGuest")
	@ResponseBody
	public GuestStudentDTO registerGuestStudent(@RequestBody GuestStudentDTO formData) {
		// 1. create barebone
		GuestStudent work = formData.convertToGuestStudent();
		// 2. register Assessment
		GuestStudent added = assessmentService.addGuestStudent(work);
		// 3. return dto
		GuestStudentDTO dto = new GuestStudentDTO(added);
		return dto;
	}

	@GetMapping("/listAssessment")
	public String listAssessment(
			@RequestParam(value = "listGrade", required = false, defaultValue = "0") String grade,
			@RequestParam(value = "listSubject", required = false, defaultValue = "0") Long subject,
			Model model) {
		List<AssessmentDTO> dtos = new ArrayList();
		dtos = assessmentService.listAssessment(grade, subject);		
		model.addAttribute(JaeConstants.ASSESSMENT_LIST, dtos);
		return "assessListPage";
	}

	// get assessment
	@GetMapping("/getAssessment/{id}")
	@ResponseBody
	public AssessmentDTO getAssessment(@PathVariable Long id) {
		Assessment work = assessmentService.getAssessment(id);
		AssessmentDTO dto = new AssessmentDTO(work);
		return dto;
	}

	@GetMapping("/listSubject/{grade}")
	@ResponseBody
	public int listSuject(@PathVariable String grade) {
		List<AssessmentDTO> dtos = new ArrayList();
		dtos = assessmentService.listAssessment(grade);		
		// return count
		return dtos.size();
	}

	// update existing practice
	@PutMapping("/updateAssessment")
	@ResponseBody
	public ResponseEntity<String> updateAssessment(@RequestBody AssessmentDTO formData) {
		try{
			// 1. create barebone Homework
			Assessment work = formData.convertToAssessment();
			// 2. update Assessment
			work = assessmentService.updateAssessment(work, Long.parseLong(formData.getId()));
			// 3.return flag
			return ResponseEntity.ok("\"Assessment updated\"");
		}catch(Exception e){
			String message = "Error updating Assessment : " + e.getMessage();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
		}
	}

	@PostMapping(value = "/saveAssessAnswer")
	@ResponseBody
    public ResponseEntity<String> saveAssessAnswerSheet(@RequestBody Map<String, Object> payload) {
        // Extract practiceId and answers from the payload
		String answerId = payload.get("answerId").toString();
		String assessId = payload.get("assessId").toString();
		List<AssessmentAnswerItem> items = new ArrayList<>();
		// convert the answers list from the payload to a List<Map<String, Object>>
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> answerList = mapper.convertValue(payload.get("answers"), new TypeReference<List<Map<String, Object>>>() {});

		// Now iterate over answerList to access question, answer, and topic for each entry
		for (Map<String, Object> answer : answerList) {
			String question = StringUtils.defaultString(answer.get("question").toString(), "0");
			String selectedAnswer = StringUtils.defaultString(answer.get("answer").toString(), "0");
			String topic = StringUtils.defaultString(answer.get("topic").toString());
			// create TestAnswerItem and put it into items
			AssessmentAnswerItem item = new AssessmentAnswerItem(Integer.parseInt(question), Integer.parseInt(selectedAnswer), topic);
			items.add(item);
		}

		// if answerId has some value, update TestAnswer; otherwise register.
		if(StringUtils.isBlank(answerId)){
			// ADD
			// 1. create bare bone
			AssessmentAnswer ta = new AssessmentAnswer();
			// 2. populate AssessmentAnswer
			ta.setAnswers(items);
			// 3. get Assessment
			Assessment assess = assessmentService.getAssessment(Long.parseLong(assessId));
			// 4. associate Assessment
			ta.setAssessment(assess);
			// 5. register TestAnswer
			assessmentService.addAssessmentAnswer(ta);
		}else{
			// UPDATE
			// 1. get TestAnswer
			AssessmentAnswer ta =  assessmentService.findAssessmentAnswer(Long.parseLong(answerId));
			// 2. populate AssessmentAnswer
			ta.setAnswers(items);
			// 3. update AssessmentAnswer
			assessmentService.updateAssessmentAnswer(ta, Long.parseLong(answerId));
		}
		return ResponseEntity.ok("\"Success\"");
    }


	// delete assessment by Id
	@DeleteMapping(value = "/delete/{id}")
	@ResponseBody
    public ResponseEntity<String> removeBook(@PathVariable Long id) {
       	assessmentService.deleteAssessment(id);
		return ResponseEntity.ok("\"Assessment deleted successfully\"");
    }

	// check if AssessmentAnswer exists or not
	@GetMapping("/checkAssessAnswer/{assessId}")
	@ResponseBody
	public AssessmentAnswerDTO findTestAnswer(@PathVariable Long assessId) {
		AssessmentAnswerDTO answer = assessmentService.getAssessmentAnswer(assessId);
		return answer;
	}

	// get Assessment
	@GetMapping("/getAssessInfo/{grade}/{subject}")
	@ResponseBody
	public AssessmentDTO getAssessInfo(@PathVariable String grade, @PathVariable long subject) {
		AssessmentDTO dto = assessmentService.getAssessmentInfo(grade, subject);
		return dto;
		// document URL	
		// String url = "https://jacstorage.blob.core.windows.net/extra-materials/MathsTopic/P3/document/2D_Shapes.pdf";
		// return url;
	}

	@PostMapping(value = "/markAssessment")
	public ResponseEntity<Map<String, String>> markTest(@RequestBody Map<String, Object> payload) {
		// Extract practiceId and answers from the payload
		String studentId = StringUtils.defaultString(payload.get("studentId").toString(), "0");
		String assessId = StringUtils.defaultString(payload.get("assessId").toString(), "0");
		List<Map<String, Object>> mapAns = (List<Map<String, Object>>) payload.get("answers");

		// Convert the Map of answers to List
		List<Integer> answers = convertAssessmentAnswers(mapAns);

		// Save it into GuestStudentAssessment
		GuestStudentAssessment st = new GuestStudentAssessment();
		GuestStudent student = assessmentService.getGuestStudent(Long.parseLong(studentId));
		Assessment assess = assessmentService.getAssessment(Long.parseLong(assessId));
		st.setGuestStudent(student);
		st.setAssessment(assess);
		st.setAnswers(answers);
		List<AssessmentAnswerItem> corrects = assessmentService.getAnswersByAssessment(Long.parseLong(assessId));
		double score = JaeUtils.calculateAssessmentScore(answers, corrects);
		st.setScore(score);
		assessmentService.addGuestStudentAssessment(st);

		// Build the redirect URL
		String redirectUrl = "/assessment/list";

		// Return the redirect URL in the response
		Map<String, String> response = new HashMap<>();
		response.put("redirectUrl", redirectUrl);
		// response.put("math", "done");
		// other subject ??
		List<String> subjects = assessmentService.getSubjectsByStudent(Long.parseLong(studentId));
		for(String subject : subjects){
			response.put(subject, "done");
		}
		return ResponseEntity.ok(response);
	}

	// create result as Pdfs and send them vi
	@GetMapping("/sendResult/{studentId}")
	@ResponseBody
	public ResponseEntity<String> finaliseResult(@PathVariable Long studentId) {
		// 1. create basket for pdf
		Map<String, Object> ingredients = new HashMap<String, Object>();		
		// 2. get GuestStudent info
		GuestStudent guest = assessmentService.getGuestStudent(studentId);
		ingredients.put(JaeConstants.STUDENT_INFO, guest);
		// 3. get guest student assessment
		List<GuestStudentAssessmentDTO> gsas = assessmentService.getGuestStudentAssessmentByStudent(studentId);
		List<AssessmentAnswerDTO> aas = new ArrayList<>();
		// 4. get assessment answers
		for(GuestStudentAssessmentDTO gsa : gsas){
			List<Integer> gsAnswer = gsa.getAnswers();
			AssessmentAnswerDTO aa = assessmentService.getAssessmentAnswer(gsa.getAssessmentId());
			aas.add(aa);
		}
		ingredients.put(JaeConstants.STUDENT_ANSWER, gsas);
		ingredients.put(JaeConstants.CORRECT_ANSWER, aas);
		// 5. create PDF
		byte[] pdfData = pdfService.generateAssessmentPdf(ingredients);
		// 6. send email
		String emailRecipient = "jh05052008@gmail.com";
		StringBuilder emailBodyBuilder = new StringBuilder();
		emailBodyBuilder.append("<html>")
            .append("<head>")
            .append("<style>")
            .append("table {")
            .append("  border-collapse: collapse;")
            .append("  width: 70%;")
            .append("}")
            .append("th, td {")
            .append("  border: 1px solid #dddddd;")
            .append("  text-align: left;")
            .append("  padding: 8px;")
            .append("}")
            .append("th {")
            .append("  background-color: #f2f2f2;")
            .append("  border-bottom: 2px solid #dddddd;")
            .append("}")
            .append("tr:nth-child(even) {")
            .append("  background-color: #f9f9f9;")
            .append("}")
            .append("tr:nth-child(odd) {")
            .append("  background-color: #ffffff;")
            .append("}")
            .append("tr:hover {")
            .append("  background-color: #f1f1f1;")
            .append("}")
            .append("td.score {")
            .append("  text-align: center;")
            .append("}")
            .append("</style>")
            .append("</head>")
            .append("<body>")
			.append("<p style='color: red; font-weight: bold;'>Please Do Not Reply to This Email. This email is intended for sending purposes only</p>")
            .append("<p>There is an assessment test submitted:</p>")
            .append("<p><b>Name:</b> ").append(guest.getFirstName()).append(" ").append(guest.getLastName()).append("</p>")
            .append("<p><b>Email:</b> ").append(guest.getEmail()).append("</p>")
            .append("<p><b>Contact:</b> ").append(guest.getContactNo()).append("</p>")
            .append("<br>")
            .append("<table>")
            .append("<tr>")
            .append("<th>Title</th>")
            .append("<th style='text-align: center;'>Score</th>")
			.append("</tr>");
		for (GuestStudentAssessmentDTO dto : gsas) {
			String subject = dto.getSubject();
			String grade = guest.getGrade();
			int score = (int) ((20 * dto.getScore()) / 100); // Assuming dto.getScore() returns a number
			emailBodyBuilder.append("<tr>")
				.append("<td>Assessment Test ").append(JaeUtils.getGradeName(grade)).append(" ").append(subject).append("</td>") // Assuming you want to use the grade here
				.append("<td class='score'>").append(score).append(" / 20</td>")
				.append("</tr>");
		}
		
		emailBodyBuilder.append("</table>")
			.append("</body>")
			.append("</html>");
		
		String emailBody = emailBodyBuilder.toString();
		try {
			emailService.emailReport(emailRecipient, emailBody, pdfData);
		} catch (MessagingException e) {
			String message = "Error updating Assessment : " + e.getMessage();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
		}
		return ResponseEntity.ok("\"Assessment result processed successfully. <br>Now You can close this browser.\"");
	}

	// helper method converting test answers Map to List
	private List<Integer> convertAssessmentAnswers(List<Map<String, Object>> answers) {
		// Sort the answers based on the "question" key
		answers.sort(Comparator.comparingInt(answer -> Integer.parseInt(answer.get("question").toString())));
		List<Integer> answerList = new ArrayList<>();
		for (Map<String, Object> answer : answers) {
			int selectedOption = Integer.parseInt(answer.get("answer").toString());
			answerList.add(selectedOption);
		}
		return answerList;
	}


}

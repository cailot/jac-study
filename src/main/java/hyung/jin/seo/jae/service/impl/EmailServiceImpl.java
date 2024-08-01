package hyung.jin.seo.jae.service.impl;

import java.util.Arrays;
import java.util.Collections;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.MessageSendParameterSet;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.models.UserSendMailParameterSet;
import com.microsoft.graph.requests.GraphServiceClient;

import hyung.jin.seo.jae.service.EmailService;

@Service
public class EmailServiceImpl implements EmailService {
	
	@Autowired
	JavaMailSender mailSender;

	private static final String CLIENT_ID = "b835b1b2-30f8-4a0d-b9e0-bc9d65007668";
	private static final String CLIENT_SECRET = "d8cd9e5e-3fc1-48a0-9974-b2ed1f1a0c6a";
	private static final String TENANT_ID = "62f529c0-a0f8-4c03-8aa6-976882df337d";
	
	@Autowired
	private ResourceLoader resourceLoader;


	@Override
	public void test(){
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("braybrook@jamesancollegevic.com.au");
		message.setTo("jh05052008@gmail.com");
		message.setSubject("gone??");
		message.setText("can you see me?");
		mailSender.send(message);
	}

	public void sendEmail(String to, String subject, String bodyContent) {
		 // Create the client secret credential
		 ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
		 .clientId(CLIENT_ID)
		 .clientSecret(CLIENT_SECRET)
		 .tenantId(TENANT_ID)
		 .build();

 // Create the authentication provider
 TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
		 Collections.singletonList("https://graph.microsoft.com/.default"),
		 clientSecretCredential);

 // Create the Graph client
 GraphServiceClient<?> graphClient = GraphServiceClient.builder()
		 .authenticationProvider(authProvider)
		 .buildClient();

 // Construct the email message
 Message message = new Message();
 message.subject = subject;
 ItemBody itemBody = new ItemBody();
 itemBody.contentType = BodyType.TEXT;
 itemBody.content = bodyContent;
 message.body = itemBody;

 EmailAddress emailAddress = new EmailAddress();
 emailAddress.address = to;
 Recipient recipient = new Recipient();
 recipient.emailAddress = emailAddress;
 message.toRecipients = Arrays.asList(recipient);

 // Create the parameter set for the sendMail method
 UserSendMailParameterSet parameterSet = UserSendMailParameterSet.newBuilder()
		 .withMessage(message)
		 .withSaveToSentItems(true)
		 .build();

 // Send the email
 graphClient
		 .me()
		 .sendMail(parameterSet)
		 .buildRequest()
		 .post();

 System.out.println("=================>>>>>>>>> MAIL SENT SUCCESSFULLY");
}
	
	@Override
	public void sendEmail(String from, String to, String subject, String body) {
		sendEmail(to, subject, body);
		// MimeMessage message = mailSender.createMimeMessage();
		// try {
		// 	message.setFrom(new InternetAddress(from));
		// 	message.setRecipients(MimeMessage.RecipientType.TO, to);
		// 	message.setSubject(subject);
		// 	String contents =  "<h1>This is a test Spring Boot email</h1>" +
		// 	"<marquee><p>It can contain <strong>HTML</strong> content.</p></marquee>";
		// 	message.setContent(contents, "text/html; charset=utf-8");
		// 	mailSender.send(message);
		// 	System.out.println("MAIL SENT SUCCESSFULLY");
	

		// } catch (MessagingException e) {
		// 	// TODO Auto-generated catch block
		// 	e.printStackTrace();
		// }
	}

	@Override
	public void sendEmailWithAttachment(String from, String to, String subject, String body, String fileName, byte[] pdfBytes) {
		MimeMessage message = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(body, true); // Set to true to indicate HTML content
	
			// Add the PDF attachment
			helper.addAttachment(fileName, new ByteArrayDataSource(pdfBytes, "application/pdf"));
	
			mailSender.send(message);
			System.out.println("HTML WITH ATTACHMENT MAIL SENT SUCCESSFULLY");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

}

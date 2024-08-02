package hyung.jin.seo.jae.service.impl;

import java.util.Arrays;
import java.util.Collections;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.SimpleMailMessage;
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

	@Value("${spring.sender.assessment}")
    private String assessmentName;

	@Value("${spring.sender.invoice}")
    private String invoiceName;


	@Override
	public void test(){
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(invoiceName);
		message.setTo("jh05052008@gmail.com");
		message.setSubject("gone??");
		message.setText("can you see me?");
		mailSender.send(message);
	}

	@Override
	public void sendEmail(String from, String to, String subject, String body) {
		MimeMessage message = mailSender.createMimeMessage();
		try {
			message.setFrom(new InternetAddress(from));
			message.setRecipients(MimeMessage.RecipientType.TO, to);
			message.setSubject(subject);
			String contents =  "<h1>This is a test Spring Boot email</h1>" +
			"<marquee><p>It can contain <strong>HTML</strong> content.</p></marquee>";
			message.setContent(contents, "text/html; charset=utf-8");
			mailSender.send(message);
			System.out.println("MAIL SENT SUCCESSFULLY");
	

		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

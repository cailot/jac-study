package hyung.jin.seo.jae.service.impl;

import javax.mail.MessagingException;
import javax.mail.util.ByteArrayDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import hyung.jin.seo.jae.service.EmailService;
import hyung.jin.seo.jae.utils.JaeUtils;

@Service
public class EmailServiceImpl implements EmailService {
	
	@Autowired
	JavaMailSender mailSender;

	@Value("${spring.sender.assessment}")
    private String assessmentName;

	@Value("${spring.sender.invoice}")
    private String invoiceName;


	public void test(String to){
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(invoiceName);
		message.setTo(to);
		message.setSubject("gone??");
		message.setText("can you see me?");
		mailSender.send(message);
	}

	@Override
    public void emailReport(String to, String body, byte[] fileData) throws MessagingException {

        MimeMessagePreparator preparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true); // true indicates multipart
            messageHelper.setFrom(invoiceName);
            messageHelper.setTo(to);
            messageHelper.setSubject("Fwd: Assessment Submitted " + JaeUtils.getToday());
            messageHelper.setText(body, true);

            if (fileData != null && fileData.length > 0) {
                ByteArrayDataSource dataSource = new ByteArrayDataSource(fileData, "application/octet-stream");
                messageHelper.addAttachment("assessment.pdf", dataSource);
            }
        };

        mailSender.send(preparator);
    }

}

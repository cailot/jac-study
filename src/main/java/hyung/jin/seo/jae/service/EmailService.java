package hyung.jin.seo.jae.service;

import javax.mail.MessagingException;

public interface EmailService {

	// send report email
	void emailReport(String to, String body, byte[] data) throws MessagingException;

	// send report email with attachment
	void emailReport(String from, String to, String body, byte[] data) throws MessagingException;
}

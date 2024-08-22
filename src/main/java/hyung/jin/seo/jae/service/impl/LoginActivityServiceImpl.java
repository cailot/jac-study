package hyung.jin.seo.jae.service.impl;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hyung.jin.seo.jae.model.LoginActivity;
import hyung.jin.seo.jae.repository.LoginActivityRepository;
import hyung.jin.seo.jae.service.LoginActivityService;

@Service
public class LoginActivityServiceImpl implements LoginActivityService {
	
	@Autowired
	private LoginActivityRepository loginActivityRepository;

	@Override
	@Transactional
	public void saveLoginActivity(Long studentId) {
		LoginActivity loginActivity = new LoginActivity();
		loginActivity.setStudentId(studentId);
		loginActivityRepository.save(loginActivity);
	}
		
}

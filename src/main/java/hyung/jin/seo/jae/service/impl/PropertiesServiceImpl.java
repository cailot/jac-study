package hyung.jin.seo.jae.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import hyung.jin.seo.jae.service.PropertiesService;

@Service
public class PropertiesServiceImpl implements PropertiesService {

	@Value("${connected.homework.normal.week}")
	private String homeworkNormal;

	@Value("${connected.homework.short.week}")
	private String homeworkShort;;

	@Override
	public String getHomeworkNormal() {
		return homeworkNormal;
	}

	@Override
	public String getHomeworkShort() {
		return homeworkShort;
	}

}

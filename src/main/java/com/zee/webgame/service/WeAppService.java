package com.zee.webgame.service;

import java.util.List;
import java.util.Map;

import com.zee.webgame.model.SMS;

public interface WeAppService {
	
	public List<SMS> getUserSMSList(Map<String,Object> queryMap);

	public void insertSMS(SMS sms);
}

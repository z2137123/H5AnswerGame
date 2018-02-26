package com.zee.webgame.mapper;

import java.util.List;
import java.util.Map;

import com.zee.webgame.model.SMS;

public interface SMSMapper {
	
	
	List<SMS> getSMSByopenId(Map queryMap);
	
	void insertSMS(SMS	sms);
}

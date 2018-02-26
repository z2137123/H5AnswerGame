package com.zee.webgame.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zee.webgame.mapper.SMSMapper;
import com.zee.webgame.model.SMS;
import com.zee.webgame.service.WeAppService;

@Component("weAppService")
public class WeAppServiceImpl implements WeAppService {

	@Autowired
	private SMSMapper smsMapper;
	
	public List<SMS> getUserSMSList(Map<String,Object> queryMap) {
		// TODO Auto-generated method stub
		return smsMapper.getSMSByopenId(queryMap);
	}

	public void insertSMS(SMS sms) {
		smsMapper.insertSMS(sms);
		
	}
	public SMSMapper getSmsMapper() {
		return smsMapper;
	}

	public void setSmsMapper(SMSMapper smsMapper) {
		this.smsMapper = smsMapper;
	}


}

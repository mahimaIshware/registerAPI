package com.a2z.shop.bo;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import com.a2z.shop.eo.RegistrationEO;
import com.a2z.shop.model.OTP;
import com.a2z.shop.model.User;
import com.a2z.shop.vo.RspnsVO;

import ch.qos.logback.classic.Logger;

@Service
public class RegistrationBO {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(LoginBO.class);

	@Autowired
	private RegistrationEO registerEO;
	@Autowired
	BuildProperties buildProperties;

	public RspnsVO healthCheck()
 	{
 		final String methodName = "healthCheck()";
 		logger.info("{}: Performing Health Check", methodName);
 		RspnsVO rspnsVO = new RspnsVO();
 		if(!registerEO.isDBUp())
 		{
 			rspnsVO.setRspnsCode(700);
 			rspnsVO.setRspnsMsg("DB DOWN");
 			logger.info("DB DOWN....");
 		}else {
 		
 			rspnsVO.setRspnsCode(1000);
 			rspnsVO.setRspnsMsg("Health Check Successfull"+",BuildVersion:"+buildProperties.getVersion()+",BuildTime:"+buildProperties.getTime()+",BuildGroup:"+buildProperties.getGroup());
 			logger.info("Health Check Successfull....1");
 		}
 		return rspnsVO;
 	}

	public String addUpdateUser(User req) {
		logger.info("In RegistrationBO - addUpdateUser()");
		String response = registerEO.addUpdateUser(req);
		return response;
	}

	public String verifyToken(String confirmationToken)
	{
		logger.info("In RegistrationBO - verifyToken()");
		String response = registerEO.verifyToken(confirmationToken);
		return response;
	}


	public String verifyOTP(OTP otp)
	{
		logger.info("In RegistrationBO - verifyToken()");
		String response = registerEO.verifyOTP(otp);
		return response;
	}
	
	public String getUserRole(String userId)
	{
		logger.info("In RegistrationBO - getUserRole()");
		String response = registerEO.getUserRole(userId);
		return response;
	}

	public String getUserByDesc(String roleName) {
		logger.info("In RegistrationBO - getUserByDesc()");
		String response = registerEO.getUserByDesc(roleName);
		return response;
	}

	public RspnsVO checkExistingLoginID(String emailId) {
		final String methodName = "checkExistingLoginID()";
 		logger.info("{}: Checking for existing ID ", methodName);
 		RspnsVO rspnsVO = new RspnsVO();
 		if(registerEO.checkExistingLoginID(emailId))
 		{
 			rspnsVO.setRspnsCode(700);
 			rspnsVO.setRspnsMsg("ID ALREADY EXISTS");
 			logger.info("Id already exists");
 		}else {
 			rspnsVO.setRspnsCode(1000);
 			rspnsVO.setRspnsMsg("NO MATCHING ID");
 			logger.info("NO MATCHINH ID FOUND....");
 			
 		}
 		return rspnsVO;
	}

	public String sendResetPasswordEmail(User user) {
		logger.info("In RegistrationBO - sendResetPasswordEmail()");
		String response = registerEO.sendResetPasswordEmail(user);
		return response;
	}

	public String resetPassword(User user) {
		logger.info("In RegistrationBO - resetPassword()");
		String response = registerEO.resetPassword(user);
		return response;
	}

	public RspnsVO checkExistingPhoneNumber(String phoneNumber) {
		final String methodName = "checkExistingPhoneNumber()";
 		logger.info("{}: Checking for existing Phone number ", methodName);
 		RspnsVO rspnsVO = new RspnsVO();
 		if(registerEO.checkExistingPhoneNumber(phoneNumber))
 		{
 			rspnsVO.setRspnsCode(302);
 			rspnsVO.setRspnsMsg("PHONE NUMBER ALREADY EXISTS");
 			logger.info("PHONE NUMBER ALREADY EXISTS");
 		}else {
 			rspnsVO.setRspnsCode(200);
 			rspnsVO.setRspnsMsg("NOT FOUND ANY MATCHING PHONE NUMBER");
 			logger.info("NOT FOUND ANY MATCHING PHONE NUMBER....");
 			
 		}
 		return rspnsVO;
	}

	public String addUpdateManager(User req) {
		logger.info("In RegistrationBO - addUpdateManager()");
		String response = registerEO.addUpdateManager(req);
		return response;
	}

	public String getAllManager(long createdBy) {
		logger.info("In RegistrationBO - getAllManager()");
		String response = registerEO.getAllManager(createdBy);
		return response;
	}

	public String getManagerDetailsById(long userId,long createdBy) {
		logger.info("In RegistrationBO - getManagerDetailsById()");
		String response = registerEO.getManagerDetailsById(userId,createdBy);
		return response;
	}

	public String getAllUserRoles() {
		logger.info("In RegistrationBO - getAllUserRoles()");
		String response = registerEO.getAllUserRoles();
		return response;
	}
}

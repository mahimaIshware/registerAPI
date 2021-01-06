package com.a2z.shop.eo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.a2z.shop.dao.RegisterDAO;
import com.a2z.shop.model.ForgetPassword;
import com.a2z.shop.model.OTP;
import com.a2z.shop.model.User;
import com.a2z.shop.model.UserRoles;
import com.a2z.shop.services.messaging.EmailSenderService;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;

@Service
public class RegistrationEO {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(RegistrationEO.class);
	
	@Autowired
	private RegisterDAO registerDAO;
	@Autowired
	EmailSenderService emailSenderService;
	@Value("${base.url}")
	private String baseurl;
	public boolean isDBUp()
	{
		logger.info("isDBUp....RegistrationEO");
		return registerDAO.isDBUp();
	}
	public String addUpdateUser(User req) {
		StringBuilder responseBuilder=new StringBuilder("ERROR ADDING UPDATING USER");
		try{
			responseBuilder = registerDAO.addUpdateUser(req);
		}catch(Exception e)
		{
			logger.info("EXCEPTION IN EO - STORE - ADD UPDATE USER: {}"+ e.getMessage(), "MESAGE");
		}
		return responseBuilder.toString();
	}
	public String verifyToken(String confirmationToken) {
		StringBuilder responseBuilder=new StringBuilder("ERROR VERIFYING USER");
		try{
			responseBuilder = registerDAO.verifyToken(confirmationToken);
		}catch(Exception e)
		{
			logger.info("EXCEPTION IN EO - STORE - VERIFY USER: {}"+ e.getMessage(), "MESAGE");
		}
		return responseBuilder.toString();
	}

	public String verifyOTP(OTP otp) {
		StringBuilder responseBuilder=new StringBuilder("ERROR VERIFYING USER via OTP");
		try{
			responseBuilder = registerDAO.verifyOTP(otp);
		}catch(Exception e)
		{
			logger.info("EXCEPTION IN EO - STORE - VERIFY USER: {}"+ e.getMessage(), "MESAGE");
		}
		return responseBuilder.toString();
	}
	public String getUserRole(String userId) {
		ArrayList<UserRoles> userRoles;
		String response="";
		try{
			userRoles = (ArrayList<UserRoles>) registerDAO.getUserRole(userId);
			ObjectMapper objmapper = new ObjectMapper();
			response = objmapper.writeValueAsString(userRoles);
		}catch(Exception e)
		{
			logger.info("EXCEPTION IN EO - GET USER ROLE DESC: {}"+ e.getMessage(), "MESAGE");
		}
		return response;
	}
	public String getUserByDesc(String roleName) {
		ArrayList<User> userList;
		String response="";
		try{
			userList = (ArrayList<User>) registerDAO.getUserByDesc(roleName);
			ObjectMapper objmapper = new ObjectMapper();
			response = objmapper.writeValueAsString(userList);
		}catch(Exception e)
		{
			logger.info("EXCEPTION IN EO - GET USER BY ROLE DESC: {}"+ e.getMessage(), "MESAGE");
		}
		return response;
	}
	public boolean checkExistingLoginID(String emailId) {
		return registerDAO.checkExistingLoginID(emailId);
	}

	public String sendResetPasswordEmail(User user) {
		StringBuilder responseBuilder = new StringBuilder("ERROR IN SENDING PASSWORD RESET EMAIL.");
		try {
			User dbUser = registerDAO.getUserDetailsByEmailId(user);
			if (dbUser!= null) {
				ForgetPassword forgetPassword = new ForgetPassword(dbUser.getUserId());
				long id = registerDAO.addForgetPasswordInfo(forgetPassword);
				if(id>0) {
					Map<String, Object> model = new HashMap<String, Object>();
					model.put("firstName", dbUser.getFirstName());
					model.put("lastName", dbUser.getLastName());                    
					model.put("link", baseurl+"/shop/vendor_resetpassword.html?vendor_id="+dbUser.getUserId()+"&tokenId="+id);
					emailSenderService.sendMessageUsingFreemarkerTemplate(dbUser.getEmailId(),"Password Reset", model);
					forgetPassword.setPasswordResetEmailSend(true);
					forgetPassword.setActive(true);
					forgetPassword.setId(id);
					registerDAO.updateForgetPasswordDetails(forgetPassword);
					responseBuilder = new StringBuilder("PASSWORD RESET EMAIL SEND TO USER");
				}
				responseBuilder = new StringBuilder("PASSWORD RESET EMAIL SEND TO USER");
				return responseBuilder.toString();
			}
		} catch (Exception e) {
			logger.info("EXCEPTION IN EO - SENDING PASSWORD RESET EMAIL - VERIFY USER: {}" + e.getMessage(), "MESAGE");
		}
		return responseBuilder.toString();
	}
	public String resetPassword(User user) {
		StringBuilder responseBuilder = new StringBuilder("ERROR IN  PASSWORD RESET...");
		try {
			responseBuilder = registerDAO.resetPassword(user);
			
		} catch (Exception e) {
			logger.info("EXCEPTION IN EO -PASSWORD RESET FOR  USER: {}" + e.getMessage(), "MESAGE");
		}
		return responseBuilder.toString();
	}
	public boolean checkExistingPhoneNumber(String phoneNumber) {
		return registerDAO.checkExistingPhoneNumber(phoneNumber);
	}
	public String addUpdateManager(User req) {
		StringBuilder responseBuilder=new StringBuilder("ERROR ADDING UPDATING MANGER");
		try{
			responseBuilder = registerDAO.addUpdateManager(req);
		}catch(Exception e)
		{
			logger.info("EXCEPTION IN EO - STORE - ADD UPDATE Manager: {}"+ e.getMessage(), "MESAGE");
		}
		return responseBuilder.toString();
	}
	public String getManagerDetailsById(long userId,long createdBy) {
		StringBuilder responseBuilder = new StringBuilder("ERROR IN  GET MANAGER BY ID...");
		User dbUser = new User();
		try {
			dbUser = registerDAO.getManagerDetailsById(userId,createdBy);
			ObjectMapper objectMapper = new ObjectMapper();
			responseBuilder = new StringBuilder(objectMapper.writeValueAsString(dbUser));
			
		} catch (Exception e) {
			logger.info("EXCEPTION IN EO -  GET MANAGER BY ID: {}"+ e.getMessage(), "MESAGE");
		}
		return responseBuilder.toString();
	}
	public String getAllManager(long createdBy) {
		StringBuilder responseBuilder = new StringBuilder("ERROR IN GET ALL MANAGER...");
		ArrayList<User>dbUserList = new ArrayList<User>();
		try {
			dbUserList = registerDAO.getAllManager(createdBy);
			ObjectMapper objectMapper = new ObjectMapper();
			responseBuilder = new StringBuilder(objectMapper.writeValueAsString(dbUserList));
			
		} catch (Exception e) {
			logger.info("EXCEPTION IN EO -  GET ALL MANAGER: {}"+ e.getMessage(), "MESAGE");
		}
		return responseBuilder.toString();
	}
	public String getAllUserRoles() {
		StringBuilder responseBuilder = new StringBuilder("ERROR IN GET ALL USER ROLES...");
		ArrayList<UserRoles>dbUserRolesList = new ArrayList<UserRoles>();
		try {
			dbUserRolesList = registerDAO.getAllUserRoles();
			ObjectMapper objectMapper = new ObjectMapper();
			responseBuilder = new StringBuilder(objectMapper.writeValueAsString(dbUserRolesList));
			
		} catch (Exception e) {
			logger.info("EXCEPTION IN EO -  GET ALL USER ROLES: {}"+ e.getMessage(), "MESAGE");
		}
		return responseBuilder.toString();
	}
}

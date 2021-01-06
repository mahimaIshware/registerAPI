package com.a2z.shop.eo;

import com.a2z.shop.dao.LoginDAO;
import com.a2z.shop.model.Login;

import ch.qos.logback.classic.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginEO {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(RegistrationEO.class);
	
	@Autowired
	private LoginDAO loginDAO;
	public boolean isDBUp()
	{
		logger.info("isDBUp....RegistrationEO");
		return loginDAO.isDBUp();
	}
	public String verifyUser(Login loginDetails) {
		final  String regex = "^([a-zA-Z0-9_\\.\\-])+\\@(([a-zA-Z0-9\\-])+\\.)+([a-zA-Z0-9]{2,4})+$";
		StringBuilder responseBuilder=new StringBuilder("ERROR VERIFYING USER");
		try{
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher  = pattern.matcher(loginDetails.getEmail_id());
			if(matcher.matches()) {
				responseBuilder = loginDAO.verifyUser(loginDetails);
			}else {
				responseBuilder = loginDAO.verifyUserWithPhoneNumber(loginDetails);
			}
		}catch(Exception e)
		{
			logger.info("EXCEPTION IN EO - LOGIN"+ e.getMessage(), "MESAGE");
		}
		return responseBuilder.toString();
	}
	public boolean isShopExists(String vendorId) {
		logger.info("isShopExists....RegistrationEO");
		return loginDAO.isShopExists(vendorId);
	}
}

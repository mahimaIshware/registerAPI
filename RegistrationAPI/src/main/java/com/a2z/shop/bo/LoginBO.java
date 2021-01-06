package com.a2z.shop.bo;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import com.a2z.shop.eo.LoginEO;
import com.a2z.shop.model.Login;
import com.a2z.shop.vo.RspnsVO;

import ch.qos.logback.classic.Logger;

@Service
public class LoginBO {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(LoginBO.class);

	@Autowired
	private LoginEO loginEO;
	@Autowired
	BuildProperties buildProperties;

	public RspnsVO healthCheck()
 	{
 		final String methodName = "healthCheck()";
 		logger.info("{}: Performing Health Check", methodName);
 		RspnsVO rspnsVO = new RspnsVO();
 		if(!loginEO.isDBUp())
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

	public String verifyUser(Login loginDetails) {
		logger.info("In RegistrationBO - addUpdateUser()");
		String response = loginEO.verifyUser(loginDetails);
		return response;
	}

	public RspnsVO isShopExists(String vendorId) {
		final String methodName = "isShopExists()";
 		logger.info("{}: Checking for existing shop", methodName);
 		RspnsVO rspnsVO = new RspnsVO();
 		if(loginEO.isShopExists(vendorId))
 		{
 			rspnsVO.setRspnsCode(200);
 			rspnsVO.setRspnsMsg("Shop Exists");
 			logger.info("Shop Exists....");
 		}else {
 		
 			rspnsVO.setRspnsCode(404);
 			rspnsVO.setRspnsMsg("Shop does not exists");
 			logger.info("Shop does not exists");
 		}
 		return rspnsVO;
	}

}

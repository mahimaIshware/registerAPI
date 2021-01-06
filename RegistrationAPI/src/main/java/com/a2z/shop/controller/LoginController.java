package com.a2z.shop.controller;

import static com.a2z.shop.constant.ShopConstants.LOGIN_URL;
import static com.a2z.shop.constant.ShopConstants.HEALTH_CHECK_URL;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.a2z.shop.bo.LoginBO;
import com.a2z.shop.model.Login;
import com.a2z.shop.vo.RspnsVO;

import ch.qos.logback.classic.Logger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value =LOGIN_URL, tags = {"This is the controller for the Login Services"})
@RestController
@RequestMapping(LOGIN_URL)
public class LoginController {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(LoginController.class);
	@Autowired
	private LoginBO loginBO;
	
	@ApiOperation(value = "Health Check", notes = "This is health check.")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(value = HEALTH_CHECK_URL, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RspnsVO> healthCheck() {
		final String methodName = "healthCheck()";
		logger.info("{}: Checking Health of Shop Service", methodName);
		RspnsVO healthCheckRspnsVO = new RspnsVO();
		try {
			healthCheckRspnsVO = loginBO.healthCheck();
		}catch(Exception e)
		{
			healthCheckRspnsVO.setResponse("ERROR RESPONSE");
			healthCheckRspnsVO.setRspnsCode(1010);
			healthCheckRspnsVO.setRspnsMsg(null);
			return new ResponseEntity<RspnsVO>(healthCheckRspnsVO,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<RspnsVO>(healthCheckRspnsVO,HttpStatus.OK);
	}
	
	@ApiOperation(value = "User Login", notes = "This is user login initialisation service.")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
    @RequestMapping( value = "/userLogin",method= {RequestMethod.GET, RequestMethod.POST}, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RspnsVO> UserLogin(@RequestBody Login loginDetails) throws Exception
	{
		final String methodName = "UserLogin()";
		logger.info("{}: User Login",methodName);
		RspnsVO rspnsVO = new RspnsVO();
		try {
			rspnsVO.setRspnsCode(1000);
			rspnsVO.setRspnsMsg("GOOD");
			rspnsVO.setResponse(loginBO.verifyUser(loginDetails));
		}catch(Exception e)
		{
			rspnsVO.setResponse("ERROR RESPONSE");
			rspnsVO.setRspnsCode(1010);
			rspnsVO.setRspnsMsg(null);
			return new ResponseEntity<RspnsVO>(rspnsVO,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		logger.info("Response : {}", rspnsVO.toString());
		return new ResponseEntity<RspnsVO>(rspnsVO,HttpStatus.OK);
		
	}
	
	@ApiOperation(value = "Is Shop Exists", notes = "Check for existig shop")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping( value = "/isShopExists",produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RspnsVO> isShopExists(@RequestParam ("vendorId") String vendorId) throws Exception
	{
		final String methodName = "isShopExists()";
		logger.info("{}: user login",methodName);
		RspnsVO rspnsVO = new RspnsVO();
		try {
			rspnsVO= loginBO.isShopExists(vendorId);
		}catch(Exception e)
		{
			rspnsVO.setResponse("ERROR RESPONSE");
			rspnsVO.setRspnsCode(500);
			rspnsVO.setRspnsMsg(null);
			return new ResponseEntity<RspnsVO>(rspnsVO,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		logger.info("Response : {}", rspnsVO.toString());
		return new ResponseEntity<RspnsVO>(rspnsVO,HttpStatus.OK);
		
	}
}
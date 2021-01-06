package com.a2z.shop.controller;

import static com.a2z.shop.constant.ShopConstants.CHECK_EXISTING_LOGIN_ID_URL;
import static com.a2z.shop.constant.ShopConstants.HEALTH_CHECK_URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import com.a2z.shop.bo.RegistrationBO;
import com.a2z.shop.model.User;
import com.a2z.shop.vo.RspnsVO;

import ch.qos.logback.classic.Logger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "/manager", tags = {"This is the controller for the Manager registration  Services"})
@RestController
@RequestMapping("/manager")
public class ManagerRegistrationController {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(ManagerRegistrationController.class);
	@Autowired
	private RegistrationBO registrationBO;
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(value = HEALTH_CHECK_URL, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Health Check", notes = "This is health check.")
	public ResponseEntity<RspnsVO> healthCheck() {
		final String methodName = "healthCheck()";
		logger.info("{}: Health check", methodName);
		RspnsVO healthCheckRspnsVO = new RspnsVO();
		try {
			healthCheckRspnsVO = registrationBO.healthCheck();
		}catch(Exception e)
		{
			healthCheckRspnsVO.setResponse("ERROR RESPONSE");
			healthCheckRspnsVO.setRspnsCode(1010);
			healthCheckRspnsVO.setRspnsMsg(null);
			return new ResponseEntity<RspnsVO>(healthCheckRspnsVO,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<RspnsVO>(healthCheckRspnsVO,HttpStatus.OK);
	}
	@ApiOperation(value = "Add Manager", notes = "This is add manager.")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping(value = "/addUpdateManager",consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE,
			method = RequestMethod.POST)
	public ResponseEntity<RspnsVO> addUpdateManager(HttpServletRequest request, HttpServletResponse httpResponse, @RequestBody User req) throws Exception
	{
		final String methodName = "AddUpdateManager()";
		logger.info("{}: Adding User",methodName);
		logger.info(" ID : {}", req.getUserId());

		RspnsVO rspnsVO = new RspnsVO();
		try {
			rspnsVO.setRspnsCode(1000);
			rspnsVO.setRspnsMsg("GOOD");
			if(req.getCreatedBy()>0) {
				rspnsVO.setResponse(registrationBO.addUpdateManager(req));
			}
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
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Get Existing Login Id", notes = "This service match new login Id with existing Id's.If did not get any existing id allow user to register with that ID")
	@RequestMapping(value = CHECK_EXISTING_LOGIN_ID_URL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RspnsVO> checkExistingLoginID(@RequestParam("email_id") String emailId) throws Exception {
		final String methodName = "checkExistingLoginID()";
		logger.info("{}: Checking Existing Id's of Shop Service", methodName);
		RspnsVO checkExistingResponseVO = new RspnsVO();
		try {
			checkExistingResponseVO = registrationBO.checkExistingLoginID(emailId);
		}catch(Exception e)
		{
			checkExistingResponseVO.setResponse("ERROR RESPONSE");
			checkExistingResponseVO.setRspnsCode(1010);
			checkExistingResponseVO.setRspnsMsg(null);
			return new ResponseEntity<RspnsVO>(checkExistingResponseVO,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<RspnsVO>(checkExistingResponseVO,HttpStatus.OK);
		
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Check Existing Phone No", notes = "This service match new phone no  with existing one.If did not get any matching phone number  allow user to register with that Phone number")
	@RequestMapping(value = "/checkExistingPhoneNumber", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RspnsVO> checkExistingPhoneNumber(@RequestParam("phone") String phoneNumber) throws Exception {
		final String methodName = "checkExistingPhoneNumber()";
		logger.info("{}: Checking Existing phone of Shop Service", methodName);
		RspnsVO checkExistingResponseVO = new RspnsVO();
		try {
			checkExistingResponseVO = registrationBO.checkExistingPhoneNumber(phoneNumber);
		}catch(Exception e)
		{
			checkExistingResponseVO.setResponse("ERROR RESPONSE");
			checkExistingResponseVO.setRspnsCode(1010);
			checkExistingResponseVO.setRspnsMsg(null);
			return new ResponseEntity<RspnsVO>(checkExistingResponseVO,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<RspnsVO>(checkExistingResponseVO,HttpStatus.OK);
		
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Get manager by id", notes = "This service is to get single manager.")
	@RequestMapping(value = "/getManagerById", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RspnsVO> getManagerDetailsById(@RequestParam("userId")long userId,@RequestParam("createdBy")long createdBy) throws Exception {
		final String methodName = "getManagerDetailsById()";
		logger.info("{}: Get Manager by ID", methodName);
		RspnsVO responseVo = new RspnsVO();
		try {
			if(createdBy  != 0 && userId != 0) {
				responseVo.setRspnsMsg(registrationBO.getManagerDetailsById(userId,createdBy));
				responseVo.setResponse("Good");
				responseVo.setRspnsCode(200);
			}
		}catch(Exception e)
		{
			responseVo.setResponse("ERROR RESPONSE");
			responseVo.setRspnsCode(1010);
			responseVo.setRspnsMsg(null);
			return new ResponseEntity<RspnsVO>(responseVo,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<RspnsVO>(responseVo,HttpStatus.OK);
		
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Get all manger", notes = "This service is to get all manager list.")
	@RequestMapping(value = "/getAllManager", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RspnsVO> getAllManager(@RequestParam("createdBy")long createdBy) throws Exception {
		final String methodName = "getAllManager()";
		logger.info("{}: getting all manager", methodName);
		RspnsVO responseVo = new RspnsVO();
		try {
			if(createdBy  != 0) {
				responseVo.setRspnsMsg(registrationBO.getAllManager(createdBy));
				responseVo.setResponse("Good");
				responseVo.setRspnsCode(200);
			}
		}catch(Exception e)
		{
			responseVo.setResponse("ERROR RESPONSE");
			responseVo.setRspnsCode(1010);
			responseVo.setRspnsMsg(null);
			return new ResponseEntity<RspnsVO>(responseVo,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<RspnsVO>(responseVo,HttpStatus.OK);
		
	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Get All roles list", notes = "This service is to get roles of user from database. ")
	@RequestMapping(value = "/getAllUserRoles", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RspnsVO> getAllUserRoles() throws Exception {
		final String methodName = "getAllUserRoles()";
		logger.info("{}: getting all users roles", methodName);
		RspnsVO responseVo = new RspnsVO();
		try {
				responseVo.setRspnsMsg(registrationBO.getAllUserRoles());
				responseVo.setResponse("Good");
				responseVo.setRspnsCode(200);
		}catch(Exception e)
		{
			responseVo.setResponse("ERROR RESPONSE");
			responseVo.setRspnsCode(1010);
			responseVo.setRspnsMsg(null);
			return new ResponseEntity<RspnsVO>(responseVo,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<RspnsVO>(responseVo,HttpStatus.OK);
		
	}
	
}

package com.a2z.shop.controller;

import static com.a2z.shop.constant.ShopConstants.ADD_USER_URL;
import static com.a2z.shop.constant.ShopConstants.CHECK_EXISTING_LOGIN_ID_URL;
import static com.a2z.shop.constant.ShopConstants.CONFIRM_ACCOUNT_URL;
import static com.a2z.shop.constant.ShopConstants.GET_USER_BYDESC_URL;
import static com.a2z.shop.constant.ShopConstants.GET_USER_ROLE_URL;
import static com.a2z.shop.constant.ShopConstants.HEALTH_CHECK_URL;
import static com.a2z.shop.constant.ShopConstants.OTP_URL;
import static com.a2z.shop.constant.ShopConstants.REGISTRATION_URL;
import static com.a2z.shop.constant.ShopConstants.TOKEN_URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.a2z.shop.model.OTP;
import com.a2z.shop.model.User;
import com.a2z.shop.vo.RspnsVO;

import ch.qos.logback.classic.Logger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = REGISTRATION_URL, tags = {"This is the controller for the Registration  Services"})
@RestController
@RequestMapping(REGISTRATION_URL)
public class RegistrationController {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(RegistrationController.class);
	@Autowired
	private RegistrationBO registrationBO;
	@Value("${base.url}")
	private String baseurl;
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@GetMapping(value = HEALTH_CHECK_URL, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Health Check", notes = "This is health check.")
	public ResponseEntity<RspnsVO> healthCheck() {
		final String methodName = "healthCheck()";
		logger.info("{}: Checking Health of Shop Service", methodName);
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
	@ApiOperation(value = "Add User", notes = "This is add user.")
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@RequestMapping(value = ADD_USER_URL,consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE,
			method = RequestMethod.POST)
	public ResponseEntity<RspnsVO> addUser(HttpServletRequest request, HttpServletResponse httpResponse, @RequestBody User req) throws Exception
	{
		final String methodName = "addUser()";
		logger.info("{}: Adding User",methodName);
		logger.info(" ID : {}", req.getUserId());

		RspnsVO rspnsVO = new RspnsVO();
		try {
			rspnsVO.setRspnsCode(1000);
			rspnsVO.setRspnsMsg("GOOD");
			rspnsVO.setResponse(registrationBO.addUpdateUser(req));
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
	@ApiOperation(value = "Confirm Account ", notes = "This service validate user account.")
	@RequestMapping(value=CONFIRM_ACCOUNT_URL+TOKEN_URL, method= {RequestMethod.GET, RequestMethod.POST})
	public Object confirmAccount(HttpServletRequest request, HttpServletResponse response,   @RequestParam("token") String confirmationToken) throws Exception
	{
		final String methodName = "confirmAccount()";
		logger.info("{}: Account Confirmation",methodName);
		RspnsVO rspnsVO = new RspnsVO();
		try {
			rspnsVO.setRspnsCode(1000);
			rspnsVO.setRspnsMsg("GOOD");
			String tokenresponse = registrationBO.verifyToken(confirmationToken);
			if(tokenresponse != null && !tokenresponse.isEmpty()) {
				rspnsVO.setResponse(tokenresponse);
				if(tokenresponse.equals("Account verified via Web Token!")||tokenresponse.equals("Account already verified!")) {
					response.sendRedirect(baseurl+"/shop/vendor_setup_shop.html");
				}else if(tokenresponse.equals("Token is expired!")) {
					String url = baseurl+"/shop/vendor_signin.html";
					return "<p>Token is Expired.</p><a target=\"_blank\" href="+url+">Re login</a>";
				}else if(tokenresponse.equals("Enter Token does not match")) {
					String url = baseurl+"/shop/vendor_signin.html";
					return "<p>Token does not exists.</p><a target=\"_blank\" href="+url+">Re login</a>";
				}else if(tokenresponse.equals("Invalid Token")) {
					String url = baseurl+"/shop/vendor_signin.html";
					return "<p>Invalid Token!</p><a target=\"_blank\" href="+url+"><button>Re login</button></a>";
				}else{
					response.sendRedirect(baseurl+"/shop/vendor_signin.html");
				}
				
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
	@ApiOperation(value = "Confirm Account OTP", notes = "This is confirm account otp.")
	@RequestMapping(value=CONFIRM_ACCOUNT_URL+OTP_URL, method= {RequestMethod.GET, RequestMethod.POST})
	public ResponseEntity<RspnsVO> confirmAccountOTP(@RequestBody OTP otp) throws Exception
	{
		final String methodName = "confirmAccountOTP()";
		logger.info("{}: Account Confirmation",methodName);
		RspnsVO rspnsVO = new RspnsVO();
		try {
			rspnsVO.setRspnsCode(1000);
			rspnsVO.setRspnsMsg("GOOD");
			rspnsVO.setResponse(registrationBO.verifyOTP(otp));
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
	@ApiOperation(value = "Get User Role", notes = "This service check perticular user role from db and return user role.")
	@RequestMapping(value=GET_USER_ROLE_URL, method= RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RspnsVO> getUserRole(@RequestParam("userId") String userId) throws Exception
	{
		final String methodName = "getUserRole()";
		logger.info("{}: Getting User Role",methodName);
		logger.info("User Role ID : {}", userId);

		RspnsVO rspnsVO = new RspnsVO();
		try {
			rspnsVO.setRspnsCode(1000);
			rspnsVO.setRspnsMsg("GOOD");
			if (!"".equals(userId) && !userId.isEmpty()) {
				rspnsVO.setResponse(registrationBO.getUserRole(userId));

			}
		} catch (Exception e) {
			rspnsVO.setResponse("ERROR RESPONSE");
			rspnsVO.setRspnsCode(1010);
			rspnsVO.setRspnsMsg(null);
			return new ResponseEntity<RspnsVO>(rspnsVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		logger.info("Response : {}", rspnsVO.toString());
		return new ResponseEntity<RspnsVO>(rspnsVO, HttpStatus.OK);
	}
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Get User By Description", notes = "This service fetch list of all user having any specific role e.g @Admin")
	@RequestMapping(value = GET_USER_BYDESC_URL, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RspnsVO> getUserByDesc(@RequestParam("roleName") String roleName) throws Exception {
		final String methodName = "getUserByDesc()";
		logger.info("{}: Getting User By Role Desc", methodName);
		logger.info("User Role Desc : {}", roleName);

		RspnsVO rspnsVO = new RspnsVO();
		try {
			rspnsVO.setRspnsCode(1000);
			rspnsVO.setRspnsMsg("GOOD");
			if (!"".equals(roleName) && !roleName.isEmpty()) {
				rspnsVO.setResponse(registrationBO.getUserByDesc(roleName));

			}
		} catch (Exception e) {
			rspnsVO.setResponse("ERROR RESPONSE");
			rspnsVO.setRspnsCode(1010);
			rspnsVO.setRspnsMsg(null);
			return new ResponseEntity<RspnsVO>(rspnsVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		logger.info("Response : {}", rspnsVO.toString());
		return new ResponseEntity<RspnsVO>(rspnsVO, HttpStatus.OK);

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
	@ApiOperation(value = "Send Reset password email to user.", notes = "This api send use an email that will help to reset user password.")
	@RequestMapping(value = "/sendResetEmail", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RspnsVO> sendResetPasswordEmail(@RequestBody User user) throws Exception {
		final String methodName = "sendResetPasswordEmail()";
		logger.info("sending password reset email to user {}:", methodName);
		logger.info("Sending email to the user to password reset : {}", user.getEmailId());

		RspnsVO rspnsVO = new RspnsVO();
		try {
			rspnsVO.setRspnsCode(1000);
			rspnsVO.setRspnsMsg("GOOD");
			if (user.getEmailId() != null  && !user.getEmailId().equals("")) {
				rspnsVO.setResponse(registrationBO.sendResetPasswordEmail(user));

			}
		} catch (Exception e) {
			rspnsVO.setResponse("ERROR RESPONSE");
			rspnsVO.setRspnsCode(1010);
			rspnsVO.setRspnsMsg(null);
			return new ResponseEntity<RspnsVO>(rspnsVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		logger.info("Response : {}", rspnsVO.toString());
		return new ResponseEntity<RspnsVO>(rspnsVO, HttpStatus.OK);

	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Reset password for user.", notes = "This api change password for registerd user.")
	@RequestMapping(value = "/resetPassword", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RspnsVO> resetPassword(@RequestBody User user) throws Exception {
		final String methodName = "resetPassword()";
		logger.info("Resetting user password {}:", methodName);

		RspnsVO rspnsVO = new RspnsVO();
		try {
			rspnsVO.setRspnsCode(1000);
			rspnsVO.setRspnsMsg("GOOD");
			if (user.getUserId() != 0  && user.getPassword() != null  && !user.getPassword().equals("") ) {
				rspnsVO.setResponse(registrationBO.resetPassword(user));

			}
		} catch (Exception e) {
			rspnsVO.setResponse("ERROR RESPONSE");
			rspnsVO.setRspnsCode(1010);
			rspnsVO.setRspnsMsg(null);
			return new ResponseEntity<RspnsVO>(rspnsVO, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		logger.info("Response : {}", rspnsVO.toString());
		return new ResponseEntity<RspnsVO>(rspnsVO, HttpStatus.OK);

	}
	
	@CrossOrigin(origins = "*", allowedHeaders = "*")
	@ApiOperation(value = "Check Existing Phone No", notes = "This service match new phone no  with existing one.If did not get any matching phone number  allow user to register with that Phone number")
	@RequestMapping(value = "/checkExistingPhoneNumber", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<RspnsVO> checkExistingPhoneNumber(@RequestParam("phone") String phoneNumber) throws Exception {
		final String methodName = "checkExistingPhoneNumber()";
		logger.info("{}: Checking Existing Id's of Shop Service", methodName);
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
}

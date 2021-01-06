package com.a2z.shop.dao;

import static com.a2z.shop.constant.ShopConstants.COUNT_USER_SQL;
import static com.a2z.shop.constant.ShopConstants.GET_EMAIL_SQL;
import static com.a2z.shop.constant.ShopConstants.GET_OTP_SQL;
import static com.a2z.shop.constant.ShopConstants.GET_STATUS_SQL;
import static com.a2z.shop.constant.ShopConstants.GET_TIME_OTP_SQL;
import static com.a2z.shop.constant.ShopConstants.GET_TIME_SQL;
import static com.a2z.shop.constant.ShopConstants.GET_TOKEN_SQL;
import static com.a2z.shop.constant.ShopConstants.GET_USERID_SQL;
import static com.a2z.shop.constant.ShopConstants.GET_USEROLE_SQL;
import static com.a2z.shop.constant.ShopConstants.GET_USER_BYDESC_SQL;
import static com.a2z.shop.constant.ShopConstants.GET_USER_BY_ID;
import static com.a2z.shop.constant.ShopConstants.GET_USER_SQL;
import static com.a2z.shop.constant.ShopConstants.HEALTH_CHECK_SQL;
import static com.a2z.shop.constant.ShopConstants.INSERT_TOKEN_SQL;
import static com.a2z.shop.constant.ShopConstants.INSERT_USER_ROLE_SQL;
import static com.a2z.shop.constant.ShopConstants.INSERT_USER_SQL;
import static com.a2z.shop.constant.ShopConstants.UPDATE_USER_SQL;
import static com.a2z.shop.constant.ShopConstants.UPDATE_CONFIRMATION_TOKEN_STATUS_SQL;
import static com.a2z.shop.constant.ShopConstants.GET_EXISTING_EMAIL_ID_SQL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import com.a2z.shop.model.ConfirmationToken;
import com.a2z.shop.model.ForgetPassword;
import com.a2z.shop.model.Login;
import com.a2z.shop.model.OTP;
import com.a2z.shop.model.User;
import com.a2z.shop.model.UserRoles;
import com.a2z.shop.services.messaging.EmailSenderService;

@Component
public class RegisterDAO {
	private static final Logger logger = LoggerFactory.getLogger(RegisterDAO.class);

	// protected static final String HEALTH_CHECK_SQL = "Select CURRENT_DATE()";

	private transient static JdbcTemplate jdbcTemplate;

	@Autowired
	private transient DataSource datasource;
	@Autowired
	private EmailSenderService emailSenderService;
	@Value("${base.url}")
	private String baseurl;
	@Value("${base.url.token}")
	private String baseurlToken;

	@Autowired
	public RegisterDAO(DataSource dataSource) throws SQLException {
		logger.debug("Inside RegisterDAO Constructor");
		this.datasource = dataSource;
		jdbcTemplate = new JdbcTemplate(this.datasource);
	}

	public boolean isDBUp() {
		final String methodName = "isDBUp()";
		logger.info("{}: Logger DB HEALTH CHECK ", methodName);
		boolean isDBStatusUp = false;

		try {
			// long startTime = System.currentTimeMillis();
			jdbcTemplate.queryForObject(HEALTH_CHECK_SQL, new Object[] {}, Date.class);
			logger.info("The Database is Up & Running");
			isDBStatusUp = true;
		} catch (Exception e) {
			logger.error("Exception Occurred while Connecting to database :{} " + e.getMessage(), e);
			;
		}
		return isDBStatusUp;
	}

	public StringBuilder addUpdateUser(User req) {
		final String methodName = "addUpdateUser()";
		StringBuilder response = new StringBuilder("ERROR ADDING UPDATING USER");
		logger.info("{}:", methodName);
		try {
			String count = (String) jdbcTemplate.queryForObject(COUNT_USER_SQL, String.class, req.getEmailId());
			if (!count.equals("1")) {
				logger.info("INSERTING USER:" + req.getUserId());
				jdbcTemplate.update(INSERT_USER_SQL, req.getEmailId(), req.getPassword(), false, req.getPhone(),
						req.getFirstName(), req.getLastName(),new Timestamp(System.currentTimeMillis()),new Timestamp(System.currentTimeMillis()));
				long id = (long) jdbcTemplate.queryForObject(GET_USERID_SQL, long.class, req.getEmailId());
				logger.info("ID from the Table : {}", id);
				req.setUserId(id);
				if (!req.getRoles().isEmpty()) {
					for (UserRoles r : req.getRoles()) {
						jdbcTemplate.update(INSERT_USER_ROLE_SQL, id, r.getRoleId());
						logger.info("User Role Added from the Table : {}", r.getRoleName());
					}
				} else {
					/**
					 * Default Role Added for user if not provide role id in json.
					 */
					jdbcTemplate.update(INSERT_USER_ROLE_SQL, id, 1);
					logger.info("User Role Added from the Table : {}", "ADMIN");
				}
				ConfirmationToken token = new ConfirmationToken(req);
				jdbcTemplate.update(INSERT_TOKEN_SQL, token.getTokenid(), token.getConfirmationToken(),
						token.getCreatedDate(), token.getVerificationLimit(), req.getUserId(), token.getOTP(),
						token.getTokenStatus());
				Map<String, Object> model = new HashMap<String, Object>();
				model.put("firstName", req.getFirstName());
				model.put("lastName", req.getLastName());
				model.put("otp", token.getOTP());
				model.put("tokenVerification",
						baseurlToken+"/registration/confirm-account/token?token=" + token.getConfirmationToken());
				model.put("otpVerification",
						baseurl + "/shop/vendor_otp.html?uid=" + req.getUserId() + "&otp=" + token.getOTP());
				emailSenderService.sendMessageUsingFreemarkerTemplateTokenVerification(req.getEmailId(),
						"Account Verification", model);
				logger.info("User data inserted for User ID " + req.getUserId());
				logger.info("GENERATING CONFIRMATION TOKEN FOR:" + req.getUserId());
				response = new StringBuilder("User data inserted and token generated for UserID= " + req.getUserId());
			} else {
				response = new StringBuilder("User already exists!");
			}
		} catch (Exception e) {
			logger.error("Exception Adding Updating User to database :{} " + e.getMessage(), e);
		}
		return response;
	}

	public StringBuilder verifyOTP(OTP otp) {
		final String methodName = "verifyOTP()";
		StringBuilder response = new StringBuilder("ERROR VERIFYING USER VIA OTP");
		logger.info("{}:", methodName);
		try {
			boolean isOtpExisted = jdbcTemplate.queryForObject("Select Exists(SELECT otp from confirmation_token where user_id = ? and otp=CAST(? AS UNSIGNEd))", new Object[] { otp.getUid(),otp.getOtp()},boolean.class);
			if(isOtpExisted) {
				String dbotp =  jdbcTemplate.queryForObject(GET_OTP_SQL, String.class, new Object[] { otp.getUid(),otp.getOtp()});
				String email =  jdbcTemplate.queryForObject(GET_EMAIL_SQL, String.class, otp.getUid());
				String firstName = jdbcTemplate.queryForObject(
						"SELECT first_name FROM shopdb.vendor_user_details WHERE user_id=?", String.class, otp.getUid());
				String lastName = jdbcTemplate.queryForObject(
						"SELECT last_name FROM shopdb.vendor_user_details WHERE user_id=?", String.class, otp.getUid());
				Date limit = jdbcTemplate.queryForObject(GET_TIME_OTP_SQL, Date.class, otp.getOtp());
				Date dt = new Date();
				boolean isActivelink = limit.compareTo(dt) > 0;
				boolean isOtpmatch = dbotp.equals(otp.getOtp());
				if(!isActivelink) {
					return response = new StringBuilder("OTP is expired!");
				}
				if(!isOtpmatch) {
					return response = new StringBuilder("Enter otp does not match.");
				}
				boolean status = jdbcTemplate.queryForObject(GET_STATUS_SQL, boolean.class, email);
				if (!status) {
					if (isActivelink && isOtpmatch) {
						jdbcTemplate.update(UPDATE_USER_SQL, otp.getUid());
						jdbcTemplate.update(UPDATE_CONFIRMATION_TOKEN_STATUS_SQL, otp.getUid());
						Map<String, Object> model = new HashMap<String, Object>();
						model.put("firstName", firstName);
						model.put("lastName", lastName);
						emailSenderService.sendMessageUsingFreemarkerTemplateRegistrationSuccess(email,
								"Registration Success", model);
						response = new StringBuilder("Account verified via OTP!");
					}
				} else {
					response = new StringBuilder("Account already verified!");
				}
			}else {
				response = new StringBuilder("Invalid OTP!");
			}
		} catch (Exception e) {
			logger.error("Exception Adding Updating User to database :{} " + e.getMessage(), e);
		}
		return response;
	}

	public StringBuilder verifyToken(String confirmationToken) {
		final String methodName = "verifyToken()";
		StringBuilder response = new StringBuilder("ERROR VERIFYING TOKEN");
		logger.info("{}:", methodName);
		try {
			boolean isTokenExisted = jdbcTemplate.queryForObject("Select Exists(SELECT confirmation_token from confirmation_token where confirmation_token = ?)", new Object[] { confirmationToken},boolean.class);
			if(isTokenExisted) {
				String token =  jdbcTemplate.queryForObject(GET_TOKEN_SQL, String.class, confirmationToken);
				Date limit = jdbcTemplate.queryForObject(GET_TIME_SQL, Date.class, confirmationToken);
				Date dt = new Date();
				long userId =  jdbcTemplate.queryForObject(GET_USER_SQL, long.class, confirmationToken);
				String email =  jdbcTemplate.queryForObject(GET_EMAIL_SQL, String.class, userId);
				boolean status =  jdbcTemplate.queryForObject(GET_STATUS_SQL, boolean.class, email);
				String firstName = jdbcTemplate.queryForObject(
						"SELECT first_name FROM shopdb.vendor_user_details WHERE user_id=?", String.class,
						userId);
				String lastName = jdbcTemplate.queryForObject(
						"SELECT last_name FROM shopdb.vendor_user_details WHERE user_id=?", String.class,
						userId);
				boolean isActivelink = limit.getTime() -dt.getTime() > 0;
				boolean isTokenMatch = token.equals(confirmationToken);
				if(!isActivelink) {
					return response = new StringBuilder("Token is expired!");
				}
				if(!isTokenMatch) {
					return response = new StringBuilder("Enter Token does not match");
				}
				if (!status) {
					logger.info("Token : {}, Confirmation Token : {}, User-ID : {}, Time : {}", token, confirmationToken,
							userId, limit);
					if (token.equals(confirmationToken) && limit.compareTo(dt) > 0) {
						jdbcTemplate.update(UPDATE_USER_SQL, userId);
						jdbcTemplate.update(UPDATE_CONFIRMATION_TOKEN_STATUS_SQL, userId);
						Map<String, Object> model = new HashMap<String, Object>();
						model.put("firstName", firstName);
						model.put("lastName", lastName);
						emailSenderService.sendMessageUsingFreemarkerTemplateRegistrationSuccess(email,
								"Registration Success", model);
						response = new StringBuilder("Account verified via Web Token!");
					}
				} else {
					response = new StringBuilder("Account already verified!");
				}
			}else {
				response = new StringBuilder("Invalid Token");
			}
		} catch (Exception e) {
			logger.error("Exception Verifying User :{} " + e.getMessage(), e);
		}
		return response;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<UserRoles> getUserRole(String userId) {
		return jdbcTemplate.query(GET_USEROLE_SQL, new Object[] { userId }, new RowMapper() {

			@Override
			public ArrayList<UserRoles> mapRow(ResultSet rs, int rowNum) throws SQLException {
				if (rs.getString("role_name") != null) {
					ArrayList<UserRoles> roleName = new ArrayList<UserRoles>();
					UserRoles u = new UserRoles();
					u.setRoleId(rs.getLong("role_id"));
					u.setRoleName(rs.getString("role_name"));
					u.setCreatedDate(rs.getDate("created_date"));
					u.setLastModifiedDate(rs.getDate("last_modified_date"));
					roleName.add(u);
					return roleName;
				}
				return null;
			}
		});
	}

	@SuppressWarnings("unchecked")
	public ArrayList<User> getUserByDesc(String roleName) {
		return (ArrayList<User>) jdbcTemplate.query(GET_USER_BYDESC_SQL, new Object[] { roleName }, new RowMapper() {

			@Override
			public List<User> mapRow(ResultSet rs, int rowNum) throws SQLException {
				if (rs.getString("user_id") != null) {
					ArrayList<User> userList = new ArrayList<User>();
					User u = new User();
					u.setUserId(rs.getLong("user_id"));
					u.setEmailId(rs.getString("email_id"));
					u.setEnabled(rs.getBoolean("is_enabled"));
					u.setFirstName(rs.getString("first_name"));
					u.setLastName(rs.getString("last_name"));
					if (rs.getString("role_name") != null) {
						UserRoles roles = new UserRoles();
						roles.setRoleId(rs.getLong("role_id"));
						roles.setRoleName(rs.getString("role_name"));
						ArrayList<UserRoles> r = new ArrayList<UserRoles>();
						r.add(roles);
						u.setRoles(r);
					}
					userList.add(u);
					return userList;
				}
				return null;
			}
		});
	}

	public boolean checkExistingLoginID(String emailId) {
		final String methodName = "checkExistingLoginID()";
		logger.info("{}: Logger DB CHECK  EXISTING ID", methodName);
		boolean isMatchFound = false;
		try {
			isMatchFound = jdbcTemplate.queryForObject(GET_EXISTING_EMAIL_ID_SQL, boolean.class, emailId);
		} catch (Exception e) {
			logger.error("Exception Occurred while getting Existing ID :{} " + e.getMessage(), e);
		}
		return isMatchFound;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public User getUserDetailsByEmailId(User user) {
		logger.info("Getting user info by ID :{} ");
		ArrayList<User> adminUserList = new ArrayList<User>();
		System.out.println(adminUserList);
		if (user.getEmailId() != null && !user.getEmailId().equals("")) {
			boolean isExists = jdbcTemplate.queryForObject(
					"SELECT EXISTS(SELECT 1 from shopdb.vendor_user_details where email_id=?)",
					new Object[] { user.getEmailId() }, boolean.class);
			if (isExists) {
				try {
					String queryVendorId = "SELECT user_id FROM shopdb.vendor_user_details WHERE email_id=?";
					int id = jdbcTemplate.queryForObject(queryVendorId, new Object[] { user.getEmailId() }, int.class);
					if (id != 0) {
						adminUserList = (ArrayList<User>) jdbcTemplate.query(GET_USER_BY_ID, new Object[] { id },
								new RowMapper() {

									@Override
									public User mapRow(ResultSet rs, int rowNum) throws SQLException {
										if (rs.getString("user_id") != null) {
											ArrayList<UserRoles> r = new ArrayList<UserRoles>();
											User u = new User();
											u.setUserId(rs.getLong("user_id"));
											u.setEmailId(rs.getString("email_id"));
											u.setEnabled(rs.getBoolean("is_enabled"));
											u.setFirstName(rs.getString("first_name"));
											u.setLastName(rs.getString("last_name"));
											if (rs.getString("role_name").split(",").length > 1) {
												for (int i = 0; i < rs.getString("role_name").split(",").length; i++) {
													UserRoles roles = new UserRoles();
													roles.setRoleId(Long.decode(rs.getString("role_id").split(",")[i]));
													roles.setRoleName(rs.getString("role_name").split(",")[i]);
													r.add(roles);
												}
											} else {
												if (rs.getString("role_name") != null) {
													UserRoles roles = new UserRoles();
													roles.setRoleId(rs.getLong("role_id"));
													roles.setRoleName(rs.getString("role_name"));
													r.add(roles);
												}
											}
											u.setRoles(r);
											return u;
										}
										return null;
									}
								});
					}
					
				} catch (Exception e) {
					logger.error("Exception Occurred while Connecting to database :{} ", e.getMessage());
				}
				if (adminUserList != null && !adminUserList.isEmpty()) {
					return adminUserList.get(0);
				}
				return null;
			}
		}
		return null;

	}

	public StringBuilder resetPassword(User user) {
		StringBuilder response = new StringBuilder("ERROR IN CHANGING USER PASSWORD");
		try {
			/* this is forget password table id */
			long id = user.getCreatedBy();
			Date validationLimit = jdbcTemplate.queryForObject(
					"select verification_limit from shopdb.vendor_forget_password where id=? and active='1'", new Object[] { id },
					Date.class);
			boolean emailLinkSent = jdbcTemplate.queryForObject(
					"select pass_reset_mail_send from shopdb.vendor_forget_password where id=?", new Object[] { id },
					boolean.class);
			boolean active = jdbcTemplate.queryForObject("select active from shopdb.vendor_forget_password where id=?  and active='1' ",
					new Object[] { id }, boolean.class);
			if (!active) {
				return response = new StringBuilder("Link not active.Please send another link.");
			}
			if (!emailLinkSent) {
				return response = new StringBuilder("Not get any request to  password change.");
			}
			if (validationLimit.getTime() - new Date().getTime() == 0
					|| validationLimit.getTime() - new Date().getTime() < 0) {
				return response = new StringBuilder("Email Link expired.Please generate new link to reset password.");
			}

			if (validationLimit.getTime() - new Date().getTime() > 0 && emailLinkSent && active) {
				boolean isExists = jdbcTemplate.queryForObject(
						"SELECT EXISTS(SELECT 1 from shopdb.vendor_user_details where user_id=?)",
						new Object[] { user.getUserId() }, boolean.class);
				if (isExists) {
					jdbcTemplate.update("UPDATE shopdb.vendor_user_details SET password =? WHERE user_id=?",
							new Object[] { user.getPassword(), user.getUserId() });
					jdbcTemplate.update("UPDATE shopdb.vendor_forget_password set active=? where id=?", false,id);
					return response = new StringBuilder("PASSWORD UPDATED SUCCESSFULLY");
				}
			}
		} catch (Exception e) {
			logger.error("Exception Occurred while Connecting to database :{} ", e.getMessage());
		}

		return response;

	}

	public boolean checkExistingPhoneNumber(String phoneNumber) {
		final String methodName = "checkExistingPhoneNumber()";
		logger.info("Checking for duplicate entry:" + phoneNumber);
		logger.info("{}:", methodName);
		boolean numberExists = false;
		try {
			if (phoneNumber != null && !phoneNumber.equals("")) {
				numberExists = jdbcTemplate.queryForObject(
						"SELECT EXISTS( SELECT 1 FROM shopdb.vendor_user_details where phone LIKE ?)",
						new Object[] { phoneNumber }, new int[] { Types.VARCHAR }, boolean.class);
			}
		} catch (Exception e) {
			logger.error("ERROR VERIFYING USER : {} " + e.getMessage(), e);
		}
		return numberExists;
	}

	public StringBuilder addUpdateManager(User req) {
		final String methodName = "addUpdateManager()";
		StringBuilder response = new StringBuilder("ERROR ADDING UPDATING MANAGER");
		logger.info("{}:", methodName);
		try {
			String count = (String) jdbcTemplate.queryForObject(COUNT_USER_SQL, String.class, req.getEmailId());
			if (!count.equals("1") && req.getUserId()==0) {
				logger.info("INSERTING Manager Details:" + req.getUserId());
				String query = "INSERT INTO shopdb.vendor_user_details (email_id, password, is_enabled, phone, first_name, last_name,update_ts, create_ts, created_by) VALUES(?,?,?,?,?,?,?,?,?)";
				jdbcTemplate.update(query, req.getEmailId(), req.getPassword(), false, req.getPhone(),
						req.getFirstName(), req.getLastName(), new Timestamp(System.currentTimeMillis()),
						new Timestamp(System.currentTimeMillis()), req.getCreatedBy());
				long id = (long) jdbcTemplate.queryForObject(GET_USERID_SQL, long.class, req.getEmailId());
				logger.info("ID from the Table : {}", id);
				req.setUserId(id);
				if (!req.getRoles().isEmpty()) {
					for (UserRoles r : req.getRoles()) {
						jdbcTemplate.update(INSERT_USER_ROLE_SQL, id, r.getRoleId());
						logger.info("User Role Added from the Table : {}", r.getRoleName());
					}
				} else {
					/**
					 * Default Role Added for manger if not provide role id in json.
					 */
					jdbcTemplate.update(INSERT_USER_ROLE_SQL, id, 4);
					logger.info("User Role Added from the Table : {}", "MANAGER");
				}
				ConfirmationToken token = new ConfirmationToken(req);
				jdbcTemplate.update(INSERT_TOKEN_SQL, token.getTokenid(), token.getConfirmationToken(),
						token.getCreatedDate(), token.getVerificationLimit(), req.getUserId(), token.getOTP(),
						token.getTokenStatus());

				jdbcTemplate.update(UPDATE_USER_SQL, id);
				jdbcTemplate.update(UPDATE_CONFIRMATION_TOKEN_STATUS_SQL, id);
				Map<String, Object> model = new HashMap<String, Object>();
				model.put("firstName", req.getFirstName());
				model.put("lastName", req.getLastName());
				emailSenderService.sendMessageUsingFreemarkerTemplateRegistrationSuccess(req.getEmailId(),
						"Manager Registration Success", model);
				response = new StringBuilder("User data inserted and token generated for UserID= " + req.getUserId());
			} else if(count.equals("1")&& req.getUserId()>0) {
				logger.info("Updating Manager Details:" + req.getUserId());
				String query = "UPDATE shopdb.vendor_user_details SET password=?, is_enabled=?, phone=?, first_name=?, last_name=?,update_ts=? Where email_id=? and user_id=? and created_by=?";
				jdbcTemplate.update(query, req.getPassword(), true, req.getPhone(),
						req.getFirstName(), req.getLastName(), new Timestamp(System.currentTimeMillis()),req.getEmailId(),req.getUserId(), req.getCreatedBy());
				if (!req.getRoles().isEmpty()) {
					for (UserRoles r : req.getRoles()) {
						jdbcTemplate.update("UPDATE shopdb.users_roles SET user_id=?, role_id=? ", req.getUserId(), r.getRoleId());
						logger.info("User Role Added from the Table : {}", r.getRoleName());
					}
				} else {
					/**
					 * Default Role Added for manger if not provide role id in json.
					 */
					jdbcTemplate.update("UPDATE shopdb.users_roles SET user_id=?, role_id=? ", req.getUserId(), 4);
					logger.info("User Role Added from the Table : {}", "MANAGER");
				}
				response = new StringBuilder("Manager Details are edited  UserID= " + req.getUserId());
			}
		} catch (Exception e) {
			logger.error("Exception Adding Updating manager to database :{} " + e.getMessage(), e);
		}
		return response;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public User getManagerDetailsById(long userId, long createdBy) {
		logger.info("Getting manger info by ID :{} ", "getManagerDetailsById()");
		ArrayList<User> mangerUserList = new ArrayList<User>();
		if (userId != 0 && createdBy != 0) {
			boolean isExists = jdbcTemplate.queryForObject(
					"SELECT EXISTS(SELECT 1 from shopdb.vendor_user_details where user_id=? and created_by = ?)",
					new Object[] { userId, createdBy }, boolean.class);
			if (isExists) {
				try {
					String getManagerQuery = "SELECT vud.user_id,vud.phone,vud.password, vud.email_id, vud.first_name, vud.last_name, vud.is_enabled,vud.created_by, group_concat(r.role_id) as role_id , group_concat(r.role_name) as role_name  from vendor_user_details vud   inner join users_roles urls on vud.user_id = urls.user_id inner join roles r on r.role_id = urls.role_id where  vud.user_id =? and vud.created_by = ? group by vud.user_id";
					mangerUserList = (ArrayList<User>) jdbcTemplate.query(getManagerQuery,
							new Object[] { userId, createdBy }, new RowMapper() {

								@Override
								public User mapRow(ResultSet rs, int rowNum) throws SQLException {
									if (rs.getString("user_id") != null) {
										ArrayList<UserRoles> r = new ArrayList<UserRoles>();
										User u = new User();
										u.setUserId(rs.getLong("user_id"));
										u.setEmailId(rs.getString("email_id"));
										u.setEnabled(rs.getBoolean("is_enabled"));
										u.setFirstName(rs.getString("first_name"));
										u.setLastName(rs.getString("last_name"));
										u.setCreatedBy(rs.getLong("created_by"));
										u.setPhone(rs.getString("phone"));
										u.setPassword(rs.getString("password"));
										if (rs.getString("role_name").split(",").length > 1) {
											for (int i = 0; i < rs.getString("role_name").split(",").length; i++) {
												UserRoles roles = new UserRoles();
												roles.setRoleId(Long.decode(rs.getString("role_id").split(",")[i]));
												roles.setRoleName(rs.getString("role_name").split(",")[i]);
												r.add(roles);
											}
										} else {
											if (rs.getString("role_name") != null) {
												UserRoles roles = new UserRoles();
												roles.setRoleId(rs.getLong("role_id"));
												roles.setRoleName(rs.getString("role_name"));
												r.add(roles);
											}
										}
										u.setRoles(r);
										return u;
									}
									return null;
								}
							});

				} catch (Exception e) {
					logger.error("Exception Occurred while Connecting to database :{} ", e.getMessage());
				}
				if (mangerUserList != null && !mangerUserList.isEmpty()) {
					return mangerUserList.get(0);
				}
				return null;
			}
		}
		return null;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<User> getAllManager(long createdBy) {
		logger.info("Getting All manger info by :{} ", "getAllManager()");
		ArrayList<User> mangerUserList = new ArrayList<User>();
		if (createdBy != 0) {
			try {
				String getManagerQuery = "SELECT vud.user_id, vud.email_id,vud.phone, vud.first_name, vud.last_name, vud.is_enabled,vud.created_by, group_concat(r.role_id) as role_id , group_concat(r.role_name) as role_name  from vendor_user_details vud   inner join users_roles urls on vud.user_id = urls.user_id inner join roles r on r.role_id = urls.role_id where vud.created_by = ? group by vud.user_id";
				mangerUserList = (ArrayList<User>) jdbcTemplate.query(getManagerQuery, new Object[] { createdBy },
						new RowMapper() {

							@Override
							public User mapRow(ResultSet rs, int rowNum) throws SQLException {
								if (rs.getString("user_id") != null) {
									ArrayList<UserRoles> r = new ArrayList<UserRoles>();
									User u = new User();
									u.setUserId(rs.getLong("user_id"));
									u.setEmailId(rs.getString("email_id"));
									u.setPhone(rs.getString("phone"));
									u.setEnabled(rs.getBoolean("is_enabled"));
									u.setFirstName(rs.getString("first_name"));
									u.setLastName(rs.getString("last_name"));
									u.setCreatedBy(rs.getLong("created_by"));
									if (rs.getString("role_name").split(",").length > 1) {
										for (int i = 0; i < rs.getString("role_name").split(",").length; i++) {
											UserRoles roles = new UserRoles();
											roles.setRoleId(Long.decode(rs.getString("role_id").split(",")[i]));
											roles.setRoleName(rs.getString("role_name").split(",")[i]);
											r.add(roles);
										}
									} else {
										if (rs.getString("role_name") != null) {
											UserRoles roles = new UserRoles();
											roles.setRoleId(rs.getLong("role_id"));
											roles.setRoleName(rs.getString("role_name"));
											r.add(roles);
										}
									}
									u.setRoles(r);
									return u;
								}
								return null;
							}
						});

			} catch (Exception e) {
				logger.error("Exception Occurred while Connecting to database :{} ", e.getMessage());
			}
			return mangerUserList;
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<UserRoles> getAllUserRoles() {
		ArrayList<UserRoles> userRoles = new ArrayList<UserRoles>();
		try {
			String userRolesQuery = "select role_id, role_name from roles ";
			return userRoles = (ArrayList<UserRoles>) jdbcTemplate.query(userRolesQuery, new RowMapper() {

				@Override
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					if (rs.getString("role_id") != null) {
						UserRoles userRoles = new UserRoles();
						userRoles.setRoleId(rs.getLong("role_id"));
						userRoles.setRoleName(rs.getString("role_name"));
						return userRoles;
					}
					return null;
				}
			});

		} catch (Exception e) {
			logger.error("Exception Occurred while Connecting to database :{} ", e.getMessage());
		}
		return null;
	}

	public long addForgetPasswordInfo(ForgetPassword forgetPassword) {
		String methodName = "addForgetPasswordInfo()";
		logger.info("{}: to insert data for password reset email", methodName);
		long id = 0;
		try {
			logger.info("{}: inserting data in vendor_forget_password", methodName);
			String insertQuery = "INSERT INTO shopdb.vendor_forget_password (vendor_id, pass_reset_mail_send, active, update_ts, create_ts,token,verification_limit) VALUES(?,?,?,?,?,?,?)";
			jdbcTemplate.update(insertQuery, forgetPassword.getVendorId(), forgetPassword.isPasswordResetEmailSend(),
					forgetPassword.isActive(), forgetPassword.getUpdateTs(), forgetPassword.getCreatedTs(),
					forgetPassword.getToken(), forgetPassword.getVerificationLimit());
			String getIdSql = "select vfp.id from shopdb.vendor_forget_password as vfp  where vfp.vendor_id=? and vfp.pass_reset_mail_send=? and vfp.active=?  and token=?";
			id = jdbcTemplate.queryForObject(getIdSql, new Object[] { forgetPassword.getVendorId(),
					forgetPassword.isPasswordResetEmailSend(), forgetPassword.isActive(), forgetPassword.getToken() },
					long.class);
		} catch (Exception e) {
			logger.error("Exception Occurred while Connecting to database :{} ", e.getMessage());
		}
		return id;

	}

	public void updateForgetPasswordDetails(ForgetPassword forgetPassword) {
		String methodName = "updateForgetPasswordDetails()";
		logger.info("{}: to updating  data for password reset email", methodName);
		try {
			logger.info("{}: updating data in vendor_forget_password", methodName);
			String updateQuery = "UPDATE shopdb.vendor_forget_password set pass_reset_mail_send=?, active=?, update_ts=? where id=? and vendor_id=?";
			jdbcTemplate.update(updateQuery, forgetPassword.isPasswordResetEmailSend(), forgetPassword.isActive(),
					new Date(), forgetPassword.getId(), forgetPassword.getVendorId());
		} catch (Exception e) {
			logger.error("Exception Occurred while Connecting to database :{} ", e.getMessage());
		}
	}

}

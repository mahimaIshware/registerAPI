package com.a2z.shop.dao;

import static com.a2z.shop.constant.ShopConstants.COUNT_USER_SQL;
import static com.a2z.shop.constant.ShopConstants.GET_PASSWORD_SQL;
import static com.a2z.shop.constant.ShopConstants.HEALTH_CHECK_SQL;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.a2z.shop.constant.ShopConstants;
import com.a2z.shop.model.Login;

import ch.qos.logback.classic.Logger;

@Component
public class LoginDAO {
	private static final Logger logger = (Logger) LoggerFactory.getLogger(LoginDAO.class);
	
	//protected static final String HEALTH_CHECK_SQL = "Select CURRENT_DATE()";
	
	private transient static JdbcTemplate jdbcTemplate;

	@Autowired
	private transient DataSource datasource;

	@Autowired
	public LoginDAO(DataSource dataSource) throws SQLException {
		logger.debug("Inside LoginDAO Constructor");
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
		} catch(Exception e)
		{
			logger.error("Exception Occurred while Connecting to database :{} "+e.getMessage(), e);;
		}
		return isDBStatusUp;
	}
	
	public StringBuilder verifyUser(Login loginDetails)
	{
		final String methodName = "verifyUser()";
        StringBuilder response = new StringBuilder("ERROR VERIFYING USER");
        logger.info(loginDetails.getEmail_id()+" "+loginDetails.getPassword());
		logger.info("{}:", methodName);
		try {
			int user_id;
            String count = (String)jdbcTemplate.queryForObject(COUNT_USER_SQL,String.class, loginDetails.getEmail_id());
            logger.info(count);
            if(count.equals("1"))
            {
            String password = (String)jdbcTemplate.queryForObject(GET_PASSWORD_SQL,String.class, loginDetails.getEmail_id());
            logger.info(password);
            if(password.equals(loginDetails.getPassword()))
            {
            	user_id = (Integer)jdbcTemplate.queryForObject(ShopConstants.GET_USERID_SQL,Integer.class, loginDetails.getEmail_id());
                response = new StringBuilder("USER VERIFIED WITH USER ID="+user_id);
            }
			}
			else
			{
				response = new StringBuilder("USER DOESN'T EXIST!");
			}
			} catch(Exception e)
			{
				logger.error("ERROR VERIFYING USER : {} "+e.getMessage(), e);;
			}
		return response;	
	}

	public boolean isShopExists(String vendorId) {
		final String methodName = "isShopExists()";
		logger.info("{}: Shop CHECK ", methodName);
		boolean isExist = false;

		if (vendorId != null && !vendorId.isEmpty() && !"".equals(vendorId)) {
			try {
				String query = "SELECT EXISTS(SELECT 1 FROM shopdb.vendor_config  WHERE vendor_id =CAST(? AS UNSIGNED) AND is_enabled=1)";
				isExist = jdbcTemplate.queryForObject(query, new Object[] { vendorId }, boolean.class);
			} catch (Exception e) {
				logger.error("Exception Occurred while Connecting to database :{} " + e.getMessage(), e);
			}

		}
		return isExist;
	}

	public StringBuilder verifyUserWithPhoneNumber(Login loginDetails) {
		final String methodName = "verifyUserWithPhoneNumber()";
		StringBuilder response = new StringBuilder("ERROR VERIFYING USER");
		logger.info(loginDetails.getEmail_id() + " " + loginDetails.getPassword());
		logger.info("{}:", methodName);
		try {
			int user_id;
			String phoneNumber = loginDetails.getEmail_id();
			boolean userExists = jdbcTemplate.queryForObject(
					"SELECT EXISTS( SELECT 1 FROM shopdb.vendor_user_details where phone LIKE '%'||?||'%' AND is_enabled=1)",
					new Object[] { phoneNumber }, new int[] {Types.VARCHAR}, boolean.class);
			if (userExists) {
				String password = (String) jdbcTemplate.queryForObject(
						"SELECT password from shopdb.vendor_user_details where phone=?", String.class,
						loginDetails.getEmail_id());
				logger.info(password);
				if (password.equals(loginDetails.getPassword())) {
					user_id = (Integer) jdbcTemplate.queryForObject(
							"SELECT user_id from vendor_user_details where phone=?", Integer.class,
							loginDetails.getEmail_id());
					response = new StringBuilder("USER VERIFIED WITH USER ID=" + user_id);
				}
			} else {
				response = new StringBuilder("USER DOESN'T EXIST!");
			}
		} catch (Exception e) {
			logger.error("ERROR VERIFYING USER : {} " + e.getMessage(), e);
		}
		return response;
	}

}

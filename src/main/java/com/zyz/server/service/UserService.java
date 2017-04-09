package com.zyz.server.service;

import com.zyz.server.bean.User;
import com.zyz.server.bean.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	private static volatile Map<Long,UserSession> SESSIONMAP = new HashMap<Long, UserSession>();

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final String SPLITSTRING = "#";

	@Autowired
	private UserCache userCache;

	public boolean createUser(User user)
	{
		return userCache.saveUserCache(user);
	}

	/**
	 * 删除用户,只是删除缓存中的数据
	 * @param userId
	 * @return
	 */
	public boolean deleteUser(long userId)
	{
		try{
			userCache.deleteUserCache(userId);
		}catch (Exception e){
			LOGGER.error("deleteUser error:",e);
		}
		return true;
	}

	public boolean disableUser(long userId)
	{
		try{
			User user = userCache.getUserFromCache(userId);
			if(null==user){
				LOGGER.error("userId=>{} noExistent ",userId);
			}
			user.setEnabled(false);
		}catch (Exception e){
			LOGGER.error("disableUser error:",e);
			return false;
		}
		return true;
	}

	public List<User> queryUsers(String userNamePrex,boolean onlyValidUser)
	{
		return userCache.queryUsersFromCache(userNamePrex,onlyValidUser);
	}

	/**
	 * 如果密码不对，返回的UserSession对象里sessionId为空，客户端可以依次判断，参照UserSession.isValid方法
	 * @param userName
	 * @param md5EncodedPassword
	 * @return
	 */
	public UserSession login(String userName, String md5EncodedPassword)
	{
		UserSession userSession = new UserSession();
		if(null==userName||"".equals(userName)){
			LOGGER.error("userName is null");
			return userSession;
		}
		if(null==md5EncodedPassword||"".equals(md5EncodedPassword)){
			LOGGER.error("password is null");
			return userSession;
		}
		User user = this.getUser(userName);
		if(null==user){
			LOGGER.error("userName=>{} is existed",userName);
			return userSession;
		}
		try{
			if(encoderByMd5(user.getPassword()).equals(md5EncodedPassword)){
				if(!SESSIONMAP.containsKey(user.getUserId())){
					userSession.setCreateTime(System.currentTimeMillis());
					userSession.setSessionId(UUID.randomUUID().toString());
					userSession.setUserId(user.getUserId());
					userSession.setUserName(user.getUserName());
					userSession.setValidSeconds((short) (30*60));
					SESSIONMAP.put(user.getUserId(),userSession);
				}else{
					userSession = SESSIONMAP.get(user.getUserId());
					LOGGER.info("userId=>{} already login",user.getUserId());
				}
			}
		}catch (Exception e){
			LOGGER.error("check passWord error:",e);
		}
		return userSession;
	}

	public User getUser(String userName){
		if(null==userName||userName.length()==0){
			LOGGER.error("userName is null break");
			return null;
		}
		return userCache.getUserFromCache(userName);
	}

	private String encoderByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
	    //确定计算方法
		MessageDigest md5= MessageDigest.getInstance("MD5");
		BASE64Encoder base64en = new BASE64Encoder();
        //加密后的字符串
		String newstr=base64en.encode(md5.digest(str.getBytes("utf-8")));
		return newstr;
	}

}

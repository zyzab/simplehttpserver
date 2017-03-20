package com.zyz.server.service;

import com.zyz.server.bean.User;
import com.zyz.server.bean.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;



public class UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	public boolean createUser(User user)
	{
		return true;
	}
	public boolean deleteUser(long userId)
	{
		return true;
	}
	public boolean disableUser(long userId)
	{
		return true;
	}
	public List<User> queryUsers(String userNamePrex,boolean onlyValidUser)
	{
		return null;
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
		if(null==user.getPassword()){

		}
		return userSession;
	}

	public User getUser(String userName){
		return null;
	}

	public static void main(String[] args) {

	}

	public String EncoderByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
	    //确定计算方法
 　　　 MessageDigest md5= MessageDigest.getInstance("MD5");
　　　　BASE64Encoder base64en = new BASE64Encoder();
        //加密后的字符串
　　　　String newstr=base64en.encode(md5.digest(str.getBytes("utf-8")));
 　　　 return newstr;
　　}

}

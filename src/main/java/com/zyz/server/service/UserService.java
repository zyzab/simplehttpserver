package com.zyz.server.service;

import com.zyz.server.bean.User;
import com.zyz.server.bean.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;


public class UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	private static final Map<Long,User> USERMAP = new TreeMap<Long,User>();

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
		try{
			if(encoderByMd5(user.getPassword()).equals(md5EncodedPassword)){
				userSession.setCreateTime(System.currentTimeMillis());
				userSession.setSessionId(UUID.randomUUID().toString());
				userSession.setUserId(user.getUserId());
				userSession.setUserName(user.getUserName());
				userSession.setValidSeconds((short) (30*60));
			}
		}catch (Exception e){
			LOGGER.error("check passWord error:",e);
		}
		return userSession;
	}

	public User getUser(String userName){
		return null;
	}

	public static void main(String[] args) throws Exception{
		Map<Long,User> userMap = reloadUsers();
		LOGGER.info("info");
	}

	private String encoderByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
	    //确定计算方法
		MessageDigest md5= MessageDigest.getInstance("MD5");
		BASE64Encoder base64en = new BASE64Encoder();
        //加密后的字符串
		String newstr=base64en.encode(md5.digest(str.getBytes("utf-8")));
		return newstr;
	}


	public static Map<Long,User> reloadUsers(){
		Map<Long,User> userMap = new TreeMap<Long, User>();
		File userFile = new File("./user.txt");
		if(null==userFile||!userFile.exists()){
			return null;
		}
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(userFile));
			String tempString = null;
			String[] userArrayStr = null;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				userArrayStr = tempString.split("|");
				User user = new User();
				user.setUserName(userArrayStr[1]);
				user.setUserId(Long.valueOf(userArrayStr[0]));
				user.setEnabled(Boolean.valueOf(userArrayStr[3]));
				user.setPassword(userArrayStr[2]);

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

				user.setRegDate(sdf.parse(userArrayStr[4]));
				userMap.put(Long.valueOf(userArrayStr[0]),user);
			}
			reader.close();
		}catch (Exception e){
			LOGGER.error("reload users error:",e);
		}finally {
			if(null!=reader){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return userMap;
	}

}

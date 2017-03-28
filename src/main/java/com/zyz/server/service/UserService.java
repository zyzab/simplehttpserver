package com.zyz.server.service;

import com.zyz.server.bean.User;
import com.zyz.server.bean.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;


public class UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	private static volatile Map<Long,User> USERCACHE = new TreeMap<Long,User>();

	private static volatile Map<Long,UserSession> SESSIONMAP = new HashMap<Long, UserSession>();

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final String SPLITSTRING = "#";

	public boolean createUser(User user)
	{
		File userFile = null;
		try{
			userFile = new File("./user.txt");
			if(null==userFile||!userFile.exists()){
				LOGGER.warn("user resource不存在,已创建");
				userFile.createNewFile();
				userFile = new File("./user.txt");
			}
			StringBuilder userBuilder = new StringBuilder();
			this.fillUserString(user,userBuilder);
			this.saveFile(userBuilder.toString(),userFile,true);
			USERCACHE.put(user.getUserId(),user);
		}catch (Exception e){
			LOGGER.error("create users error:",e);
			USERCACHE.remove(user.getUserId());
			return false;
		}
		return true;
	}

	public boolean deleteUser(long userId)
	{
		try{
			USERCACHE.remove(userId);
		}catch (Exception e){
			LOGGER.error("deleteUser error:",e);
		}
		return true;
	}

	public boolean disableUser(long userId)
	{
		try{
			User user = USERCACHE.get(userId);
			user.setEnabled(false);
		}catch (Exception e){
			LOGGER.error("disableUser error:",e);
		}
		return true;
	}

	public List<User> queryUsers(String userNamePrex,boolean onlyValidUser)
	{

		List<User> list = new ArrayList<User>();
		for(User user : USERCACHE.values()){
			if(user.getUserName().startsWith(userNamePrex)&&user.isEnabled()==onlyValidUser){
				list.add(user);
			}
		}
		return list;
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
		User user = null;
		for(User temp : USERCACHE.values()){
			if(temp.getUserName().equals(userName)){
				user = temp;
				break;
			}
		}
		return user;
	}

	private String encoderByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
	    //确定计算方法
		MessageDigest md5= MessageDigest.getInstance("MD5");
		BASE64Encoder base64en = new BASE64Encoder();
        //加密后的字符串
		String newstr=base64en.encode(md5.digest(str.getBytes("utf-8")));
		return newstr;
	}

	public void reWrite(){
		File userFile = new File("../user.txt");
		if(USERCACHE.isEmpty()){
			userFile.deleteOnExit();
			LOGGER.info("缓存中不存在数据,清空文件");
			return;
		}
		StringBuilder userString = new StringBuilder(1024);
		for(User user : USERCACHE.values()){
			fillUserString(user,userString);
		}
		saveFile(userString.toString(),userFile,false);
	}

	/**
	 * 把User对象数据填充到对应格式的字符串
	 * @param user
	 * @param userBuilder
	 */
	private void fillUserString (User user,StringBuilder userBuilder){
		userBuilder.append(user.getUserId()).append(SPLITSTRING);
		userBuilder.append(user.getUserName()).append(SPLITSTRING);
		userBuilder.append(user.getPassword()).append(SPLITSTRING);
		userBuilder.append(0).append(SPLITSTRING);
		userBuilder.append(SDF.format(new Date()));
		userBuilder.append("\r\n");
	}

	/**
	 * 保存文件
	 * @param str
	 * @param file
	 * @param isAppend  是否累加(在文件末尾添加)
	 */
	private void saveFile(String str,File file,boolean isAppend){
		BufferedWriter writer = null;
		try{
			if(null==file||!file.exists()){
				LOGGER.warn("file 不存在,已创建");
				file.createNewFile();
				file = new File("./user.txt");
			}
			writer = new BufferedWriter(new FileWriter(file,isAppend));
			writer.append(str);
			writer.close();
		}catch (Exception e){
			LOGGER.error(" saveFile error:",e);
		}finally {
			if(null!=writer){
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//lock.unlock();
		}
	}

	/**
	 * 把文件的用户信息加载到缓存
	 * @return
	 */
	public static void reloadUsersCache(){
		Map<Long,User> userMap = new TreeMap<Long, User>();
		File userFile = new File("./user.txt");
		if(null==userFile||!userFile.exists()){
			return;
		}
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(userFile));
			String tempString = null;
			String[] userArrayStr = null;
			// 一次读入一行，直到读入null为文件结束
			while ((tempString = reader.readLine()) != null) {
				userArrayStr = tempString.split("#");
				User user = new User();
				user.setUserName(userArrayStr[1]);
				user.setUserId(Long.valueOf(userArrayStr[0]));
				user.setEnabled(Boolean.valueOf(userArrayStr[3]));
				user.setPassword(userArrayStr[2]);
				user.setRegDate(SDF.parse(userArrayStr[4]));
				userMap.put(Long.valueOf(userArrayStr[0]), user);
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
		USERCACHE = userMap;
	}

}

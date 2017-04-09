package com.zyz.server.service;

import com.zyz.server.bean.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zyz on 17/4/3.
 */
public class UserCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCache.class);

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String SPLITSTRING = "#";

    private String userFilePath;

    private static final Map<Long,User> USERCACHEMAP = new HashMap<Long, User>();

    public boolean saveUserCache(User user){
        if(null==user||null==user.getUserId()){
            LOGGER.info("saveUserCache fail , user is null break!");
            return false;
        }
        File userFile = null;
        try{
            userFile = new File(this.getUserFilePath());
            if(null==userFile||!userFile.exists()){
                LOGGER.warn("user resource不存在,已创建");
                userFile.createNewFile();
                userFile = new File(this.getUserFilePath());
            }
            StringBuilder userBuilder = new StringBuilder();
            this.fillUserString(user,userBuilder);
            this.saveFile(userBuilder.toString(),userFile,true);
            USERCACHEMAP.put(user.getUserId(),user);
            return true;
        }catch (Exception e){
            LOGGER.error("create users error:",e);
            return false;
        }
    }

    public void deleteUserCache(Long userId){
        if(null==userId){
            LOGGER.info("delete UserCache fail , userId is null break!");
            return;
        }
        USERCACHEMAP.remove(userId);
    }

    public User getUserFromCache(Long userId){
        return USERCACHEMAP.get(userId);
    }

    public User getUserFromCache(String userName){
        User user = null;
        for(User temp : USERCACHEMAP.values()){
            if(temp.getUserName().equals(userName)){
                user = temp;
                break;
            }
        }
        return user;
    }

    public List<User> queryUsersFromCache(String userNamePrex, boolean onlyValidUser)
    {

        List<User> list = new ArrayList<User>();
        for(User user : USERCACHEMAP.values()){
            if(user.getUserName().startsWith(userNamePrex)&&user.isEnabled()==onlyValidUser){
                list.add(user);
            }
        }
        return list;
    }


    /**
     * 把文件的用户信息加载到缓存
     * @return
     */
    public void reloadUsersCache(){
        File userFile = new File(this.getUserFilePath());
        if(null==userFile||!userFile.exists()){
            try{
                userFile.createNewFile();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(userFile));
            String tempString = null;
            String[] userArrayStr = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                userArrayStr = tempString.split(SPLITSTRING);
                User user = new User();
                user.setUserName(userArrayStr[1]);
                user.setUserId(Long.valueOf(userArrayStr[0]));
                user.setEnabled(Boolean.valueOf(userArrayStr[3]));
                user.setPassword(userArrayStr[2]);
                user.setRegDate(SDF.parse(userArrayStr[4]));
                USERCACHEMAP.put(Long.valueOf(userArrayStr[0]), user);
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
    }

    public String getUserFilePath(){
        return userFilePath;
    }

    public void setUserFilePath(String userFilePath){
        this.userFilePath = userFilePath;
    }

    /**
     * 程序关闭时,调用保存缓存中用户的信息
     */
    public void reWrite(){
        File userFile = new File("../user.txt");
        if(USERCACHEMAP.isEmpty()){
            userFile.deleteOnExit();
            LOGGER.info("缓存中不存在数据,清空文件");
            return;
        }
        StringBuilder userString = new StringBuilder(1024);
        for(User user : USERCACHEMAP.values()){
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
                file = new File(this.getUserFilePath());
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
        }
    }
}

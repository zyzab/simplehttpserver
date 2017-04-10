package com.zyz;

import com.zyz.server.bean.User;
import com.zyz.server.bean.UserSession;
import com.zyz.server.config.ServerConfig;
import com.zyz.server.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Date;

/**
 * Created by zyz on 17/4/9.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@ContextConfiguration(classes=ServerConfig.class, loader=AnnotationConfigContextLoader.class)
public class UserServerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServerTest.class);

    @Autowired
    private UserService userService;

    @Test
    public void createUserTest(){
        User user = new User();
        user.setUserName("zyzab123");
        user.setEnabled(true);
        user.setPassword("123");
        user.setRegDate(new Date());
        user.setUserId(6L);
        userService.createUser(user);
        LOGGER.info("createUser finish");
    }

    @Test
    public void deleteUserTest(){
        Long userId = 6L;
        userService.deleteUser(userId);
        LOGGER.info("delete userId=>{} finish",userId);
    }


    @Test
    public void disableUserTest(){
        Long userId = 5L;
        userService.disableUser(userId);
        LOGGER.info("disableUser userId=>{} finish",userId);
    }

    @Test
    public void getUserTest(){
        User user = userService.getUser("zyz");
        LOGGER.info("getUser userName=>{} finish",user.getUserName());
    }


    @Test
    public void loginTest() throws Exception{
        String userName = "zyzab";
        UserSession userSession = userService.login(userName,userService.encoderByMd5("123"));
        if(userSession.isValid()){
            LOGGER.info("userName=>{} login success",userName);
        }else {
            LOGGER.info("userName=>{} login fail",userName);
        }
    }

}

package com.zyz.server;

import com.zyz.server.bean.User;
import com.zyz.server.config.ServerConfig;
import com.zyz.server.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Date;

/**
 * Created by zyz on 17/3/22.
 */
public class Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        System.setProperty("spring.profiles.default","prod");
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(ServerConfig.class);
        UserService userService = applicationContext.getBean(UserService.class);
        User user = new User();
        user.setUserName("zyzab12");
        user.setEnabled(true);
        user.setPassword("123");
        user.setRegDate(new Date());
        user.setUserId(4L);
        userService.createUser(user);
    }
}

package com.zyz.server.config;

import com.zyz.server.service.UserCache;
import org.springframework.context.annotation.*;

/**
 * Created by zyz on 17/4/7.
 */
@Configuration
@ComponentScan("com.zyz.server.*")
public class ServerConfig {

    @Bean(initMethod = "reloadUsersCache", destroyMethod = "reWrite" )
    @Profile("prod")
    public UserCache getUserCache(){
        UserCache userCache = new UserCache();
        userCache.setUserFilePath("./user_prod.txt");
        userCache.reloadUsersCache();
        return  userCache;
    }

    @Bean(initMethod = "reloadUsersCache", destroyMethod = "reWrite" )
    @Profile("test")
    public UserCache getTestUserCache(){
        UserCache userCache = new UserCache();
        userCache.setUserFilePath("./user_test.txt");
        userCache.reloadUsersCache();
        return  userCache;
    }

}

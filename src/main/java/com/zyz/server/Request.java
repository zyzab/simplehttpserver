package com.zyz.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zyz on 17/3/16.
 */
public class Request {

    private static final Logger logger = LoggerFactory.getLogger(Request.class);

    private InputStream inputStream;

    private String url;

    public InputStream getInputStream() {
        return inputStream;
    }

    public  Request(InputStream inputStream){
        this.inputStream = inputStream;
    }

    public void parse(){
        StringBuilder requestStr = new StringBuilder(2048);
        int i;
        byte[] bytes = new byte[2048];
        try{
            i = inputStream.read(bytes);
        }catch (IOException e){
            logger.error("request parse error :",e);
            i = -1;
        }
        for(int j=0;j<i;j++){
            requestStr.append((char)bytes[j]);
        }
        logger.info("request {}",requestStr);
        url = parseUrl(requestStr.toString());
    }

    private String parseUrl(String requestString){
        int index1,index2;
        index1 = requestString.indexOf("  ");
        if (index1>1){
            index2 = requestString.indexOf("  ",index1+1);
            if (index2>index1){
                return requestString.substring(index1+1,index2);
            }
        }
        return null;
    }

    public String getUrl() {
        return url;
    }
}

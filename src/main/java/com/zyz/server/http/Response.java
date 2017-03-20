package com.zyz.server.http;

import com.zyz.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by zyz on 17/3/16.
 */
public class Response {

    private static final Logger logger = LoggerFactory.getLogger(Response.class);

    private static final int BUFFER_SIZE = 1024;

    Request request;

    OutputStream outputStream;

    public Response(OutputStream outputStream){
        this.outputStream = outputStream;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void sendStaticResource() throws IOException{
        byte[] bytes = new byte[BUFFER_SIZE];
        FileInputStream fileInputStream = null;
        try{
            File file = new File(HttpServer.WEB_ROOT,request.getUrl());
            if(file.exists()){
                fileInputStream = new FileInputStream(file);
                int ch = fileInputStream.read(bytes,0,BUFFER_SIZE);
                while (ch!=-1){
                    outputStream.write(bytes);
                    ch = fileInputStream.read(bytes,0,BUFFER_SIZE);
                }
            }else {
                StringBuilder errorMessage = new StringBuilder("HTTP/1.1 404 File Not Fount\r\n");
                errorMessage = errorMessage.append("Content-Type: text/html\r\n");
                errorMessage = errorMessage.append("Content-Length: 23\r\n");
                errorMessage = errorMessage.append("\r\n");
                errorMessage = errorMessage.append("<h1>File Not Found</h1>");
                outputStream.write(errorMessage.toString().getBytes());
            }
        }catch (Exception e){
            logger.error("response error:",e);
        }finally {
            if(null!=fileInputStream){
                fileInputStream.close();
            }
        }
    }
}

package com.example.app;


import org.apache.commons.io.FileUtils;
import org.apache.rocketmq.broker.BrokerStartup;
import org.apache.rocketmq.namesrv.NamesrvStartup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class MockServer {

    public static void main(String[] args) throws Exception {
        startRocketMQ();
        SpringApplication.run(MockServer.class, args);
    }

    public static void startRocketMQ() throws Exception {
        String userHomeDir = System.getProperty("user.home");
        System.out.println("User Home Directory: " + userHomeDir);
        File directory = new File(userHomeDir + "/store");

        try {
            FileUtils.cleanDirectory(directory);
            System.out.println("Directory contents cleared using Apache Commons IO." + directory.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.setProperty("rocketmq.home.dir", "rocketmq-all");
        new Thread(() -> {
            try {
                NamesrvStartup.main(new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(3000);
        new Thread(() -> {
            try {
                BrokerStartup.main(new String[]{
                        "-n", "localhost:9876"
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        System.out.println("RocketMQ Broker + NameServer started from Java");
    }

}
package com.example.app;

import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.rocketmq.namesrv.NamesrvStartup;
import org.apache.rocketmq.broker.BrokerStartup;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;

@SpringBootApplication
public class AppInfraBootstrap {

    @Autowired
    JdbcTemplate jdbcTemplate;

    public static void main(String[] args) throws Exception {
        dbserver();
        broker();
        SpringApplication.run(AppInfraBootstrap.class, args);
    }

    public static void dbserver() {
        try {
            NetworkServerControl server = new NetworkServerControl();
            System.out.println("Starting Derby Network Server...");
            server.start(null);
            System.out.println("Derby Network Server started.");
        } catch (Exception e) {
            System.err.println("Failed to start the Derby Network Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void broker() throws Exception {

        String userHomeDir = System.getProperty("user.home");
        System.out.println("User Home Directory: " + userHomeDir);
        File directory = new File(userHomeDir+"/store");
        try {
            FileUtils.cleanDirectory(directory);
            System.out.println("Directory contents cleared using Apache Commons IO."+ directory.getPath());
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


    public void init() {
        // This code runs after dependency injection is complete
        System.out.println("MyApplication initialized. Performing startup tasks...");
        // Useful for:
        // *   Establishing connections
        // *   Pre-loading cache data
        // *   Logging initialization messages
    }

    @PostConstruct
    private void executeDDL(){
        //System.out.println("M-------------------------------------------------s...");

        String sql = "drop table mock_event_details";
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ex) {

        }
        sql = "drop table api_event_details";
        try {
            jdbcTemplate.execute(sql);
        } catch (DataAccessException ex) {

        }
        sql = """
                CREATE TABLE  API_EVENT_DETAILS
                (
                    EVENT_ID    INT UNIQUE NOT NULL,
                    EVENT_STATUS BOOLEAN DEFAULT FALSE ,
                    EVENT_SCORE  VARCHAR(255),
                    CREATED_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UPDATED_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        jdbcTemplate.execute(sql);
        sql = """
                CREATE TABLE  MOCK_EVENT_DETAILS
                (
                    EVENT_ID   INT NOT NULL GENERATED ALWAYS AS IDENTITY,
                    EVENT_STATUS BOOLEAN DEFAULT FALSE,
                    EVENT_SCORE  VARCHAR(255),
                    CREATED_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UPDATED_TIME TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        jdbcTemplate.execute(sql);
    }

}

package com.example.app;

import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.rocketmq.broker.BrokerStartup;
import org.apache.rocketmq.namesrv.NamesrvStartup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MockServer {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MockServer.class, args);
    }

}

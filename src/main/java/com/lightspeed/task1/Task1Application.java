package com.lightspeed.task1;

import com.lightspeed.task1.redis.RedisMap;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import redis.clients.jedis.HostAndPort;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
@Slf4j
public class Task1Application {

    public static final String HOST = "127.0.0.1";

    public static void main(String[] args) {
        SpringApplication.run(Task1Application.class, args);
    }

    @PostConstruct
    public void init() {
// This is a test.
// Please run the local Redis cluster
// (refer to the README.md for the steps you need to follow to have the Redis cluster running locally).
// You can run tests also to check the RedisMap

        Set<HostAndPort> clusterNodes = new HashSet<>();
        clusterNodes.add(new HostAndPort(HOST, 7100));
        clusterNodes.add(new HostAndPort(HOST, 7101));
        clusterNodes.add(new HostAndPort(HOST, 7102));
        clusterNodes.add(new HostAndPort(HOST, 7103));
        clusterNodes.add(new HostAndPort(HOST, 7104));
        clusterNodes.add(new HostAndPort(HOST, 7105));

        RedisMap redisMap = new RedisMap(clusterNodes);
        redisMap.clear();
        redisMap.put("key1", "value1");
        redisMap.put("key2", "value2");
        log.info("Size of map after adding 2 items: [{}]", redisMap.size());
        redisMap.clear();
        log.info("Size of map after removing 2 items: [{}]", redisMap.size());
    }

}

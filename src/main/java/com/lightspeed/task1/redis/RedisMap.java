package com.lightspeed.task1.redis;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
public class RedisMap implements Map<String, String> {

    private final JedisCluster jedisCluster;
    private static final String MAP_KEY = "redis_map";

    public RedisMap(Set<HostAndPort> redisNodes) {
        this.jedisCluster = new JedisCluster(redisNodes);
    }

    @Override
    public int size() {
        try {
            var size = jedisCluster.hlen(MAP_KEY);
            return (int) size;
        } catch (Exception e) {
            log.error("Error in size(): {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof String) {
            try {
                return jedisCluster.hexists(MAP_KEY, (String) key);
            } catch (Exception e) {
                log.error("Error in containsKey(): {}", e.getMessage(), e);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if (!(value instanceof String targetValue)) {
            return false;
        }
        try {
            Collection<String> values = values();
            for (String val : values) {
                if (val.equals(targetValue)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Error in containsValue(): {}", e.getMessage(), e);
        }
        return false;
    }

    @Override
    public String get(Object key) {
        if (key instanceof String) {
            try {
                return jedisCluster.hget(MAP_KEY, (String) key);
            } catch (Exception e) {
                log.error("Error in get(): {}", e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public String put(String key, String value) {
        try {
            var previousValue = jedisCluster.hget(MAP_KEY, key);
            jedisCluster.hset(MAP_KEY, key, value);
            return previousValue;
        } catch (Exception e) {
            log.error("Error in put(): {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String remove(Object key) {
        if (key instanceof String) {
            try {
                var previousValue = jedisCluster.hget(MAP_KEY, (String) key);
                jedisCluster.hdel(MAP_KEY, (String) key);
                return previousValue;
            } catch (Exception e) {
                log.error("Error in remove(): {}", e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        if (m == null || m.isEmpty()) {
            return;
        }
        try {
            for (Map.Entry<? extends String, ? extends String> entry : m.entrySet()) {
                jedisCluster.hset(MAP_KEY, entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            log.error("Error in putAll(): {}", e.getMessage(), e);
        }
    }

    @Override
    public void clear() {
        try {
            jedisCluster.del(MAP_KEY);
        } catch (Exception e) {
            log.error("Error in clear(): {}", e.getMessage(), e);
        }
    }

    @Override
    public Set<String> keySet() {
        try {
            return jedisCluster.hkeys(MAP_KEY);
        } catch (Exception e) {
            log.error("Error in keySet(): {}", e.getMessage(), e);
            return new HashSet<>();
        }
    }

    @Override
    public Collection<String> values() {
        try {
            return jedisCluster.hvals(MAP_KEY);
        } catch (Exception e) {
            log.error("Error in values(): {}", e.getMessage(), e);
            return new HashSet<>();
        }
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        Set<Entry<String, String>> entries = new HashSet<>();
        try {
            Map<String, String> map = jedisCluster.hgetAll(MAP_KEY);
            for (Entry<String, String> entry : map.entrySet()) {
                entries.add(new AbstractMap.SimpleEntry<>(entry));
            }
        } catch (Exception e) {
            log.error("Error in entrySet(): {}", e.getMessage(), e);
        }
        return entries;
    }
}


package com.lightspeed.task1.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.ConnectionPool;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.json.JsonObjectMapper;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class RedisMap implements Map<String, String> {
    private final JedisCluster jedisCluster;
    private final JsonObjectMapper objectMapper;
    private static final String KEY_PREFIX = "map:";


    @Override
    public int size() {
        try {
            int totalSize;
            Set<String> keys = keySet();
            totalSize = keys.size();
            return totalSize;
        } catch (Exception e) {
            log.error("Error in size(): {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public boolean isEmpty() {
        try {
            return keySet().isEmpty();
        } catch (Exception e) {
            log.error("Error in isEmpty(): {}", e.getMessage(), e);
            return true;
        }
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof String) {
            try {
                return jedisCluster.exists(getRedisKey((String) key));
            } catch (Exception e) {
                log.error("Error in containsKey(): {}", e.getMessage(), e);
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        try {
            for (String key : keySet()) {
                Object val = get(key);
                if (val != null && val.equals(value)) {
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
                return jedisCluster.get(getRedisKey((String) key));
            } catch (Exception e) {
                log.error("Error in get(): {}", e.getMessage(), e);
            }
        }
        return null;
    }


    @Override
    public String put(String key, String value) {
        try {
            String redisKey = getRedisKey(key);
            String previousValue = jedisCluster.get(redisKey);
            jedisCluster.set(redisKey, value);
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
                String redisKey = getRedisKey((String) key);
                String previousValue = jedisCluster.get(redisKey);
                jedisCluster.del(redisKey);
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
        for (Entry<? extends String, ? extends String> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        try {
            Set<String> keys = keySet();
            for (String key : keys) {
                jedisCluster.del(getRedisKey(key));
            }
        } catch (Exception e) {
            log.error("Error in clear(): {}", e.getMessage(), e);
        }
    }

    //Since JedisCluster doesn't support keys or scan across the cluster directly,
    // we need to implement that operation across the nodes manually

    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>();
        try {
            Map<String, ConnectionPool> clusterNodes = jedisCluster.getClusterNodes();
            for (String nodeKey : clusterNodes.keySet()) {
                String[] hostPort = nodeKey.split(":");
                String host = hostPort[0];
                int port = Integer.parseInt(hostPort[1]);

                try (Jedis jedis = new Jedis(host, port)) {
                    String cursor = ScanParams.SCAN_POINTER_START;
                    ScanParams scanParams = new ScanParams().match(KEY_PREFIX + "*").count(1000);

                    do {
                        ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                        List<String> matchedKeys = scanResult.getResult();

                        for (String fullKey : matchedKeys) {
                            // Remove the prefix before adding to the set
                            String key = fullKey.substring(KEY_PREFIX.length());
                            keys.add(key);
                        }

                        cursor = scanResult.getCursor();
                    } while (!cursor.equals(ScanParams.SCAN_POINTER_START));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return keys;
    }


    @Override
    public Collection<String> values() {
        Set<String> values = new HashSet<>();
        try {
            for (String key : keySet()) {
                String value = get(key);
                if (value != null) {
                    values.add(value);
                }
            }
        } catch (Exception e) {
            log.error("Error in values(): {}", e.getMessage(), e);
        }
        return values;
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        Set<Entry<String, String>> entries = new HashSet<>();
        try {
            for (String key : keySet()) {
                String value = get(key);
                if (value != null) {
                    entries.add(new AbstractMap.SimpleEntry<>(key, value));
                }
            }
        } catch (Exception e) {
            log.error("Error in entrySet(): {}", e.getMessage(), e);
        }
        return entries;
    }

    private String getRedisKey(String key) {
        return KEY_PREFIX + key;
    }
}

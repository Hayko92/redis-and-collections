package com.lightspeed.task1.redis;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.ConnectionPool;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class RedisMap implements Map<String, Object> {
    private final JedisCluster jedisCluster;
    private static final String KEY_PREFIX = "map:";

    public RedisMap(Set<HostAndPort> redisNodes) {
        this.jedisCluster = new JedisCluster(redisNodes);
    }

    @Override
    public int size() {
        int totalSize = 0;
        try {
            Map<String, ConnectionPool> clusterNodes = jedisCluster.getClusterNodes();
            for (String nodeKey : clusterNodes.keySet()) {
                String[] hostPort = nodeKey.split(":");
                String host = hostPort[0];
                int port = Integer.parseInt(hostPort[1]);

                try (Jedis jedis = new Jedis(host, port)) {
                    long dbSize = jedis.dbSize();
                    totalSize += (int) dbSize;
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return totalSize;
    }


    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof String) {
            return jedisCluster.exists((String) key);
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            return false;
        }
        String targetValue = value.toString();

        try {
            Map<String, ConnectionPool> clusterNodes = jedisCluster.getClusterNodes();
            for (String nodeInfo : clusterNodes.keySet()) {
                String[] hostPort = nodeInfo.split(":");
                String host = hostPort[0];
                int port = Integer.parseInt(hostPort[1]);

                try (Jedis jedis = new Jedis(host, port)) {
                    String cursor = ScanParams.SCAN_POINTER_START;
                    ScanParams scanParams = new ScanParams().count(1000);

                    do {
                        ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                        List<String> keys = scanResult.getResult();

                        if (!keys.isEmpty()) {
                            Pipeline pipeline = jedis.pipelined();
                            for (String key : keys) {
                                pipeline.get(key);
                            }
                            List<Object> values = pipeline.syncAndReturnAll();

                            for (Object val : values) {
                                if (val != null && val.toString().equals(targetValue)) {
                                    return true;
                                }
                            }
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
        return false;
    }

    @Override
    public Object get(Object key) {
        if (key instanceof String) {
            return jedisCluster.get(KEY_PREFIX + key);
        }
        return null;
    }

    @Override
    public Object put(String key, Object value) {
        jedisCluster.set(KEY_PREFIX + key, value.toString());
        return value;
    }

    @Override
    public Integer remove(Object key) {
        if (key instanceof String) {
            jedisCluster.del(KEY_PREFIX + key);
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        for (Entry<? extends String, ?> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
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
                        List<String> keys = scanResult.getResult();

                        if (!keys.isEmpty()) {
                            Pipeline pipeline = jedis.pipelined();
                            for (String key : keys) {
                                pipeline.del(key);
                            }
                            pipeline.sync();
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
    }

    @Override
    public Set<String> keySet() {
        return Set.of();
    }

    @Override
    public Collection<Object> values() {
        return List.of();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Set.of();
    }

}

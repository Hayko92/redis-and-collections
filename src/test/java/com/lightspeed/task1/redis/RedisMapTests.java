package com.lightspeed.task1.redis;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.HostAndPort;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RedisMapTests {
    private static RedisMap redisMap;

    @BeforeAll
    public static void setUp() {
        Set<HostAndPort> clusterNodes = new HashSet<>();
        clusterNodes.add(new HostAndPort("127.0.0.1", 7100));
        clusterNodes.add(new HostAndPort("127.0.0.1", 7101));
        clusterNodes.add(new HostAndPort("127.0.0.1", 7102));
        clusterNodes.add(new HostAndPort("127.0.0.1", 7103));
        clusterNodes.add(new HostAndPort("127.0.0.1", 7104));
        clusterNodes.add(new HostAndPort("127.0.0.1", 7105));
        redisMap = new RedisMap(clusterNodes);
    }

    @Test
    public void testPutAndGet() {
        redisMap.clear();

        redisMap.put("key1", "value1");
        redisMap.put("key2", "value2");

        assertEquals("value1", redisMap.get("key1"));
        assertEquals("value2", redisMap.get("key2"));
        assertNull(redisMap.get("key3"));
    }

    @Test
    public void testSizeAndIsEmpty() {
        redisMap.clear();

        assertTrue(redisMap.isEmpty());
        assertEquals(0, redisMap.size());

        redisMap.put("key1", "value1");
        redisMap.put("key2", "value2");

        assertFalse(redisMap.isEmpty());
        assertEquals(2, redisMap.size());
    }

    @Test
    public void testContainsKeyAndContainsValue() {
        redisMap.clear();

        redisMap.put("key1", "value1");
        redisMap.put("key2", "value2");

        assertTrue(redisMap.containsKey("key1"));
        assertFalse(redisMap.containsKey("key3"));

        assertTrue(redisMap.containsValue("value2"));
        assertFalse(redisMap.containsValue("value3"));
    }

    @Test
    public void testRemove() {
        redisMap.clear();

        redisMap.put("key1", "value1");
        redisMap.put("key2", "value2");

        String removedValue = redisMap.remove("key1");
        assertEquals("value1", removedValue);
        assertNull(redisMap.get("key1"));
        assertEquals(1, redisMap.size());
    }

    @Test
    public void testPutAll() {
        redisMap.clear();

        Map<String, String> mapToPut = new HashMap<>();
        mapToPut.put("key1", "value1");
        mapToPut.put("key2", "value2");
        mapToPut.put("key3", "value3");

        redisMap.putAll(mapToPut);

        assertEquals(3, redisMap.size());
        assertEquals("value1", redisMap.get("key1"));
        assertEquals("value2", redisMap.get("key2"));
        assertEquals("value3", redisMap.get("key3"));
    }

    @Test
    public void testClear() {
        redisMap.clear();

        redisMap.put("key1", "value1");
        redisMap.put("key2", "value2");

        redisMap.clear();

        assertTrue(redisMap.isEmpty());
        assertNull(redisMap.get("key1"));
        assertNull(redisMap.get("key2"));
    }

    @Test
    public void testKeySetValuesEntrySet() {
        redisMap.clear();

        redisMap.put("key1", "value1");
        redisMap.put("key2", "value2");
        redisMap.put("key3", "value3");

        Set<String> expectedKeys = Set.of("key1", "key2", "key3");
        assertEquals(expectedKeys, redisMap.keySet());

        Set<String> expectedValues = Set.of("value1", "value2", "value3");
        assertEquals(expectedValues, new HashSet<>(redisMap.values()));

        Set<Map.Entry<String, String>> expectedEntries = new HashSet<>();
        expectedEntries.add(Map.entry("key1", "value1"));
        expectedEntries.add(Map.entry("key2", "value2"));
        expectedEntries.add(Map.entry("key3", "value3"));

        assertEquals(expectedEntries, redisMap.entrySet());
    }
}

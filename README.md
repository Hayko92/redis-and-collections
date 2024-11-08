# Project Overview
This project implements a RedisMap class in Java, providing a Map<String, String> backed by Redis. The RedisMap supports standard map operations such as put, get, remove, size, etc., using Redis as the data store. The project includes integration tests to verify the functionality against a real Redis Cluster.

# Prerequisites
- Java Development Kit (JDK) 21 
- Maven for building the project
- Docker for running the Redis Cluster
- Git (optional, for cloning the repository)

# Set Up the Local Redis Cluster
Important: You need to have a local Redis Cluster running for both the tests and the application to function correctly. Please follow the steps below to set up the Redis Cluster locally.

a. Start Redis Cluster Containers
Run the following command to start the Redis Cluster containers:

```docker 
docker-compose up -d
```

# Project Details
RedisMap Class
- Location: src/main/java/com/lightspeed/task1/redis/RedisMap.java
- Description: Implements Map<String, String> using Redis as the backend store. Supports operations like put, get, remove, size, etc.

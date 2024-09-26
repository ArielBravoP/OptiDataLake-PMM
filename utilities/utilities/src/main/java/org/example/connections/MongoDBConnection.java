package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {
    private static final String URI = "mongodb://localhost:27017";

    public static MongoDatabase getConnection(String databaseName) {
        MongoClient mongoClient = MongoClients.create(URI);
        return mongoClient.getDatabase(databaseName);
    }
}

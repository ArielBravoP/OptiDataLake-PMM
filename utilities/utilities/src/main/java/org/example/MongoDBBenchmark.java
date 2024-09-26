package org.example;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MongoDBBenchmark {
    public static void main(String[] args) {
        // Conexión a la base de datos MongoDB
        String uri = "mongodb://localhost:27017";
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("benchmarkDB");
            MongoCollection<Document> collection = database.getCollection("benchmarkCollection");

            // Realizar benchmarks
            benchmarkInsert(collection);
            benchmarkRead(collection);
            benchmarkUpdate(collection);
            benchmarkDelete(collection);
        }
    }

    private static void benchmarkInsert(MongoCollection<Document> collection) {
        int numberOfDocuments = 10000;
        long startTime = System.nanoTime();

        for (int i = 0; i < numberOfDocuments; i++) {
            Document document = new Document("key", i)
                    .append("value", "Benchmark test " + i);
            collection.insertOne(document);
        }

        long endTime = System.nanoTime();
        System.out.println("Inserciones de " + numberOfDocuments + " documentos en " + (endTime - startTime) / 1_000_000 + " ms");
    }

    private static void benchmarkRead(MongoCollection<Document> collection) {
        long startTime = System.nanoTime();

        for (int i = 0; i < 10000; i++) {
            collection.find(new Document("key", i)).first();
        }

        long endTime = System.nanoTime();
        System.out.println("Lectura de 10000 documentos en " + (endTime - startTime) / 1_000_000 + " ms");
    }

    private static void benchmarkUpdate(MongoCollection<Document> collection) {
        long startTime = System.nanoTime();

        for (int i = 0; i < 10000; i++) {
            collection.updateOne(new Document("key", i),
                    new Document("$set", new Document("value", "Updated Value " + i)));
        }

        long endTime = System.nanoTime();
        System.out.println("Actualización de 10000 documentos en " + (endTime - startTime) / 1_000_000 + " ms");
    }

    private static void benchmarkDelete(MongoCollection<Document> collection) {
        long startTime = System.nanoTime();

        for (int i = 0; i < 10000; i++) {
            collection.deleteOne(new Document("key", i));
        }

        long endTime = System.nanoTime();
        System.out.println("Eliminación de 10000 documentos en " + (endTime - startTime) / 1_000_000 + " ms");
    }
}

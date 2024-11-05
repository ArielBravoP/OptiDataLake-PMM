package com.odl;

import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SparkKafkaToMongoApplication {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.data.mongodb.host}")
    private String mongoHost;

    @Value("${spring.data.mongodb.port}")
    private String mongoPort;

    @Value("${spring.data.mongodb.database}")
    private String mongoDatabase;

    public static void main(String[] args) {
        SpringApplication.run(SparkKafkaToMongoApplication.class, args);
    }

    @Bean
    public SparkSession sparkSession() {
        return SparkSession.builder()
                .appName("SparkKafkaToMongo")
                .master("local[*]")
                .config("spark.mongodb.input.uri", "mongodb://" + mongoHost + ":" + mongoPort + "/" + mongoDatabase)
                .config("spark.mongodb.output.uri", "mongodb://" + mongoHost + ":" + mongoPort + "/" + mongoDatabase)
                .getOrCreate();
    }
}

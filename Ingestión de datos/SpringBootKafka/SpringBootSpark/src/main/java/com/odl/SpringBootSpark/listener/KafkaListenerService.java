package com.odl.SpringBootSpark.listener;

import com.odl.SpringBootSpark.processor.SparkKafkaProcessor;
import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.apache.spark.sql.functions;


import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import static org.apache.parquet.example.Paper.schema;

@Service
public class KafkaListenerService {

    private final SparkSession sparkSession;
    private final SparkKafkaProcessor sparkKafkaProcessor;

    @Autowired
    public KafkaListenerService(SparkSession sparkSession, SparkKafkaProcessor sparkKafkaProcessor) {
        this.sparkSession = sparkSession;
        this.sparkKafkaProcessor = sparkKafkaProcessor;
    }

    StructType schema = new StructType()
            .add("e", DataTypes.StringType)
            .add("E", DataTypes.LongType)
            .add("s", DataTypes.StringType)
            .add("t", DataTypes.LongType)
            .add("p", DataTypes.StringType)
            .add("q", DataTypes.StringType)
            .add("T", DataTypes.LongType)
            .add("m", DataTypes.BooleanType)
            .add("M", DataTypes.BooleanType);

    @KafkaListener(topics = "odl-ticks", groupId = "spark-group-id")
    public void listen(String message) throws TimeoutException, StreamingQueryException {
        Dataset<Row> streamingDataFrame = sparkSession
                .readStream()
                .format("kafka")
                .option("kafka.bootstrap.servers", "localhost:9092")
                .option("subscribe", "odl-ticks")
                .option("startingOffsets", "latest")
                .load()
                .selectExpr("CAST(value AS STRING) as json_message")
                .select(functions.from_json(functions.col("json_message"), schema).as("data"))
                .select("data.*");

        streamingDataFrame = streamingDataFrame.withColumn("timestamp", functions.current_timestamp());

        // Enviamos los datos a procesar
        sparkKafkaProcessor.processAndSaveToMongo(streamingDataFrame);
    }
}
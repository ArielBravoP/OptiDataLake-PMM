package com.odl;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataController {

    private final SparkSession spark;

    public DataController() {
        this.spark = SparkSession.builder()
                .appName("Spring Spark MongoDB")
                .master("local")
                .getOrCreate();
    }

    @GetMapping("/getData")
    public String getData() {
        Dataset<Row> df = spark.read()
                .format("mongodb")
                .option("spark.mongodb.read.connection.uri", "mongodb://localhost:27017/OptiDataLake.Raw_data")
                .load();

        df.show();
        return df.toJSON().collectAsList().toString();
    }
}

package com.odl;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

public class SparkMongoDemo {
    public static void main(String[] args) {
        // Crear la sesión de Spark
        SparkSession spark = SparkSession.builder()
                .appName("MongoDB to Spark")
                .master("local")
                .config("spark.mongodb.read.connection.uri", "mongodb://localhost:27017/OptiDataLake.Raw_data")
                .getOrCreate();

        // Leer la colección desde MongoDB
        Dataset<Row> rawData = spark.read()
                .format("mongodb")
                .option("database", "OptiDataLake")
                .option("collection", "Raw_data")
                .load();

        // Mostrar el esquema de los datos
        rawData.printSchema();
        
        // Mostrar los primeros 20 registros
        rawData.show(false);

        // Detener la sesión de Spark
        spark.stop();
    }
}

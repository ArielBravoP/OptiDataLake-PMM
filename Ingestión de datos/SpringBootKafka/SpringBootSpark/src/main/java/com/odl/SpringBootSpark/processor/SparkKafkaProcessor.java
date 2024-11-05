package com.odl.SpringBootSpark.processor;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.apache.spark.sql.streaming.StreamingQuery;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.concurrent.TimeoutException;

@Component
public class SparkKafkaProcessor {

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${mongodb.collection}")
    private String collection;

    public void processAndSaveToMongo(Dataset<Row> dataFrame) throws TimeoutException, StreamingQueryException {
        // Convertir columnas a tipo numérico
        Dataset<Row> numericData = dataFrame.withColumn("q", dataFrame.col("q").cast("double"))
                .withColumn("p", dataFrame.col("p").cast("double"));

        // Definir una ventana de 1 minuto para el volumen total, promedio de precios y movimiento de precios con watermark
        Dataset<Row> windowedData = numericData
                .withWatermark("timestamp", "1 minutes")
                .groupBy(functions.window(numericData.col("timestamp"), "1 minute"), numericData.col("s"))
                .agg(
                        functions.sum("q").alias("volumen_total"),
                        functions.avg("p").alias("promedio_precios"),
                        functions.stddev("p").alias("volatilidad"),
                        functions.first("p").alias("precio_apertura"),
                        functions.last("p").alias("precio_cierre")
                )
                .withColumn("movimiento_precios", functions.col("precio_cierre").minus(functions.col("precio_apertura")));

        // Configurar el esquema esperado
        Dataset<Row> resultData = windowedData
                .withColumn("cryptocurrency", windowedData.col("s"))
                .withColumn("time_window", windowedData.col("window.start"))
                .drop("window");

        // Guardar en MongoDB usando foreachBatch para manejar cada micro-lote
        StreamingQuery query = resultData.writeStream()
                .foreachBatch((batchDataFrame, batchId) -> {
                    batchDataFrame.show();

                    // Conectar a MongoDB y guardar los datos del micro-lote
                    batchDataFrame.javaRDD().foreachPartition(rows -> {
                        try (var mongoClient = MongoClients.create("mongodb://localhost:27017")) {
                            MongoDatabase database = mongoClient.getDatabase("OptiDataLake");
                            MongoCollection<Document> collection = database.getCollection("Analytics");

                            rows.forEachRemaining(row -> {
                                Document doc = new Document("cryptocurrency", row.getAs("cryptocurrency"))
                                        .append("time_window", row.getAs("time_window"))
                                        .append("volumen_total", row.getAs("volumen_total"))
                                        .append("promedio_precios", row.getAs("promedio_precios"))
                                        .append("volatilidad", row.getAs("volatilidad"))
                                        .append("movimiento_precios", row.getAs("movimiento_precios"));
                                collection.insertOne(doc);
                            });
                        }
                    });
                })
                .outputMode("update")
                .option("checkpointLocation", "spark-checkpoints")
                .trigger(org.apache.spark.sql.streaming.Trigger.ProcessingTime("1 minute"))
                .start();

        query.awaitTermination();
        query.stop();
    }
}
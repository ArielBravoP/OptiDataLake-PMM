package com.odl.processor;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
public class SparkKafkaToMongoService implements Serializable {

    private final SparkSession sparkSession;
    private final Map<String, String> productosMap = new HashMap<>();

    public SparkKafkaToMongoService(SparkSession sparkSession) {
        this.sparkSession = sparkSession;
        // Inicializar el mapa con los pares de símbolos y sus nombres
        productosMap.put("BTCUSDT", "Bitcoin");
        productosMap.put("ETHUSDT", "Ethereum");
        productosMap.put("XRPUSDT", "Ripple");
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
    public void listen() throws TimeoutException, StreamingQueryException {
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

        // Guardar en MongoDB usando foreachBatch para manejar cada micro-lote
        StreamingQuery query = streamingDataFrame.writeStream()
                .foreachBatch((batchDataFrame, batchId) -> {
                    batchDataFrame.cache();
                    batchDataFrame.show();

                    // Agregar lógica para contar registros cada minuto
                    long currentMinute = Instant.now().getEpochSecond() / 60;
                    recordCountPerMinute += batchDataFrame.count();
                    if (lastLoggedMinute == -1) {
                        lastLoggedMinute = currentMinute;
                    } else if (currentMinute > lastLoggedMinute) {
                        logDataInsertion(recordCountPerMinute, lastLoggedMinute * 60);
                        lastLoggedMinute = currentMinute;
                        recordCountPerMinute = 0;
                    }

                    // Conectar a MongoDB y guardar los datos del micro-lote
                    batchDataFrame.javaRDD().foreachPartition(partition -> {
                        if (!partition.hasNext()) return; // Verificar si hay datos antes de conectar a MongoDB
                        try (var mongoClient = MongoClients.create("mongodb://localhost:27017")) {
                            MongoDatabase database = mongoClient.getDatabase("OptiDataLake");
                            MongoCollection<Document> productCollection = database.getCollection("productos_financieros");
                            MongoCollection<Document> marketCollection = database.getCollection("mercados_financieros");

                            List<Document> productDocs = new ArrayList<>();
                            List<Document> marketDocs = new ArrayList<>();

                            partition.forEachRemaining(row -> {
                                long insertionTime = System.currentTimeMillis();
                                // Crear y agregar documentos para las colecciones adicionales a las listas
                                String symbol = row.getAs("s");
                                String nombreProducto = getNombreProducto(symbol);

                                Document productDoc = new Document("producto_id", symbol)
                                        .append("nombre_producto", nombreProducto)
                                        .append("timestamp", insertionTime)
                                        .append("nombre_producto", nombreProducto);
                                productDocs.add(productDoc);

                                String mercadoId = symbol;
                                String nombreMercado = nombreProducto + " Exchange";
                                Document marketDoc = new Document("mercado_id", mercadoId)
                                        .append("nombre_mercado", nombreMercado)
                                        .append("tipo_mercado", "Spot")
                                        .append("timestamp", insertionTime)
                                        .append("nombre_mercado", nombreMercado)
                                        .append("tipo_mercado", "Spot");
                                marketDocs.add(marketDoc);
                            });

                            // Insertar todos los documentos de una sola vez para mejorar la eficiencia
                            if (!productDocs.isEmpty()) {
                                productCollection.insertMany(productDocs);
                            }
                            if (!marketDocs.isEmpty()) {
                                marketCollection.insertMany(marketDocs);
                            }
                        }
                    });
                    batchDataFrame.unpersist(); // Liberar memoria después del procesamiento
                })
                .outputMode("append")
                .option("checkpointLocation", "/home/optidatalake/Escritorio/Git_optidatalake/OptiDataLake-PMM/Ingestión de datos/SpringBootKafka/SparkKafkaToMongo/src/main/java/com/odl/checkpointSk") // Cambiar la ruta del checkpoint
                .trigger(Trigger.ProcessingTime("1 seconds")) // Procesamiento continuo
                .start();

        try {
            Thread.sleep(60000); // Esperar 1 minuto antes de detener el proceso
            for (int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(60000); // Esperar 1 minuto antes de registrar el siguiente conteo
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            query.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getNombreProducto(String symbol) {
        return productosMap.getOrDefault(symbol, "Producto Desconocido");
    }

    private long recordCountPerMinute = 0;
    private long lastLoggedMinute = -1;

    private void logDataInsertion(long recordCount, long timestamp) {
        try (FileWriter fileWriter = new FileWriter("data_ingestion_log.txt", true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.printf("Timestamp: %d, Records Inserted: %d%n", timestamp, recordCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

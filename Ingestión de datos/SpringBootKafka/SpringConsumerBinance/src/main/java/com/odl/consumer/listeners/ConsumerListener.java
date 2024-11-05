package com.odl.consumer.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odl.consumer.model.TradeData;
import com.odl.consumer.repository.TradeDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;

@Configuration
public class ConsumerListener {

    private Logger LOGGER = LoggerFactory.getLogger(ConsumerListener.class);

    @Autowired
    private TradeDataRepository tradeDataRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private long recordCountPerMinute = 0;
    private long lastLoggedMinute = -1;

    public ConsumerListener() {
        // Hilo para registrar el conteo de datos cada minuto
        new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                try {
                    Thread.sleep(60000); // Esperar 1 minuto
                    logDataInsertion(recordCountPerMinute, Instant.now().getEpochSecond());
                    recordCountPerMinute = 0; // Reiniciar el conteo
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @KafkaListener(topics = {"odl-ticks"}, groupId = "my-group-id")
    public void listener(String message) {
        try {
            // Parsear el mensaje JSON a la clase TradeData
            TradeData tradeData = objectMapper.readValue(message, TradeData.class);

            // Guardar en MongoDB
            tradeDataRepository.save(tradeData);

            // Incrementar el contador por cada mensaje procesado
            recordCountPerMinute += 1;

            LOGGER.info("Datos guardados en MongoDB: " + tradeData);
        } catch (Exception e) {
            LOGGER.error("Error al procesar el mensaje: ", e);
        }
    }

    private void logDataInsertion(long recordCount, long timestamp) {
        try (FileWriter fileWriter = new FileWriter("raw_data_ingestion_log.txt", true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.printf("Timestamp: %d, Records Inserted: %d%n", timestamp, recordCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

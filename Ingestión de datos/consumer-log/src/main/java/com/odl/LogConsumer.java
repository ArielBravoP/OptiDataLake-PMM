package com.odl;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class LogConsumer {

    private static final Logger log = LogManager.getLogger(LogConsumer.class);

    public static void main(String[] args) {
        // Cargar configuración desde consumer.properties
        Properties props = new Properties();
        try {
            props.load(new FileReader("src/main/resources/consumer.properties"));
        } catch (IOException e) {
            log.error("Error al cargar el archivo de propiedades: " + e.getMessage());
            return;
        }

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("odl"));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                // Procesa cada línea del archivo .log enviada por el productor
                System.out.println("Línea de log recibida: " + record.value());
            }
        }
    }
}

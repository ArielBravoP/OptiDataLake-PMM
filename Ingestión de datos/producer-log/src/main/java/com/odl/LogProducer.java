package com.odl;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class LogProducer {

    private static final Logger log = LogManager.getLogger(LogProducer.class);

    public static void main(String[] args) {
        Properties props = new Properties();

        try {
            // Carga el archivo producer.properties
            props.load(new FileReader("src/main/resources/producer.properties"));
        } catch (IOException e) {
            log.error("Error al cargar el archivo de propiedades: " + e.getMessage());
            return;
        }

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        String logFilePath = "messages.log";

        try (BufferedReader br = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Envía cada línea como un mensaje a Kafka
                producer.send(new ProducerRecord<>("odl", line));
                System.out.println("Mensaje enviado: " + line);

                // Pausa de 1 segundo entre envíos
                Thread.sleep(1000);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }
}

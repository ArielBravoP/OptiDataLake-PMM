package com.odl;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public class Producer {
    
    private static Producer producer;
    private KafkaProducer<String, String> kafkaProducer;

    private Producer() {
        try {
            var conf = new Properties();
            conf.load(new FileReader("src/main/resources/producer.properties"));
            
            this.kafkaProducer = new KafkaProducer<>(conf);
            
        } catch (IOException ioe) {
            log.error(ioe.getMessage());
        }
    }

    public void send(String key, String value) { // Para mandar mensajes
        try {
            var record = new ProducerRecord<String, String>(TOPIC, PARTITION, key, value);
            this.kafkaProducer.send(record);
        } catch (KafkaException e) {
            log.error(e.getMessage());
            this.close();
        }
    }

    public void close() { //Cierra el producer
        this.kafkaProducer.close();
    }

    // Se implementa el patrón Singleton para inicializar el producer Kafka.
    // Si ya existe una instancia, la devuelve. Si no, crea una nueva instancia.
    public static Producer getInstance() {
        return (Objects.nonNull(producer)) ? producer : new Producer();
    }

    private static final String TOPIC = "odl"; // TOPIC creado anteriormente con docker-compose.yml
    private static final Integer PARTITION = 0;
    private static final Logger log = LogManager.getLogger(Producer.class);


}

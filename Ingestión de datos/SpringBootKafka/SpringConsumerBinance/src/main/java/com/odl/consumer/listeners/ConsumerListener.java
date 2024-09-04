package com.odl.consumer.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

@Configuration
public class ConsumerListener {

    private Logger LOGGER = LoggerFactory.getLogger(ConsumerListener.class);

    @KafkaListener(topics = {"odl-ticks"}, groupId = "my-group-id")
    public void listener(String message){
        LOGGER.info("Mensaje recibido: " + message);
    }
}
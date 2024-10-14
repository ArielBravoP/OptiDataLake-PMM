package com.odl.consumer.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odl.consumer.model.TradeData;
import com.odl.consumer.repository.TradeDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

@Configuration
public class ConsumerListener {

    private Logger LOGGER = LoggerFactory.getLogger(ConsumerListener.class);

    @Autowired
    private TradeDataRepository tradeDataRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = {"odl-ticks"}, groupId = "my-group-id")
    public void listener(String message) {
        //LOGGER.info("Mensaje recibido: " + message);

        try {
            // Parsear el mensaje JSON a la clase TradeData
            TradeData tradeData = objectMapper.readValue(message, TradeData.class);

            // Guardar en MongoDB
            tradeDataRepository.save(tradeData);

            LOGGER.info("Datos guardados en MongoDB: " + tradeData);
        } catch (Exception e) {
            LOGGER.error("Error al procesar el mensaje: ", e);
        }
    }
}
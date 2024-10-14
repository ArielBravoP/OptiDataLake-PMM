package com.odl.producer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import java.util.HashMap;
import java.util.Map;


@Configuration
public class ConfigTopic {

    @Bean
    public NewTopic generateTopic() {

        Map<String, String> configurations = new HashMap<>();
        configurations.put(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE); // delete y compact (Mantiene el mensaje mas actual)
        configurations.put(TopicConfig.RETENTION_MS_CONFIG, "0");
        configurations.put(TopicConfig.SEGMENT_BYTES_CONFIG, "1073741824"); // Tamaño máximo del segmento = 1GB
        //configurations.put(TopicConfig.MAX_MESSAGE_BYTES_CONFIG, "1000000"); // Tamaño max de cada mensaje

        NewTopic newTopic = TopicBuilder.name("odl")
                .partitions(2) // Particiones
                .replicas(1)
                .configs(configurations)
                .build();

        return newTopic;
    }
}
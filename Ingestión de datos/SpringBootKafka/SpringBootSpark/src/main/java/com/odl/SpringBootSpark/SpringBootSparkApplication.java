package com.odl.SpringBootSpark;

import jakarta.annotation.PostConstruct;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

@SpringBootApplication
public class SpringBootSparkApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootSparkApplication.class, args);
    }

    @Bean
    public SparkSession sparkSession() {
        return SparkSession.builder()
                .appName("SpringBootSpark")
                .master("local[*]")
                .config("spark.mongodb.input.uri", "mongodb://localhost:27017/OptiDataLake.Raw_data")
                .config("spark.mongodb.output.uri", "mongodb://localhost:27017/OptiDataLake.Analytics")
                .getOrCreate();
    }

    @Bean
    public JavaSparkContext javaSparkContext(SparkSession sparkSession) {
        return new JavaSparkContext(sparkSession.sparkContext());
    }

    //Resetear Consumer
    @PostConstruct
    public void resetOffsets() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sudo", "-S", "/home/optidatalake/Escritorio/Git_optidatalake/OptiDataLake-PMM/Ingestión de datos/SpringBootKafka/reset_consumer.sh"});
            OutputStream outputStream = process.getOutputStream();
            outputStream.write("data\n".getBytes());
            outputStream.flush();
            outputStream.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Error: " + line);
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Consumer reseteado exitosamente");
            } else {
                System.out.println("Error en el reseteo, código de salida: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
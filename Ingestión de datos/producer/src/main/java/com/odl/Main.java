package com.odl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {

        var kafkaProducer = Producer.getInstance();
        var scanner = new Scanner(System.in);

        do {
            var line = scanner.next();
            var isValidLine = validFormat(line); // Verificar el formato del mensaje

            if (isValidLine || line.equals("exit")) {
                if (line.equals("exit")) {
                    log.info("Cerrando...");
                    kafkaProducer.close();
                    break;
                }
                var keyAndMessage = line.split(":");
                kafkaProducer.send(keyAndMessage[0], keyAndMessage[1]);
            } else {
                log.error("El formato debe ser = string:string");
            }

        } while (true);
    }

    private static Boolean validFormat(String line) {
        var regex = Pattern.compile("\\w+:\\w+");
        var matcher = regex.matcher(line);
        return matcher.matches();
    }

    private static final Logger log = LogManager.getLogger(Main.class);

}
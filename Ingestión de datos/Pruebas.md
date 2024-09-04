## Pruebas de productor - consumidor con Apache Kafka

Programas instalados:
- Docker version 26.1.4<br>
- Apache Kafka 3.7.1<br>

### Primera prueba
Se crea el archivo de ["docker-compose.yml"](https://github.com/ArielBravoP/OptiDataLake-PMM/blob/main/Ingesti%C3%B3n%20de%20datos/docker-compose.yml) para realizar la conexión entre estos servicios, este documento va a contener información sobre el servidor local que se creará para hacer las pruebas.<br>

Luego se utiliza el siguiente comando en el terminal:<br>
"docker-compose up"<br>

Y se abren dos terminales CMD de windows, en el primero se pone lo siguiente:<br>
"docker ps" -> Nos mostrará los contenedores en funcionamiento, en este caso serían 2, el de Kafka y de Zookeper.<br>
"docker exec -it kafka-broker-1 bash" -> Ingresamos al contenedor kafka mediante bash.<br>
"kafka-topics --bootstrap-server kafka-broker-1:9092 --create --topic odl" -> Creamos un tópico que se llamará odl.<br>
"kafka-console-producer --bootstrap-server kafka-broker-1:9092 --topic odl" -> Ingresamos al tópico como producer para ingresar datos.<br>

Ahora en el segundo CMD:<br>
"docker exec -it kafka-broker-1 bash"<br>
"kafka-console-consumer --bootstrap-server kafka-broker-1:9092 --topic odl --from-beginning" -> Ingresamos como consumidor y podremos ver todo lo que uno escriba en la primer CMD.<br>

### Segunda prueba
Se realiza una prueba creando un producer y consumer en Apache Kafka, lo que se logró fue enviar mensajes correctamente en el formato "string:string" y que el consumidor lo vea correctamente.<br>
- Primero se ejecuta el código del producer ["producer/src/main/java/com/odl/Main.java"](https://github.com/ArielBravoP/OptiDataLake-PMM/blob/main/Ingesti%C3%B3n%20de%20datos/producer/src/main/java/com/odl/Main.java).<br>
- Segundo se abre una CMD y se ejecuta el comando: <br>
  - Se inicia el contenedor docker con "docker-compose up".<br>
- Luego se ejecuta el código del consumer ["consumer/src/main/java/com/odl/Main.java"](https://github.com/ArielBravoP/OptiDataLake-PMM/blob/main/Ingesti%C3%B3n%20de%20datos/consumer/src/main/java/com/odl/Main.java).<br>

Luego desde la ejecución del Main del producer, en su terminal, pondremos mensajes con el formato string:string, en donde el primer string será la key y no se mostrará en el consumidor, en cambio, el segundo string después de ":" aparecerá en la CMD del consumidor.

### Tercera prueba
Ahora se crearon los proyectos ["producer-log"](https://github.com/ArielBravoP/OptiDataLake-PMM/tree/main/Ingesti%C3%B3n%20de%20datos/producer-log), ["consumer-log"](https://github.com/ArielBravoP/OptiDataLake-PMM/tree/main/Ingesti%C3%B3n%20de%20datos/consumer-log) para que nuestro Apache Kafka lea el archivo .log y vaya enviando los mensajes con pausas predefinidas, en este caso se dejó una pausa de 1 segundo.
- Para realizar la prueba se utilizó el topic "odl" creado anteriormente y se levanta docker-compose.
- Luego se ejecuta el consumer-log y producer-log mediante intellij, teniendo en cuenta que debemos tener el archivo "message.log" en la carpeta raiz del producer.

Con eso tendremos al productor enviando las líneas del .log cada 1 segundo y lo podremos ver en el terminal de consumer-log

### Cuarta prueba
Se creó el proyecto ["SpringBootKafka"](https://github.com/ArielBravoP/OptiDataLake-PMM/tree/main/Ingesti%C3%B3n%20de%20datos/SpringBootKafka) con la finalidad de utilizar SpringBoot y realizar la conexión con Binance. En esta ocasión se logra extraer la información de BTC en intervalos de tiempo, en donde el producer va a estar pidiendo información del precio de la criptomoneda cada 1 segundo por ejemplo, y lo envía para que le llegue al consumer.<br>
Link de referencia: [Ejemplo Apache Kafka con SpringBoot](https://github.com/UnProgramadorNaceOfficial/spring-apache-kafka)

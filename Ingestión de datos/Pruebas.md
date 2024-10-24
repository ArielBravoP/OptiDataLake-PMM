## Pruebas de funcionamiento con Apache Kafka y Apache Spark

Programas y dependencias relevantes:
- Docker version 26.1.4
- Spring 3.3.4
- Apache Kafka 3.8.0
- Apache Spark 4.0.0-preview2
- Scala 2.13.15

### Quinta prueba

**[V1]** Se crea el proyecto [Spark-Demo](https://github.com/ArielBravoP/OptiDataLake-PMM/tree/main/Ingesti%C3%B3n%20de%20datos/Spark-Demo) para testear el funcionamiento de Spring y Spark junto a MongoDB. Se logra extraer información desde una colección de MongoDB y aparte se logra leer un archivo json.

**[V2]** Dentro del proyecto "SpringBootKafka" se crea el proyecto [SpringBootSpark](https://github.com/ArielBravoP/OptiDataLake-PMM/tree/main/Ingesti%C3%B3n%20de%20datos/SpringBootKafka/SpringBootSpark), aquí se implementa el uso de Spark al proyecto en general. La información que estaremos procesando será la que nos proporcione el producer de Binance que creamos antes, entonces, tendremos un consumer de Kafka en este proyecto, después esos datos son pasados a Spark para realizar el cálculo de métricas con una ventana de tiempo de 1 minuto, y son guardados en la colección "Analytics" en MongoDB.

**[V2.1]** Se crea el archivo [Dashboard.ipynb](https://github.com/ArielBravoP/OptiDataLake-PMM/blob/main/Ingesti%C3%B3n%20de%20datos/Dashboard.ipynb) con la finalidad de desplegar gráficos interactivos y ejemplificar un caso de uso.

### Cuarta prueba
**[V1]** Se creó el proyecto ["SpringBootKafka"](https://github.com/ArielBravoP/OptiDataLake-PMM/tree/main/Ingesti%C3%B3n%20de%20datos/SpringBootKafka) con la finalidad de utilizar SpringBoot y realizar la conexión con Binance. En esta ocasión se logra extraer la información de BTC en intervalos de tiempo, en donde el producer va a estar pidiendo información del precio de la criptomoneda cada 1 segundo por ejemplo, y lo envía para que le llegue al consumer.<br>
Link de referencia: [Ejemplo Apache Kafka con SpringBoot](https://github.com/UnProgramadorNaceOfficial/spring-apache-kafka)<br>

**[V2]** Se crea un nuevo topic llamado "odl-ticks" y se añaden archivos de configuración para que el producer envíe información mediante ticks, es decir, que con cada compra/venta de BTC/USD va a estar enviando un mensaje a ese topic y en paralelo se envían los mensajes cada 1 segundo al topic "odl".

**[V3]** Se realizan las conexiones con MongoDB.

**[V4]** Ahora se envía información de 3 criptomonedas a MongoDB: BTC/USDT, ETH/USDT, XRP/USDT. Se ha tenido problemas con el WebSocket de Binance debido al límite que tiene de 5 mensajes por segundo, pero se realizó una conexión por separado de cada moneda para intentar evitar que nos baje la conexión con Binance, y se realiza una reconexión pasados los 5 segundos en caso de algún problema.

**[V5]** Se crea el archivo [MongoQuery](https://github.com/ArielBravoP/OptiDataLake-PMM/blob/main/Ingesti%C3%B3n%20de%20datos/SpringBootKafka/SpringConsumerBinance/src/main/java/com/odl/consumer/repository/MongoQuery.java) para ver el tiempo de las consultas a MongoDB, mediante una interfaz simple se pide al usuario que rellene los campos: "Símbolo de la moneda", "Precio mínimo", "Precio máximo".

### Tercera prueba
Ahora se crearon los proyectos ["producer-log"](https://github.com/ArielBravoP/OptiDataLake-PMM/tree/main/Ingesti%C3%B3n%20de%20datos/producer-log), ["consumer-log"](https://github.com/ArielBravoP/OptiDataLake-PMM/tree/main/Ingesti%C3%B3n%20de%20datos/consumer-log) para que nuestro Apache Kafka lea el archivo .log y vaya enviando los mensajes con pausas predefinidas, en este caso se dejó una pausa de 1 segundo.
- Para realizar la prueba se utilizó el topic "odl" creado anteriormente y se levanta docker-compose.
- Luego se ejecuta el consumer-log y producer-log mediante intellij, teniendo en cuenta que debemos tener el archivo "message.log" en la carpeta raiz del producer.

Con eso tendremos al productor enviando las líneas del .log cada 1 segundo y lo podremos ver en el terminal de consumer-log

### Segunda prueba
Se realiza una prueba creando un [producer](https://github.com/ArielBravoP/OptiDataLake-PMM/tree/main/Ingesti%C3%B3n%20de%20datos/producer) y [consumer](https://github.com/ArielBravoP/OptiDataLake-PMM/tree/main/Ingesti%C3%B3n%20de%20datos/consumer) en Apache Kafka, lo que se logró fue enviar mensajes correctamente en el formato "string:string" y que el consumidor lo vea correctamente.<br>
- Primero se abre una CMD y se ejecuta el comando: <br>
  - "docker-compose up" -> Se inicia el contenedor docker.<br>
- Segundo se ejecuta el código del producer ["producer/src/main/java/com/odl/Main.java"](https://github.com/ArielBravoP/OptiDataLake-PMM/blob/main/Ingesti%C3%B3n%20de%20datos/producer/src/main/java/com/odl/Main.java).<br>
- Luego se ejecuta el código del consumer ["consumer/src/main/java/com/odl/Main.java"](https://github.com/ArielBravoP/OptiDataLake-PMM/blob/main/Ingesti%C3%B3n%20de%20datos/consumer/src/main/java/com/odl/Main.java).<br>

Luego desde la ejecución del Main del producer, en su terminal, pondremos mensajes con el formato string:string, en donde el primer string será la key y no se mostrará en el consumidor, en cambio, el segundo string después de ":" aparecerá en la CMD del consumidor.

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

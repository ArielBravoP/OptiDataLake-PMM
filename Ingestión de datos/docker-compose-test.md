## Prueba de productor - consumidor con Apache Kafka

Programas instalados:
- Docker version 26.1.4
- Apache Kafka 3.7.1

Se crea el archivo de "docker-compose.yml" para realizar la conexión entre estos servicios, este documento va a contener información sobre el servidor local que se creará para hacer las pruebas.

Luego se utiliza el siguiente comando en el terminal de Visual Studio Code:<br>
"docker-compose up"

Y se abren dos terminales CMD de windows, en el primero se pone lo siguiente:<br>
"docker ps" -> Nos mostrará los contenedores en funcionamiento, en este caso serían 2, el de Kafka y de Zookeper.<br>
"docker exec -it kafka-broker-1 bash" -> Ingresamos al contenedor kafka mediante bash.<br>
"kafka-topics --bootstrap-server kafka-broker-1:9092 --create --topic odl" -> Creamos un tópico que se llamará odl.<br>
"kafka-console-producer --bootstrap-server kafka-broker-1:9092 --topic odl" -> Ingresamos al tópico como producer para ingresar datos.<br>

Ahora en el segundo CMD:<br>
"docker exec -it kafka-broker-1 bash"<br>
"kafka-console-consumer --bootstrap-server kafka-broker-1:9092 --topic odl --from-beginning" -> Ingresamos como consumidor y podremos ver todo lo que uno escriba en la primer CMD.<br>
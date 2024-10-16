#!/usr/bin/expect -f
# Ingresar al contenedor Docker y ejecutar los comandos para resetear los offsets
spawn docker exec -it kafka-broker-1 bash
expect "#"
send "kafka-consumer-groups --bootstrap-server localhost:9092 --group spark-group-id --reset-offsets --to-latest --execute --topic odl-ticks\n"
expect "#"
send "kafka-consumer-groups --bootstrap-server localhost:9092 --group my-group-id --reset-offsets --to-latest --execute --topic odl-ticks\n"
expect "#"
send "exit\n"
interact

# OptiDataLake-PMM

### Ingestión de datos
Se comienza a utilizar Apache Kafka, con la intención de simular la llegada de datos en tiempo real con el archivo .log que tenemos.

Se realizaron diversas pruebas para probar la implementación de Apache Kafka, el detalle se encuentra en el archivo "Pruebas.md" dentro de la carpeta Ingestión de datos.


Para ello se utilizó:
- Apache Kafka 3.7.1
- IntelliJ IDEA 2024.2.0.2 (Community Edition)
- Visual Studio Code 1.91.1
- Java JDK 22

### Lectura de datos
Dentro tenemos dos carpetas: 
- Binance: En esta carpeta tenemos un código que nos muestra los datos hitóricos de Binance en una ventana de tiempo
- Lectura: Archivo que lee el archivo .log
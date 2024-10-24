# OptiDataLake-PMM
Última modificación: [Dashboard.ipynb](https://github.com/ArielBravoP/OptiDataLake-PMM/blob/main/Ingesti%C3%B3n%20de%20datos/Dashboard.ipynb) <br>
Detalle de las modificaciones: ["Pruebas.md"](https://github.com/ArielBravoP/OptiDataLake-PMM/blob/main/Ingesti%C3%B3n%20de%20datos/Pruebas.md)

### Ingestión de datos
Se utiliza Apache Kafka, con la intención de simular la llegada de datos en tiempo real, y también se utiliza Apache Spark, con el fin de realizar procesamiento a los datos y calcular métricas.

Se realizaron diversas pruebas para la implementación de Apache Kafka y Spark, el detalle se encuentra en el archivo ["Pruebas.md"](https://github.com/ArielBravoP/OptiDataLake-PMM/blob/main/Ingesti%C3%B3n%20de%20datos/Pruebas.md) dentro de la carpeta Ingestión de datos.

Para ello se utilizó:
- Apache Kafka 3.8.0
- Apache Spark 4.0.0-preview2
- IntelliJ IDEA 2024.2.0.2 (Community Edition)
- Visual Studio Code 1.91.1
- Java JDK 22.0.2

### Lectura de datos
Dentro tenemos dos carpetas: 
- Binance: En esta carpeta tenemos un código que nos muestra los datos hitóricos de Binance en una ventana de tiempo
- Lectura: Archivo que lee el archivo .log

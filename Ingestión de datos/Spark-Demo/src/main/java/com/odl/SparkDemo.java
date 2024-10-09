package com.odl;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

public class SparkDemo {
    public static void main(String[] args) {
        // Crear la sesión de Spark
        SparkSession spark = SparkSession.builder()
                .appName("Simple Spark Demo")
                .master("local")
                .getOrCreate();

        // Definir el esquema esperado del archivo JSON
        StructType schema = new StructType()
                .add("nombre", DataTypes.StringType, true)
                .add("edad", DataTypes.IntegerType, true)
                .add("ciudad", DataTypes.StringType, true);

        // Leer el archivo JSON con el esquema definido
        Dataset<Row> df = spark.read()
                .option("mode", "DROPMALFORMED")
                .option("columnNameOfCorruptRecord", "_corrupt_record")  // Mostrar datos corruptos en esta columna
                .schema(schema)  // Proporcionar el esquema explícitamente
                .json("src/main/java/com/odl/archivo.json");

        // Mostrar el esquema y las primeras filas del DataFrame
        df.printSchema();
        df.show(false);

        spark.stop();
    }
}
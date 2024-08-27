package com.odl;

public class Main {

    public static void main(String[] args) {
        var consumer = Consumer.getInstance();
        consumer.start();
    }
}
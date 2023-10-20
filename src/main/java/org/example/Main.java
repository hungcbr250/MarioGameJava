package org.example;


import org.example.jade.Window;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        Window window=Window.get();
        window.run();

    }
}
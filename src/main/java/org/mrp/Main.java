package org.mrp;

import com.sun.net.httpserver.HttpServer;
import org.mrp.serverHandler.AuthHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {

        //Creating server on Port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        //Handler for different parts
        server.createContext("/", new AuthHandler());
        //server.createContext("/media", new MediaHandler());
        //server.createContext("/user", new UserHandler());

        //Setting Executor (null = Standard-Executor)
        server.setExecutor(null);

        //Starting Server
        server.start();
        System.out.println("Server is running on http://localhost:8000");
    }
}
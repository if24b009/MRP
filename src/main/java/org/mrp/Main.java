package org.mrp;

import com.sun.net.httpserver.HttpServer;
import org.mrp.serverHandler.AuthHandler;
import org.mrp.serverHandler.MediaEntryHandler;
import org.mrp.serverHandler.RatingHandler;
import org.mrp.serverHandler.UserHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    //SERVER
    public static void main(String[] args) throws IOException {

        try {
            //Creating server on Port 8000
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

            //Handler for different parts
            server.createContext("/", new AuthHandler());
            server.createContext("/mediaEntry", new MediaEntryHandler());
            server.createContext("/rating", new RatingHandler());
            server.createContext("/users", new UserHandler());

            //Setting Executor (null = Standard-Executor)
            server.setExecutor(null);

            //Starting Server
            server.start();
            System.out.println("Server is running on http://localhost:8000");
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            System.exit(1);
        }

    }
}
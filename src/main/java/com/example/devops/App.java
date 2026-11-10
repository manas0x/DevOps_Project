package com.example.devops;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class App {
    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8081"));
        HttpServer server = createServer(port);
        server.start();
        System.out.println("Deployment demo running at http://localhost:" + port);
    }

    static HttpServer createServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", exchange -> {
            if (!exchange.getRequestURI().getPath().equals("/")) { respond(exchange, 404, "Not found", "text/plain"); return; }
            respond(exchange, 200, "<!doctype html><html><head><title>DevOps Deployment Demo</title>"
                + "<style>body{font-family:Arial;max-width:750px;margin:80px auto;padding:0 24px;color:#172337}"
                + "h1{color:#164b8c}main{background:#edf4fb;padding:30px;border-radius:12px}"
                + "</style></head><body><main><h1>DevOps Deployment Demo</h1>"
                + "<p>The Java application is running successfully.</p>"
                + "<p>Project: Manual and Automated Software Deployment</p>"
                + "<p>Built using Maven. Automated deployment uses Jenkins.</p></main></body></html>", "text/html");
        });
        server.createContext("/health", exchange -> respond(exchange, 200,
            "deployment-demo-ok" + System.getProperty("project3.token", ""), "text/plain"));
        return server;
    }

    private static void respond(HttpExchange exchange, int code, String body, String type) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (var output = exchange.getResponseBody()) { output.write(bytes); }
    }
}

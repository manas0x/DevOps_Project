package com.example.devops;

import static org.junit.jupiter.api.Assertions.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

class AppTest {
    @Test void serverReturnsHealthAndPage() throws Exception {
        var server = App.createServer(0);
        server.start();
        try {
            var client = HttpClient.newHttpClient();
            var base = "http://127.0.0.1:" + server.getAddress().getPort();
            var health = client.send(HttpRequest.newBuilder(URI.create(base + "/health")).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, health.statusCode());
            assertEquals("deployment-demo-ok", health.body());
            var page = client.send(HttpRequest.newBuilder(URI.create(base + "/")).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, page.statusCode());
            assertTrue(page.body().contains("DevOps Deployment Demo"));
        } finally { server.stop(0); }
    }
}

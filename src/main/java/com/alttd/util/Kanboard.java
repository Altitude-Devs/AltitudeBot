package com.alttd.util;

import com.alttd.config.SettingsConfig;
import net.dv8tion.jda.api.entities.Message;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Kanboard {

    public static CompletableFuture<Boolean> forwardMessageToKanboard(String title, String body) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;
        try {
            String jsonPayload = getPayload(UUID.randomUUID().toString(), title, body);

            request = HttpRequest.newBuilder()
                    .uri(new URI("https://kanboard.alttd.com/jsonrpc.php"))
                    .header("Content-Type", "application/json")
                    .setHeader("Authorization", SettingsConfig.KANBOARD_TOKEN)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
        } catch (URISyntaxException e) {
            Logger.altitudeLogs.error(e);
            //TODO handle better
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            HttpResponse<String> response;
            try {
                response = client.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (InterruptedException | IOException e) {
                Logger.altitudeLogs.error(e);
                return false;
            }

            if (response.statusCode() == 200) {
                Logger.altitudeLogs.info(response.body());
                return true;
            } else {
                Logger.altitudeLogs.error(String.format("Invalid response [%s]", response.body()));
                return false;
            }
        });
    }

    public static CompletableFuture<Boolean> forwardMessageToKanboard(Message message) {
        String body = message.getContentDisplay();
        String[] split = body.split("\n");
        String title;
        if (split.length == 1) {
            title = "No title";
        } else {
            title = split[0];
            body = Arrays.stream(split).skip(1).collect(Collectors.joining("\n"));
        }
        return forwardMessageToKanboard(title, body);
    }

    private static String getPayload(String id, String title, String body) {
        return String.format("""
                {
                    "jsonrpc": "2.0",
                    "method": "createTask",
                    "id": %s,
                    "params": {
                        "title": "%s",
                        "project_id": "3",
                        "description": "%s",
                        "column_id": "0",
                        "color_id": "red"
                    }
                }""", id, title, body);
    }

}

package com.alttd.util;

import com.alttd.config.SettingsConfig;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Kanboard {

    public static CompletableFuture<Boolean> forwardMessageToKanboard(String title, String body, String webLink) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;
        String jsonPayload = getPayload(UUID.randomUUID().toString(), title, body, webLink);
        try {
            byte[] kanboardTokenBytes = String.join(":", "jsonrpc", SettingsConfig.KANBOARD_TOKEN).getBytes(StandardCharsets.UTF_8);
            String token = Base64.getEncoder().encodeToString(kanboardTokenBytes);
            request = HttpRequest.newBuilder()
                    .uri(new URI("https://kanboard.alttd.com/jsonrpc.php"))
                    .header("Content-Type", "application/json")
                    .setHeader("Authorization", "Basic " + token)
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

            if (response.statusCode() == 200 && response.body().matches(".*\"result\":[0-9]+,.*")) {
                Logger.altitudeLogs.info(String.format("Response body: [%s]\nFor JsonPayload: [%s]", response.body(), jsonPayload));
                return true;
            } else {
                Logger.altitudeLogs.error(String.format("Invalid response: [%s]\nPayload: [%s]", response.body(), jsonPayload));
                return false;
            }
        });
    }

    public static CompletableFuture<Boolean> forwardMessageToKanboard(Message message) {
        String body = message.getContentDisplay();
        if (messageIsSuggestion(message) && message.getGuildChannel() instanceof ThreadChannel threadChannel) {
            return forwardSuggestionToKanboard(message, threadChannel);
        }
        String[] split = body.split("\n");
        String title;
        if (split.length == 1) {
            title = "No title";
        } else {
            title = split[0];
            body = Arrays.stream(split).skip(1).filter(str -> !str.isBlank()).collect(Collectors.joining("\n"));
        }
        return forwardMessageToKanboard(title, body, getWebLink(message));
    }

    private static CompletableFuture<Boolean> forwardSuggestionToKanboard(Message message, ThreadChannel threadChannel) {
        String title = threadChannel.getName();
        String content = Arrays.stream(message.getContentDisplay().split("\n")).skip(1).filter(str -> !str.isBlank()).collect(Collectors.joining("\n"));
        return forwardMessageToKanboard(title, content, getWebLink(message));
    }

    private static boolean messageIsSuggestion(Message message) {
        Member member = message.getMember();
        if (member == null)
            return false;

        if (!member.getUser().equals(message.getJDA().getSelfUser())) {
            return false;
        }
        if (!message.getContentRaw().startsWith("**Suggestion by:")) {
            return false;
        }
        return true;
    }

    private static String getPayload(String id, String title, String body, String webLink) {
        return String.format("""
                {
                    "jsonrpc": "2.0",
                    "method": "createTask",
                    "id": "%s",
                    "params": {
                        "title": "%s",
                        "project_id": "3",
                        "description": "%s%s",
                        "column_id": "0",
                        "color_id": "red"
                    }
                }""",
                escapeSpecialJsonChars(id),
                escapeSpecialJsonChars(title),
                escapeSpecialJsonChars(body),
                escapeSpecialJsonChars(String.format("\n\n[Discord link](%s)", webLink)));
    }

    private static String getWebLink(Message message) {
        return String.format("https://discord.com/channels/%d/%d/%d", message.getGuildIdLong(), message.getChannelIdLong(), message.getIdLong());
    }

    private static String escapeSpecialJsonChars(String s) {
        return s.replace("\"", "\\\"")
                .replace("\\", "\\\\")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}

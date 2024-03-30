package com.alttd.listeners;

import com.alttd.config.SettingsConfig;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.channel.forum.ForumTagAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TagAdded extends ListenerAdapter {

    public void tagAdded(ForumTagAddEvent event) {
        if (event.getTag().getIdLong() != 0L) {//TODO add tag id
            return;
        }
        if (!(event.getChannel() instanceof ThreadChannel threadChannel)) {
            return;
        }
        threadChannel.retrieveStartMessage().queue(this::forwardMessageToKanboard, error -> {
            Logger.altitudeLogs.error(error);
        });
    }

    //TODO move to its own util class
    public void forwardMessageToKanboard(Message message) {
        String contentDisplay = message.getContentDisplay();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;
        try {
            String jsonPayload = String.format("{"
                    + "\"title\": \"%s\","
                    + "\"description\": \"%s\","
                    + "\"project_id\": \"%d\""
                    + "}", "New Task" /*TODO config*/, contentDisplay, 3/*TODO config*/);

            request = HttpRequest.newBuilder()
                    .uri(new URI("https://kanboard.alttd.com/jsonrpc.php")) //TODO correct URL
                    .header("Content-Type", "application/json")
                    .setHeader("Authorization", SettingsConfig.KANBOARD_TOKEN)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
        } catch (URISyntaxException e) {
            Logger.altitudeLogs.error(e);
            //TODO handle better
            return;
        }

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            Logger.altitudeLogs.error(e);
            return;
        }

        if (response.statusCode() == 200) {
            Logger.altitudeLogs.info(response.body());
        } else {
            Logger.altitudeLogs.error(String.format("Invalid response [%s]", response.body()));
        }
    }

}

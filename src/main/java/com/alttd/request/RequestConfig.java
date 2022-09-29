package com.alttd.request;

import com.alttd.config.AbstractConfig;
import com.alttd.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class RequestConfig extends AbstractConfig {

    static RequestConfig requestConfig;

    public RequestConfig() {
        super("requests.yml");
    }

    public static void reload() {
        requestConfig = new RequestConfig();
        requestConfig.readConfig(RequestConfig.class, requestConfig);
    }

    public static String REQUEST_GUILD_ID = "776590138296893480";
    public static String REQUEST_CATEGORY = "776590138296893481";
    public static String REQUEST_CHANNEL = "1017787342561476709";
    public static String REQUEST_MESSAGE = "";
    private void settings() {
        REQUEST_GUILD_ID = requestConfig.getString("request.guild", REQUEST_GUILD_ID);
        REQUEST_CATEGORY = requestConfig.getString("request.category", REQUEST_CATEGORY);
        REQUEST_CHANNEL = requestConfig.getString("request.channel", REQUEST_CHANNEL);
        REQUEST_MESSAGE = requestConfig.getString("request.message", REQUEST_MESSAGE);
    }

    public static void setRequestMessage(String messageId) {
        REQUEST_MESSAGE = messageId;
        requestConfig.update("request.message", REQUEST_MESSAGE);
        requestConfig.save();
    }

    public static final List<Request> requests = new ArrayList<>();
    private void loadRequests() {
        requests.clear();
        requestConfig.getNode("types").childrenMap().forEach((key, value) -> {
            String id = key.toString();
            String category = value.node("category").getString();
            String channel = value.node("channel").getString();
            String name = value.node("name").getString();
            String title = value.node("title").getString();
            String description = value.node("description").getString();
            String message = value.node("message").getString();
            if (id == null || category == null || channel == null || name == null || description == null || message == null) {
                Logger.warning("Requests are set up incorrectly!");
            } else {
                requests.add(new Request(id, category, channel, name, title, description, message));
            }
        });
    }

}

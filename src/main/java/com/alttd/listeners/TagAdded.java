package com.alttd.listeners;

import com.alttd.config.SettingsConfig;
import com.alttd.util.Kanboard;
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

    @Override
    public void onForumTagAdd(ForumTagAddEvent event) {
        if (event.getTag().getIdLong() != 0L) {//TODO add tag id
            return;
        }
        if (!(event.getChannel() instanceof ThreadChannel threadChannel)) {
            return;
        }
        threadChannel.retrieveStartMessage().queue(Kanboard::forwardMessageToKanboard, error -> {
            Logger.altitudeLogs.error(error);
        });
    }

}

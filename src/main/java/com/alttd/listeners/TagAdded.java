package com.alttd.listeners;

import com.alttd.util.Kanboard;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateAppliedTagsEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class TagAdded extends ListenerAdapter {

    @Override
    public void onChannelUpdateAppliedTags(ChannelUpdateAppliedTagsEvent event) {
        List<ForumTag> addedTags = event.getAddedTags();
        if (addedTags.stream().map(ISnowflake::getIdLong).noneMatch(id -> id == 1020378607052398632L)) {//TODO add tag id to config
            return;
        }
        if (!(event.getChannel() instanceof ThreadChannel threadChannel)) {
            return;
        }
        threadChannel.retrieveStartMessage().queue(Kanboard::forwardMessageToKanboard, error -> Logger.altitudeLogs.error(error));
    }

}

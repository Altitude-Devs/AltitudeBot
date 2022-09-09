package com.alttd.commandManager.listeners;

import com.alttd.commandManager.CommandManager;
import com.alttd.request.RequestManager;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class JDAListener extends ListenerAdapter {

    private final JDA jda;

    public JDAListener(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Logger.info("JDA ready registering commands.");
        jda.addEventListener(new CommandManager(jda));
    }

    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteractionEvent event) {
        String s = event.getComponentId();
        if (s.startsWith("request:")) {
            RequestManager.onSelectMenuInteraction(event);
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String s = event.getModalId();
        if (s.startsWith("request:")) {
            RequestManager.onModalInteractionEvent(event);
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String s =  event.getComponentId();
        if (s.startsWith("request:")) {
            RequestManager.onButtonInteractionEvent(event);
        }
    }

}

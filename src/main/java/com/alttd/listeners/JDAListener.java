package com.alttd.listeners;

import com.alttd.commandManager.CommandManager;
import com.alttd.modalManager.ModalManager;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class JDAListener extends ListenerAdapter {

    private final JDA jda;

    public JDAListener(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        Logger.info("JDA ready to register commands.");
        jda.addEventListener(new CommandManager(jda, new ModalManager()));
    }

}

package com.alttd.listeners;

import com.alttd.buttonManager.ButtonManager;
import com.alttd.commandManager.CommandManager;
import com.alttd.contextMenuManager.ContextMenuManager;
import com.alttd.modalManager.ModalManager;
import com.alttd.schedulers.AuctionScheduler;
import com.alttd.schedulers.ReminderScheduler;
import com.alttd.request.RequestManager;
import com.alttd.selectMenuManager.SelectMenuManager;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
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
        ChatListener chatListener = new ChatListener();
        ButtonManager buttonManager = new ButtonManager();
        ModalManager modalManager = new ModalManager(buttonManager);
        ContextMenuManager contextMenuManager = new ContextMenuManager(modalManager);
        SelectMenuManager selectMenuManager = new SelectMenuManager();
        CommandManager commandManager = new CommandManager(jda, modalManager, contextMenuManager, chatListener, selectMenuManager);
        jda.addEventListener(buttonManager, modalManager, commandManager, contextMenuManager, chatListener, selectMenuManager);
        startSchedulers();
//        RequestManager.init();
    }

    private void startSchedulers() {
        ReminderScheduler reminderScheduler = ReminderScheduler.getInstance(jda);
        if (reminderScheduler == null)
            Logger.severe("Unable to start reminder scheduler!");

        AuctionScheduler auctionScheduler = AuctionScheduler.getInstance();
        if (auctionScheduler == null)
            Logger.severe("Unable to start auction scheduler!");
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

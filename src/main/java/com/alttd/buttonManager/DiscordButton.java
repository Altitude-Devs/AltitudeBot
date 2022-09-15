package com.alttd.buttonManager;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public abstract class DiscordButton {

    public abstract String getButtonId();

    public abstract void execute(ButtonInteractionEvent event);

    public abstract Button getButton();
}

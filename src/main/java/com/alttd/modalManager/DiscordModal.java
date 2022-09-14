package com.alttd.modalManager;

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.Modal;

public abstract class DiscordModal {

    public abstract String getModalId();

    public abstract void execute(ModalInteractionEvent event);

    public abstract Modal getModal();

}

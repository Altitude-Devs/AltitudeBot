package com.alttd.selectMenuManager;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

public abstract class DiscordSelectMenu {

    public abstract String getSelectMenuId();

    public abstract void execute(SelectMenuInteractionEvent event);

    public abstract SelectMenu getSelectMenu(SelectOption ...selectOptions);
}

package com.alttd.selectMenuManager;

import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.List;

public abstract class DiscordSelectMenu {

    public abstract String getSelectMenuId();

    public abstract void execute(StringSelectInteractionEvent event);

    public abstract SelectMenu getSelectMenu(List<SelectOption> selectOptions);
}

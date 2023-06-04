package com.alttd.commandManager.commands.PollCommand;

import com.alttd.buttonManager.ButtonManager;
import com.alttd.buttonManager.buttons.pollButton.PollButton;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.commandManager.SubCommand;
import com.alttd.commandManager.SubCommandGroup;
import com.alttd.database.queries.Poll.Poll;
import com.alttd.database.queries.Poll.PollButtonQueries;
import com.alttd.templates.Parser;
import com.alttd.templates.Template;
import com.alttd.util.Logger;
import com.alttd.util.OptionMappingParsing;
import com.alttd.util.Util;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubCommandAddButton extends SubCommand {
    private final ButtonManager buttonManager;
    protected SubCommandAddButton(SubCommandGroup parentGroup, DiscordCommand parent, ButtonManager buttonManager) {
        super(parentGroup, parent);
        this.buttonManager = buttonManager;
    }

    @Override
    public String getName() {
        return "add_button";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        PollChannel pollChannel = PollUtil.getPollHandleErrors(event, getName());
        if (pollChannel == null)
            return;

        Long rowLong = Util.parseLong(OptionMappingParsing.getString("button_row", event, getName()));
        if (rowLong == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve button row."))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        int rowId = rowLong.intValue();
        if (rowId < 1 || rowId > 5) {
            event.replyEmbeds(Util.genericErrorEmbed("Error",
                            Parser.parse("Invalid row id `<id>`, only 1-5 are valid.",
                                    Template.of("id", String.valueOf(rowId)))))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String buttonName = OptionMappingParsing.getString("button_name", event, getName());
        if (buttonName == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve button name."))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply(true).queue(hook ->
                pollChannel.textChannel().retrieveMessageById(pollChannel.poll().getPollId()).queue(
                        message -> updatePoll(rowId, buttonName, message, hook),
                        throwable -> failedToGetMessage(throwable, hook)));
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {
        PollUtil.handleSuggestMessageId(event);
    }

    private void failedToGetMessage(Throwable throwable, InteractionHook hook) {
        Logger.altitudeLogs.warning(throwable.getMessage());
        hook.editOriginalEmbeds(Util.genericErrorEmbed("Failed to get poll message",
                        "Please check if the poll still exists and the message id is correct."))
                .queue();
    }

    private void updatePoll(int rowId, String buttonName, Message message, InteractionHook hook) {
        String buttonId = message.getId() + buttonName;
        Poll poll = Poll.getPoll(message.getIdLong());
        PollButtonQueries.addButton(message.getIdLong(), buttonId, buttonName);
        List<PollButton> pollButtons = PollButtonQueries.loadButtons(poll, buttonManager);
        if (pollButtons == null) {
            hook.editOriginalEmbeds(Util.genericErrorEmbed("Error","Unable to retrieve buttons for this poll")).queue();
            return;
        }

        Optional<PollButton> any = pollButtons.stream().filter(button -> button.getButtonId().equals(buttonId)).findAny();
        if (any.isEmpty()) {
            hook.editOriginalEmbeds(Util.genericErrorEmbed("Error", "Unable to find newly created button")).queue();
            return;
        }

        PollButton pollButton = any.get();
        List<ActionRow> actionRows = message.getActionRows();
        if (rowId > 1) {//todo fix if needed in the future
            hook.editOriginalEmbeds(Util.genericErrorEmbed("Error",
                            "Polls have only been set up to handle 1 row if you need more than one row update the code."))
                    .queue();
            return;
        }

        List<ItemComponent> components;
        if (!actionRows.isEmpty()) {
            components = actionRows.get(0).getComponents();
        } else
            components = new ArrayList<>();

        components.add(pollButton.getButton());
        message.editMessageComponents().setActionRow(components).queue();
        hook.editOriginalEmbeds(Util.genericSuccessEmbed("Success", "Added a button")).queue();
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}

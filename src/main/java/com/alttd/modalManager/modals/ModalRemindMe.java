package com.alttd.modalManager.modals;

import com.alttd.buttonManager.ButtonManager;
import com.alttd.buttonManager.buttons.remindMeConfirm.ButtonRemindMeConfirm;
import com.alttd.database.queries.QueriesReminders.Reminder;
import com.alttd.modalManager.DiscordModal;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.utils.TimeUtil;

import java.util.Date;
import java.util.HashMap;

public class ModalRemindMe extends DiscordModal {

    private static final HashMap<Long, RemindMeData> userToRemindMeMap = new HashMap<>();

    public static synchronized void putData(long userId, TextChannel channel, long timestamp) {
        userToRemindMeMap.put(userId, new RemindMeData(channel, timestamp));
    }

    private static synchronized RemindMeData pullData(long userId) {
        return userToRemindMeMap.remove(userId);
    }

    private final ButtonManager buttonManager;

    public ModalRemindMe(ButtonManager buttonManager) {
        this.buttonManager = buttonManager;
    }

    @Override
    public String getModalId() {
        return "remindme";
    }

    @Override
    public void execute(ModalInteractionEvent event) {
        String title = getValidString(event.getValue("title"), event);
        if (title == null)
            return;

        String desc = getValidString(event.getValue("description"), event);
        if (desc == null)
            return;

        long userId = event.getUser().getIdLong();
        RemindMeData remindMeData = pullData(userId);
        if (remindMeData == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't find the data from the command that triggered this modal"))
                    .setEphemeral(true).queue();
            return;
        }

        Reminder reminder = new Reminder(
                -1,
                title,
                desc,
                userId,
                remindMeData.textChannel.getGuild().getIdLong(),
                remindMeData.textChannel.getIdLong(),
                0,
                false,
                new Date().getTime(),
                remindMeData.timestamp);

        Button remindMeConfirm = buttonManager.getButtonFor("remind_me_confirm");
        Button remindMeCancel = buttonManager.getButtonFor("remind_me_cancel");
        if (remindMeConfirm == null || remindMeCancel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Unable to retrieve continue/cancel buttons"))
                    .setEphemeral(true).queue();
            return;
        }

        long discordTimestamp = TimeUtil.getDiscordTimestamp(reminder.creationDate());
        MessageEmbed messageEmbed = new EmbedBuilder()
                .setTitle(reminder.title())
                .setDescription(reminder.description())
                .setFooter("Requested <t:" + discordTimestamp + ":R>")
                .build();
        ButtonRemindMeConfirm.putReminder(userId, reminder);
        event.replyEmbeds(messageEmbed).queue(message ->
                message.editOriginalComponents().setActionRow(remindMeConfirm, remindMeCancel)
                        .queue(RestAction.getDefaultSuccess(), Util::handleFailure));
    }

    public String getValidString(ModalMapping modalMapping, ModalInteractionEvent event) {
        if (modalMapping == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't find modal"))
                    .setEphemeral(true).queue();
            return null;
        }

        String string = modalMapping.getAsString();
        if (string.isEmpty()) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "Couldn't find contents of modal"))
                    .setEphemeral(true).queue();
            return null;
        }

        return string;
    }

    @Override
    public Modal getModal() {
        TextInput title = TextInput.create("title", "Title", TextInputStyle.SHORT)
                .setPlaceholder("reminder title:")
                .setRequiredRange(1, 256)
                .setRequired(true)
                .build();

        TextInput desc = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                .setPlaceholder("optional reminder description:")
                .setRequiredRange(1, 4096)
                .setRequired(false)
                .build();

        return Modal.create(getModalId(), "Remind Me")
                .addActionRows(ActionRow.of(title), ActionRow.of(desc))
                .build();
    }

    private record RemindMeData(TextChannel textChannel, long timestamp) {}
}

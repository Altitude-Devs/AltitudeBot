package com.alttd.commandManager.commands;

import com.alttd.AltitudeBot;
import com.alttd.commandManager.CommandManager;
import com.alttd.commandManager.DiscordCommand;
import com.alttd.database.queries.QueriesAuctions.Auction;
import com.alttd.database.queries.commandOutputChannels.CommandOutputChannels;
import com.alttd.database.queries.commandOutputChannels.OutputType;
import com.alttd.schedulers.AuctionScheduler;
import com.alttd.selectMenuManager.SelectMenuManager;
import com.alttd.util.Logger;
import com.alttd.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.AttachedFile;

import java.awt.*;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CommandAuction extends DiscordCommand {

    private final CommandData commandData;
    private final SelectMenuManager selectMenuManager;
    public CommandAuction(JDA jda, CommandManager commandManager, SelectMenuManager selectMenuManager) {
        commandData = Commands.slash(getName(), "Create an auction")
                .addOption(OptionType.STRING, "item", "The name (and type) of the item you're selling", true)
                .addOption(OptionType.INTEGER, "amount", "How many of the item you're selling", true)
                .addOption(OptionType.INTEGER, "starting-price", "How much the bids should start at (the minimum price you're willing to accept for the item", true)
                .addOption(OptionType.INTEGER, "minimum-increase", "The minimum amount of money bids should increase by", true)
                .addOption(OptionType.INTEGER, "insta-buy", "(optional) A price you're willing to sell the item for immediately (ends the auction)", false)
                .addOption(OptionType.STRING, "description", "(optional) A further explanation of the item", false)
                .addOption(OptionType.ATTACHMENT, "screenshot", "(optional) A screenshot of the item you're selling if needed", false)
                .setDefaultPermissions(DefaultMemberPermissions.ENABLED);

        this.selectMenuManager = selectMenuManager;
        Util.registerCommand(commandManager, jda, commandData, getName());
    }

    @Override
    public String getName() {
        return "auction";
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            handleError(event, "This command can only be executed within a guild");
            return;
        }

        GuildChannel outputChannel = CommandOutputChannels.getOutputChannel(guild, OutputType.AUCTION);
        if (outputChannel == null) {
            handleError(event, "This guild does not have an Auction channel set");
            return;
        }

        TextChannel textChannel = validTextChannel(event, outputChannel);
        if (textChannel == null) {
            handleError(event, "This guild has an invalid Auction channel");
            return;
        }

        Integer minimumIncrease = event.getOption("minimum-increase", OptionMapping::getAsInt);
        if (minimumIncrease == null) {
            handleError(event, "Missing required minimum increase option");
            return;
        }

        MessageEmbed messageEmbed = buildAuctionEmbed(event, minimumIncrease);
        if (messageEmbed == null)
            return;
        ReplyCallbackAction replyCallbackAction = event.deferReply(true);
        textChannel.sendMessageEmbeds(messageEmbed).queue(
                success -> onSuccessSendAuctionMessage(event, success, replyCallbackAction, minimumIncrease),
                error -> replyCallbackAction.setEmbeds(Util.genericErrorEmbed("Error", "Unable to send your auction to the auction channel"))
                .queue());
    }

    private void onSuccessSendAuctionMessage(SlashCommandInteractionEvent event, Message message, ReplyCallbackAction replyCallbackAction, int minimumIncrease) {
        Message.Attachment screenshot = event.getOption("screenshot", OptionMapping::getAsAttachment);
        if (screenshot != null)
            addScreenshot(screenshot, message);

        Integer startingPrice = event.getOption("starting-price", OptionMapping::getAsInt);
        if (startingPrice == null) {
            Logger.severe("Starting price magically became null");
            replyCallbackAction.setEmbeds(Util.genericSuccessEmbed("Error", "Failed to store auction"))
                    .queue();
            return;
        }

        Auction auction = new Auction(
                event.getUser().getIdLong(),
                message,
                message.getChannel().getIdLong(),
                message.getGuild().getIdLong(),
                startingPrice,
                Instant.now().toEpochMilli() + TimeUnit.DAYS.toMillis(1),
                minimumIncrease,
                event.getOption("insta-buy", OptionMapping::getAsInt));

        SelectMenu selectMenu = auction.getSelectMenu(selectMenuManager, false);
        if (selectMenu == null) {
            replyCallbackAction.setEmbeds(Util.genericErrorEmbed("Error", "Unable to find select menu for your auction, removing message..."))
                    .queue();
            message.delete().queue();
            return;
        }

        message.editMessageComponents().setActionRow(selectMenu).queue();

        AuctionScheduler auctionScheduler = AuctionScheduler.getInstance();
        if (auctionScheduler == null) {
            replyCallbackAction.setEmbeds(Util.genericSuccessEmbed("Error", "Failed to store auction in scheduler"))
                    .queue();
            return;
        }
        auctionScheduler.addAuction(auction);
        replyCallbackAction.setEmbeds(Util.genericSuccessEmbed("Success", "Your auction was created"))
                .queue();
    }

    private void addScreenshot(Message.Attachment screenshot, Message message) {
        String dataFolder = AltitudeBot.getInstance().getDataFolder();
        Path parent = Path.of(dataFolder).getParent();
        Path path = Path.of(parent.toString() + UUID.randomUUID() + "." + screenshot.getFileExtension());
        screenshot.getProxy().downloadToFile(path.toFile()).whenComplete((file, throwable) ->
                        message.editMessageAttachments(AttachedFile.fromData(file)).queue(done -> file.delete(), failed -> {
                            Util.handleFailure(failed);
                            file.delete();
                        }))
                .exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
    }

    private MessageEmbed buildAuctionEmbed(SlashCommandInteractionEvent event, int minimumIncrease) {
        Member member = event.getMember();
        if (member == null) {
            return handleBuildEmbedError(event, "You are not a member of this guild");
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        String item = event.getOption("item", OptionMapping::getAsString);
        if (item == null)
            return handleBuildEmbedError(event, "Missing required item option");
        if (item.length() > 128)
            return handleBuildEmbedError(event, "Your item name is too long");
        embedBuilder.appendDescription("**Item**: " + item);

        Integer amount = event.getOption("amount", OptionMapping::getAsInt);
        if (amount == null)
            return handleBuildEmbedError(event, "Missing required amount option");
        embedBuilder.appendDescription("\n**Amount**: " + Util.formatNumber(amount));

        Integer startingPrice = event.getOption("starting-price", OptionMapping::getAsInt);
        if (startingPrice == null)
            return handleBuildEmbedError(event, "Missing required starting price option");
        embedBuilder.appendDescription("\n**Starting Price**: $" + Util.formatNumber(startingPrice));

        Integer instaBuy = event.getOption("insta-buy", OptionMapping::getAsInt);
        if (instaBuy != null) {
            if (instaBuy == minimumIncrease)
                return handleBuildEmbedError(event, "Insta buy can't be the same as minimum increase");
            embedBuilder.appendDescription("\n**Insta Buy**: $" + Util.formatNumber(instaBuy));
        }


        String description = event.getOption("description", OptionMapping::getAsString);
        if (description != null)
            embedBuilder.appendDescription("\n**Description**: " + description);

        embedBuilder
                .setAuthor(member.getEffectiveName(), null, member.getAvatarUrl())
                .setTitle("Auction")
                .setColor(Color.ORANGE)
                .appendDescription("\n\nCloses <t:" + (Instant.now().getEpochSecond() + TimeUnit.DAYS.toSeconds(1)) + ":R>");
        return embedBuilder.build();
    }

    private MessageEmbed handleBuildEmbedError(SlashCommandInteractionEvent event, String s) {
        event.replyEmbeds(Util.genericErrorEmbed("Error", s))
                .setEphemeral(true)
                .queue();
        return null;
    }

    private void handleError(SlashCommandInteractionEvent event, String s) {
        event.replyEmbeds(Util.genericErrorEmbed("Error", s))
                .setEphemeral(true)
                .queue();
    }

    private TextChannel validTextChannel(SlashCommandInteractionEvent event, GuildChannel guildChannel) {
        if (guildChannel == null) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "This server does not have a valid auction channel"))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return null;
        }

        if (!(guildChannel instanceof TextChannel channel)) {
            event.replyEmbeds(Util.genericErrorEmbed("Error", "A auction channel can't be of type: " + guildChannel.getType().name()))
                    .setEphemeral(true).queue(RestAction.getDefaultSuccess(), Util::handleFailure);
            return null;
        }
        return channel;
    }

    @Override
    public void suggest(CommandAutoCompleteInteractionEvent event) {

    }

    @Override
    public String getHelpMessage() {
        return null;
    }

    @Override
    public CommandData getCommandData() {
        return commandData;
    }
}

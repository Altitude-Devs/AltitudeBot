package com.alttd.communication.contact;

import com.alttd.AltitudeBot;
import com.alttd.communication.formData.ContactFormData;
import jakarta.validation.Valid;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/contact")
public class ContactEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(ContactEndpoint.class);


    @PostMapping("/submitContactForm")
    public CompletableFuture<ResponseEntity<String>> sendFormToDiscord(@Valid @RequestBody ContactFormData formData) {
        logger.debug("Sending form to Discord: " + formData);
        MessageEmbed messageEmbed = formData.toMessageEmbed();
        Guild guild = AltitudeBot.getInstance().getJDA().getGuildById(514920774923059209L);
        if (guild == null) {
            logger.error("Unable to retrieve staff guild");
            return CompletableFuture.completedFuture(ResponseEntity.internalServerError().body("Failed to submit form to Discord"));
        }
        TextChannel channel = guild.getChannelById(TextChannel.class, 514922567883292673L);
        if (channel == null) {
            logger.error("Unable to retrieve contact form channel");
            return CompletableFuture.completedFuture(ResponseEntity.internalServerError().body("Failed to submit form to Discord"));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                Message complete = channel.sendMessageEmbeds(messageEmbed).complete();
                if (complete != null)
                    return ResponseEntity.ok("");
            } catch (Exception exception) {
                logger.error("Failed to send message to Discord", exception);
            }
            return ResponseEntity.internalServerError().body("Failed to submit form to Discord");
        });
    }

}

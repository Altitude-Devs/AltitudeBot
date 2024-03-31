package com.alttd.communication.formData;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.hibernate.validator.constraints.Length;

import java.awt.*;

public class ContactFormData extends Form {

    public ContactFormData(String username, String email, String question) {
        this.username = username;
        this.email = email;
        this.question = question;
    }

    @NotEmpty(message = "You have to provide a username")
    @Length(min = 3, max = 16, message = "Usernames have to be between 3 and 16 characters")
    @Pattern(regexp = "[a-zA-Z-0-9_]{3,16}", message = "Your username has to be a valid Minecraft username")
    public final String username;

    @NotEmpty(message = "You have to provide an e-mail address")
    @Email(regexp = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])",
            message = "This is not a valid e-mail address")
    public final String email;

    @Length(min = 11, max = 2000, message = "Your question should have between 10 and 2000 characters")
    public final String question;

    @Override
    public String toString() {
        return "ContactFormData{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", question='" + question + '\'' +
                '}';
    }

    @Override
    public MessageEmbed toMessageEmbed() {
        return new EmbedBuilder()
                .addField("Username", username, false)
                .addField("email", email, false)
                .appendDescription(question)
                .setColor(Color.GREEN)
                .build();
    }
}
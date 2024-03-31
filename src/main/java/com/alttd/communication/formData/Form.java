package com.alttd.communication.formData;

import net.dv8tion.jda.api.entities.MessageEmbed;

public abstract class Form {

    @Override
    public abstract String toString();

    public abstract MessageEmbed toMessageEmbed();
}

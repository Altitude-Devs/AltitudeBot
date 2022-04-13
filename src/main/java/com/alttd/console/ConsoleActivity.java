package com.alttd.console;

import com.alttd.config.SettingsConfig;
import com.alttd.util.Logger;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;

import java.util.Arrays;

public class ConsoleActivity extends ConsoleCommand {
    private final JDA jda;

    public ConsoleActivity(JDA jda) {
        super();
        this.jda = jda;
    }

    @Override
    public String getName() {
        return "activity";
    }

    @Override
    public void execute(String command, String[] args) {
        if (args.length == 1) {
            Activity activity = jda.getPresence().getActivity();
            if (activity == null)
                Logger.info("No activity found.");
            else
                Logger.info("Current activity: Listening to " + activity.getName());
            return;
        }
        String newActivity = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        SettingsConfig.setActivity(newActivity);
        jda.getPresence().setActivity(Activity.listening(newActivity));
        Logger.info("Set activity to: Listening to " + newActivity);
    }

    @Override
    public String getHelpMessage() {
        return "activity [set] [text] Display the current activity or set it.";
    }
}

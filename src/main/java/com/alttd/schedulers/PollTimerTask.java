package com.alttd.schedulers;

import com.alttd.AltitudeLogs;
import com.alttd.commandManager.commands.PollCommand.PollUtil;
import com.alttd.database.queries.Poll.Poll;
import net.dv8tion.jda.api.JDA;

import java.util.Collection;
import java.util.TimerTask;

public class PollTimerTask extends TimerTask {

    private final JDA jda;
    private final AltitudeLogs logger;

    public PollTimerTask(JDA jda, AltitudeLogs logger) {
        this.jda = jda;
        this.logger = logger;
    }

    @Override
    public void run() {
        Collection<Poll> polls = Poll.getAllPolls();
        for (Poll poll : polls) {
            if (poll.needsUpdate()) {
                PollUtil.updatePoll(jda, logger, poll);
            }
        }
    }
}

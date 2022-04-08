package com.alttd.commandManager.commands.PollCommand;

import java.util.HashMap;

public class Polls {

    private static Polls instance = null;
    private HashMap<Long, PollData> pollDataMap;

    private Polls() {
        pollDataMap = new HashMap<>();
        //TODO load poll data
    }

    public static Polls getInstance() {
        if (instance == null)
            instance = new Polls();
        return instance;
    }

    public boolean addPoll(long pollId, PollData pollData) {
        if (pollDataMap.containsKey(pollId))
            return (false);
        pollDataMap.put(pollId, pollData);
        return true;
    }

    public PollData getPoll(long pollId) {
        return pollDataMap.getOrDefault(pollId, null);
    }

}

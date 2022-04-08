package com.alttd.commandManager.commands.PollCommand;

import java.util.HashMap;
import java.util.UUID;

public class PollData {
    private boolean enabled;
    private final int pollId;
    private final HashMap<UUID, ButtonData> buttons;

    public PollData(int pollId) {
        enabled = false;
        this.pollId = pollId;
        this.buttons = new HashMap<>();
    }

    public PollData(boolean enabled, int pollId, HashMap<UUID, ButtonData> buttons) {
        this.enabled = enabled;
        this.pollId = pollId;
        this.buttons = buttons;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPollId() {
        return pollId;
    }

    public ButtonData getButtonData(UUID uuid) {
        return buttons.getOrDefault(uuid, null);
    }

    public boolean addButtonData(UUID uuid, ButtonData buttonData) {
        if (buttons.containsKey(uuid))
            return false;
        buttons.put(uuid, buttonData);
        return true;
    }
}

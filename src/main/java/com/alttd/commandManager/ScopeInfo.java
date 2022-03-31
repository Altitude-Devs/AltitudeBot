package com.alttd.commandManager;

public class ScopeInfo {

    CommandScope scope;
    long id;

    public ScopeInfo(CommandScope scope, long id) {
        this.scope = scope;
        this.id = id;
    }

    public CommandScope getScope() {
        return scope;
    }

    public long getId() {
        return id;
    }
}

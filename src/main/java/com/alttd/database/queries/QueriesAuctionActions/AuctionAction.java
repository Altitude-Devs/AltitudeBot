package com.alttd.database.queries.QueriesAuctionActions;

import org.jetbrains.annotations.NotNull;

public record AuctionAction(AuctionType auctionType, long userId, long messageId, int price, long time) implements Comparable<AuctionAction> {

    @Override
    public int compareTo(@NotNull AuctionAction auctionAction) {
        return Long.compare(this.time(), auctionAction.time());
    }
}

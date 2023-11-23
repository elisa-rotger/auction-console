package com.weareadaptive.auctionhouse.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static com.weareadaptive.auctionhouse.TestData.*;
import static org.junit.jupiter.api.Assertions.*;

public class AuctionStateTest {
    private AuctionState state;

    @BeforeEach
    public void initState() {
        state = new AuctionState();
        Stream.of(
                // USER 1 - AAPL and FB
                AUCTION1,
                AUCTION2,
                // USER 2 - AAPL and EBAY
                AUCTION3,
                AUCTION4
        ).forEach(u -> state.add(u));
        state.setNextId(AUCTION4.getId());
    }


    @Test
    @DisplayName("findAuctionsByOwner should return the correct auctions")
    public void shouldFindAuctionsByOwner() {
        // Returns correct auction stream
        var auctionsUser1 = state.findAuctionsByOwner(USER1.getUsername());
        Auction[] testAuctionsUser1 = {AUCTION1, AUCTION2};
        assertArrayEquals(auctionsUser1.toArray(), testAuctionsUser1);

        var auctionsUser2 = state.findAuctionsByOwner(USER2.getUsername());
        Auction[] testAuctionsUser2 = {AUCTION3, AUCTION4};
        assertArrayEquals(auctionsUser2.toArray(), testAuctionsUser2);

        var auctionsUser3 = state.findAuctionsByOwner(USER3.getUsername());
        assertEquals(auctionsUser3.toArray().length, 0);
    }

    @Test
    @DisplayName("findWonBids should return the owner's won bids")
    public void shouldFindWonBids() {
        var owner = USER2.getUsername();
        // Valid bids
        AUCTION1.bid(owner, 26, 3);
        AUCTION2.bid(owner, 46, 2);

        // Close auctions
        AUCTION1.closeAuction();
        AUCTION2.closeAuction();

        var wonBids = state.findWonBids(owner);

        System.out.println(wonBids);
        assertEquals(wonBids.toArray().length, 2);
        assertEquals(wonBids.get(0).qtyWon(), 3);
    }
}

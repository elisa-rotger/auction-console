package com.weareadaptive.auctionhouse.console;

import com.weareadaptive.auctionhouse.model.Auction;
import com.weareadaptive.auctionhouse.model.AuctionSummary;
import com.weareadaptive.auctionhouse.model.BusinessException;

import java.awt.*;
import java.util.Arrays;

public class AuctionMenu extends ConsoleMenu {
    @Override
    public void display(MenuContext context) {
        createMenu(
                context,
                option("Create new auction", this::createAuction),
                option("Close auction", this::closeAuction),
                option("View my auctions", this::listOwnAuctions),
                option("Bid on an auction", this::createBid),
                option("View bids won", this::viewWonBids),
                // TODO: Lost bids
                option("View bids lost", this::testFn),
                leave("Go Back")
        );
    }

    private void testFn(MenuContext context) {
        context.getOut().println("Test function");
    }
    private void createAuction(MenuContext context) {
        var out = context.getOut();
        var scanner = context.getScanner();
        var auctionState = context.getState().auctionState();

        try {
            var owner = context.getCurrentUser().getUsername();

            out.println("Enter the instrument symbol:");
            final var symbol = scanner.nextLine();

            out.println("Enter the available quantity:");
            final var availableQty = Integer.parseInt(scanner.nextLine());

            out.println("Enter the minimum price:");
            final var minPrice = Double.parseDouble(scanner.nextLine());

            var newAuction = new Auction(auctionState.nextId(), owner, symbol, minPrice, availableQty);
            auctionState.add(newAuction);

            out.printf("Auction with id %s has been added. Symbol: %s %n", newAuction.getId(), newAuction.getSymbol());
            pressEnter(context);
        } catch(BusinessException businessException) {
            out.println("Cannot create auction.");
            out.println(businessException.getMessage());
        }

    }
    private void listOwnAuctions(MenuContext context) {
        context.getState()
                .auctionState()
                .findAuctionsByOwner(context.getCurrentUser().getUsername())
                .forEach(a -> {
                    context.getOut().printf(
                                "Symbol: %s, Minimum price: %s, Available quantity: %s || %s %n",
                                a.getSymbol(),
                                a.getMinPrice(),
                                a.getAvailableQty(),
                                a.getIsOpen() ? "(Open)" : "(Closed)"
                    );
                    if (a.getIsOpen()) {
                        // If auction is open -> show current open bids
                        displayBidSummary(context, a);
                    } else {
                        // If auction is closed -> show summary
                        displayAuctionSummary(context, a.getAuctionSummary());
                    }
                    });
        pressEnter(context);
    }
    private void createBid(MenuContext context) {
        var out = context.getOut();
        var bidder = context.getCurrentUser().getUsername();

        var auctionOptions = context.getState()
                .auctionState()
                .findOtherAuctions(bidder)
                .stream()
                .map(auction -> option("Symbol: " + auction.getSymbol() +
                                ", Minimum price: " + auction.getMinPrice() +
                                ", Available quantity: " + auction.getAvailableQty(),
                        () -> {
                            try {
                                out.println("Input price (per lot) to bid:");
                                final var price = Double.parseDouble(context.getScanner().nextLine());

                                out.println("Enter the bid quantity:");
                                final var quantity = Integer.parseInt(context.getScanner().nextLine());

                                auction.bid(bidder, price, quantity);

                                out.println("Bid created!");
                                pressEnter(context);
                            } catch(BusinessException businessException) {
                                out.println("Cannot create bid.");
                                out.println(businessException.getMessage());
                            }
                        }))
                .toArray(MenuOption[]::new);
        // Menu with auction options + go back option
        createMenu(context, append(auctionOptions, leave("Go Back")));
    }
    private void closeAuction(MenuContext context) {
        var auctionOptions = context.getState()
                .auctionState()
                .findAuctionsByOwner(context.getCurrentUser().getUsername())
                .stream()
                .map(auction ->
                        option("Auction ID: " + auction.getId() +
                                        ", Symbol: " + auction.getSymbol() +
                                        ", Minimum price: " + auction.getMinPrice() +
                                        ", Available quantity: " + auction.getAvailableQty()
                                , () -> {
                                    context.getOut().println("Closing auction...");
                                    auction.closeAuction();
                                    displayAuctionSummary(context, auction.getAuctionSummary());
                                }
                        ))
                .toArray(MenuOption[]::new);
        // Menu with auction options + go back option
        createMenu(context, append(auctionOptions, leave("Go Back")));
    }
    private void displayAuctionSummary(MenuContext context, AuctionSummary summary) {
        var out = context.getOut();

        out.println("Summary of closed auction: ---------->");
        out.printf(
                "Total revenue: %s, Sold quantity: %s, Remaining quantity: %s %n",
                summary.totalRevenue(),
                summary.soldQty(),
                summary.remainingQty()
        );
        out.println("List of winning bids: ---------->");
        summary.winningBids().forEach(b ->
                out.printf(
                        "Quantity: %s, Price: %s, Bidder: %s %n",
                        b.amount(),
                        b.originalBid().getPrice(),
                        b.originalBid().getOwner()
                ));
    }

    private void displayBidSummary(MenuContext context, Auction auction) {
        context.getOut().println("List of bids: ---------->");
        auction.getBidList()
                .forEach(b -> context.getOut().printf(
                    "Quantity: %s, Price: %s, Bidder: %s %n",
                    b.getQuantity(),
                    b.getPrice(),
                    b.getOwner()));
    }
    private void viewWonBids(MenuContext context) {
        context.getState()
                .auctionState()
                .findWonBids(context.getCurrentUser().getUsername())
                .forEach(bidWon -> context.getOut()
                        .printf("Auction ID: %s, Symbol: %s, Quantity bought: %s, Price: %s %n",
                                bidWon.AuctionId(),
                                bidWon.symbol(),
                                bidWon.qtyWon(),
                                bidWon.pricePerLot()
                            )
                );

    }
}

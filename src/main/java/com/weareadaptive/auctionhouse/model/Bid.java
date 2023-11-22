package com.weareadaptive.auctionhouse.model;

import static com.weareadaptive.auctionhouse.IntUtil.isValidQty;
import static com.weareadaptive.auctionhouse.StringUtil.isNullOrEmpty;

public class Bid implements Model {
    private final int id;
    private final double price;
    private final int quantity;
    private final long submissionTime;

    public int getId() {
        return id;
    }

    public Bid(int id, double price, int quantity) {
        if (!isValidQty(price)) {
            throw new BusinessException("price needs to be higher than 0");
        }

        if (!isValidQty(quantity)) {
            throw new BusinessException("quantity needs to be higher than 0");
        }

        this.id = id;
        this.price = price;
        this.quantity = quantity;
        // Save time the bid was created on
        this.submissionTime = System.currentTimeMillis();
    }

    public double getPrice() { return price; }

    public int getQuantity() { return quantity; }

    public long getSubmissionTime() { return submissionTime; }
}
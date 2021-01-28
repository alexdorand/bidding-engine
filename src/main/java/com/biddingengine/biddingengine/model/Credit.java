package com.biddingengine.biddingengine.model;

import lombok.Data;

@Data
public class Credit {

    private String userId;
    private float amount;

    public boolean canSpend(float amount) {
        return this.amount >= (this.amount-amount);
    }

    public void debit(float amount) {
        this.amount = this.amount - amount;
    }

    public void credit(float amount) {
        this.amount = this.amount + amount;
    }

}

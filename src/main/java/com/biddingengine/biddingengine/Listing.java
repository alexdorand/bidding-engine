package com.biddingengine.biddingengine;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Listing {

    String id;
    String winner;
    String runnerUp;
    float amount = 0;
    float nextBidAmount;
    float minAddition;

    public Listing(String id, float amount, float minAddition) {
        this.id = id;
        this.amount = amount;
        this.minAddition = minAddition;
        this.nextBidAmount = this.amount + minAddition;
    }
}

package com.biddingengine.biddingengine;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Bid {

    long start;
    long end;

    String itemId;
    float amount;
    String userId;
    String id;


    public Bid(String id, String userId, String itemId, float amount) {
        this.id = id;
        this.itemId = itemId;
        this.amount = amount;
        this.userId = userId;
    }

    long duration() {
        return start -end;
    }
}

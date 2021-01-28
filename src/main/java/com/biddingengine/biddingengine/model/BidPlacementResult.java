package com.biddingengine.biddingengine.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BidPlacementResult {

    private boolean ableToBid;
    private List<String> errors;

    public BidPlacementResult() {
        errors = new ArrayList<>();
    }
}

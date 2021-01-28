package com.biddingengine.biddingengine.model;

import com.biddingengine.biddingengine.Bid;
import com.biddingengine.biddingengine.Listing;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.subjects.ReplaySubject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InMemoryItemBids {

    private Listing listing;
    private Bid winningBid;
    private Bid runnerUp;
    private ReplaySubject<Bid> bidsStream;
    private List<Bid> rejectedBids;

    public static InMemoryItemBids of(Listing listing, Consumer<Bid> consumer) {
        ReplaySubject<Bid> bidsStream = ReplaySubject.create();
        bidsStream.subscribe(consumer);
        return new InMemoryItemBids(listing, null, null, bidsStream, new ArrayList<>());
    }

    public BidPlacementResult evaluate(Bid bid) {
        BidPlacementResult result = new BidPlacementResult();
        result.setAbleToBid(winningBid != null ? listing.getNextBidAmount() + listing.getMinAddition() <= bid.getAmount() : true);
        if(bidsStream.hasComplete()) {
            result.setAbleToBid(false);
            result.getErrors().add("Auction is closed");
        }

        return result;
    }

    public boolean hasNoWinningBid() {
        return winningBid==null;
    }

    public void attemptPlacingBid(Bid bid) {
        bidsStream.onNext(bid);
    }

    public void acceptBid(Bid bid) {
        runnerUp = winningBid;
        winningBid = bid;
        listing.setNextBidAmount(winningBid.getAmount());
    }

    public void recordRejectedBid(Bid bid) {
        rejectedBids.add(bid);
    }

}

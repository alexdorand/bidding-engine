package com.biddingengine.biddingengine;

import com.biddingengine.biddingengine.model.InMemoryItemBids;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class RxTest {

//    Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
//    Bucket bidsBucket = cluster.bucket("bids");
//    Collection bidsCollection = bidsBucket.defaultCollection();

//    Bucket itemsBucket = cluster.bucket("items");
//    Collection itemsCollection = itemsBucket.defaultCollection();

    ConcurrentHashMap<String, InMemoryItemBids> itemsSubjects = new ConcurrentHashMap<>();
    BehaviorSubject<InMemoryItemBids> failedResponseSubject = BehaviorSubject.create();
    BehaviorSubject<InMemoryItemBids> successResponseSubject = BehaviorSubject.create();

    static AtomicInteger failed = new AtomicInteger(0);
    static AtomicInteger success = new AtomicInteger(0);

    //    @SneakyThrows
    public static void main(String[] args) {


        RxTest rxTest = new RxTest();
        long start = System.currentTimeMillis();
        rxTest.loadItemIntoDatabase();
        long end = System.currentTimeMillis();
        System.out.println("load into memory duration:" + (end - start));

        long s1 = System.currentTimeMillis();
        rxTest.flowBids();
        long e1 = System.currentTimeMillis();
        System.out.println("Rejected bids: " + failed.get() + " accepted bids: " + success.get() + " = " + (failed.get() + success.get()));
        System.out.println(e1 - s1);

        rxTest.getItemsSubjects().keySet()
                .stream()
                .forEach(s -> {
                    rxTest.getItemsSubjects().get(s).getBidsStream().onComplete();
                });

        long e2 = System.currentTimeMillis();
        System.out.println("Concluded the auction: "+(e2-e1));

    }

    public ConcurrentHashMap<String, InMemoryItemBids> getItemsSubjects() {
        return itemsSubjects;
    }

    public Flowable<Boolean> placeBid(String id, float amount) {
        return Flowable.just(true);
    }


    public RxTest() {

        failedResponseSubject.subscribe(inMemoryItemBids -> {
            failed.incrementAndGet();
            System.out.println("Respond Reject: "+inMemoryItemBids.getListing()+"-"+inMemoryItemBids.getWinningBid().getAmount());
//            Flowable
//                    .just(bid)
//                    .flatMap(bidToBeRecorded -> bidsCollection.reactive().upsert("bid-"+bid.getId(), bid))
//                    .subscribe();
        });

        successResponseSubject.subscribe(inMemoryItemBids -> {
            success.incrementAndGet();
//            System.out.println("Respond Success: "+inMemoryItemBids.getListing()+"-"+inMemoryItemBids.getWinningBid().getAmount());
        });

    }

    static int numberOfItems = 5000;

    public void flowBids() {

        long start = System.currentTimeMillis();
        AtomicInteger atomicInteger = new AtomicInteger(0);

        IntStream.range(1, 1000000)
                .forEach(integer -> {

                    int itemSubSection = atomicInteger.incrementAndGet() % 5000;
                    String itemId = Integer.toString( itemSubSection == 0? 1: itemSubSection);
                    Bid bid = new Bid(UUID.randomUUID().toString(), UUID.randomUUID().toString(), itemId, integer * 10);

                    if (itemsSubjects.get(bid.itemId) != null) {
                        itemsSubjects.get(bid.itemId).attemptPlacingBid(bid);
                    }

                });

        long end = System.currentTimeMillis();
        System.out.println("bids total: " + (end - start));

    }

    public void loadItemIntoDatabase() {

        long start = System.currentTimeMillis();

        IntStream.range(1, numberOfItems)
                .forEach(v -> {
                    String id = Integer.toString(v % 5000);
                    itemsSubjects.put(id, InMemoryItemBids.of(new Listing(id, v * 10, 100),
                            bid -> {
                                InMemoryItemBids inMemoryItem = itemsSubjects.get(bid.getItemId());
                                if(inMemoryItem.hasNoWinningBid()) {
                                    inMemoryItem.setWinningBid(bid);
                                    successResponseSubject.onNext(inMemoryItem);
                                } else {
                                    if(inMemoryItem.evaluate(bid).isAbleToBid()) {
                                        inMemoryItem.acceptBid(bid);
                                        successResponseSubject.onNext(inMemoryItem);
                                    } else {
                                        inMemoryItem.recordRejectedBid(bid);
                                        failedResponseSubject.onNext(inMemoryItem);
                                    }
                                }
                            }));
                });


        long end = System.currentTimeMillis();
        System.out.println("Time taken to load all items: " + (end - start));


    }


}

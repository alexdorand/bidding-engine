package com.biddingengine.biddingengine;

import com.biddingengine.biddingengine.model.Credit;
import com.couchbase.client.core.msg.kv.MutationToken;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.ReactiveCollection;
import com.couchbase.client.java.kv.MutationResult;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BiddingEngineTest {

    public static final String USER_CREDIT = "user::credit::";
    Cluster cluster = Cluster.connect("127.0.0.1", "Administrator", "password");
    Bucket userBucket = cluster.bucket("user");
    ReactiveCollection userCollection = userBucket.defaultCollection().reactive();

    Bucket itemBucket = cluster.bucket("item");
    ReactiveCollection itemCollection = userBucket.defaultCollection().reactive();
    ConcurrentHashMap<String, Listing> inMemoryItems = new ConcurrentHashMap<>();

    public static void main(String[] args) {

        List<String> ids = new ArrayList<>();
        ids.add("1");
        ids.add("2");
        ids.add("3");
        ids.add("4");
        ids.add("5");

        List<Listing> itemsToBeWritten = new ArrayList<>();
        for(int i=1;i<=5000;i++) {
            itemsToBeWritten.add(new Listing(""+i, i*10, 100));
        }

        BiddingEngineTest engineTest = new BiddingEngineTest();
        engineTest.bulkWriteItems(itemsToBeWritten)
            .blockingSubscribe();



    }

    public Flowable<Mono<MutationResult>> bulkWriteItems(List<Listing> listings) {
        return Flowable
                .fromStream(listings.stream())
                .parallel()
                .runOn(Schedulers.io())
                .map(listing -> itemCollection.upsert("item::"+ listing.getId(), listing))
                .sequential();
    }

    public Flowable<Listing> getItems(List<String> itemIds) {
        return Flowable.fromArray(itemIds)
                .parallel()
                .runOn(Schedulers.io())
                .flatMap(itemIdToBeRetrieved -> itemCollection.get("item::" + itemIdToBeRetrieved))
                .map(getResult -> getResult.contentAs(Listing.class))
                .sequential();
    }

    public Flowable<Listing> getItem(String itemId) {
        return Flowable.just(itemId)
                .flatMap(itemIdToBeRetrieved -> itemCollection.get("item::" + itemIdToBeRetrieved))
                .map(getResult -> getResult.contentAs(Listing.class));
    }

    public Flowable<Credit> loadCredit(String userId) {
        return Flowable.just(userId)
                .flatMap(userIdToBeRetrieved -> userCollection.get(USER_CREDIT + userIdToBeRetrieved))
                .map(getResult -> getResult.contentAs(Credit.class));
    }

    public Flowable<MutationToken> updateCredit(Credit credit) {
        return Flowable.just(credit)
                .flatMap(creditToBeStored -> userCollection.upsert(USER_CREDIT + credit.getUserId(), credit))
                .map(mutationResult -> mutationResult.mutationToken().get());
    }

}

package com.biddingengine.biddingengine;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Pair<F, S> {

    private F first;
    private S seconds;

    static <F, S> Pair<F,S> of(F first, S second) {
        return new Pair<>(first, second);
    }

    @Override
    public String toString() {
        return "Pair{" +
                "seconds=" + seconds +
                '}';
    }
}

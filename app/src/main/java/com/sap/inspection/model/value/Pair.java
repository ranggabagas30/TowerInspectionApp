package com.sap.inspection.model.value;

/**
 * Created by domikado on 11/23/17.
 */

public class Pair<A, B> {
    private A first;
    private B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public void setFirst(A first){
        this.first = first;
    }

    public void setSecond(B second){
        this.second = second;
    }

    public A first() {
        return first;
    }

    public B second(){
        return second;
    }
}

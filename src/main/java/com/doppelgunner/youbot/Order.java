package com.doppelgunner.youbot;

import java.util.EnumSet;

/**
 * Created by robertoguazon on 16/07/2017.
 */
public enum Order {

    RELEVANCE("relevance"),
    DATE("date"),
    RATING("rating"),
    ALPHABETICAL("title"),
    VIEW_COUNT("viewcount"),
    ;

    public static final EnumSet<Order> all = EnumSet.allOf(Order.class);
    private final String order;

    Order(String order) {
        this.order = order;
    }

    public String toString() {
        return name();
    }

    public String value() {
        return order;
    }

    public boolean isSame(String order) {
        return this.order.equals(order);
    }

    public boolean isSame(Order order) {
        return isSame(order.value());
    }

    public boolean sameName(String name) {
        return name().equals(name);
    }

}

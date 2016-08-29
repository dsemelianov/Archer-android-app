package com.archer.android;

/**
 * Created by davidemelianov on 1/22/16.
 */
public class SponsoredListItem {
    public String name;
    public String description;

    public SponsoredListItem(String title) {
        this(title, "bla bla");
    }

    public SponsoredListItem(String title, String description) {
        this.name = title;
        this.description = description;
    }
}
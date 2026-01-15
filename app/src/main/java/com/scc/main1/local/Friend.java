package com.scc.main1.local;

public class Friend {
    private String email;

    public Friend() {
        // Required empty public constructor for Firestore
    }

    public Friend(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

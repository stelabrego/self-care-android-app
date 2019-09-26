package com.example.finalstelabrego;

// This is a representation of one mental health entry a user creates in the app.

public class Entry {
    public int mood;
    public String feelings;
    public String future;
    public boolean breathing;
    public String date;

    public Entry(String date, String future, String feelings, boolean breathing, int mood) {
        this.date = date;
        this.mood = mood;
        this.feelings = feelings;
        this.future = future;
        this.breathing = breathing;
    }
}

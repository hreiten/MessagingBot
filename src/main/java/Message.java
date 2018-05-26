package main.java;

import java.util.Date;
import java.util.List;

public class Message {
    public String content;
    public List<String> categories;
    public boolean isSent;     // boolean stating whether the message has been sent or not
    public Date dateValidFrom;     // sets the date that the message can be sent from
    public Date dateValidTo;     // sets the date that the message has to be sent before

    public Message(String content, List<String> categories, Date dateValidFrom, Date dateValidTo) {
        this.content = content;
        this.categories = categories;
        this.isSent = false;
        this.dateValidFrom = dateValidFrom;
        this.dateValidTo = dateValidTo;
    }

    public boolean hasBeenSent() {
        return isSent;
    }

    public void setSentStatus(boolean bol) {
        if (bol) {
            this.isSent = true;
        }
    }

    public String getContent() {
        return content;
    }

    public Date getFromDate() {
        return dateValidFrom;
    }

    public Date getToDate() {
        return dateValidTo;
    }

    public List<String> getCategories() {
        return categories;
    }

    @Override
    public String toString() {
        return "Content: " + getContent() +
               "; Categories: " + getCategories() + "\n";
    }
}

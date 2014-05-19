
package com.zolomon.simplechat.shared;

public class Message {
    public String author;
    public String line;
    public long epochTimeStamp;

    public Message(String author, String line, long epochTimeStamp) {
        this.author = author;
        this.line = line;
        this.epochTimeStamp = epochTimeStamp;
    }

}

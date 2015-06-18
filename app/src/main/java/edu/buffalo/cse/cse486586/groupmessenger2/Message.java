package edu.buffalo.cse.cse486586.groupmessenger2; /**
 * Created by seongsu on 2015-02-28.
 */

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by seongsu on 2015-02-16.
 */
public class Message implements Serializable{
    //Referenced http://www.drdobbs.com/jvm/increase-java-serialization-performance/240159166?pgno=1
    private static final long serialVersionUID = 1L;
    public static final int MESSAGE_TYPE_USER = 1;
    public static final int MESSAGE_TYPE_QUIT = 2;
    private String text ="a";
    private String myPort ="a";
    private int seq = -1;
    private int identifier = 0;
    Date currentDate = new Date();
    Timestamp timestamp = new Timestamp(currentDate.getTime());

    public void setSeq(int id) {
        this.seq = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getSeq() {
        return seq;
    }

    public Message() {
    }

//    public Message(String myPort, String text) {
//        this.text = text;
//        this.myPort = myPort;
//    }
public Message(String myPort, String text) {
    this.text = text;
    this.myPort = myPort;
}
    public Message(String myPort, String text,Timestamp timestamp) {
        this.text = text;
        this.myPort = myPort;
        this.timestamp = timestamp;
    }
    public int getIdentifier() {
        return identifier;
    }

    public void setIdentifier(int identifier) {

        this.identifier = identifier;
    }

    public void setMyPort(String myPort) {
        this.myPort = myPort;
    }

    public String getMyPort() {
        return myPort;
    }



    public String getText() {
        return text;
    }

}

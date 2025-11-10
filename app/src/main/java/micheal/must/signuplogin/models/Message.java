package micheal.must.signuplogin.models;

public class Message {
    public static final int SENT_BY_USER = 0;
    public static final int SENT_BY_AI = 1;

    private final String content;
    private final int sentBy;

    public Message(String content, int sentBy) {
        this.content = content;
        this.sentBy = sentBy;
    }

    public String getContent() {
        return content;
    }

    public int getSentBy() {
        return sentBy;
    }
}

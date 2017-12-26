public class Tweet {
    String tweet;
    User sender;
    Long timestamp;

    public Tweet(String tweet, User sender, Long timestamp) {
        this.tweet = tweet;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Tweet{" +
                "tweet='" + tweet + '\'' +
                ", sender=" + sender.toString() +
                ", timestamp=" + timestamp +
                '}';
    }

    public String getTweet() {
        return tweet;
    }

    public void setTweet(String tweet) {
        this.tweet = tweet;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

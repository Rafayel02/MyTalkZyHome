package com.app.talkzy.Stories;

public class Story {

    private String imageURL;
    private long timeStart;
    private long timeEnd;
    private String storyId;
    private String userId;

    public Story(String imageURL, long timeStart, long timeEnd, String storyId, String userId) {
        this.imageURL = imageURL;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.storyId = storyId;
        this.userId = userId;
    }

    public Story() {
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(long timeStart) {
        this.timeStart = timeStart;
    }

    public long getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(long timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

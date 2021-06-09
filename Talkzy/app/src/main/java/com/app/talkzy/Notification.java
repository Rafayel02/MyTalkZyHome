package com.app.talkzy;

public class Notification {

    private long id;
    private String fromUserId;
    private String notificationType;
    private boolean isSeen;
    private long notificationTime;

    public long getId() { return id; }

    public String getFromUserId() { return fromUserId; }

    public String getNotificationType() { return notificationType; }

    public boolean getIsSeen() { return isSeen; }

    public long getNotificationTime() { return notificationTime; }

    public void setId(long id) { this.id = id; }

    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    public void setNotificationType(String notificationType) { this.notificationType = notificationType; }

    public void setIsSeen(boolean seen) { isSeen = seen; }

    public void setNotificationTime(long notificationTime) { this.notificationTime = notificationTime; }

}

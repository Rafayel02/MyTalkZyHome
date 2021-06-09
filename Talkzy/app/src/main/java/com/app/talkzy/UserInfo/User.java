package com.app.talkzy.UserInfo;

public class User {

    private String id;
    private String fullName;
    private String userName;
    private String email;
    private String password;
    private String imageUri;
    private String birthday;
    private String status;
    private boolean isVerified;
    private Long lastSeen;
    private String typingTo;
    private int friendsCount;
    private int convosCount;
    private String bioText;
    private String gender;
    private String country;
    private String continent;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getConvosCount() {
        return convosCount;
    }

    public void setConvosCount(int convosCount) {
        this.convosCount = convosCount;
    }

    public String getBioText() {
        return bioText;
    }

    public void setBioText(String bioText) {
        this.bioText = bioText;
    }

    public boolean getIsVerified() {
        return isVerified;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public String getTypingTo() { return typingTo; }

    public Long getLastSeen() { return lastSeen; }

    public String getStatus() { return status; }

    public String getBirthday() {
        return this.birthday;
    }

    public String getFullName() {
        return this.fullName;
    }

    public String getId() { return this.id; }

    public String getUserName() {
        return this.userName;
    }

    public String getEmail() {
        return this.email;
    }

    public String tGetPassword() {
        return this.password;
    }

    public String getImageUri() {
        return this.imageUri;
    }

    public String tGetFirstName() {
        String[] words = fullName.split("\\s+");
        return  words[0];
    }

    public String tGetLastName() {
        String firstName = tGetFirstName();
        return  fullName.replace(firstName, "");
    }

    public void setIsVerified(boolean verified) { isVerified = verified; }

    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }

    public void setTypingTo(String typingTo) { this.typingTo = typingTo; }

    public void setLastSeen(Long lastSeen) { this.lastSeen = lastSeen; }

    public void setStatus(String status) { this.status = status; }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setId(String id) {this.id = id;}

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public void setImageUri(String imageUri) {
        if(!imageUri.equals("default")){
            this.imageUri = String.valueOf(imageUri).replace("s96-c","s240-c");

        }
        else{
            this.imageUri = imageUri;
        }
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

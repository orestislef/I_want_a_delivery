package com.revinad.iwantadelivery.Utills;

public class Posts {

    private String idOfUser, postDate, postDesc, userProfileImageUrl, username, onMyWayUsername, completedDate, street;
    private Boolean onMyWay, completed;

    public Posts() {
    }

    public Posts(String idOfUser, String postDate, String postDesc, String userProfileImageUrl, String username, String onMyWayUsername, String completedDate, Boolean onMyWay, Boolean completed, String street) {
        this.idOfUser = idOfUser;
        this.postDate = postDate;
        this.postDesc = postDesc;
        this.userProfileImageUrl = userProfileImageUrl;
        this.username = username;
        this.onMyWayUsername = onMyWayUsername;
        this.completedDate = completedDate;
        this.onMyWay = onMyWay;
        this.completed = completed;
        this.street = street;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getIdOfUser() {
        return idOfUser;
    }

    public void setIdOfUser(String idOfUser) {
        this.idOfUser = idOfUser;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public String getPostDesc() {
        return postDesc;
    }

    public void setPostDesc(String postDesc) {
        this.postDesc = postDesc;
    }

    public String getUserProfileImageUrl() {
        return userProfileImageUrl;
    }

    public void setUserProfileImageUrl(String userProfileImageUrl) {
        this.userProfileImageUrl = userProfileImageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOnMyWayUsername() {
        return onMyWayUsername;
    }

    public void setOnMyWayUsername(String onMyWayUsername) {
        this.onMyWayUsername = onMyWayUsername;
    }

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
    }

    public Boolean getOnMyWay() {
        return onMyWay;
    }

    public void setOnMyWay(Boolean onMyWay) {
        this.onMyWay = onMyWay;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
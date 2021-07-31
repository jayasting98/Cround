package com.cround.cround.api;

import java.util.Date;

public class UserDetails {
    private String uid;
    private boolean isDeleted;
    private String username;
    private String email;
    private String displayName;
    private String description;
    private Date birthday;
    private String displayObjectId;
    private String headerDisplayObjectId;
    private Date timestampCreated;
    private long points;
    private long kiloPoints;
    private long followerCount;
    private long followingCount;
    private long communityCount;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getDisplayObjectId() {
        return displayObjectId;
    }

    public void setDisplayObjectId(String displayObjectId) {
        this.displayObjectId = displayObjectId;
    }

    public String getHeaderDisplayObjectId() {
        return headerDisplayObjectId;
    }

    public void setHeaderDisplayObjectId(String headerDisplayObjectId) {
        this.headerDisplayObjectId = headerDisplayObjectId;
    }

    public Date getTimestampCreated() {
        return timestampCreated;
    }

    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public long getKiloPoints() {
        return kiloPoints;
    }

    public void setKiloPoints(long kiloPoints) {
        this.kiloPoints = kiloPoints;
    }

    public long getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(long followerCount) {
        this.followerCount = followerCount;
    }

    public long getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(long followingCount) {
        this.followingCount = followingCount;
    }

    public long getCommunityCount() {
        return communityCount;
    }

    public void setCommunityCount(long communityCount) {
        this.communityCount = communityCount;
    }
}

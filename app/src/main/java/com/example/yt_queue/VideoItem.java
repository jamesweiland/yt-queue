package com.example.yt_queue;

public class VideoItem {
    private final String title;
    private final String channelName;
    private final String thumbnailUrl;
    private final String videoId;

    public VideoItem(String title, String channelName, String thumbnailUrl, String videoId) {
        this.title = title;
        this.channelName = channelName;
        this.thumbnailUrl = thumbnailUrl;
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getVideoId() {
        return videoId;
    }
}

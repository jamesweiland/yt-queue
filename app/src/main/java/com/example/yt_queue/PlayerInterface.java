package com.example.yt_queue;

import android.webkit.JavascriptInterface;

public class PlayerInterface {
    MainActivity mainActivity;

    PlayerInterface(MainActivity activity) {
        this.mainActivity = activity;
    }

    @JavascriptInterface
    public void onVideoStarted() {
        mainActivity.runOnUiThread(() -> {
            mainActivity.onVideoStarted();
        });
    }

    @JavascriptInterface
    public void onVideoEnded() {
        // js will call this method when a video ends
        // it will call from a background thread, so
        // post to main thread
        mainActivity.runOnUiThread(() -> {
            mainActivity.onNextVideo();
        });
    }

    @JavascriptInterface
    public void onVideoError(int errorCode) {
        // js will call this when it has an error that it doesn't know what to do about
        // for now, lets just play the next video
        mainActivity.runOnUiThread(() -> {
            mainActivity.onNextVideo();
        });
    }
}

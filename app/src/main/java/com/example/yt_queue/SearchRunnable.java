package com.example.yt_queue;

import android.os.Looper;
import android.os.Handler;

import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchRunnable implements Runnable {
    private String query;
    private SearchResultAdapter videoAdapter;
    private YoutubeSearchService service;
    // SearchRunnable runs on a background thread, so need to set up a handler so
    // it can update the UI on the main thread
    private Handler mainHandler;

    void setSearchQuery(String q) { query = q; }

    public SearchRunnable(SearchResultAdapter adapter)
    {
        videoAdapter = adapter;
        service = new YoutubeSearchService();
        // set up the handler to communicate with main thread
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void run() {

        try {
            // search youtube for the query and update the UI with the results
            List<VideoItem> videos = service.searchVideos(query);
            mainHandler.post(() -> {
                updateResults(videos);
            });
        } catch (IOException e) {
            // in case of an error, show it on the UI
            mainHandler.post(() -> {showError(e);});
        }
    }

    private void updateResults(List<VideoItem> videos) {
        if (videoAdapter != null) {
            videoAdapter.addAll(videos);
        }
    }

    private void showError(IOException e) {
        System.out.println(e.toString());
        videoAdapter.clear();
        VideoItem error = new VideoItem("Search failed", "", "", "");
        List<VideoItem> errorList = new ArrayList<>();
        errorList.add(error);
        videoAdapter.addAll(errorList);
    }
}

package com.example.yt_queue;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class YoutubeSearchService {
    private static final String API_KEY = Env.youtubeApiKey;
    private static final String BASE_URL = "https://www.googleapis.com/youtube/v3";
    private static final int MAX_RESULTS = 25;
    private OkHttpClient client = new OkHttpClient();
    private Gson gson = new Gson();

    private static class YoutubeSearchResponse {
        // response class for JSON parsing
        List<Item> items;
        static class Item {
            Id id;
            Snippet snippet;

            static class Id {
                String videoId;
            }

            static class Snippet {
                String title;
                String channelTitle;
                Thumbnails thumbnails;

                static class Thumbnails {
                    Thumbnail medium;

                    static class Thumbnail {
                        String url;
                    }
                }
            }
        }
    }

    public List<VideoItem> searchVideos(String query) throws IOException {
        String url = BASE_URL + "/search?part=snippet&type=video&q=" +
                query.replace(" ", "+") +
                "&maxResults=" + MAX_RESULTS +
                "&key=" + API_KEY;

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(("YouTube API search failed: " + response));
            }

            assert response.body() != null;
            String jsonResponse = response.body().string();
            YoutubeSearchResponse searchResponse = gson.fromJson(jsonResponse, YoutubeSearchResponse.class);

            return convertToVideoItems(searchResponse);
        }
    }

    private List<VideoItem> convertToVideoItems(YoutubeSearchResponse response) {
        List<VideoItem> videos = new ArrayList<>();
        if (response.items != null) {
            for (YoutubeSearchResponse.Item item : response.items) {
                VideoItem video = new VideoItem(
                        item.snippet.title,
                        item.snippet.channelTitle,
                        item.snippet.thumbnails.medium.url,
                        item.id.videoId
                );
                videos.add(video);
            }
        }
        return videos;
    }
}

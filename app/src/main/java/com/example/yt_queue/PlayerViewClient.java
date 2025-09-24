package com.example.yt_queue;

import android.content.Context;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class PlayerViewClient extends WebViewClient {
    private Context context;
    private static final Set<String> AD_HOSTS = new HashSet<>();

    private YoutubePlayerHandler handler;

    static {
        AD_HOSTS.add("googleads.g.doubleclick.net");
        AD_HOSTS.add("adservice.google.com");
        AD_HOSTS.add("pagead2.googlesyndication.com");
        AD_HOSTS.add("securepubads.g.doubleclick.net");
    }

    public interface YoutubePlayerHandler {
        public ValueCallback<String> onVideoStarted();
    }

    public void setYoutubePlayerHandler(YoutubePlayerHandler handler) {
        this.handler = handler;
    }

    public PlayerViewClient(Context context) {
        this.context = context;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (url != null && (url.startsWith("https://m.youtube.com/watch?v=") || url.startsWith("https://www.youtube.com/watch?v="))) {
            System.out.println("Finished loading url " + url);

            // inject javascript
            String autoplayScript = (
                    "console.log('in the script');" +
                    "document.addEventListener('keydown', (event) => {" +
                            "if (event.key === 'f') {" +
                                "const player = document.querySelector('#movie_player');" +
                                "player.requestFullscreen();" +
                            "}" +
                        "}" +
                    ");" +
                    "function waitForPlayerReady() {" +
                        "const observer = new MutationObserver((mutations, observer) => {" +
                                "console.log('checking for player');" +
                                "const player = document.querySelector('#movie_player > div.html5-video-container > video');" +
                                "if (player) {" +
                                    "console.log('player found');" +
                                    "observer.disconnect();" +
                                    "if (player.paused) {" +
                                        "console.log('playing');" +
                                        "player.click();" +
                                    "}" +
                                "}" +
                        "});" +
                        "observer.observe(document.body, {" +
                                "childList: true," +
                                "subtree: true," +
                        "});" +
                    "}" +
                    "waitForPlayerReady();"
            );

            view.evaluateJavascript(autoplayScript, handler.onVideoStarted());
        }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        String host = request.getUrl().getHost();

        // check if host is a known ad host
        if (host != null) {
            for (String adHost : AD_HOSTS) {
                if (host.contains(adHost)) {
                    // block the ad by returning an empty response
                    System.out.println("Blocked navigation to " + url);
                    return new WebResourceResponse("text/plain", "utf-8", null);
                }
            }
        }

        // otherwise, proceed normally
        System.out.println("Allowing navigation to " + url);
        return super.shouldInterceptRequest(view, request);
    }
}



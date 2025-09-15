package com.example.yt_queue;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewParent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Main Activity class that loads {@link MainFragment}.
 */
//public class MainActivity extends FragmentActivity {
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.search_fragment_container, new SearchSupportFragment())
//                    .commitNow();
//        }
//    }
//}

public class MainActivity extends FragmentActivity implements
        KeyboardFragment.KeyboardListener,
        SearchResultAdapter.SearchResultButtonListener,
        QueueAdapter.QueueListener {

    // search variables
    private static final int SEARCH_DELAY_MS = 300;
    private RecyclerView searchResultsRecyclerView;
    private KeyboardContainer keyboardContainer;
    private EditText searchBar;
    private SearchResultAdapter searchResultAdapter;
    private SearchRunnable mDelayedLoad;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mHandler = new Handler();
    private StringBuilder searchText = new StringBuilder();

    // queue variables
    private QueueAdapter queueAdapter;
    private RecyclerView queueRecyclerView;

    // playback variables
    private WebView webView;

    // keyboard
    private KeyboardFragment keyboard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setup();

        // addToQueue keyboard fragment and set the video search fragment as the listener
        keyboard = new KeyboardFragment();
        keyboard.setKeyboardListener(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.keyboard_container, keyboard)
                .hide(keyboard)
                .commit();
    }

    private void setup() {
        setupViews();
        // setup search
        setupSearchResultRecyclerView();
        setupSearchBarListeners();
        setupSearchRunnable();

        // setup queue
        setupQueueAdapter();

        // setup playback
    }
    private void setupViews() {
        // search
        searchBar = findViewById(R.id.search_bar);
        keyboardContainer = findViewById(R.id.keyboard_container);
        searchResultsRecyclerView = findViewById(R.id.search_results);
        // recyclerview hardcodes focusable == true in the constructor, so explicitly set it here
        searchResultsRecyclerView.setFocusable(false);
        searchResultsRecyclerView.setFocusableInTouchMode(false);
        searchResultsRecyclerView.setClickable(false);
        System.out.println(searchResultsRecyclerView.isFocusable());
        searchBar.setShowSoftInputOnFocus(false);



        // queue
        queueRecyclerView = findViewById(R.id.queue);
        // same thing as search result recycler view
        queueRecyclerView.setFocusable(false);
        queueRecyclerView.setFocusableInTouchMode(false);
        queueRecyclerView.setClickable(false);


        // playback
        webView = findViewById(R.id.video_playback);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); // enable js
        webView.addJavascriptInterface(new PlayerInterface(this), "Android"); // give interface to player
        settings.setDomStorageEnabled(true);
        // let the webview access the html file
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        // enable autoplay without user clicking on the video
        settings.setMediaPlaybackRequiresUserGesture(false);
        // load the html file with youtube player script
        webView.loadUrl("file:///android_asset/player_container.html");
    }

    private void setupQueueAdapter() {
        queueAdapter = new QueueAdapter(this);
        queueAdapter.setQueueListener(this);

        TVFocusLayoutManager layoutManager = new TVFocusLayoutManager(this);
        queueRecyclerView.setLayoutManager(layoutManager);
        queueRecyclerView.setAdapter(queueAdapter);
    }

    private void setupSearchResultRecyclerView() {
        searchResultAdapter = new SearchResultAdapter(this);
        searchResultAdapter.setOnButtonClickListener(this);

        System.out.println(searchResultsRecyclerView.isFocusable());
        TVFocusLayoutManager layoutManager = new TVFocusLayoutManager(this);
        searchResultsRecyclerView.setLayoutManager(layoutManager);
        searchResultsRecyclerView.setAdapter(searchResultAdapter);
        System.out.println(searchResultsRecyclerView.isFocusable());

    }

    private void setupSearchBarListeners() {
        searchBar.setOnClickListener(v -> {
            if (keyboardHidden()) {
                showKeyboard();
            }

            // set focus to the keyboard
            findViewById(R.id.key_q).requestFocus();
        });
    }

    private void setupSearchRunnable() {
        mDelayedLoad = new SearchRunnable(searchResultAdapter);
    }

    public boolean keyboardHidden() {
        return keyboardContainer.getVisibility() == View.GONE;
    }

    private boolean isViewInKeyboard(View view) {
        if (view == null) {
            return false;
        }
        ViewParent parent = view.getParent();
        while (parent != null) {
            if (parent == keyboardContainer) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public void showKeyboard() {
        keyboardContainer.setVisibility(View.VISIBLE);

        getSupportFragmentManager()
                .beginTransaction()
                .show(keyboard)
                .commit();
    }

    public void hideKeyboard() {
        getSupportFragmentManager()
                .beginTransaction()
                .hide(keyboard)
                .commit();

        keyboardContainer.setVisibility(View.GONE);
    }

    @Override
    public void onKeyPressed(String key) {
        searchText.append(key);
        String query = searchText.toString();
        updateSearchBar(query);
        performSearch(query);
    }

    @Override
    public void onBackspacePressed() {
        if (searchText.length() > 0) {
            searchText.deleteCharAt(searchText.length() - 1);
            String query = searchText.toString();
            updateSearchBar(query);
            performSearch(query);
        }
    }

    @Override
    public void onSpacePressed() {
        searchText.append(' ');
        String query = searchText.toString();
        updateSearchBar(query);
        performSearch(query);
    }

    @Override
    public void onSearchPressed() {
        performSearch(searchText.toString());
        hideKeyboard();
    }

    @Override
    public void onDismissPressed() {
        hideKeyboard();
        searchBar.clearFocus();
    }

    private void updateSearchBar(String query) {
        searchBar.setText(query);
        searchBar.setSelection(searchText.length());
    }

    private void performSearch(String query) {

        searchResultAdapter.clear();
        if (!TextUtils.isEmpty(query)) {
            mDelayedLoad.setSearchQuery(query);
            mHandler.removeCallbacks(mDelayedLoad);
            mHandler.postDelayed(() -> {
                executor.submit(mDelayedLoad);
            }, SEARCH_DELAY_MS);
        }
    }

    public void onNextVideo() {
        VideoItem nextVideo = queueAdapter.getNextVideo();
        if (nextVideo != null) {
            embedVideo(nextVideo.getVideoId());

            queueAdapter.remove(0);
        }
    }

    public void embedVideo(String videoId) {
        // tell javascript api to play this video
        webView.evaluateJavascript("playVideo('" + videoId + "');", null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

    // button listeners for the search results recyclerview
    @Override
    public void onPlayNext(VideoItem video) {
        System.out.println("Play next clicked");
        queueAdapter.playNext(video);
    }

    @Override
    public void onAddToQueue(VideoItem video) {
        System.out.println("Add to queue clicked");
        queueAdapter.addToQueue(video);
    }

    @Override
    public void onVideoAddedToEmptyQueue(VideoItem video) {
        webView.evaluateJavascript("isVideoPlaying();", (playing) -> {
            if (playing.equals("false")) {
                String id = video.getVideoId();
                embedVideo(id);
                queueAdapter.remove(0);
            }
        });
    }
}

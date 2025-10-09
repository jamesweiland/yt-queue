package com.example.yt_queue;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentActivity;
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
        QueueAdapter.QueueListener,
        PlayerViewClient.YoutubePlayerHandler {

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
    private boolean playing = false; // track whether a video is playing in the webview or not

    // a custom chrome client to log messages from js and go full screen
    public class MyChromeClient extends WebChromeClient {
        View fullscreen = null;
        private CustomViewCallback customViewCallback;
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            System.out.println("JAVASCRIPT: " + consoleMessage.message());
            return true;
        }

        @Override
        public void onHideCustomView() {
            System.out.println("Hiding custom view");

            if (fullscreen == null) {
                return;
            }

            // get parent of everything
            ConstraintLayout parent = findViewById(R.id.right_container);
            FrameLayout videoContainer = findViewById(R.id.video_container);

            // remove fullscreen view from container
            videoContainer.removeView(fullscreen);
            fullscreen = null;

            // restore the original constraints
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(parent);

            // queue's top constraint should now go to the webview
            constraintSet.connect(R.id.queue, ConstraintSet.TOP, R.id.video_playback, ConstraintSet.BOTTOM, 0);
            constraintSet.applyTo(parent);

            videoContainer.setVisibility(View.GONE);
            // show the regular webview again
            webView.setVisibility(View.VISIBLE);

            // invoke callback
            if (customViewCallback != null) {
                customViewCallback.onCustomViewHidden();
            }
            customViewCallback = null;
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            System.out.println("Showing custom view");

            if (fullscreen != null) {
                onHideCustomView();
            }
            customViewCallback = callback;

            // get parent of everything
            ConstraintLayout parent = findViewById(R.id.right_container);
            FrameLayout videoContainer = findViewById(R.id.video_container);

            // update constraints of queue, video_container
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(parent);

            // queue's top constraint should now go to video_container
            constraintSet.connect(R.id.queue, ConstraintSet.TOP, R.id.video_container, ConstraintSet.BOTTOM, 0);
            // chain constraints on fullscreen container
            constraintSet.setVerticalChainStyle(R.id.video_container, ConstraintSet.CHAIN_SPREAD);
            constraintSet.setVerticalWeight(R.id.video_container, 1.0f);

            // apply constraint changes
            constraintSet.applyTo(parent);

            // show fullscreen container, hide webview
            webView.setVisibility(View.GONE);
            videoContainer.setVisibility(View.VISIBLE);



            fullscreen = view;
            videoContainer.addView(fullscreen, new FrameLayout.LayoutParams(-1, -1));
            fullscreen.setVisibility(View.VISIBLE);
        }
    }

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

//        playVideoById("2yfyPeAEV3A");
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
        WebView.setWebContentsDebuggingEnabled(true);
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
        settings.setAllowContentAccess(true);

        PlayerViewClient viewClient = new PlayerViewClient(this);
        viewClient.setYoutubePlayerHandler(this);
        webView.setWebViewClient(viewClient);

        // set custome chrome client to log messages from js to here
        webView.setWebChromeClient(new MyChromeClient());
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

    public void onVideoEnded() {
        System.out.println("onVideoEnded called");
        VideoItem nextVideo = queueAdapter.getNextVideo();
        if (nextVideo != null) {
            playVideoById(nextVideo.getVideoId());

            queueAdapter.remove(0);
        } else {
            playing = false;
            webView.loadUrl("about:blank"); // TODO get some placeholder view
        }
    }

    public void playVideoById(String videoId) {
        // load the url
        String url = "https://www.youtube.com/watch?v=" + videoId;
        webView.loadUrl(url); // javascript will be injected once the page is finished loading
        playing = true;
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

        if (!playing) {
            String id = video.getVideoId();
            playVideoById(id);
            queueAdapter.remove(0);
        }
    }

    @Override
    public void onVideoStarted() {
        System.out.println("onVideoStarted called");

        // simulate the user pressing the f key to trigger full screen mode

        long downTime = SystemClock.uptimeMillis();
        KeyEvent fDown = new KeyEvent(downTime, downTime, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_F, 0);
        KeyEvent fUp = new KeyEvent(downTime, SystemClock.uptimeMillis(), KeyEvent.ACTION_UP, KeyEvent.KEYCODE_F, 0);
        webView.dispatchKeyEvent(fDown);
        webView.dispatchKeyEvent(fUp);
    }
}

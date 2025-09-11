
// load iframe api
let tag = document.createElement('script');
tag.src = "https://www.youtube.com/iframe_api";
let firstScriptTag = document.getElementsByTagName("script")[0];
firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

// this function is called after api code downloads,
// and creates an <iframe> and YouTube player
let player;
function onYouTubeIframeAPIReady() {
    console.log("Iframe API code downloaded");
    player = new YT.Player("player", {
        height: "100%",
        width: "100%",
        playerVars: {
            "playsinline": 1,
            "disablekb": 1,
            "enablejsapi": 1,
            "fs": 0,
            "iv_load_policy": 3,
            "autoplay": 1
        },
        events: {
            "onReady": onPlayerReady,
            "onStateChange": onPlayerStateChange
        }
    });
};

// this function is called when the video player is ready
let isPlayerReady = false;
function onPlayerReady(event) {
    console.log("Player is ready");
    isPlayerReady = true;
};

// this function is called when the player's state changes
let done = false;
function onPlayerStateChange(event) {
    // if (event.data === YT.PlayerState.PLAYING && !done) {
    //     setTimeout(stopVideo, 6000);
    //     done = true;
    // }
}

// load a new video and play it
function playVideo(videoId) {
    console.log("playVideo called with: ", videoId);
    if (isPlayerReady) {
        player.loadVideoById({
            videoId: videoId,
            startSeconds: 0
        });
        player.playVideo();
    } else {
        console.log("Player not ready, retrying in 500ms...");
        setTimeout(() => playVideo(videoId), 500);
    }
};

// returns true if a video is currently playing, false if not
function isVideoPlaying() {
    return player 
    && isPlayerReady 
    && (
        player.getPlayerState() === YT.PlayerState.PLAYING 
        || player.getPlayerState() === YT.PlayerState.PAUSED 
        || player.getPlayerState() === YT.PlayerState.BUFFERING
    );
}





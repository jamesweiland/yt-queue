
// add an on touch listener: 

function playVideo() {
    // locate the video
    const video = document.querySelector('.video.video-stream');

    console.log("video paused: " + video.paused);

    if (video && !video.paused) {
        console.log('dont need to do anything');
        window.Android.onVideoStarted();
    } else {
        // the video is paused or not existent: poll until we find it and then click
        const playInterval = setInterval(() => {
            console.log('looking for play button');
            const playButton = document.querySelector('.ytp-large-play-button');
            if (playButton) {
                console.log('found play button');
                playButton.click();
                clearInterval(playInterval); // stop polling

                window.Android.onVideoStarted();
            }
        })
    }
}

playVideo();


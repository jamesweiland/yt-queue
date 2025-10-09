console.log('in the script');

// Keydown listener for fullscreen
document.addEventListener('keydown', (event) => {
    if (event.key === 'f') {
        console.log('requesting fullscreen');
        const player = document.querySelector('#movie_player');
        if (player) {
            player.requestFullscreen().catch(e => {
                console.error('Fullscreen failed: ' + e);
            });
        }
    }
});

// Function to set up the initial observer
function waitForPlayerReady() {
    const playerReadyObserver = new MutationObserver((mutations, observer) => {
        const video = document.querySelector('#movie_player > div.html5-video-container > video');
        if (video) {
            console.log('player found');
            console.log('player = ' + video);
            observer.disconnect(); // Stop observing once the player is found

            // Autoplay logic
            if (video.paused) {
                console.log('Attempting to play video');
                video.click(); // Click once to trigger playback
                Android.onVideoStarted();

                
                // now, set up a mutation observer for watching the end of the video
                const player = document.querySelector('#movie_player');

                if (player.sawBefore) {
                    console.log('I SAW THIS PLAYER BEFORE');
                } else {
                    player.sawBefore = true;
                }

                const checkForEnded = (classList) => {
                    if (classList.contains('ended-mode')) {
                        return true;
                    }
                    return false;
                };

                const videoEndObserver = new MutationObserver((mutations, observer) => {
                    for (const mutation of mutations) {
                        if (mutation.type === 'attributes' && mutation.attributeName === 'class') {
                            const classList = mutation.target.classList;
                            if (checkForEnded(classList)) {
                                console.log('Video ended');
                                Android.onVideoEnded();
                                observer.disconnect();
                                break;
                            }
                        }
                    }
                });
                videoEndObserver.observe(player, {
                    attributes: true,
                    attributeFilter: ['class']
                });
            }
        }
    });

    // Start observing the DOM for the player
    playerReadyObserver.observe(document.body, {
        childList: true,
        subtree: true
    });
}

// Finally, call the function to start the whole process
waitForPlayerReady();

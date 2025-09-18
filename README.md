# yt-queue
The native YouTube app for Fire TV interrupts the video on search and doesn't allow the user to queue videos. This app solves that by letting you search for and queue YouTube videos without interrupting the playing video. This can let you use YouTube with your TV as a sort of speaker by being able to queue music videos.

## Installation

Simply clone this github repository:

  `git clone github.com/jamesweiland/yt-queue`

Or, with SSH:

`git clone git@github.com:jamesweiland/yt-queue`

## Deployment

To deploy this app to your Fire TV, you need to make sure [debug mode is enabled on your Fire TV](https://developer.amazon.com/docs/fire-tv/connecting-adb-to-device.html#firsttab). This allows your laptop to connect to your TV through Android Debug Bridge (ADB). Make sure to take note of your TV's IP address in the settings. You also need to make sure you have ADB installed (automatically installed with Android Studio, which I recommend using for deployment).

Once you have the required dependencies, connect to your TV through ADB:

`adb connect $<ip-address>:5555`

Once you are connected, you can run the app in Android Studio and it will display on th TV.


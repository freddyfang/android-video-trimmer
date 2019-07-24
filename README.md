# Android Video Trimmer View

<img src="/screenshots/screenshot_1.png" width="30%"> <img src="/screenshots/screenshot_2.png" width="30%">

Note that this is only the PURE android view. Consider using ffmpeg to actually trim the video.

## Import to your project
Add this to project level build.gradle
```
  allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
  }
```

And then in app level build.gradle
```
  implementation 'com.github.freddyfang:android-video-trimmer:v1.0.0'
```

## How to Use
***Layout xml***
```
  <idv.luchafang.videotrimmer.VideoTrimmerView
        android:id="@+id/videoTrimmerView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:vtv_window_bar_width="10dp"
        app:vtv_window_border_color="@android:color/white"
        app:vtv_window_border_width="2dp"
        app:vtv_window_left_bar="@drawable/trimmer_left_bar"
        app:vtv_window_right_bar="@drawable/trimmer_right_bar"/>
```

***Display it***
```
  videoTrimmerView
    .setVideo(File(path))
    .setMaxDuration(30_000)                   // millis
    .setMinDuration(3_000)                    // millis
    .setFrameCountInWindow(8)
    .setExtraDragSpace(10)                    // pixels
    .setOnSelectedRangeChangedListener(this)
    .show()
```

***OnSelectedRangeChangedListener***
```
  override fun onSelectRangeStart() {
    // Start to drag range bar or start to scroll the video frame list
  }

  override fun onSelectRange(startMillis: Long, endMillis: Long) {
    // Range is changing
  }

  override fun onSelectRangeEnd(startMillis: Long, endMillis: Long) {
    // Range selected, play the video here
  }
```

Also, you can save the current UI state by calling 
```
  val draft = videoTrimmerView.getTrimmerDraft()
  // draft is a parcelable object
```
and restore it when necessary.
```
  videoTrimmerView.restoreTrimmer(draft)
```

package com.db.dbvplayer;

import androidx.appcompat.app.AppCompatActivity;
//import android.annotation.SuppressLint;
//import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

//import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
//import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
//import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
import static com.db.dbvplayer.VideoAdapter.videoFiles;
import static com.db.dbvplayer.VideoFolderAdapter.folderVideoFiles;
import android.os.Handler;


public class PlayerActivity extends AppCompatActivity {

    PlayerView playerView;
    SimpleExoPlayer simpleExoPlayer;
    int position = -1;
    ArrayList<VideoFiles> myFiles = new ArrayList<>();
    private static final int HIDE_UI_DELAY = 3000; // Delay in milliseconds to hide UI
    private static final float DEFAULT_PLAYBACK_SPEED = 1.0f; // Default playback speed
    private static final float INCREASE_SPEED_FACTOR = 2.0f; // Factor to increase speed
    private GestureDetector gestureDetector;
    private boolean isLongPressing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreenMethod();
        setContentView(R.layout.activity_player);
        getSupportActionBar().hide();

        playerView = findViewById(R.id.exoplayer_movie);
        position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");

        if (sender.equals("FolderIsSending")) {
            myFiles = folderVideoFiles;
        } else {
            myFiles = videoFiles;
        }

        String path = myFiles.get(position).getPath();

        if (path != null) {
            Uri uri = Uri.parse(path);

            simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();
            DataSource.Factory factory = new DefaultDataSourceFactory(
                    this, Util.getUserAgent(this, "My App Name"));
            ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(
                    factory, extractorsFactory).createMediaSource(uri);

            playerView.setPlayer(simpleExoPlayer);
            playerView.setKeepScreenOn(true);
            simpleExoPlayer.prepare(mediaSource);
            simpleExoPlayer.setPlayWhenReady(true);

            playerView.setOnTouchListener(new View.OnTouchListener() {
                private final Handler handler = new Handler();
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            handler.postDelayed(longPressRunnable, 500); // Detect long press after 500 milliseconds
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            handler.removeCallbacks(longPressRunnable); // Cancel long press detection
                            break;
                    }
                    return true;
                }
                private final Runnable longPressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        isLongPressing = true;
                        // Increase playback speed when long pressed
                        float newSpeed = simpleExoPlayer.getPlaybackParameters().speed + INCREASE_SPEED_FACTOR;
                        PlaybackParameters playbackParameters = new PlaybackParameters(newSpeed);
                        simpleExoPlayer.setPlaybackParameters(playbackParameters);
                    }
                };
            });
            // Set up GestureDetector
            gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    // Perform action on single tap (e.g., toggle visibility of player UI)
                    if (playerView.isControllerVisible()) {
                        playerView.hideController();
                    } else {
                        playerView.showController();
                    }
                    return true;
                }
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    // Perform action on double tap (e.g., pause the player)
                    if (simpleExoPlayer.getPlayWhenReady()) {
                        simpleExoPlayer.setPlayWhenReady(false);
                    } else {
                        simpleExoPlayer.setPlayWhenReady(true);
                    }
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    // Long press detected
                    isLongPressing = true;
                    // Increase playback speed when long pressed
                    float newSpeed = simpleExoPlayer.getPlaybackParameters().speed + INCREASE_SPEED_FACTOR;
                    PlaybackParameters playbackParameters = new PlaybackParameters(newSpeed);
                    simpleExoPlayer.setPlaybackParameters(playbackParameters);
                }

                public void onLongPressUp(MotionEvent e) {
                    // Long press released
                    isLongPressing = false;
                    // Reset playback speed to default
                    PlaybackParameters playbackParameters = new PlaybackParameters(DEFAULT_PLAYBACK_SPEED);
                    simpleExoPlayer.setPlaybackParameters(playbackParameters);
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    // Detect swipe left or right and seek accordingly
                    float deltaX = e2.getX() - e1.getX();
                    if (Math.abs(deltaX) > 50 && Math.abs(velocityX) > 50) {
                        if (deltaX > 0) {
                            // Swipe right, seek forward
                            seekVideo(10 * 1000); // 10 seconds forward (adjust as needed)
                        } else {
                            // Swipe left, seek backward
                            seekVideo(-10 * 1000); // 10 seconds backward (adjust as needed)
                        }
                    }
                    return super.onFling(e1, e2, velocityX, velocityY);
                }
            });
        }
    }

    private void setFullScreenMethod() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        simpleExoPlayer.pause();
    }
    private void seekVideo(long positionMs) {
        long currentPosition = simpleExoPlayer.getCurrentPosition();
        long newPosition = currentPosition + positionMs;

        if (newPosition < 0) {
            newPosition = 0;
        } else if (newPosition > simpleExoPlayer.getDuration()) {
            newPosition = simpleExoPlayer.getDuration();
        }

        simpleExoPlayer.seekTo(newPosition);
    }
}

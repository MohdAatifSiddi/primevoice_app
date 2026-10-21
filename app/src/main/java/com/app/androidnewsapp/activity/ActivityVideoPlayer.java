package com.app.androidnewsapp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.LoadControl;
import androidx.media3.exoplayer.ima.ImaAdsLoader;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.trackselection.AdaptiveTrackSelection;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;
import androidx.media3.ui.PlayerView;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.config.AppConfig;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.util.Tools;
import com.app.androidnewsapp.util.TrackSelectionDialog;
import com.google.ads.interactivemedia.v3.api.AdEvent;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import com.google.ads.interactivemedia.v3.api.ImaSdkSettings;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ActivityVideoPlayer extends AppCompatActivity {

    private static final String TAG = "ActivityStreamPlayer";
    String video_url;
    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    DefaultDataSource.Factory dataSourceFactory;
    private ProgressBar progressBar;
    boolean fullscreen = false;
    private ImageView fullscreenButton;
    SharedPref sharedPref;
    Boolean contentLoaded = false;
    ImaAdsLoader adsLoader;
    ImaSdkSettings imaSdkSettings;
    ImageView icRewind10S, icFFwD10S;
    OnBackPressedDispatcher onBackPressedDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);
        Tools.setPlayerNavigation(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.color_black));
            this.getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.color_black));
            this.getWindow().getDecorView().setSystemUiVisibility(0);
        }

        Intent intent = getIntent();
        video_url = intent.getStringExtra("video_url");

        sharedPref = new SharedPref(this);

        progressBar = findViewById(R.id.progressBar);
        playerView = findViewById(R.id.player_view);

        icFFwD10S = playerView.findViewById(R.id.ic_ffwd_10s);
        icRewind10S = playerView.findViewById(R.id.ic_rew_10s);

        icFFwD10S.setOnClickListener(view -> exoPlayer.seekTo(exoPlayer.getCurrentPosition() + 10000));
        icRewind10S.setOnClickListener(view -> exoPlayer.seekTo(exoPlayer.getCurrentPosition() - 10000));

        playerOrientation();

        if (AppConfig.FORCE_VIDEO_PLAYER_TO_LANDSCAPE) {
            setLandscape();
        }

        handleOnBackPressed();
        prepareExoPlayer();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void prepareExoPlayer() {
        adsLoader = new ImaAdsLoader.Builder(this)
                .setAdEventListener(buildAdEventListener())
                .setImaSdkSettings(getImaSdkSettings())
                .build();

        ImaSdkFactory.getInstance().initialize(this, getImaSdkSettings());

        HttpDataSource.Factory httpDataSourceFactory = new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setUserAgent(Util.getUserAgent(this, getString(R.string.app_name)));
        //.setUserAgent(getUserAgent());
        dataSourceFactory = new DefaultDataSource.Factory(this, httpDataSourceFactory);

        MediaSource.Factory mediaSourceFactory = new DefaultMediaSourceFactory(dataSourceFactory)
                .setLocalAdInsertionComponents(unusedAdTagUri -> adsLoader, playerView);

        LoadControl loadControl = new DefaultLoadControl();

        AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(this, trackSelectionFactory);

        exoPlayer = new ExoPlayer.Builder(this)
                .setTrackSelector(trackSelector)
                .setMediaSourceFactory(mediaSourceFactory)
                .setLoadControl(loadControl)
                .build();

        playerView.setPlayer(exoPlayer);
        playerView.setUseController(true);
        playerView.requestFocus();

        playerOrientation();

        adsLoader.setPlayer(exoPlayer);

        // Create the MediaItem to play, specifying the content URI and ad tag URI.
        Uri contentUri = Uri.parse(video_url);
        Uri adTagUri = Uri.parse(AppConfig.GOOGLE_ADS_IMA_VAST_TAG_URL);
        Log.d(TAG, "Vast Ads Tag Url: " + adTagUri);

        MediaItem mediaItem = new MediaItem.Builder()
                .setUri(contentUri)
                .setAdsConfiguration(new MediaItem.AdsConfiguration.Builder(adTagUri).build())
                .build();

        // Prepare the content and ad to be played with the SimpleExoPlayer.
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();

        // Set PlayWhenReady. If true, content and ads will autoplay.
        exoPlayer.setPlayWhenReady(true);

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onTimelineChanged(@NonNull Timeline timeline, int reason) {

            }

            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_READY:
                        progressBar.setVisibility(View.GONE);

                        exoPlayer.setPlayWhenReady(true);
                        break;
                    case Player.STATE_ENDED:
                        exoPlayer.seekTo(0);
                        break;
                    case Player.STATE_BUFFERING:
                        progressBar.setVisibility(View.VISIBLE);
                        if (video_url.contains(".m3u8")) {
                            exoPlayer.seekTo(0);
                        }
                        break;
                    case Player.STATE_IDLE:
                        break;
                }

            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Player.Listener.super.onIsPlayingChanged(isPlaying);
                if (isPlaying) {
                    progressBar.setVisibility(View.GONE);
                    contentLoaded = true;
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                exoPlayer.stop();
                errorDialog();
                Log.d(TAG, "onPlayerError rawr " + error);
            }

            @Override
            public void onPlayerErrorChanged(@Nullable PlaybackException error) {
                Log.d(TAG, "onPlayerErrorChanged rawr " + error);
            }

        });

        playerView.findViewById(R.id.exo_track_selection_view).setOnClickListener(view -> {
            if (contentLoaded) {
                TrackSelectionDialog trackSelectionDialog = TrackSelectionDialog.createForPlayer(
                        exoPlayer, dismissedDialog -> {
                        });
                trackSelectionDialog.show(getSupportFragmentManager(), null);
            } else {
                Toast.makeText(getApplicationContext(), R.string.title_please_wait, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public AdEvent.AdEventListener buildAdEventListener() {
        return event -> {
            AdEvent.AdEventType eventType = event.getType();
            if (eventType == AdEvent.AdEventType.AD_PROGRESS) {
                return;
            }
            String log = "IMA event: " + eventType;
            Log.d(TAG, log);
        };
    }

    public void handleOnBackPressed() {
        onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                closePlayer();
            }
        });
    }

    private void playerOrientation() {
        fullscreenButton = playerView.findViewById(R.id.exo_fullscreen_icon);
        fullscreenButton.setOnClickListener(view -> {
            if (fullscreen) {
                setPortrait();
            } else {
                setLandscape();
            }
        });
    }

    private ImaSdkSettings getImaSdkSettings() {
        if (imaSdkSettings == null) {
            imaSdkSettings = ImaSdkFactory.getInstance().createImaSdkSettings();
            // Set any IMA SDK settings here.
        }
        return imaSdkSettings;
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void setPortrait() {
        fullscreenButton.setImageDrawable(ContextCompat.getDrawable(ActivityVideoPlayer.this, R.drawable.ic_exo_fullscreen_open));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playerView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        playerView.setLayoutParams(params);
        fullscreen = false;
    }

    private void setLandscape() {
        fullscreenButton.setImageDrawable(ContextCompat.getDrawable(ActivityVideoPlayer.this, R.drawable.ic_exo_fullscreen_close));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) playerView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        playerView.setLayoutParams(params);
        fullscreen = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.getPlaybackState();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (exoPlayer != null) {
            exoPlayer.setPlayWhenReady(true);
            exoPlayer.getPlaybackState();
        }
    }

    public void closePlayer() {
        if (exoPlayer != null) {
            finish();
            exoPlayer.stop();
        }
    }

    public void errorDialog() {
        new MaterialAlertDialogBuilder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.whops))
                .setCancelable(false)
                .setMessage(getString(R.string.msg_failed_stream))
                .setPositiveButton(getString(R.string.btn_retry), (dialog, which) -> retryLoad())
                .setNegativeButton(getString(R.string.dialog_no), (dialogInterface, i) -> finish())
                .show();
    }

    public void retryLoad() {
        prepareExoPlayer();
        progressBar.setVisibility(View.VISIBLE);
    }

}

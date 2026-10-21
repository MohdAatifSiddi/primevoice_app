package com.app.androidnewsapp.activity;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.config.AppConfig;
import com.app.androidnewsapp.util.Tools;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.DefaultPlayerUiController;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.utils.FadeViewHelper;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.views.YouTubePlayerSeekBar;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class ActivityYoutubePlayer extends AppCompatActivity {

    private YouTubePlayerView youTubePlayerView;
    private YouTubePlayerSeekBar youTubePlayerSeekBar;
    private String videoId;
    boolean fullscreen = false;
    RelativeLayout customControlsContainer;
    FadeViewHelper fadeViewHelper;
    ImageButton btnPlayPause;
    ImageButton btnRewind;
    ImageButton btnForward;
    ImageView btnFullScreen;
    View panelView;
    public static final boolean ENABLE_CUSTOM_UI = false;
    FrameLayout fullScreenViewContainer;
    ProgressBar progressBar;
    private long exitTime = 0;
    YouTubePlayer youTubePlayer;
    boolean isPaused = false;
    boolean isCueVideo = true;
    OnBackPressedDispatcher onBackPressedDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_youtube);
        Tools.setPlayerNavigation(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.getWindow().setNavigationBarColor(ContextCompat.getColor(getApplicationContext(), R.color.color_black));
            this.getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.color_black));
            this.getWindow().getDecorView().setSystemUiVisibility(0);
        }
        videoId = getIntent().getStringExtra("video_id");
        youTubePlayerView = findViewById(R.id.youtube_player_view);
        youTubePlayerSeekBar = findViewById(R.id.youtube_player_seekbar);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnRewind = findViewById(R.id.btn_rew_10s);
        btnForward = findViewById(R.id.btn_ffwd_10s);
        btnFullScreen = findViewById(R.id.btn_fullscreen);
        customControlsContainer = findViewById(R.id.custom_controls_container);
        panelView = findViewById(R.id.panel_view);
        fullScreenViewContainer = findViewById(R.id.full_screen_view_container);
        progressBar = findViewById(R.id.progress_bar);

        if (ENABLE_CUSTOM_UI) {
            initCustomYouTubePlayerView();
            customControlsContainer.setVisibility(View.VISIBLE);
            panelView.setVisibility(View.VISIBLE);
        } else {
            initDefaultYouTubePlayerView();
            customControlsContainer.setVisibility(View.GONE);
            panelView.setVisibility(View.GONE);
        }

        if (AppConfig.FORCE_VIDEO_PLAYER_TO_LANDSCAPE) {
            setLandscape();
        }

        handleOnBackPressed();

    }

    public void handleOnBackPressed() {
        onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    private void initDefaultYouTubePlayerView() {
        youTubePlayerView.setEnableAutomaticInitialization(false);
        YouTubePlayerListener listener = new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer player) {
                youTubePlayer = player;
                if (isPaused) {
                    youTubePlayer.cueVideo(videoId, 0);
                } else {
                    youTubePlayer.loadVideo(videoId, 0);
                    Tools.postDelayed(() -> {
                        progressBar.setVisibility(View.GONE);
                        youTubePlayerView.setVisibility(View.VISIBLE);
                    }, 1500);

                }
            }
        };
        IFramePlayerOptions options = new IFramePlayerOptions.Builder(this)
                .controls(1)
                .fullscreen(1)
                .rel(0)
                .ivLoadPolicy(3)
                .ccLoadPolicy(0)
                .build();

        youTubePlayerView.addFullscreenListener(new FullscreenListener() {
            @Override
            public void onEnterFullscreen(@NonNull View view, @NonNull Function0<Unit> function0) {
                youTubePlayerView.setVisibility(View.GONE);
                fullScreenViewContainer.setVisibility(View.VISIBLE);
                fullScreenViewContainer.addView(view);
                setLandscape();
            }

            @Override
            public void onExitFullscreen() {
                youTubePlayerView.setVisibility(View.VISIBLE);
                fullScreenViewContainer.setVisibility(View.GONE);
                fullScreenViewContainer.removeAllViews();
                setPortrait();
            }
        });

        youTubePlayerView.initialize(listener, options);

        getLifecycle().addObserver(youTubePlayerView);
    }

    private void initCustomYouTubePlayerView() {

        youTubePlayerView.setEnableAutomaticInitialization(false);

        YouTubePlayerListener listener = new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer player) {
                Tools.postDelayed(() -> {
                    progressBar.setVisibility(View.GONE);
                    youTubePlayerView.setVisibility(View.VISIBLE);
                }, 1500);

                youTubePlayer = player;
                if (isPaused) {
                    youTubePlayer.cueVideo(videoId, 0);
                } else {
                    youTubePlayer.loadVideo(videoId, 0);
                }

                DefaultPlayerUiController defaultPlayerUiController = new DefaultPlayerUiController(youTubePlayerView, player);
                defaultPlayerUiController.showUi(false);

                youTubePlayerSeekBar.setYoutubePlayerSeekBarListener(player::seekTo);
                player.addListener(youTubePlayerSeekBar);

                fadeViewHelper = new FadeViewHelper(customControlsContainer);
                fadeViewHelper.setAnimationDuration(FadeViewHelper.DEFAULT_ANIMATION_DURATION);
                fadeViewHelper.setFadeOutDelay(FadeViewHelper.DEFAULT_FADE_OUT_DELAY);
                player.addListener(fadeViewHelper);

                panelView.setOnClickListener(view -> fadeViewHelper.toggleVisibility());

                YouTubePlayerTracker playerTracker = new YouTubePlayerTracker();
                player.addListener(playerTracker);

                btnForward.setVisibility(View.VISIBLE);
                btnRewind.setVisibility(View.VISIBLE);
                btnPlayPause.setVisibility(View.VISIBLE);

                btnPlayPause.setOnClickListener(view -> {
                    if (playerTracker.getState() == PlayerConstants.PlayerState.PLAYING) {
                        player.pause();
                        btnPlayPause.setImageResource(R.drawable.ic_exo_play);
                    } else {
                        player.play();
                        btnPlayPause.setImageResource(R.drawable.ic_exo_pause);
                    }
                });

                btnFullScreen.setOnClickListener(view -> {
                    if (fullscreen) {
                        setPortrait();
                    } else {
                        setLandscape();
                    }
                });

                youTubePlayerView.setCustomPlayerUi(defaultPlayerUiController.getRootView());
            }

            @Override
            public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float second) {
                btnForward.setOnClickListener(view -> youTubePlayer.seekTo(second + 10));
                btnRewind.setOnClickListener(view -> youTubePlayer.seekTo(second - 10));
            }
        };

        IFramePlayerOptions options = new IFramePlayerOptions.Builder(this)
                .controls(0)
                .fullscreen(0)
                .rel(0)
                .ivLoadPolicy(3)
                .ccLoadPolicy(0)
                .build();

        youTubePlayerView.initialize(listener, options);

        getLifecycle().addObserver(youTubePlayerView);

    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void setPortrait() {
        btnFullScreen.setImageDrawable(ContextCompat.getDrawable(ActivityYoutubePlayer.this, R.drawable.ic_exo_fullscreen_open));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().show();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) youTubePlayerView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        youTubePlayerView.setLayoutParams(params);
        fullscreen = false;
    }

    private void setLandscape() {
        btnFullScreen.setImageDrawable(ContextCompat.getDrawable(ActivityYoutubePlayer.this, R.drawable.ic_exo_fullscreen_close));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) youTubePlayerView.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        youTubePlayerView.setLayoutParams(params);
        fullscreen = true;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        isPaused = true;
        if (youTubePlayer != null) {
            youTubePlayer.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (youTubePlayer != null) {
            youTubePlayer.play();
            if (isCueVideo) {
                Tools.postDelayed(() -> {
                    progressBar.setVisibility(View.GONE);
                    youTubePlayerView.setVisibility(View.VISIBLE);
                    isCueVideo = false;
                }, 1000);
            }
        }
    }

}
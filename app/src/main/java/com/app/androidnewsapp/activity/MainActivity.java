package com.app.androidnewsapp.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowMetrics;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.viewpager.widget.ViewPager;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.callback.CallbackUser;
import com.app.androidnewsapp.config.AppConfig;
import com.app.androidnewsapp.database.prefs.AdsPref;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.models.User;
import com.app.androidnewsapp.rest.ApiInterface;
import com.app.androidnewsapp.rest.RestAdapter;
import com.app.androidnewsapp.util.AdsManager;
import com.app.androidnewsapp.util.AppBarLayoutBehavior;
import com.app.androidnewsapp.util.Constant;
import com.app.androidnewsapp.util.NavigationModeChecker;
import com.app.androidnewsapp.util.RtlViewPager;
import com.app.androidnewsapp.util.Tools;
import com.app.androidnewsapp.util.ViewPagerHelper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.solodroid.push.sdk.provider.OneSignalPush;
import com.solodroidx.ads.appopen.AppOpenAd;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements DefaultLifecycleObserver {

    private static final String TAG = "MainActivity";
    private long exitTime = 0;
    private BottomNavigationView navigation;
    private ViewPager viewPager;
    private RtlViewPager viewPagerRTL;
    TextView titleToolbar;
    User user;
    Call<CallbackUser> callbackCall = null;
    ImageView imgProfile;
    ImageView imgAvatar;
    RelativeLayout btnProfile;
    ImageButton btnSearch;
    ImageView btnSettings;
    CardView lytSearchBar;
    LinearLayout searchBar;
    Toolbar toolbar;
    SharedPref sharedPref;
    AdsPref adsPref;
    AdsManager adsManager;
    CoordinatorLayout parentView;
    ViewPagerHelper viewPagerHelper;
    private AppUpdateManager appUpdateManager;
    private ActivityResultLauncher<IntentSenderRequest> appUpdateResultLauncher;
    OneSignalPush.Builder onesignal;
    OnBackPressedDispatcher onBackPressedDispatcher;
    LinearLayout customDialogLayout;
    View dimBackground;
    FloatingActionButton btnRate;
    FloatingActionButton btnShare;
    Button btnExit;
    Button btnCancel;
    LinearLayout nativeAdView;
    private int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_main);
        Tools.setNavigation(this);
        Tools.setupAppMainBarLayout(this);

        adsPref = new AdsPref(this);
        sharedPref = new SharedPref(this);
        viewPagerHelper = new ViewPagerHelper(this);
        adsManager = new AdsManager(this);

        initView();

        if (AppConfig.FORCE_TO_SHOW_APP_OPEN_AD_ON_START) {
            ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
            adsPref.setIsOpenAd(true);
        } else {
            sharedPref.setIsFirstTimeLaunch(false);
        }

        Tools.postDelayed(() -> {
            adsManager.initializeAd();
            adsManager.updateConsentStatus();
            adsManager.loadAppOpenAd(adsPref.getIsAppOpenAdOnResume(), false, () -> {
            });
            adsManager.loadBannerAd(adsPref.getIsBannerHome());
            adsManager.loadInterstitialAd(adsPref.getIsInterstitialPostList(), adsPref.getInterstitialAdInterval());
        }, 100);

        onesignal = new OneSignalPush.Builder(this);
        onesignal.requestNotificationPermission();

        navigation.getMenu().clear();

        if (AppConfig.SET_CATEGORY_AS_MAIN_PAGE) {
            if (sharedPref.getVideoMenu().equals("yes")) {
                navigation.inflateMenu(R.menu.menu_navigation_category);
            } else {
                navigation.inflateMenu(R.menu.menu_navigation_category_no_video);
            }
        } else {
            if (sharedPref.getVideoMenu().equals("yes")) {
                navigation.inflateMenu(R.menu.menu_navigation_default);
            } else {
                navigation.inflateMenu(R.menu.menu_navigation_default_no_video);
            }
        }

        navigation.setLabelVisibilityMode(BottomNavigationView.LABEL_VISIBILITY_LABELED);
        if (sharedPref.getIsDarkTheme()) {
            navigation.setBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_bottom_navigation));
        } else {
            navigation.setBackgroundColor(ContextCompat.getColor(this, R.color.color_light_bottom_navigation));
        }

        if (AppConfig.ENABLE_RTL_MODE) {
            viewPagerHelper.setupViewPagerRTL(viewPagerRTL, navigation, titleToolbar);
        } else {
            viewPagerHelper.setupViewPager(viewPager, navigation, titleToolbar);
        }

        Tools.notificationOpenHandler(this, getIntent());

        if (AppConfig.ENABLE_NEW_APP_DESIGN) {
            setupNewToolbar();
        } else {
            setupToolbar();
        }

        displayUserProfile();

        if (!Tools.isDebug()) {
            inAppUpdate();
            inAppReview();
        }

        if (!Tools.isConnect(this)) {
            if (sharedPref.getVideoMenu().equals("yes")) {
                if (AppConfig.ENABLE_RTL_MODE) {
                    viewPagerRTL.setCurrentItem(3);
                } else {
                    viewPager.setCurrentItem(3);
                }
            } else {
                if (AppConfig.ENABLE_RTL_MODE) {
                    viewPagerRTL.setCurrentItem(2);
                } else {
                    viewPager.setCurrentItem(2);
                }
            }
        }

        handleOnBackPressed();

        initExitDialog();
        getScreenDimensions();
        setCustomDialogWidth();
    }

    public void handleOnBackPressed() {
        onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (AppConfig.ENABLE_RTL_MODE) {
                    if (viewPagerRTL.getCurrentItem() != 0) {
                        viewPagerRTL.setCurrentItem((0), true);
                    } else {
                        exitApp();
                    }
                } else {
                    if (viewPager.getCurrentItem() != 0) {
                        viewPager.setCurrentItem((0), true);
                    } else {
                        exitApp();
                    }
                }
            }
        });
    }

    private void initView() {
        AppBarLayout appBarLayout = findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());
        if (AppConfig.ENABLE_NEW_APP_DESIGN) {
            if (sharedPref.getIsDarkTheme()) {
                appBarLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_background));
            } else {
                appBarLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.color_light_background));
            }
        }

        parentView = findViewById(R.id.coordinator_layout);
        titleToolbar = findViewById(R.id.title_toolbar);
        imgProfile = findViewById(R.id.img_profile);
        imgAvatar = findViewById(R.id.img_avatar);
        btnProfile = findViewById(R.id.btn_profile);
        btnSettings = findViewById(R.id.btn_settings);
        lytSearchBar = findViewById(R.id.lyt_search_bar);
        searchBar = findViewById(R.id.search_bar);
        btnSearch = findViewById(R.id.btn_search);
        toolbar = findViewById(R.id.toolbar);
        navigation = findViewById(R.id.navigation);
        viewPager = findViewById(R.id.viewpager);
        viewPagerRTL = findViewById(R.id.viewpager_rtl);

    }

    public void setupToolbar() {
        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_toolbar));
            navigation.setBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_bottom_navigation));
        } else {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_light_primary));
        }

        btnSearch.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), ActivitySearch.class));
            destroyBannerAd();
        });

        btnProfile.setOnClickListener(view -> openSettings());
        btnSettings.setOnClickListener(view -> openSettings());

        if (sharedPref.getLoginFeature().equals("yes")) {
            btnProfile.setVisibility(View.VISIBLE);
            btnSettings.setVisibility(View.GONE);
        } else {
            btnProfile.setVisibility(View.GONE);
            btnSettings.setVisibility(View.VISIBLE);
        }
    }

    private void setupNewToolbar() {
        if (sharedPref.getIsDarkTheme()) {
            lytSearchBar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_search_bar));
            btnSearch.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            titleToolbar.setTextColor(ContextCompat.getColor(this, R.color.color_dark_icon));
        } else {
            lytSearchBar.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_light_search_bar));
            btnSearch.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            titleToolbar.setTextColor(ContextCompat.getColor(this, R.color.color_light_text));
        }

        searchBar.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), ActivitySearch.class));
            destroyBannerAd();
        });

        btnSearch.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), ActivitySearch.class));
            destroyBannerAd();
        });

        titleToolbar.setText(getString(R.string.app_name));

        btnProfile.setOnClickListener(view -> openSettings());
        btnSettings.setOnClickListener(view -> openSettings());

        if (sharedPref.getLoginFeature().equals("yes")) {
            btnProfile.setVisibility(View.VISIBLE);
            btnSettings.setVisibility(View.GONE);
        } else {
            btnProfile.setVisibility(View.GONE);
            btnSettings.setVisibility(View.VISIBLE);
        }
    }

    public void exitApp() {
        if (AppConfig.ENABLE_EXIT_DIALOG) {
            if (customDialogLayout != null) {
                if (customDialogLayout.getVisibility() == View.VISIBLE) {
                    hideCustomDialog();
                } else {
                    showCustomDialog();
                }
            } else {
                showCustomDialog();
            }
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                showSnackBar(getString(R.string.press_again_to_exit));
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                destroyBannerAd();
                destroyAppOpenAd();
            }
        }
    }

    private void initExitDialog() {
        customDialogLayout = findViewById(R.id.custom_dialog_layout);
        if (sharedPref.getIsDarkTheme()) {
            customDialogLayout.setBackgroundResource(R.drawable.bg_dialog_dark);
        } else {
            customDialogLayout.setBackgroundResource(R.drawable.bg_dialog_light);
        }
        customDialogLayout.setOnClickListener(v -> {
            //do nothing
        });

        dimBackground = findViewById(R.id.dim_background);
        btnRate = findViewById(R.id.btn_rate);
        btnShare = findViewById(R.id.btn_share);
        btnExit = findViewById(R.id.btn_exit);
        btnCancel = findViewById(R.id.btn_cancel);
        nativeAdView = findViewById(R.id.native_ad_view);

        Tools.setNativeAdStyle(MainActivity.this, nativeAdView, adsPref.getNativeAdStyleExitDialog());
        adsManager.loadNativeAd(adsPref.getIsNativeAdExitDialog(), adsPref.getNativeAdStyleExitDialog());

        btnRate.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + Tools.getApplicationId())));
            hideCustomDialog();
        });

        btnShare.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content) + "\n" + "https://play.google.com/store/apps/details?id=" + Tools.getApplicationId());
            intent.setType("text/plain");
            startActivity(intent);
            hideCustomDialog();
        });

        btnExit.setOnClickListener(v -> {
            finish();
            destroyBannerAd();
            destroyAppOpenAd();
            hideCustomDialog();
        });

        dimBackground.setOnClickListener(v -> hideCustomDialog());
        btnCancel.setOnClickListener(v -> hideCustomDialog());
    }

    private void getScreenDimensions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
            screenWidth = windowMetrics.getBounds().width();
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            screenWidth = displayMetrics.widthPixels;
        }
    }

    private void setCustomDialogWidth() {
        int desiredWidth = (int) (screenWidth * 0.85);
        ViewGroup.LayoutParams params = customDialogLayout.getLayoutParams();
        params.width = desiredWidth;
        customDialogLayout.setLayoutParams(params);
    }

    private void showCustomDialog() {
        customDialogLayout.setAlpha(0f);
        dimBackground.setAlpha(0f);
        customDialogLayout.setVisibility(View.VISIBLE);
        dimBackground.setVisibility(View.VISIBLE);
        customDialogLayout.animate().alpha(1f).setDuration(300).setListener(null);
        dimBackground.animate().alpha(1f).setDuration(300).setListener(null);
        isWindowLightStatusBarNavigation(false);
        showBannerAd(false);
        if (!NavigationModeChecker.isGestureNavigationEnabled(this)) {
            if (!sharedPref.getIsDarkTheme()) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    Tools.postDelayed(() -> getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.color_dialog_background_light_navigation_bar)), 100);
                }
            }
        }
    }

    private void hideCustomDialog() {
        customDialogLayout.animate().alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                customDialogLayout.setVisibility(View.GONE);
            }
        });
        dimBackground.animate().alpha(0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                dimBackground.setVisibility(View.GONE);
            }
        });
        isWindowLightStatusBarNavigation(true);
        showBannerAd(true);
        if (!NavigationModeChecker.isGestureNavigationEnabled(this)) {
            if (!sharedPref.getIsDarkTheme()) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    Tools.postDelayed(() -> Tools.lightNavigation(this), 100);
                }
            }
        }
    }

    private void isWindowLightStatusBarNavigation(boolean show) {
        if (sharedPref.getIsDarkTheme()) {
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);
            WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(false);
        } else {
            if (show) {
                if (AppConfig.ENABLE_NEW_APP_DESIGN) {
                    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(true);
                    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(true);
                } else {
                    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);
                    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(true);
                }
            } else {
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(false);
            }
        }
    }

    private void showBannerAd(boolean show) {
        if (show) {
            findViewById(R.id.bannerAdView).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.bannerAdView).setVisibility(View.GONE);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void showSnackBar(String msg) {
        Snackbar.make(parentView, msg, Snackbar.LENGTH_SHORT).show();
    }

    private void displayUserProfile() {
        if (sharedPref.getIsLogin()) {
            requestUserData();
        } else {
            imgProfile.setImageResource(R.drawable.ic_account_circle_white);
            imgAvatar.setVisibility(View.GONE);
            if (sharedPref.getIsDarkTheme()) {
                imgProfile.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            } else {
                if (AppConfig.ENABLE_NEW_APP_DESIGN) {
                    imgProfile.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
                } else {
                    imgProfile.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_white), PorterDuff.Mode.SRC_IN);
                }
            }
        }
    }

    private void requestUserData() {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        callbackCall = apiInterface.getUser(sharedPref.getUserId());
        callbackCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CallbackUser> call, @NonNull Response<CallbackUser> response) {
                CallbackUser resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    user = resp.response;
                    if (user.image.equals("")) {
                        imgProfile.setImageResource(R.drawable.ic_account_circle_white);
                        imgAvatar.setVisibility(View.GONE);
                        if (sharedPref.getIsDarkTheme()) {
                            imgProfile.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
                        } else {
                            if (AppConfig.ENABLE_NEW_APP_DESIGN) {
                                imgProfile.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
                            } else {
                                imgProfile.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.color_white), PorterDuff.Mode.SRC_IN);
                            }
                        }
                    } else {
                        Glide.with(MainActivity.this)
                                .load(sharedPref.getBaseUrl() + "/upload/avatar/" + user.image.replace(" ", "%20"))
                                .apply(new RequestOptions().override(54, 54))
                                .thumbnail(Tools.RequestBuilder(MainActivity.this))
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop()
                                .into(imgProfile);

                        Glide.with(MainActivity.this)
                                .load(sharedPref.getBaseUrl() + "/upload/avatar/" + user.image.replace(" ", "%20"))
                                .apply(new RequestOptions().override(54, 54))
                                .thumbnail(Tools.RequestBuilder(MainActivity.this))
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .centerCrop()
                                .into(imgAvatar);
                        imgAvatar.setVisibility(View.VISIBLE);
                    }

                    if (user.account_deletion_request.equals("1")) {
                        dialogAccountDeletion(sharedPref.getUserId(), getString(R.string.account_deletion_message));
                    }

                    if (user.status.equals("0")) {
                        dialogAccountDisabled(getString(R.string.login_disabled));
                        imgAvatar.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CallbackUser> call, @NonNull Throwable t) {
                if (sharedPref.getLoginFeature().equals("yes")) {
                    imgProfile.setImageResource(R.drawable.ic_account_circle_white);
                    dialogAccountDisabled(getString(R.string.login_error));
                } else {
                    imgProfile.setImageResource(R.drawable.ic_settings_white);
                }
            }

        });
    }

    private void dialogAccountDisabled(String message) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setTitle(R.string.whops);
        dialog.setMessage(message);
        dialog.setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
            sharedPref.setIsLogin(false);
            sharedPref.setUserId("");
            sharedPref.setUserName("");
            sharedPref.setUserEmail("");
            sharedPref.setUserImage("");
            sharedPref.setUserPassword("");
            Tools.postDelayed(this::recreate, 200);
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void dialogAccountDeletion(String userId, String message) {
        MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(this);
        dialog.setTitle(R.string.whops);
        dialog.setMessage(message);
        dialog.setPositiveButton(R.string.dialog_restore, (dialogInterface, i) -> {
            Tools.postAccountDeletionRequest(
                    MainActivity.this,
                    getResources().getString(R.string.restore_process),
                    userId,
                    "0",
                    "",
                    "",
                    () -> {
                        sharedPref.setIsLogin(false);
                        sharedPref.setUserId("");
                        sharedPref.setUserName("");
                        sharedPref.setUserEmail("");
                        sharedPref.setUserImage("");
                        sharedPref.setUserPassword("");
                        MaterialAlertDialogBuilder alert = getMaterialAlertDialogBuilder();
                        alert.show();
                        Constant.isProfileUpdated = true;
                    }
            );
        });
        dialog.setNegativeButton(R.string.dialog_cancel, (dialogInterface, i) -> {
            sharedPref.setIsLogin(false);
            sharedPref.setUserId("");
            sharedPref.setUserName("");
            sharedPref.setUserEmail("");
            sharedPref.setUserImage("");
            sharedPref.setUserPassword("");
            Tools.postDelayed(this::recreate, 200);
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private @NonNull MaterialAlertDialogBuilder getMaterialAlertDialogBuilder() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        builder.setMessage(R.string.restore_message);
        builder.setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
            startActivity(new Intent(getApplicationContext(), ActivityUserLogin.class));
            finish();
        });
        builder.setCancelable(false);
        return builder;
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onStart(owner);
        Tools.postDelayed(() -> {
            if (adsPref.getIsAppOpenAdOnResume()) {
                if (AppOpenAd.isAppOpenAdLoaded) {
                    adsManager.showAppOpenAd(() -> {
                    });
                    Log.d(AppOpenAd.TAG, "lifecycleObserver Show App Open Ad");
                }
            }
        }, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Constant.isProfileUpdated) {
            Tools.postDelayed(() -> Constant.isProfileUpdated = false, 100);
            displayUserProfile();
        }
        adsManager.resumeBannerAd(adsPref.getIsBannerHome());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyBannerAd();
        destroyAppOpenAd();
    }

    public void showInterstitialAd() {
        adsManager.showInterstitialAd();
    }

    public void destroyAppOpenAd() {
        AppOpenAd.isAppOpenAdLoaded = false;
        if (AppConfig.FORCE_TO_SHOW_APP_OPEN_AD_ON_START) {
            adsManager.destroyAppOpenAd();
            ProcessLifecycleOwner.get().getLifecycle().removeObserver(this);
        }
    }

    public void destroyBannerAd() {
        adsManager.destroyBannerAd();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    private void inAppReview() {
        if (sharedPref.getInAppReviewToken() <= 3) {
            sharedPref.updateInAppReviewToken(sharedPref.getInAppReviewToken() + 1);
        } else {
            ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    manager.launchReviewFlow(MainActivity.this, reviewInfo).addOnFailureListener(e -> {
                    }).addOnCompleteListener(complete -> {
                                Log.d(TAG, "In-App Review Success");
                            }
                    ).addOnFailureListener(failure -> {
                        Log.d(TAG, "In-App Review Rating Failed");
                    });
                }
            }).addOnFailureListener(failure -> Log.d("In-App Review", "In-App Request Failed " + failure));
        }
        Log.d(TAG, "in app review token : " + sharedPref.getInAppReviewToken());
    }

    private void inAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        appUpdateResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                showSnackBar(getString(R.string.msg_success_update));
            } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                showSnackBar(getString(R.string.msg_cancel_update));
                Log.d(TAG, "Update flow cancelled by user.");
            } else {
                showSnackBar(getString(R.string.msg_failed_update));
                Log.e(TAG, "Update flow failed with result code: " + result.getResultCode());
            }
        });

        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                startUpdateFlow(appUpdateInfo);
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdateFlow(appUpdateInfo);
            }
        });
    }

    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        AppUpdateOptions appUpdateOptions = AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).setAllowAssetPackDeletion(true).build();
        appUpdateManager.startUpdateFlowForResult(appUpdateInfo, appUpdateResultLauncher, appUpdateOptions);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.IMMEDIATE_APP_UPDATE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                showSnackBar(getString(R.string.msg_cancel_update));
            } else if (resultCode == RESULT_OK) {
                showSnackBar(getString(R.string.msg_success_update));
            } else {
                showSnackBar(getString(R.string.msg_failed_update));
                inAppUpdate();
            }
        }
    }

    private void openSettings() {
        Intent intent;
        if (AppConfig.ENABLE_NEW_APP_DESIGN) {
            intent = new Intent(getApplicationContext(), ActivitySettingsNew.class);
        } else {
            intent = new Intent(getApplicationContext(), ActivitySettings.class);
        }
        startActivity(intent);
    }

}

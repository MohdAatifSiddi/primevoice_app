package com.app.androidnewsapp.activity;

import static com.app.androidnewsapp.util.Constant.ITEM_ID;
import static com.app.androidnewsapp.util.Constant.ITEM_NAME;
import static com.app.androidnewsapp.util.Constant.LOCALHOST_ADDRESS;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.callback.CallbackSettings;
import com.app.androidnewsapp.config.AppConfig;
import com.app.androidnewsapp.database.prefs.AdsPref;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.models.Ads;
import com.app.androidnewsapp.models.App;
import com.app.androidnewsapp.models.License;
import com.app.androidnewsapp.models.Placement;
import com.app.androidnewsapp.models.Settings;
import com.app.androidnewsapp.rest.RestAdapter;
import com.app.androidnewsapp.util.AdsManager;
import com.app.androidnewsapp.util.Tag;
import com.app.androidnewsapp.util.Tools;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.solodroidx.ads.appopen.AppOpenAd;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySplash extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    SharedPref sharedPref;
    ImageView imgSplash;
    Call<CallbackSettings> callbackCall = null;
    AdsManager adsManager;
    AdsPref adsPref;
    Settings settings;
    Ads ads;
    Placement adsPlacement;
    App app;
    License license;
    boolean isForceOpenAds;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        Tools.transparentStatusBarNavigation(this);
        isForceOpenAds = AppConfig.FORCE_TO_SHOW_APP_OPEN_AD_ON_START;
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        adsManager = new AdsManager(this);
        adsManager.initializeAd();

        imgSplash = findViewById(R.id.img_splash);
        if (sharedPref.getIsDarkTheme()) {
            imgSplash.setImageResource(R.drawable.bg_splash_dark);
        } else {
            imgSplash.setImageResource(R.drawable.bg_splash_default);
        }

        Tools.postDelayed(this::requestConfig, AppConfig.DELAY_SPLASH_SCREEN);

    }

    @SuppressWarnings("ConstantValue")
    private void requestConfig() {
        if (AppConfig.SERVER_KEY.contains("XXXX")) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("App not configured")
                    .setMessage("Please put your Server Key and Rest API Key from settings menu in your admin panel to AppConfig, you can see the documentation for more detailed instructions.")
                    .setPositiveButton(getString(R.string.dialog_ok), (dialogInterface, i) -> showAppOpenAdIfAvailable())
                    .setCancelable(false)
                    .show();
        } else {
            String data = Tools.decode(AppConfig.SERVER_KEY);
            String[] results = data.split("_applicationId_");
            String baseUrl = results[0].replace("localhost", LOCALHOST_ADDRESS);
            String applicationId = results[1];
            sharedPref.setBaseUrl(baseUrl);

            if (applicationId.equals(Tools.getApplicationId())) {
                if (Tools.isConnect(this)) {
                    requestAPI(baseUrl);
                } else {
                    showAppOpenAdIfAvailable();
                }
            } else {
                String message = "applicationId does not match, applicationId in your app is : " + Tools.getApplicationId() +
                        "\n\n But your Server Key is registered with applicationId : " + applicationId + "\n\n" +
                        "Please update your Server Key with the appropriate registration applicationId that is used in your Android project.";
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Error")
                        .setMessage(Html.fromHtml(message))
                        .setPositiveButton(getString(R.string.dialog_ok), (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            }
        }
    }

    private void requestAPI(String apiUrl) {
        this.callbackCall = RestAdapter.createAPI(apiUrl).getSettings(Tools.getApplicationId(), AppConfig.REST_API_KEY);
        this.callbackCall.enqueue(new Callback<>() {
            public void onResponse(@NonNull Call<CallbackSettings> call, @NonNull Response<CallbackSettings> response) {
                CallbackSettings resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    settings = resp.settings;
                    app = resp.app;
                    ads = resp.ads;
                    adsPlacement = resp.ads_placement;
                    license = resp.license;

                    adsPref.saveAds(
                            ads.ad_status.equals("on"),
                            ads.ad_type,
                            ads.backup_ads,
                            ads.admob_publisher_id,
                            ads.admob_banner_unit_id,
                            ads.admob_interstitial_unit_id,
                            ads.admob_native_unit_id,
                            ads.admob_app_open_ad_unit_id,
                            ads.ad_manager_banner_unit_id,
                            ads.ad_manager_interstitial_unit_id,
                            ads.ad_manager_native_unit_id,
                            ads.ad_manager_app_open_ad_unit_id,
                            ads.fan_banner_unit_id,
                            ads.fan_interstitial_unit_id,
                            ads.fan_native_unit_id,
                            ads.startapp_app_id,
                            ads.unity_game_id,
                            ads.unity_banner_placement_id,
                            ads.unity_interstitial_placement_id,
                            ads.applovin_banner_ad_unit_id,
                            ads.applovin_interstitial_ad_unit_id,
                            ads.applovin_native_ad_manual_unit_id,
                            ads.applovin_app_open_ad_unit_id,
                            ads.applovin_banner_zone_id,
                            ads.applovin_banner_mrec_zone_id,
                            ads.applovin_interstitial_zone_id,
                            ads.ironsource_app_key,
                            ads.ironsource_banner_placement_name,
                            ads.ironsource_interstitial_placement_name,
                            ads.wortise_app_id,
                            ads.wortise_app_open_unit_id,
                            ads.wortise_banner_unit_id,
                            ads.wortise_interstitial_unit_id,
                            ads.wortise_native_unit_id,
                            ads.interstitial_ad_interval,
                            ads.native_ad_index
                    );

                    adsPref.setNativeAdStyle(
                            ads.native_ad_style_post_list,
                            ads.native_ad_style_video_list,
                            ads.native_ad_style_post_details,
                            ads.native_ad_style_exit_dialog
                    );

                    adsPref.setPlacement(
                            adsPlacement.banner_home == 1,
                            adsPlacement.banner_post_details == 1,
                            adsPlacement.banner_category_details == 1,
                            adsPlacement.banner_search == 1,
                            adsPlacement.banner_comment == 1,
                            adsPlacement.interstitial_post_list == 1,
                            adsPlacement.interstitial_post_details == 1,
                            adsPlacement.native_ad_post_list == 1,
                            adsPlacement.native_ad_post_details == 1,
                            adsPlacement.native_ad_exit_dialog == 1,
                            adsPlacement.app_open_ad_on_start == 1,
                            adsPlacement.app_open_ad_on_resume == 1
                    );

                    sharedPref.setConfig(
                            settings.privacy_policy,
                            settings.publisher_info,
                            settings.login_feature,
                            settings.comment_approval,
                            settings.video_menu,
                            settings.more_apps_url,
                            settings.youtube_api_key,
                            license.item_id,
                            license.item_name,
                            license.license_type,
                            app.redirect_url,
                            settings.providers,
                            settings.account_deletion_message
                    );

                    if (license.item_id.equals(ITEM_ID) && license.item_name.equals(ITEM_NAME)) {
                        if (app.status != null && app.status.equals("0")) {
                            Tools.postDelayed(() -> {
                                startActivity(new Intent(getApplicationContext(), ActivityRedirect.class));
                                finish();
                            }, 100);
                            Log.d(TAG, "App is inactive, call redirect method");
                        } else {
                            Tools.postDelayed(() -> showAppOpenAdIfAvailable(), 100);
                            Log.d(TAG, "App is active");
                        }
                        Log.d(TAG, "License is valid");
                    } else {
                        showDialogInvalidLicense();
                    }
                    Log.d(Tag.RETROFIT, TAG + " onSuccessResponse: " + response.message());
                } else if (resp != null && resp.status.equals("error")) {
                    showDialogInvalidLicense();
                } else {
                    showAppOpenAdIfAvailable();
                    Log.d(Tag.RETROFIT, TAG + " onErrorResponse: " + response.message());
                }
            }

            public void onFailure(@NonNull Call<CallbackSettings> call, @NonNull Throwable th) {
                Log.d(Tag.RETROFIT, TAG + " onFailure: " + th.getMessage());
                showAppOpenAdIfAvailable();
            }
        });
    }

    private void showAppOpenAdIfAvailable() {
        if (isForceOpenAds) {
            if (adsPref.getIsAppOpenAdOnStart()) {
                adsManager.loadAppOpenAd(adsPref.getIsAppOpenAdOnStart(), true, () -> {
                    startMainActivity();
                    AppOpenAd.isAppOpenAdLoaded = false;
                    Log.d(TAG, "showAppOpenAdIfAvailable");
                });
            } else {
                startMainActivity();
                Log.d(TAG, "app open on start disabled");
            }
        } else {
            if (adsPref.getAdStatus() && adsPref.getIsAppOpenAdOnStart()) {
                Application application = getApplication();
                ((MyApplication) application).showAdIfAvailable(this, this::startMainActivity);
            } else {
                startMainActivity();
                Log.d(TAG, "startMainActivity");
            }
        }
    }

    private void showDialogInvalidLicense() {
        new MaterialAlertDialogBuilder(ActivitySplash.this)
                .setTitle("Invalid License")
                .setMessage("Whoops! this application cannot be accessed due to invalid item purchase code, if you are the owner of this application, please buy this item officially on Codecanyon to get item purchase code so that the application can be accessed by your users.")
                .setPositiveButton("Buy this item", (dialog, which) -> {
                    startMainActivity();
                    Tools.purchaseItem(this);
                })
                .setNegativeButton("Later", (dialog, which) -> startMainActivity())
                .setCancelable(false)
                .show();
    }

    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

}

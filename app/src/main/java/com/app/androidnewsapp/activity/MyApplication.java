package com.app.androidnewsapp.activity;

import static com.solodroidx.ads.util.Constant.AD_STATUS_ON;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.config.AppConfig;
import com.app.androidnewsapp.database.prefs.AdsPref;
import com.app.androidnewsapp.util.Constant;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.solodroid.push.sdk.provider.OneSignalPush;
import com.solodroidx.ads.appopen.AppOpenAd;
import com.solodroidx.ads.appopen.AppOpenAdAppLovin;
import com.solodroidx.ads.appopen.AppOpenAdManager;
import com.solodroidx.ads.appopen.AppOpenAdMob;
import com.solodroidx.ads.appopen.AppOpenAdPangle;
import com.solodroidx.ads.appopen.AppOpenAdWortise;
import com.solodroidx.ads.appopen.AppOpenAdYandex;
import com.solodroidx.ads.listener.OnShowAdCompleteListener;

public class MyApplication extends Application {

    public static final String TAG = "MyApplication";
    public String prefName = "news_pref";
    public static MyApplication mInstance;
    FirebaseAnalytics mFirebaseAnalytics;
    AdsPref adsPref;
    AppOpenAd appOpenAd;

    @Override
    public void onCreate() {
        super.onCreate();
        adsPref = new AdsPref(this);
        initFirebase();
        initOpenAds();
        initNotification();
    }

    private void initFirebase() {
        FirebaseApp.initializeApp(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void initNotification() {
        new OneSignalPush.Builder(this)
                .setOneSignalAppId(getResources().getString(R.string.onesignal_app_id))
                .build(() -> {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(OneSignalPush.EXTRA_ID, OneSignalPush.Data.id);
                    intent.putExtra(OneSignalPush.EXTRA_TITLE, OneSignalPush.Data.title);
                    intent.putExtra(OneSignalPush.EXTRA_MESSAGE, OneSignalPush.Data.message);
                    intent.putExtra(OneSignalPush.EXTRA_IMAGE, OneSignalPush.Data.bigImage);
                    intent.putExtra(OneSignalPush.EXTRA_LAUNCH_URL, OneSignalPush.Data.launchUrl);
                    intent.putExtra(OneSignalPush.EXTRA_UNIQUE_ID, OneSignalPush.AdditionalData.uniqueId);
                    intent.putExtra(OneSignalPush.EXTRA_POST_ID, OneSignalPush.AdditionalData.postId);
                    intent.putExtra(OneSignalPush.EXTRA_LINK, OneSignalPush.AdditionalData.link);
                    startActivity(intent);
                });
        FirebaseMessaging.getInstance().subscribeToTopic(getResources().getString(R.string.fcm_notification_topic));
    }

    private void initOpenAds() {
        adsPref = new AdsPref(this);
        appOpenAd = new AppOpenAd();
        if (adsPref.getAdStatus()) {
            if (!AppConfig.FORCE_TO_SHOW_APP_OPEN_AD_ON_START) {
                registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
                ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleObserver);
                appOpenAd.initAppOpenAdMob(new AppOpenAdMob())
                        .initAppOpenAdManager(new AppOpenAdManager())
                        .initAppOpenAdAppLovin(new AppOpenAdAppLovin())
                        .initAppOpenAdWortise(new AppOpenAdWortise())
                        .initAppOpenAdPangle(new AppOpenAdPangle())
                        .initAppOpenAdYandex(new AppOpenAdYandex())
                        .setAdStatus(AD_STATUS_ON)
                        .setAdNetwork(adsPref.getMainAds())
                        .setBackupAdNetwork(adsPref.getBackupAds())
                        .setPlacementOnStart(adsPref.getIsAppOpenAdOnStart())
                        .setPlacementOnResume(adsPref.getIsAppOpenAdOnResume())
                        .setAdMobAppOpenId(adsPref.getAdMobAppOpenAdId())
                        .setAdManagerAppOpenId(adsPref.getAdManagerAppOpenAdId())
                        .setApplovinAppOpenId(adsPref.getAppLovinAppOpenAdUnitId())
                        .setWortiseAppOpenId(adsPref.getWortiseAppOpenId());
            }
        }
    }

    LifecycleObserver lifecycleObserver = new DefaultLifecycleObserver() {
        @Override
        public void onStart(@NonNull LifecycleOwner owner) {
            DefaultLifecycleObserver.super.onStart(owner);
            if (AppOpenAd.isAppOpenAdLoaded) {
                if (appOpenAd != null) {
                    appOpenAd.setOnStartLifecycleObserver();
                }
            }
        }
    };

    ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            if (appOpenAd != null) {
                appOpenAd.setOnStartActivityLifecycleCallbacks(activity);
            }
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
        }
    };

    public void showAdIfAvailable(@NonNull Activity activity, @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
        if (appOpenAd != null) {
            appOpenAd.showAdIfAvailable(activity, onShowAdCompleteListener);
        }
    }

}

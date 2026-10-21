package com.app.androidnewsapp.database.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {

    Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public Boolean getIsFirstTimeLaunch() {
        return sharedPreferences.getBoolean("is_first_time", true);
    }

    public void setIsFirstTimeLaunch(Boolean isFirstTimeLaunch) {
        editor.putBoolean("is_first_time", isFirstTimeLaunch);
        editor.apply();
    }

    public Boolean getIsLogin() {
        return sharedPreferences.getBoolean("login", false);
    }

    public void setIsLogin(Boolean isLogin) {
        editor.putBoolean("login", isLogin);
        editor.apply();
    }

    public Boolean getIsDarkTheme() {
        return sharedPreferences.getBoolean("theme", false);
    }

    public void setIsDarkTheme(Boolean isDarkTheme) {
        editor.putBoolean("theme", isDarkTheme);
        editor.apply();
    }

    public void setIsJustify(Boolean isJustify) {
        editor.putBoolean("justify", isJustify);
        editor.apply();
    }

    public Boolean getIsJustify() {
        return sharedPreferences.getBoolean("justify", false);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return sharedPreferences.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public Integer getFontSize() {
        return sharedPreferences.getInt("font_size", 2);
    }

    public void updateFontSize(int font_size) {
        editor.putInt("font_size", font_size);
        editor.apply();
    }

    public void setBaseUrl(String baseUrl) {
        editor.putString("base_url", baseUrl);
        editor.apply();
    }

    public void setConfig(String privacyPolicy, String publisherInfo, String loginFeature, String commentApproval, String videoMenu, String moreAppsUrl, String youtubeApiKey, String itemId, String itemName, String licenseType, String redirectUrl, String providers, String accountDeletionMessage) {
        editor.putString("privacy_policy", privacyPolicy);
        editor.putString("publisher_info", publisherInfo);
        editor.putString("login_feature", loginFeature);
        editor.putString("comment_approval", commentApproval);
        editor.putString("video_menu", videoMenu);
        editor.putString("more_apps_url", moreAppsUrl);
        editor.putString("youtube_api_key", youtubeApiKey);
        editor.putString("item_id", itemId);
        editor.putString("item_name", itemName);
        editor.putString("license_type", licenseType);
        editor.putString("redirect_url", redirectUrl);
        editor.putString("providers", providers);
        editor.putString("account_deletion_message", accountDeletionMessage);
        editor.apply();
    }

    public String getBaseUrl() {
        return sharedPreferences.getString("base_url", "");
    }

    public String getPrivacyPolicy() {
        return sharedPreferences.getString("privacy_policy", "");
    }

    public String getPublisherInfo() {
        return sharedPreferences.getString("publisher_info", "");
    }

    public String getLoginFeature() {
        return sharedPreferences.getString("login_feature", "yes");
    }

    public String getCommentApproval() {
        return sharedPreferences.getString("comment_approval", "no");
    }

    public String getVideoMenu() {
        return sharedPreferences.getString("video_menu", "yes");
    }

    public String getMoreAppsUrl() {
        return sharedPreferences.getString("more_apps_url", "");
    }

    public String getYoutubeApiKey() {
        return sharedPreferences.getString("youtube_api_key", "0");
    }

    public String getRedirectUrl() {
        return sharedPreferences.getString("redirect_url", "");
    }

    public String getPushNotificationProvider() {
        return sharedPreferences.getString("providers", "");
    }

    public String getAccountDeletionMessage() {
        return sharedPreferences.getString("account_deletion_message", "");
    }

    public String getItemId() {
        return sharedPreferences.getString("item_id", "");
    }

    public String getItemName() {
        return sharedPreferences.getString("item_name", "");
    }

    public String getLicenseType() {
        return sharedPreferences.getString("license_type", "");
    }

    public Integer getInAppReviewToken() {
        return sharedPreferences.getInt("in_app_review_token", 0);
    }

    public void updateInAppReviewToken(int value) {
        editor.putInt("in_app_review_token", value);
        editor.apply();
    }

    public void setUserId(String userId) {
        editor.putString("user_id", userId);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString("user_id", "");
    }

    public void setUserName(String userName) {
        editor.putString("user_name", userName);
        editor.apply();
    }

    public String getUserName() {
        return sharedPreferences.getString("user_name", "");
    }

    public void setUserEmail(String userEmail) {
        editor.putString("user_email", userEmail);
        editor.apply();
    }

    public String getUserEmail() {
        return sharedPreferences.getString("user_email", "");
    }

    public void setUserImage(String userImage) {
        editor.putString("user_image", userImage);
        editor.apply();
    }

    public String getUserImage() {
        return sharedPreferences.getString("user_image", "");
    }

    public void setUserPassword(String userPassword) {
        editor.putString("user_password", userPassword);
        editor.apply();
    }

    public String getUserPassword() {
        return sharedPreferences.getString("user_password", "");
    }

}

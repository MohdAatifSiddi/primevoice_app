package com.app.androidnewsapp.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.adapter.AdapterSearch;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.util.Constant;
import com.app.androidnewsapp.util.MaterialProgressDialog;
import com.app.androidnewsapp.util.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.solodroid.push.sdk.provider.OneSignalPush;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.DecimalFormat;

public class ActivitySettings extends AppCompatActivity {

    RelativeLayout lytUser;
    View lytSignIn, lytSignOut;
    TextView txtLogin;
    TextView txtRegister, txtUsername, txtEmail;
    ImageView imgProfile;
    MaterialProgressDialog.Builder progressDialog;
    Button btnLogout;
    SharedPref sharedPref;
    MaterialSwitch materialSwitch;
    RelativeLayout btnSwitchTheme;
    RelativeLayout btnTextSize;
    RelativeLayout btnNotification;

    TextView txtCacheSize;
    RelativeLayout btnClearSearchHistory;
    RelativeLayout btnPublisherInfo;
    RelativeLayout btnPrivacyPolicy;
    RelativeLayout btnShare;
    RelativeLayout btnRate;
    RelativeLayout btnMore;
    RelativeLayout btnAbout;
    AdapterSearch adapterSearch;
    private String singleChoiceSelected;
    LinearLayout parentView;
    OnBackPressedDispatcher onBackPressedDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_settings);
        Tools.setNavigation(this);
        Tools.setupAppBarLayout(this);

        sharedPref = new SharedPref(this);

        setupToolbar();
        initView();
        initSettings();
        requestAction();
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

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        String title;
        if (sharedPref.getLoginFeature().equals("yes")) {
            title = getResources().getString(R.string.title_menu_profile);
        } else {
            title = getResources().getString(R.string.title_menu_settings);
        }
        Tools.setupToolbar(this, toolbar, title, true);
    }

    private void initView() {

        parentView = findViewById(R.id.parent_view);
        lytUser = findViewById(R.id.lyt_user);

        lytSignIn = findViewById(R.id.view_sign_in);
        lytSignOut = findViewById(R.id.view_sign_out);

        txtLogin = findViewById(R.id.btn_login);
        txtRegister = findViewById(R.id.txt_register);

        txtUsername = findViewById(R.id.txt_username);
        txtEmail = findViewById(R.id.txt_email);
        imgProfile = findViewById(R.id.img_profile);

        materialSwitch = findViewById(R.id.switch_theme);
        btnSwitchTheme = findViewById(R.id.btn_switch_theme);
        btnTextSize = findViewById(R.id.btn_text_size);
        btnNotification = findViewById(R.id.btn_notification);
        btnClearSearchHistory = findViewById(R.id.btn_clear_search_history);
        btnPublisherInfo = findViewById(R.id.btn_publisher_info);
        btnPrivacyPolicy = findViewById(R.id.btn_privacy_policy);
        btnShare = findViewById(R.id.btn_share);
        btnRate = findViewById(R.id.btn_rate);
        btnMore = findViewById(R.id.btn_more);
        btnAbout = findViewById(R.id.btn_about);

        btnLogout = findViewById(R.id.btn_logout);

        viewVisibility();

    }

    private void viewVisibility() {
        btnSwitchTheme.setVisibility(View.VISIBLE);
        btnTextSize.setVisibility(View.VISIBLE);
        btnNotification.setVisibility(View.VISIBLE);
        btnClearSearchHistory.setVisibility(View.VISIBLE);
        btnPublisherInfo.setVisibility(View.VISIBLE);
        btnPrivacyPolicy.setVisibility(View.VISIBLE);
        btnShare.setVisibility(View.VISIBLE);
        btnRate.setVisibility(View.VISIBLE);
        btnMore.setVisibility(View.VISIBLE);
        btnAbout.setVisibility(View.VISIBLE);
    }

    private void initSettings() {

        if (!sharedPref.getLoginFeature().equals("yes")) {
            lytUser.setVisibility(View.GONE);
            btnLogout.setVisibility(View.GONE);
        }

        materialSwitch.setChecked(sharedPref.getIsDarkTheme());
        materialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPref.setIsDarkTheme(isChecked);
            Tools.postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }, 400);
        });

        btnSwitchTheme.setOnClickListener(v -> {
            if (materialSwitch.isChecked()) {
                sharedPref.setIsDarkTheme(false);
                materialSwitch.setChecked(false);
            } else {
                sharedPref.setIsDarkTheme(true);
                materialSwitch.setChecked(true);
            }
            Tools.postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }, 400);
        });

        btnTextSize.setOnClickListener(v -> {
            String[] items = getResources().getStringArray(R.array.dialog_font_size);
            singleChoiceSelected = items[sharedPref.getFontSize()];
            int itemSelected = sharedPref.getFontSize();
            new MaterialAlertDialogBuilder(ActivitySettings.this)
                    .setTitle(getString(R.string.title_dialog_font_size))
                    .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
                    .setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
                        if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xsmall))) {
                            sharedPref.updateFontSize(0);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_small))) {
                            sharedPref.updateFontSize(1);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_medium))) {
                            sharedPref.updateFontSize(2);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_large))) {
                            sharedPref.updateFontSize(3);
                        } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xlarge))) {
                            sharedPref.updateFontSize(4);
                        } else {
                            sharedPref.updateFontSize(2);
                        }
                        dialogInterface.dismiss();
                    })
                    .show();
        });

        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, Tools.getApplicationId());
            } else {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", Tools.getApplicationId());
                intent.putExtra("app_uid", getApplicationInfo().uid);
            }
            startActivity(intent);
        });

        if (new OneSignalPush.Builder(this).getSdkName().equals("no-notification-sdk")) {
            btnNotification.setVisibility(View.GONE);
        } else {
            btnNotification.setVisibility(View.VISIBLE);
        }

        txtCacheSize = findViewById(R.id.txt_cache_size);
        initializeCache();

        findViewById(R.id.btn_clear_cache).setOnClickListener(v -> clearCache());

        btnClearSearchHistory.setOnClickListener(v -> clearSearchHistory());

        btnPublisherInfo.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ActivityPublisherInfo.class)));

        btnPrivacyPolicy.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ActivityPrivacyPolicy.class)));

        btnShare.setOnClickListener(v -> Tools.shareApps(this));

        btnRate.setOnClickListener(v -> Tools.rateApps(this));

        btnMore.setOnClickListener(v -> Tools.moreApps(this, sharedPref.getMoreAppsUrl()));

        btnAbout.setOnClickListener(v -> Tools.aboutDialog(this));

    }

    private void requestAction() {
        if (sharedPref.getIsLogin()) {
            lytSignIn.setVisibility(View.VISIBLE);
            lytSignOut.setVisibility(View.GONE);

            btnLogout.setVisibility(View.VISIBLE);
            btnLogout.setOnClickListener(view -> logoutDialog());

            displayData();
        } else {
            lytSignIn.setVisibility(View.GONE);
            lytSignOut.setVisibility(View.VISIBLE);
            txtLogin.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ActivityUserLogin.class)));
            txtRegister.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ActivityUserRegister.class)));
            btnLogout.setVisibility(View.GONE);
        }
    }

    public void displayData() {

        ((TextView) findViewById(R.id.txt_username)).setText(Tools.usernameFormatter(sharedPref.getUserName()));
        ((TextView) findViewById(R.id.txt_email)).setText(sharedPref.getUserEmail());

        ImageView imgProfile = findViewById(R.id.img_profile);
        if (sharedPref.getUserImage().equals("")) {
            imgProfile.setImageResource(R.drawable.ic_user_account);
        } else {
            Glide.with(this)
                    .load(sharedPref.getBaseUrl() + "/upload/avatar/" + sharedPref.getUserImage().replace(" ", "%20"))
                    .placeholder(R.drawable.ic_user_account)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(new RequestOptions().override(256, 256))
                    .centerCrop()
                    .into(imgProfile);
        }

        RelativeLayout btnEdit = findViewById(R.id.btn_edit);
        btnEdit.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ActivityEditProfile.class);
            intent.putExtra("user_id", sharedPref.getUserId());
            intent.putExtra("user_name", Tools.usernameFormatter(sharedPref.getUserName()));
            intent.putExtra("user_email", sharedPref.getUserEmail());
            intent.putExtra("user_image", sharedPref.getUserImage());
            intent.putExtra("user_password", sharedPref.getUserPassword());
            startActivity(intent);
        });

    }

    private void onFailRequest() {
        if (!Tools.isConnect(this)) {
            Toast.makeText(getApplicationContext(), getString(R.string.msg_no_network), Toast.LENGTH_SHORT).show();
        }
    }

    public void logoutDialog() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ActivitySettings.this);
        builder.setTitle(R.string.logout_title);
        builder.setMessage(R.string.logout_message);
        builder.setPositiveButton(R.string.dialog_yes, (di, i) -> {

            progressDialog = new MaterialProgressDialog.Builder(ActivitySettings.this).build();
            progressDialog.setMessage(getResources().getString(R.string.logout_process));
            progressDialog.setCancelable(false);
            progressDialog.show();

            sharedPref.setIsLogin(false);
            sharedPref.setUserId("");
            sharedPref.setUserName("");
            sharedPref.setUserEmail("");
            sharedPref.setUserImage("");
            sharedPref.setUserPassword("");

            new Handler().postDelayed(() -> {
                progressDialog.dismiss();
                MaterialAlertDialogBuilder builder1 = new MaterialAlertDialogBuilder(ActivitySettings.this);
                builder1.setMessage(R.string.logout_success);
                builder1.setPositiveButton(R.string.dialog_ok, (dialogInterface, i1) -> finish());
                builder1.setCancelable(false);
                builder1.show();
            }, Constant.DELAY_PROGRESS_DIALOG);

            Constant.isProfileUpdated = true;

        });
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();

    }

    private void clearSearchHistory() {
        adapterSearch = new AdapterSearch(this);
        if (adapterSearch.getItemCount() > 0) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ActivitySettings.this);
            builder.setTitle(getString(R.string.title_dialog_clear_search_history));
            builder.setMessage(getString(R.string.msg_dialog_clear_search_history));
            builder.setPositiveButton(R.string.dialog_yes, (di, i) -> {
                adapterSearch.clearSearchHistory();
                Snackbar.make(parentView, getString(R.string.clearing_success), Snackbar.LENGTH_SHORT).show();
            });
            builder.setNegativeButton(R.string.dialog_cancel, null);
            builder.show();
        } else {
            Snackbar.make(parentView, getString(R.string.clearing_empty), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onResume() {
        super.onResume();
        requestAction();
    }

    private void clearCache() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ActivitySettings.this);
        builder.setTitle(getString(R.string.title_dialog_clear_cache));
        builder.setMessage(getString(R.string.msg_dialog_clear_cache));
        builder.setPositiveButton(R.string.dialog_yes, (di, i) -> {
            FileUtils.deleteQuietly(getCacheDir());
            FileUtils.deleteQuietly(getExternalCacheDir());
            txtCacheSize.setText(getString(R.string.sub_settings_clear_cache_start) + " 0 Bytes " + getString(R.string.sub_settings_clear_cache_end));
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.msg_cache_cleared), Snackbar.LENGTH_SHORT).show();
        });
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();
    }

    private void initializeCache() {
        txtCacheSize.setText(getString(R.string.sub_settings_clear_cache_start) + " " + readableFileSize((0 + getDirSize(getCacheDir())) + getDirSize(getExternalCacheDir())) + " " + getString(R.string.sub_settings_clear_cache_end));
    }

    public long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0 Bytes";
        }
        String[] units = new String[]{"Bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0d));
        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        double d = (double) size;
        double pow = Math.pow(1024.0d, (double) digitGroups);
        Double.isNaN(d);
        stringBuilder.append(decimalFormat.format(d / pow));
        stringBuilder.append(" ");
        stringBuilder.append(units[digitGroups]);
        return stringBuilder.toString();
    }

}

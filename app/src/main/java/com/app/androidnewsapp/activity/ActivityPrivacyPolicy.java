package com.app.androidnewsapp.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.util.Tools;

public class ActivityPrivacyPolicy extends AppCompatActivity {

    SharedPref sharedPref;
    WebView webView;
    OnBackPressedDispatcher onBackPressedDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_privacy_policy);
        Tools.setNavigation(this);
        Tools.setupAppBarLayout(this);
        sharedPref = new SharedPref(this);
        webView = findViewById(R.id.webView);
        Tools.displayPostDescription(this, webView, sharedPref.getPrivacyPolicy());
        setupToolbar();
        handleOnBackPressed();
    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, getResources().getString(R.string.title_settings_privacy), true);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

}

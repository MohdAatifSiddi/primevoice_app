package com.app.androidnewsapp.activity;

import static com.app.androidnewsapp.util.Tools.PERMISSIONS_REQUEST;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.util.Tools;
import com.app.androidnewsapp.util.TouchImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ActivityFullScreenImage extends AppCompatActivity {

    TouchImageView newsImage;
    String strImage;
    SharedPref sharedPref;
    OnBackPressedDispatcher onBackPressedDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        Tools.setNavigation(this);
        sharedPref = new SharedPref(this);
        strImage = getIntent().getStringExtra("image");

        newsImage = findViewById(R.id.image);

        Glide.with(this)
                .load(sharedPref.getBaseUrl() + "/upload/" + strImage.replace(" ", "%20"))
                .placeholder(R.drawable.ic_thumbnail)
                .thumbnail(Tools.RequestBuilder(this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(newsImage);

        initToolbar();

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

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == R.id.close_image) {
            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 300);
            return true;
        } else if (itemId == R.id.save_image) {
            new Handler(Looper.getMainLooper()).postDelayed(this::requestStoragePermission, 300);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(ActivityFullScreenImage.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSIONS_REQUEST);
            } else {
                downloadImage();
            }
        } else {
            downloadImage();
        }
    }

    public void downloadImage() {
        String image_name = strImage;
        String image_url = sharedPref.getBaseUrl() + "/upload/" + strImage;
        Tools.downloadImage(this, image_name, image_url, "image/jpeg");
    }

}

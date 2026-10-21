package com.app.androidnewsapp.activity;

import static com.app.androidnewsapp.util.Tools.PERMISSIONS_REQUEST;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.adapter.AdapterImageSlider;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.models.Images;
import com.app.androidnewsapp.util.Tools;

import java.util.ArrayList;
import java.util.List;

public class ActivityImageSlider extends AppCompatActivity {

    ImageButton btnClose, btnSave;
    TextView txtNumber;
    ViewPager2 viewPager2;
    List<Images> images = new ArrayList<>();
    int position;
    SharedPref sharedPref;
    AdapterImageSlider adapterImageSlider;
    OnBackPressedDispatcher onBackPressedDispatcher;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppDarkTheme);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_slider);
        Tools.setImageSliderNavigation(this);
        sharedPref = new SharedPref(this);

        viewPager2 = findViewById(R.id.viewPager2);
        btnClose = findViewById(R.id.lyt_close);
        btnSave = findViewById(R.id.lyt_save);
        txtNumber = findViewById(R.id.txt_number);

        position = getIntent().getIntExtra("position", 0);
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bundle");
        images = (List<Images>) bundle.getSerializable("arrayList");

        loadImages(images, position);
        setupViewPager(images);

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

    public void setupViewPager(final List<Images> images) {
        adapterImageSlider = new AdapterImageSlider(this, images);
        viewPager2.setAdapter(adapterImageSlider);
        viewPager2.setOffscreenPageLimit(images.size());
        viewPager2.setCurrentItem(position, false);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                loadImages(images, position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

    }

    public void loadImages(final List<Images> images, int position) {
        txtNumber.setText((position + 1) + " of " + images.size());
        btnSave.setOnClickListener(view -> requestStoragePermission(images, position));
        btnClose.setOnClickListener(view -> finish());
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void requestStoragePermission(final List<Images> images, final int position) {
        if (ContextCompat.checkSelfPermission(ActivityImageSlider.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSIONS_REQUEST);
            } else {
                downloadImage(images, position);
            }
        } else {
            downloadImage(images, position);
        }
    }

    public void downloadImage(final List<Images> images, final int position) {
        String image_name = images.get(position).image_name;
        String image_url = sharedPref.getBaseUrl() + "/upload/" + images.get(position).image_name;
        Tools.downloadImage(this, image_name, image_url, "image/jpeg");
    }

}

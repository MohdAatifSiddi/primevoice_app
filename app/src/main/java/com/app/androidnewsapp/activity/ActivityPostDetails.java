package com.app.androidnewsapp.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.adapter.AdapterImageDetail;
import com.app.androidnewsapp.adapter.AdapterPost;
import com.app.androidnewsapp.callback.CallbackPostDetail;
import com.app.androidnewsapp.config.AppConfig;
import com.app.androidnewsapp.database.prefs.AdsPref;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.database.sqlite.DbHandler;
import com.app.androidnewsapp.models.Images;
import com.app.androidnewsapp.models.Post;
import com.app.androidnewsapp.models.Value;
import com.app.androidnewsapp.rest.ApiInterface;
import com.app.androidnewsapp.rest.RestAdapter;
import com.app.androidnewsapp.util.AdsManager;
import com.app.androidnewsapp.util.AppBarLayoutBehavior;
import com.app.androidnewsapp.util.Constant;
import com.app.androidnewsapp.util.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.Serializable;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityPostDetails extends AppCompatActivity {

    private static final String TAG = "ActivityPostDetail";
    private Call<CallbackPostDetail> callbackCall = null;
    private View lytMainContent;
    private Post post;
    TextView txtTitle, txtCategory, txtDate, txtCommentCount, txtCommentText, txtViewCount;
    ImageView imgThumbVideo;
    ImageView icDate;
    ImageView icView;
    ImageView icComment;
    ImageButton btnFavorite;
    ImageButton btnFontSize;
    ImageButton btnOverflow;
    LinearLayout btnComment, btnView;
    private WebView webView;
    DbHandler dbHandler;
    CoordinatorLayout parentView;
    private ShimmerFrameLayout lytShimmer;
    RelativeLayout lytRelated;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String singleChoiceSelected;
    ViewPager2 viewPager2;
    TabLayout tabLayout;
    ImageView postImage;
    SharedPref sharedPref;
    AdsPref adsPref;
    AdsManager adsManager;
    Tools tools;
    OnBackPressedDispatcher onBackPressedDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_post_detail);
        Tools.setNavigation(this);
        Tools.setupAppDetailBarLayout(this);
        post = (Post) getIntent().getSerializableExtra(Constant.EXTRA_OBJC);

        dbHandler = new DbHandler(this);
        tools = new Tools(this);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        adsManager = new AdsManager(this);
        adsManager.loadBannerAd(adsPref.getIsBannerPostDetails());
        adsManager.loadInterstitialAd(adsPref.getIsInterstitialPostDetails(), 1);

        LinearLayout nativeAdView = findViewById(R.id.native_ad_view);
        Tools.setNativeAdStyle(this, nativeAdView, adsPref.getNativeAdStylePostDetails());
        adsManager.loadNativeAd(adsPref.getIsNativeAdPostDetails(), adsPref.getNativeAdStylePostDetails());

        initView();
        initShimmerView();
        requestAction();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
            lytMainContent.setVisibility(View.GONE);
            requestAction();
        });

        setupToolbar();
        updateView(post.nid);

        handleOnBackPressed();

    }

    public void handleOnBackPressed() {
        onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                adsManager.destroyBannerAd();
            }
        });
    }

    private void initView() {
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary);
        swipeRefreshLayout.setRefreshing(false);

        btnFavorite = findViewById(R.id.btn_favorite);
        btnFontSize = findViewById(R.id.btn_font_size);
        btnOverflow = findViewById(R.id.btn_overflow);

        lytMainContent = findViewById(R.id.lyt_main_content);
        lytShimmer = findViewById(R.id.shimmer_view_container);
        parentView = findViewById(R.id.coordinatorLayout);
        webView = findViewById(R.id.news_description);
        txtTitle = findViewById(R.id.title);
        txtCategory = findViewById(R.id.category);
        txtDate = findViewById(R.id.date);
        icDate = findViewById(R.id.ic_date);
        icComment = findViewById(R.id.ic_comment);
        txtCommentCount = findViewById(R.id.txt_comment_count);
        txtCommentText = findViewById(R.id.txt_comment_text);
        txtViewCount = findViewById(R.id.txt_view_count);
        btnComment = findViewById(R.id.btn_comment);
        btnView = findViewById(R.id.btn_view);
        icView = findViewById(R.id.ic_view);
        viewPager2 = findViewById(R.id.viewPager2);
        tabLayout = findViewById(R.id.tab_layout);
        postImage = findViewById(R.id.image);
        imgThumbVideo = findViewById(R.id.thumbnail_video);
        lytRelated = findViewById(R.id.lyt_related);
    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        if (Tools.isConnect(this)) {
            Tools.postDelayed(this::requestPostData, 100);
        } else {
            Tools.postDelayed(()-> {
                displayPostData(post);
                swipeProgress(false);
            }, 1000);
        }
    }

    private void requestPostData() {
        this.callbackCall = RestAdapter.createAPI(sharedPref.getBaseUrl()).getNewsDetail(post.nid);
        this.callbackCall.enqueue(new Callback<>() {
            public void onResponse(@NonNull Call<CallbackPostDetail> call, @NonNull Response<CallbackPostDetail> response) {
                CallbackPostDetail resp = response.body();
                if (resp == null || !resp.status.equals("ok")) {
                    onFailRequest();
                    return;
                }
                displayAllData(resp);
                swipeProgress(false);
                lytMainContent.setVisibility(View.VISIBLE);
            }

            public void onFailure(@NonNull Call<CallbackPostDetail> call, @NonNull Throwable th) {
                Log.e("onFailure", th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        lytMainContent.setVisibility(View.GONE);
        if (Tools.isConnect(ActivityPostDetails.this)) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_offline));
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed_home);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            lytMainContent.setVisibility(View.VISIBLE);
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
            lytMainContent.setVisibility(View.GONE);
        });
    }

    private void displayAllData(CallbackPostDetail resp) {
        displayImages(resp.images);
        displayPostData(resp.post);
        displayRelated(resp.related);
    }

    private void displayPostData(final Post post) {
        txtTitle.setText(Html.fromHtml(post.news_title));
        txtCommentCount.setText("" + post.comments_count);

        new Handler().postDelayed(() -> {
            if (post.comments_count == 0) {
                txtCommentText.setText(R.string.txt_no_comment);
            }
            if (post.comments_count == 1) {
                txtCommentText.setText(getResources().getString(R.string.txt_read) + " " + post.comments_count + " " + getResources().getString(R.string.txt_comment));
            } else if (post.comments_count > 1) {
                txtCommentText.setText(getResources().getString(R.string.txt_read) + " " + post.comments_count + " " + getResources().getString(R.string.txt_comments));
            }
        }, 1500);

        Tools.displayPostDescription(this, webView, post.news_description);

        txtCategory.setText(post.category_name);
        txtCategory.setBackgroundColor(ContextCompat.getColor(this, R.color.color_category));

        if (AppConfig.SHOW_POST_DATE) {
            txtDate.setVisibility(View.VISIBLE);
            icDate.setVisibility(View.VISIBLE);
        } else {
            txtDate.setVisibility(View.GONE);
            icDate.setVisibility(View.GONE);
        }
        txtDate.setText(Tools.getFormatedDate(post.news_date));

        if (!post.content_type.equals("Post")) {
            imgThumbVideo.setVisibility(View.VISIBLE);
        } else {
            imgThumbVideo.setVisibility(View.GONE);
        }

        if (Tools.isConnect(this)) {
            Tools.postDelayed(() -> {
                lytRelated.setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.txt_related)).setText(getString(R.string.txt_suggested));
            }, 2000);
        } else {
            lytRelated.setVisibility(View.GONE);
        }

        btnComment.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityComments.class);
            intent.putExtra("nid", post.nid);
            intent.putExtra("count", post.comments_count);
            intent.putExtra("post_title", post.news_title);
            startActivity(intent);
            adsManager.destroyBannerAd();
        });

        txtCommentText.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityComments.class);
            intent.putExtra("nid", post.nid);
            intent.putExtra("count", post.comments_count);
            intent.putExtra("post_title", post.news_title);
            startActivity(intent);
            adsManager.destroyBannerAd();
        });

        if (!sharedPref.getLoginFeature().equals("yes")) {
            btnComment.setVisibility(View.GONE);
            txtCommentText.setVisibility(View.GONE);
        } else {
            btnComment.setVisibility(View.VISIBLE);
            txtCommentText.setVisibility(View.VISIBLE);
        }

        if (AppConfig.SHOW_POST_VIEW_COUNT) {
            btnView.setVisibility(View.VISIBLE);
            txtViewCount.setText("" + Tools.withSuffix(post.view_count));
        } else {
            btnView.setVisibility(View.GONE);
        }

        if (!Tools.isConnect(this)) {
            RelativeLayout lytImage = findViewById(R.id.lyt_image);
            if (AppConfig.ENABLE_NEW_APP_DESIGN) {
                lytImage.setPadding(
                        getResources().getDimensionPixelSize(R.dimen.item_post_padding_medium),
                        getResources().getDimensionPixelSize(R.dimen.item_post_padding_medium),
                        getResources().getDimensionPixelSize(R.dimen.item_post_padding_medium),
                        getResources().getDimensionPixelSize(R.dimen.no_padding)
                );
                CardView cardView = findViewById(R.id.card_view);
                cardView.setRadius(getResources().getDimensionPixelSize(R.dimen.rounded_corner_radius));
            } else {
                lytImage.setPadding(
                        getResources().getDimensionPixelSize(R.dimen.no_padding),
                        getResources().getDimensionPixelSize(R.dimen.no_padding),
                        getResources().getDimensionPixelSize(R.dimen.no_padding),
                        getResources().getDimensionPixelSize(R.dimen.no_padding)
                );
            }
            postImage.setVisibility(View.VISIBLE);
            btnView.setVisibility(View.GONE);
            viewPager2.setVisibility(View.GONE);
            tabLayout.setVisibility(View.GONE);
            if (post.content_type != null && post.content_type.equals("youtube")) {
                Glide.with(this)
                        .load(Constant.YOUTUBE_IMG_FRONT + post.video_id + Constant.YOUTUBE_IMG_BACK)
                        .placeholder(R.drawable.ic_thumbnail)
                        .thumbnail(Tools.RequestBuilder(this))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(postImage);
            } else {
                Glide.with(this)
                        .load(sharedPref.getBaseUrl() + "/upload/" + post.news_image.replace(" ", "%20"))
                        .placeholder(R.drawable.ic_thumbnail)
                        .thumbnail(Tools.RequestBuilder(this))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(postImage);
            }

            postImage.setOnClickListener(v -> {
                switch (post.content_type) {
                    case "youtube": {
                        Intent intent = new Intent(getApplicationContext(), ActivityYoutubePlayer.class);
                        intent.putExtra("video_id", post.video_id);
                        startActivity(intent);
                        break;
                    }
                    case "Url": {
                        Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                        intent.putExtra("video_url", post.video_url);
                        startActivity(intent);
                        break;
                    }
                    case "Upload": {
                        Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                        intent.putExtra("video_url", sharedPref.getBaseUrl() + "/upload/video/" + post.video_url);
                        startActivity(intent);
                        break;
                    }
                    default: {
                        Intent intent = new Intent(getApplicationContext(), ActivityFullScreenImage.class);
                        intent.putExtra("image",  post.news_image);
                        startActivity(intent);
                    }
                }
            });
        } else {
            postImage.setVisibility(View.GONE);
            viewPager2.setVisibility(View.VISIBLE);
        }

    }

    private void displayImages(final List<Images> images) {
        AdapterImageDetail adapter = new AdapterImageDetail(ActivityPostDetails.this, images);
        viewPager2.setAdapter(adapter);
        viewPager2.setOffscreenPageLimit(images.size());
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
        }).attach();

        if (images.size() > 1) {
            tabLayout.setVisibility(View.VISIBLE);
        } else {
            tabLayout.setVisibility(View.GONE);
        }

        adapter.setOnItemClickListener((view, p, position) -> {
            switch (p.content_type) {
                case "youtube": {
                    Intent intent = new Intent(getApplicationContext(), ActivityYoutubePlayer.class);
                    intent.putExtra("video_id", p.video_id);
                    startActivity(intent);
                    break;
                }
                case "Url": {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", post.video_url);
                    startActivity(intent);
                    break;
                }
                case "Upload": {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", sharedPref.getBaseUrl() + "/upload/video/" + post.video_url);
                    startActivity(intent);
                    break;
                }
                default: {
                    Intent intent = new Intent(getApplicationContext(), ActivityImageSlider.class);
                    intent.putExtra("position", position);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("arrayList", (Serializable) images);
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
                    break;
                }
            }

            showInterstitialAdCounter();

        });

    }

    private void showInterstitialAdCounter() {
        if (adsPref.getCounter() >= adsPref.getInterstitialAdInterval()) {
            Log.d("COUNTER_STATUS", "reset and show interstitial");
            adsPref.saveCounter(1);
            adsManager.showInterstitialAd();
        } else {
            adsPref.saveCounter(adsPref.getCounter() + 1);
        }
    }

    private void displayRelated(List<Post> list) {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_related);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
        AdapterPost adapterPost = new AdapterPost(ActivityPostDetails.this, recyclerView, list, false);
        recyclerView.setAdapter(adapterPost);
        recyclerView.setNestedScrollingEnabled(false);
        adapterPost.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityPostDetails.class);
            intent.putExtra(Constant.EXTRA_OBJC, obj);
            startActivity(intent);
            showInterstitialAdCounter();
            adsManager.destroyBannerAd();
        });
        adapterPost.setOnItemOverflowClickListener((view, obj, position) -> tools.showBottomSheetDialogMoreOptions(parentView, obj, false, () -> {

        }));
    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, "", true);

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(post.category_name);
        if (AppConfig.ENABLE_NEW_APP_DESIGN) {
            if (sharedPref.getIsDarkTheme()) {
                toolbarTitle.setTextColor(ContextCompat.getColor(this, R.color.color_dark_text));
                btnFontSize.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
                btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
                btnOverflow.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            } else {
                toolbarTitle.setTextColor(ContextCompat.getColor(this, R.color.color_light_text));
                btnFontSize.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
                btnFavorite.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
                btnOverflow.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            }
        }

        if (sharedPref.getIsDarkTheme()) {
            icDate.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            txtDate.setTextColor(ContextCompat.getColor(this, R.color.color_dark_text));
            icComment.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            txtCommentCount.setTextColor(ContextCompat.getColor(this, R.color.color_dark_text));
            icView.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            txtViewCount.setTextColor(ContextCompat.getColor(this, R.color.color_dark_text));
        } else {
            icDate.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            txtDate.setTextColor(ContextCompat.getColor(this, R.color.color_light_text));
            icComment.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            txtCommentCount.setTextColor(ContextCompat.getColor(this, R.color.color_light_text));
            icView.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            txtViewCount.setTextColor(ContextCompat.getColor(this, R.color.color_light_text));
        }

        List<Post> data = dbHandler.getFavRow(post.nid);
        if (data.size() == 0) {
            btnFavorite.setImageResource(R.drawable.ic_favorite_outline_white);
        } else {
            if (data.get(0).getNid() == post.nid) {
                btnFavorite.setImageResource(R.drawable.ic_favorite_white);
            }
        }

        btnFontSize.setOnClickListener(view -> {
            String[] items = getResources().getStringArray(R.array.dialog_font_size);
            singleChoiceSelected = items[sharedPref.getFontSize()];
            int itemSelected = sharedPref.getFontSize();
            MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(ActivityPostDetails.this);
            dialog.setTitle(getString(R.string.title_dialog_font_size));
            dialog.setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i]);
            dialog.setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
                WebSettings webSettings = webView.getSettings();
                if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xsmall))) {
                    sharedPref.updateFontSize(0);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_small))) {
                    sharedPref.updateFontSize(1);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_medium))) {
                    sharedPref.updateFontSize(2);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_large))) {
                    sharedPref.updateFontSize(3);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
                } else if (singleChoiceSelected.equals(getResources().getString(R.string.font_size_xlarge))) {
                    sharedPref.updateFontSize(4);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
                } else {
                    sharedPref.updateFontSize(2);
                    webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                }
                dialogInterface.dismiss();
            });
            dialog.show();
        });

        btnFavorite.setOnClickListener(view -> onFavoriteClicked(post));

        btnOverflow.setOnClickListener(view -> tools.showBottomSheetDialogMoreOptions(parentView, post, true, () -> {

        }));

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void onFavoriteClicked(Post post) {
        List<Post> data = dbHandler.getFavRow(post.nid);
        if (data.size() == 0) {
            dbHandler.AddtoFavorite(new Post(
                    post.nid,
                    post.news_title,
                    post.category_name,
                    post.news_date,
                    post.news_image,
                    post.news_description,
                    post.content_type,
                    post.video_url,
                    post.video_id,
                    post.comments_count
            ));
            Snackbar.make(parentView, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
            btnFavorite.setImageResource(R.drawable.ic_favorite_white);
        } else {
            if (data.get(0).getNid() == post.nid) {
                dbHandler.RemoveFav(new Post(post.nid));
                Snackbar.make(parentView, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show();
                btnFavorite.setImageResource(R.drawable.ic_favorite_outline_white);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed();
        }
        return true;
    }

    private void updateView(long nid) {
        if (Tools.isConnect(this)) {
            ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
            Call<Value> call = apiInterface.updateView(nid);
            call.enqueue(new Callback<Value>() {
                @Override
                public void onResponse(@NonNull Call<Value> call, @NonNull Response<Value> response) {
                    Value data = response.body();
                    if (data != null) {
                        Log.d(TAG, "View counter updated +" + data.value);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Value> call, @NonNull Throwable t) {
                    Log.d(TAG, "error " + t.getMessage());
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adsManager.resumeBannerAd(adsPref.getIsBannerPostDetails());
        Log.d("COUNTER", "counter : " + adsPref.getCounter());
    }

    public void onDestroy() {
        super.onDestroy();
        if (!(callbackCall == null || callbackCall.isCanceled())) {
            this.callbackCall.cancel();
        }
        lytShimmer.stopShimmer();
        adsManager.destroyBannerAd();
    }

    private void initShimmerView() {
        ViewStub shimmerPostDetail = findViewById(R.id.shimmer_view_post_details);
        if (AppConfig.ENABLE_NEW_APP_DESIGN) {
            shimmerPostDetail.setLayoutResource(R.layout.shimmer_new_post_detail);
        } else {
            shimmerPostDetail.setLayoutResource(R.layout.shimmer_post_detail);
        }
        shimmerPostDetail.inflate();
    }

}

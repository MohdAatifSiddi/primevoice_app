package com.app.androidnewsapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.adapter.AdapterPost;
import com.app.androidnewsapp.callback.CallbackCategoryDetails;
import com.app.androidnewsapp.config.AppConfig;
import com.app.androidnewsapp.database.prefs.AdsPref;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.models.Category;
import com.app.androidnewsapp.models.Post;
import com.app.androidnewsapp.rest.ApiInterface;
import com.app.androidnewsapp.rest.RestAdapter;
import com.app.androidnewsapp.util.AdsManager;
import com.app.androidnewsapp.util.Constant;
import com.app.androidnewsapp.util.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityCategoryDetails extends AppCompatActivity {

    Toolbar toolbar;
    private RecyclerView recyclerView;
    private AdapterPost adapterPost;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Call<CallbackCategoryDetails> callbackCall = null;
    private Category category;
    private long postTotal = 0;
    private long failedPage = 0;
    CoordinatorLayout parentView;
    private AdsManager adsManager;
    AdsPref adsPref;
    private ShimmerFrameLayout lytShimmer;
    List<Post> posts = new ArrayList<>();
    SharedPref sharedPref;
    Tools tools;
    OnBackPressedDispatcher onBackPressedDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_category_details);

        Tools.setNavigation(this);
        Tools.setupAppBarLayout(this);

        tools = new Tools(this);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        adsManager = new AdsManager(this);
        adsManager.loadBannerAd(adsPref.getIsBannerCategoryDetails());
        adsManager.loadInterstitialAd(adsPref.getIsInterstitialPostList(), adsPref.getInterstitialAdInterval());

        category = (Category) getIntent().getSerializableExtra(Constant.EXTRA_OBJC);
        if (category != null) {
            postTotal = category.post_count;
        }

        lytShimmer = findViewById(R.id.shimmer_view_container);
        initShimmerView();

        parentView = findViewById(R.id.parent_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        adapterPost = new AdapterPost(this, recyclerView, posts, true);
        recyclerView.setAdapter(adapterPost);

        adapterPost.setOnItemClickListener((v, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityPostDetails.class);
            intent.putExtra(Constant.EXTRA_OBJC, obj);
            startActivity(intent);
            adsManager.showInterstitialAd();
            adsManager.destroyBannerAd();
        });

        adapterPost.setOnItemOverflowClickListener((view, obj, position) -> tools.showBottomSheetDialogMoreOptions(parentView, obj, false, () -> {

        }));

        adapterPost.setOnLoadMoreListener(this::setLoadMore);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) {
                callbackCall.cancel();
            }
            adapterPost.resetListData();
            requestAction(1);
        });

        requestAction(1);

        initToolbar();

        if (AppConfig.SHOW_HEADER_VIEW && !AppConfig.BIG_NEWS_LIST_STYLE) {
            recyclerView.setPadding(0, 0, 0, getResources().getDimensionPixelOffset(R.dimen.item_post_padding_small));
        } else {
            recyclerView.setPadding(0, getResources().getDimensionPixelOffset(R.dimen.item_post_padding_small), 0, getResources().getDimensionPixelOffset(R.dimen.item_post_padding_small));
        }

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

    public void setLoadMore(int current_page) {
        Log.d("page", "currentPage: " + current_page);
        int totalItemBeforeAds = (adapterPost.getItemCount() - current_page);
        if (postTotal > totalItemBeforeAds && current_page != 0) {
            int next_page = current_page + 1;
            requestAction(next_page);
        } else {
            adapterPost.setLoaded();
        }
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, category.category_name, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed();
            return true;
        } else if (itemId == R.id.action_search) {
            Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void displayApiResult(final List<Post> posts) {
        adapterPost.insertDataWithNativeAd(posts);
        swipeProgress(false);
        if (posts.isEmpty()) {
            showNoItemView(true);
        }
    }

    private void requestPostApi(final long page_no) {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        callbackCall = apiInterface.getCategoryDetailsByPage(category.cid, AppConfig.REST_API_KEY, page_no, AppConfig.LOAD_MORE);
        callbackCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CallbackCategoryDetails> call, @NonNull Response<CallbackCategoryDetails> response) {
                CallbackCategoryDetails resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    displayApiResult(resp.posts);
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CallbackCategoryDetails> call, @NonNull Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }

        });
    }

    private void onFailRequest(long page_no) {
        failedPage = page_no;
        adapterPost.setLoaded();
        swipeProgress(false);
        if (Tools.isConnect(getApplicationContext())) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_offline));
        }
    }

    private void requestAction(final long page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            adapterPost.setLoading();
        }
        new Handler().postDelayed(() -> requestPostApi(page_no), Constant.DELAY_TIME);
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction(failedPage));
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.msg_no_news);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lytShimmer.stopShimmer();
        adsManager.destroyBannerAd();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adsManager.resumeBannerAd(adsPref.getIsBannerCategoryDetails());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initShimmerView() {
        ViewStub shimmerPostHead = findViewById(R.id.shimmer_view_head);
        ViewStub shimmerPostList = findViewById(R.id.shimmer_view_post);
        if (AppConfig.ENABLE_NEW_APP_DESIGN) {
            if (AppConfig.SHOW_HEADER_VIEW) {
                shimmerPostHead.setLayoutResource(R.layout.shimmer_new_post_head);
            } else {
                shimmerPostHead.setLayoutResource(R.layout.shimmer_new_post_list_default);
            }
            if (AppConfig.BIG_NEWS_LIST_STYLE) {
                shimmerPostList.setLayoutResource(R.layout.shimmer_new_post_list_large);
            } else {
                shimmerPostList.setLayoutResource(R.layout.shimmer_new_post_list_default);
            }
        } else {
            if (AppConfig.SHOW_HEADER_VIEW) {
                shimmerPostHead.setLayoutResource(R.layout.shimmer_post_head);
            } else {
                shimmerPostHead.setLayoutResource(R.layout.shimmer_post_list_default);
            }
            if (AppConfig.BIG_NEWS_LIST_STYLE) {
                shimmerPostList.setLayoutResource(R.layout.shimmer_post_list_large);
            } else {
                shimmerPostList.setLayoutResource(R.layout.shimmer_post_list_default);
            }
        }
        shimmerPostHead.inflate();
        shimmerPostList.inflate();
    }

}

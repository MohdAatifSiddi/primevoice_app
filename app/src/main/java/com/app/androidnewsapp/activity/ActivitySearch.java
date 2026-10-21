package com.app.androidnewsapp.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.adapter.AdapterPost;
import com.app.androidnewsapp.adapter.AdapterSearch;
import com.app.androidnewsapp.callback.CallbackRecent;
import com.app.androidnewsapp.config.AppConfig;
import com.app.androidnewsapp.database.prefs.AdsPref;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.models.Post;
import com.app.androidnewsapp.rest.ApiInterface;
import com.app.androidnewsapp.rest.RestAdapter;
import com.app.androidnewsapp.util.AdsManager;
import com.app.androidnewsapp.util.Constant;
import com.app.androidnewsapp.util.Tag;
import com.app.androidnewsapp.util.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySearch extends AppCompatActivity {

    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";
    private EditText edtSearch;
    private RecyclerView recyclerView;
    private AdapterPost adapterPost;
    private AdapterSearch adapterSearch;
    private LinearLayout lytSuggestion;
    private ImageButton btnClear;
    Call<CallbackRecent> callbackCall = null;
    private ShimmerFrameLayout lytShimmer;
    RecyclerView recyclerViewSuggestion;
    CoordinatorLayout parentView;
    AdsPref adsPref;
    private int postTotal = 0;
    private int failedPage = 0;
    private AdsManager adsManager;
    LinearLayout lytAds;
    SharedPref sharedPref;
    List<Post> posts = new ArrayList<>();
    Tools tools;
    OnBackPressedDispatcher onBackPressedDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_search);
        Tools.setNavigation(this);
        Tools.setupAppSearchBarLayout(this);
        adsPref = new AdsPref(this);
        adsManager = new AdsManager(this);
        sharedPref = new SharedPref(this);
        tools = new Tools(this);

        initComponent();
        adapterPost.setOnLoadMoreListener(this::setLoadMore);
        setupToolbar();
        adsManager.loadBannerAd(adsPref.getIsBannerSearch());
        adsManager.loadInterstitialAd(adsPref.getIsInterstitialPostList(), adsPref.getInterstitialAdInterval());
        handleOnBackPressed();

    }

    public void handleOnBackPressed() {
        onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (edtSearch.length() > 0) {
                    edtSearch.setText("");
                } else {
                    finish();
                    adsManager.destroyBannerAd();
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("ClickableViewAccessibility")
    private void initComponent() {
        parentView = findViewById(R.id.parent_view);
        edtSearch = findViewById(R.id.edt_search);
        btnClear = findViewById(R.id.bt_clear);
        btnClear.setVisibility(View.GONE);
        lytShimmer = findViewById(R.id.shimmer_view_container);
        initShimmerView();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        lytAds = findViewById(R.id.lyt_ads);
        recyclerViewSuggestion = findViewById(R.id.recyclerSuggestion);
        recyclerViewSuggestion.setLayoutManager(new LinearLayoutManager(this));

        edtSearch.addTextChangedListener(textWatcher);

        adapterPost = new AdapterPost(this, recyclerView, posts, false);
        recyclerView.setAdapter(adapterPost);

        adapterPost.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityPostDetails.class);
            intent.putExtra(EXTRA_OBJC, obj);
            startActivity(intent);
            adsManager.showInterstitialAd();
            adsManager.destroyBannerAd();
        });

        adapterPost.setOnItemOverflowClickListener((view, obj, position) -> tools.showBottomSheetDialogMoreOptions(parentView, obj, false, () -> {

        }));

        lytSuggestion = findViewById(R.id.lyt_suggestion);
        if (sharedPref.getIsDarkTheme()) {
            lytSuggestion.setBackgroundColor(getResources().getColor(R.color.color_dark_background));
        } else {
            lytSuggestion.setBackgroundColor(getResources().getColor(R.color.color_light_background));
        }

        adapterSearch = new AdapterSearch(this);
        recyclerViewSuggestion.setAdapter(adapterSearch);
        showSuggestionSearch();
        adapterSearch.setOnItemClickListener((view, viewModel, pos) -> {
            edtSearch.setText(viewModel);
            edtSearch.setSelection(viewModel.length());
            lytSuggestion.setVisibility(View.GONE);
            hideKeyboard();
            searchAction(1);
        });

        adapterSearch.setOnItemActionClickListener((view, viewModel, pos) -> {
            edtSearch.setText(viewModel);
            edtSearch.setSelection(viewModel.length());
        });

        btnClear.setOnClickListener(v -> edtSearch.setText(""));

        edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                searchAction(1);
                return true;
            }
            return false;
        });

        edtSearch.setOnTouchListener((view, motionEvent) -> {
            if (adapterSearch.getItemCount() > 0) {
                showSuggestionSearch();
            } else {
                lytSuggestion.setVisibility(View.GONE);
                showEmptySearch();
            }
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            return false;
        });

        swipeProgress(false);

        if (adapterSearch.getItemCount() <= 0) {
            lytSuggestion.setVisibility(View.GONE);
            showEmptySearch();
        }

        Tools.setWindowInsetsLayout(this);

    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, "", true);
        if (AppConfig.ENABLE_NEW_APP_DESIGN) {
            if (sharedPref.getIsDarkTheme()) {
                edtSearch.setTextColor(ContextCompat.getColor(this, R.color.color_white));
                btnClear.setColorFilter(ContextCompat.getColor(this, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
                lytSuggestion.setBackgroundColor(ContextCompat.getColor(this, R.color.color_dark_background));
            } else {
                btnClear.setColorFilter(ContextCompat.getColor(this, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
                lytSuggestion.setBackgroundColor(ContextCompat.getColor(this, R.color.color_light_background));
            }
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().isEmpty()) {
                btnClear.setVisibility(View.GONE);
            } else {
                btnClear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void requestSearchApi(final String query, final int page_no) {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        if (AppConfig.ENABLE_RTL_MODE) {
            callbackCall = apiInterface.getSearchPostsRTL(AppConfig.REST_API_KEY, query, page_no, AppConfig.LOAD_MORE);
        } else {
            callbackCall = apiInterface.getSearchPosts(AppConfig.REST_API_KEY, query, page_no, AppConfig.LOAD_MORE);
        }
        callbackCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CallbackRecent> call, @NonNull Response<CallbackRecent> response) {
                CallbackRecent resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    postTotal = resp.count_total;
                    displayApiResult(resp.posts);
                } else if (resp != null && resp.status.equals("error")) {
                    showInvalidRequest(true);
                } else {
                    onFailRequest(page_no);
                }
                swipeProgress(false);
            }

            @Override
            public void onFailure(@NonNull Call<CallbackRecent> call, @NonNull Throwable t) {
                onFailRequest(page_no);
                swipeProgress(false);
            }

        });
    }

    public void setLoadMore(int current_page) {
        Log.d("page", "currentPage: " + current_page);
        // Assuming final total items equal to real post items plus the ad
        int totalItemBeforeAds = (adapterPost.getItemCount() - current_page);
        if (postTotal > totalItemBeforeAds && current_page != 0) {
            int next_page = current_page + 1;
            searchAction(next_page);
        } else {
            adapterPost.setLoaded();
        }
    }

    private void displayApiResult(final List<Post> posts) {
        adapterPost.insertDataWithNativeAd(posts);
        swipeProgress(false);
        if (posts.isEmpty()) {
            showNotFoundView(true);
        } else {
            lytAds.setVisibility(View.VISIBLE);
        }
    }

    private void onFailRequest(int page_no) {
        failedPage = page_no;
        adapterPost.setLoaded();
        swipeProgress(false);
        if (Tools.isConnect(this)) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_offline));
        }
    }

    private void searchAction(final int page_no) {
        lytSuggestion.setVisibility(View.GONE);
        showFailedView(false, "");
        showNotFoundView(false);
        showInvalidRequest(false);
        final String query = edtSearch.getText().toString().trim();
        if (!query.isEmpty()) {
            if (page_no == 1) {
                adapterSearch.addSearchHistory(query);
                adapterPost.resetListData();
                swipeProgress(true);
            } else {
                adapterPost.setLoading();
            }
            Tools.postDelayed(() -> requestSearchApi(query, page_no), Constant.DELAY_TIME);
        } else {
            Toast.makeText(this, R.string.msg_search_input, Toast.LENGTH_SHORT).show();
            swipeProgress(false);
        }
    }

    private void showSuggestionSearch() {
        adapterSearch.refreshItems();
        lytSuggestion.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
        findViewById(R.id.failed_retry).setOnClickListener(view -> searchAction(failedPage));
    }

    private void showNotFoundView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.msg_no_news_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void showInvalidRequest(boolean show) {
        swipeProgress(false);
        View lytInvalidLicense = findViewById(R.id.lyt_invalid_license);
        Button btnPurchaseNow = findViewById(R.id.btn_purchase_now);
        if (show) {
            lytInvalidLicense.setVisibility(View.VISIBLE);
            btnPurchaseNow.setOnClickListener(view -> Tools.purchaseItem(this));
        } else {
            lytInvalidLicense.setVisibility(View.GONE);
        }
    }

    private void showEmptySearch() {
        View lytNotFound = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_title)).setText(getString(R.string.title_menu_search));
        ((TextView) findViewById(R.id.no_item_message)).setText(getString(R.string.msg_search));
        lytNotFound.setVisibility(View.VISIBLE);
    }

    private void swipeProgress(final boolean show) {
        if (show) {
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        } else {
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        adsManager.resumeBannerAd(adsPref.getIsBannerSearch());
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
        ViewStub shimmerPostList = findViewById(R.id.shimmer_view_post);
        if (AppConfig.ENABLE_NEW_APP_DESIGN) {
            if (AppConfig.BIG_NEWS_LIST_STYLE) {
                shimmerPostList.setLayoutResource(R.layout.shimmer_new_post_list_large);
            } else {
                shimmerPostList.setLayoutResource(R.layout.shimmer_new_post_list_default);
            }
        } else {
            if (AppConfig.BIG_NEWS_LIST_STYLE) {
                shimmerPostList.setLayoutResource(R.layout.shimmer_post_list_large);
            } else {
                shimmerPostList.setLayoutResource(R.layout.shimmer_post_list_default);
            }
        }
        shimmerPostList.inflate();
    }

}

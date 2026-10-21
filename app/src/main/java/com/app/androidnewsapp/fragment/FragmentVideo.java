package com.app.androidnewsapp.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.activity.ActivityPostDetails;
import com.app.androidnewsapp.activity.MainActivity;
import com.app.androidnewsapp.adapter.AdapterVideo;
import com.app.androidnewsapp.callback.CallbackRecent;
import com.app.androidnewsapp.config.AppConfig;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.models.Post;
import com.app.androidnewsapp.rest.ApiInterface;
import com.app.androidnewsapp.rest.RestAdapter;
import com.app.androidnewsapp.util.Constant;
import com.app.androidnewsapp.util.Tag;
import com.app.androidnewsapp.util.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentVideo extends Fragment {

    private View rootView;
    private RecyclerView recyclerView;
    private AdapterVideo adapterVideo;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Call<CallbackRecent> callbackCall = null;
    private int postTotal = 0;
    private int failedPage = 0;
    List<Post> posts = new ArrayList<>();
    ShimmerFrameLayout lytShimmer;
    SharedPref sharedPref;
    Tools tools;
    Activity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_video, container, false);

        sharedPref = new SharedPref(activity);
        tools = new Tools(activity);

        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout_home);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        adapterVideo = new AdapterVideo(activity, recyclerView, posts);
        recyclerView.setAdapter(adapterVideo);

        adapterVideo.setOnItemClickListener((v, obj, position) -> {
            Intent intent = new Intent(activity, ActivityPostDetails.class);
            intent.putExtra(Constant.EXTRA_OBJC, obj);
            startActivity(intent);
            ((MainActivity) activity).showInterstitialAd();
            ((MainActivity) activity).destroyBannerAd();
        });

        adapterVideo.setOnItemOverflowClickListener((view, obj, position) -> tools.showBottomSheetDialogMoreOptions(activity.findViewById(R.id.coordinator_layout), obj, false, () -> {

        }));

        adapterVideo.setOnLoadMoreListener(current_page -> {
            if (postTotal > adapterVideo.getItemCount() && current_page != 0) {
                int next_page = current_page + 1;
                requestAction(next_page);
            } else {
                adapterVideo.setLoaded();
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            adapterVideo.resetListData();
            requestAction(1);
        });

        requestAction(1);

        return rootView;
    }

    private void displayApiResult(final List<Post> posts) {
        adapterVideo.insertDataWithNativeAd(posts);
        swipeProgress(false);
        if (posts.isEmpty()) {
            showNoItemView(true);
        }
    }

    private void requestListPostApi(final int page_no) {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        callbackCall = apiInterface.getVideoPost(AppConfig.REST_API_KEY, page_no, AppConfig.LOAD_MORE);
        callbackCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<CallbackRecent> call, @NonNull Response<CallbackRecent> response) {
                CallbackRecent resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    postTotal = resp.count_total;
                    displayApiResult(resp.posts);
                } else if (resp != null && resp.status.equals("error")) {
                    showInvalidRequest(true);
                    Log.d(Tag.RETROFIT, " Invalid: " + response.message());
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CallbackRecent> call, @NonNull Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }

        });
    }

    private void onFailRequest(int page_no) {
        failedPage = page_no;
        adapterVideo.setLoaded();
        swipeProgress(false);
        if (Tools.isConnect(activity)) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_offline));
        }
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showInvalidRequest(false);
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            adapterVideo.setLoading();
        }
        new Handler().postDelayed(() -> requestListPostApi(page_no), Constant.DELAY_TIME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lytShimmer.stopShimmer();
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = rootView.findViewById(R.id.lyt_failed_home);
        ((TextView) rootView.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        rootView.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction(failedPage));
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = rootView.findViewById(R.id.lyt_no_item_home);
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(R.string.msg_no_news);
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
        View lytInvalidLicense = rootView.findViewById(R.id.lyt_invalid_license);
        Button btnPurchaseNow = rootView.findViewById(R.id.btn_purchase_now);
        if (show) {
            lytInvalidLicense.setVisibility(View.VISIBLE);
            btnPurchaseNow.setOnClickListener(view -> Tools.purchaseItem(activity));
        } else {
            lytInvalidLicense.setVisibility(View.GONE);
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

}


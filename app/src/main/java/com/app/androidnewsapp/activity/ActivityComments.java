package com.app.androidnewsapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.adapter.AdapterComments;
import com.app.androidnewsapp.callback.CallbackComments;
import com.app.androidnewsapp.database.prefs.AdsPref;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.models.Comments;
import com.app.androidnewsapp.models.Value;
import com.app.androidnewsapp.rest.ApiInterface;
import com.app.androidnewsapp.rest.RestAdapter;
import com.app.androidnewsapp.util.AdsManager;
import com.app.androidnewsapp.util.Constant;
import com.app.androidnewsapp.util.MaterialProgressDialog;
import com.app.androidnewsapp.util.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityComments extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AdapterComments adapterComments;
    private Call<CallbackComments> callbackCall = null;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    Long nid, commentsCount;
    private ShimmerFrameLayout lytShimmer;
    String postTitle;
    LinearLayout lytCommentHeader;
    EditText edtCommentMessage;
    LinearLayout btnPostComment;
    MaterialProgressDialog.Builder progressDialog;
    AdsManager adsManager;
    SharedPref sharedPref;
    AdsPref adsPref;
    CoordinatorLayout parentView;
    String singleChoiceSelected;
    OnBackPressedDispatcher onBackPressedDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_comments);
        Tools.setNavigation(this);
        Tools.setupAppBarLayout(this);
        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        adsManager = new AdsManager(this);
        adsManager.loadBannerAd(adsPref.getIsBannerComment());

        nid = getIntent().getLongExtra("nid", 0);
        commentsCount = getIntent().getLongExtra("count", 0);
        postTitle = getIntent().getStringExtra("post_title");

        setupToolbar();

        parentView = findViewById(R.id.parent_view);
        lytShimmer = findViewById(R.id.shimmer_view_container);
        lytCommentHeader = findViewById(R.id.lyt_comment_header);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_category);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_light_primary);
        edtCommentMessage = findViewById(R.id.edt_comment_message);
        btnPostComment = findViewById(R.id.btn_post_comment);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, 1);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        //set data and list adapter
        adapterComments = new AdapterComments(ActivityComments.this, new ArrayList<>());
        recyclerView.setAdapter(adapterComments);

        // on item list clicked
        adapterComments.setOnItemClickListener((v, obj, position, context) -> {

            if (sharedPref.getIsLogin() && sharedPref.getUserId().equals(obj.user_id)) {

                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
                View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_edit, null);

                final MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(context);
                alert.setView(mView);

                final LinearLayout btn_edit = mView.findViewById(R.id.menu_edit);
                final LinearLayout btn_delete = mView.findViewById(R.id.menu_delete);

                final AlertDialog alertDialog = alert.create();

                btn_edit.setOnClickListener(view -> {
                    alertDialog.dismiss();
                    dialogUpdateComment(obj);
                });

                btn_delete.setOnClickListener(view -> {

                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                    builder.setMessage(getString(R.string.confirm_delete_comment));
                    builder.setPositiveButton(getString(R.string.dialog_yes), (dialog, which) -> {
                        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
                        Call<Value> call = apiInterface.deleteComment(obj.comment_id);
                        call.enqueue(new Callback<Value>() {
                            @Override
                            public void onResponse(@NonNull Call<Value> call, @NonNull Response<Value> response) {
                                assert response.body() != null;
                                String value = response.body().value;
                                if (value.equals("1")) {
                                    Snackbar.make(parentView, getString(R.string.msg_success_delete_comment), Snackbar.LENGTH_SHORT).show();
                                    adapterComments.resetListData();
                                    edtCommentMessage.setText("");
                                    requestAction();
                                    hideKeyboard();
                                } else {
                                    Snackbar.make(parentView, getString(R.string.msg_failed_delete_comment), Snackbar.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Value> call, @NonNull Throwable t) {
                                t.printStackTrace();
                                Snackbar.make(parentView, getString(R.string.msg_no_comment), Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    });

                    builder.setNegativeButton(getString(R.string.dialog_no), null);
                    AlertDialog alert1 = builder.create();
                    alert1.show();

                    alertDialog.dismiss();
                });

                alertDialog.show();

            } else {

                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
                View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_reply, null);

                final MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(context);
                alert.setView(mView);

                final LinearLayout btn_reply = mView.findViewById(R.id.menu_reply);
                final LinearLayout btn_report = mView.findViewById(R.id.menu_report);

                final AlertDialog alertDialog = alert.create();

                btn_reply.setOnClickListener(view -> {
                    if (sharedPref.getIsLogin()) {
                        edtCommentMessage.setText("@" + Tools.usernameFormatter(obj.name) + " ");
                        edtCommentMessage.setSelection(edtCommentMessage.getText().length());
                        edtCommentMessage.requestFocus();
                        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        manager.showSoftInput(edtCommentMessage, InputMethodManager.SHOW_IMPLICIT);
                    } else {
                        startActivity(new Intent(getApplicationContext(), ActivityUserLogin.class));
                    }
                    alertDialog.dismiss();
                });

                btn_report.setOnClickListener(view -> {
                    if (sharedPref.getIsLogin()) {
                        String[] items = getResources().getStringArray(R.array.dialog_report_comment);
                        singleChoiceSelected = items[0];
                        int itemSelected = 0;
                        new MaterialAlertDialogBuilder(ActivityComments.this)
                                .setTitle(getString(R.string.txt_dialog_title_report_comment))
                                .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
                                .setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
                                    Tools.sendReport(this, "comment", "0", obj.comment_id, sharedPref.getUserId(), obj.user_id, obj.name, obj.content, singleChoiceSelected);
                                    dialogInterface.dismiss();
                                })
                                .setNegativeButton(R.string.dialog_cancel, null)
                                .show();
                    } else {
                        startActivity(new Intent(getApplicationContext(), ActivityUserLogin.class));
                    }
                    alertDialog.dismiss();
                });

                alertDialog.show();

            }

        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            adapterComments.resetListData();
            requestActionOnRefresh();
        });

        requestAction();
        handleOnBackPressed();

        Tools.setWindowInsetsLayout(this);

    }

    public void handleOnBackPressed() {
        onBackPressedDispatcher = getOnBackPressedDispatcher();
        onBackPressedDispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (edtCommentMessage.length() > 0) {
                    edtCommentMessage.setText("");
                } else {
                    finish();
                    adsManager.destroyBannerAd();
                }
            }
        });
    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, getResources().getString(R.string.title_comments), true);
    }

    private void displayApiResult(final List<Comments> categories) {
        swipeProgress(false);
        adapterComments.setListData(categories);
        if (categories.size() == 0) {
            showNoItemView(true);
        }
    }

    private void onFailRequest() {
        swipeProgress(false);
        if (Tools.isConnect(ActivityComments.this)) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_no_network));
        }
    }

    private void requestAPI() {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        callbackCall = apiInterface.getComments(nid);
        callbackCall.enqueue(new Callback<CallbackComments>() {
            @Override
            public void onResponse(@NonNull Call<CallbackComments> call, @NonNull Response<CallbackComments> response) {
                CallbackComments resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    displayApiResult(resp.comments);
                    initPostData();
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CallbackComments> call, @NonNull Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        showNoItemView(false);
        new Handler().postDelayed(this::requestAPI, Constant.DELAY_TIME);
    }

    private void requestActionOnRefresh() {
        showFailedView(false, "");
        swipeProgressOnRefresh(true);
        showNoItemView(false);
        new Handler().postDelayed(this::requestAPI, Constant.DELAY_TIME);
    }

    private void initPostData() {
        edtCommentMessage.setOnClickListener(view -> {
            if (!sharedPref.getIsLogin()) {
                startActivity(new Intent(getApplicationContext(), ActivityUserLogin.class));
            }
        });
        edtCommentMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (!sharedPref.getIsLogin()) {
                startActivity(new Intent(getApplicationContext(), ActivityUserLogin.class));
            }
        });

        ((TextView) findViewById(R.id.txt_comment_count)).setText("" + adapterComments.getItemCount());

        TextView txt_comment_text = findViewById(R.id.txt_comment_text);
        if (adapterComments.getItemCount() <= 1) {
            txt_comment_text.setText(getString(R.string.txt_comment));
        } else {
            txt_comment_text.setText(getString(R.string.txt_comments));
        }

        ((TextView) findViewById(R.id.txt_post_title)).setText(postTitle);

        btnPostComment.setOnClickListener(view -> {
            if (edtCommentMessage.getText().toString().equals("")) {
                Snackbar.make(parentView, getString(R.string.msg_write_comment), Snackbar.LENGTH_SHORT).show();
            } else if (edtCommentMessage.getText().toString().length() <= 6) {
                Snackbar.make(parentView, getString(R.string.msg_write_comment_character), Snackbar.LENGTH_SHORT).show();
            } else {
                dialogSendComment();
            }
        });

    }

    public void dialogSendComment() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ActivityComments.this);
        builder.setMessage(getString(R.string.confirm_send_comment));
        builder.setPositiveButton(getString(R.string.dialog_yes), (dialogInterface, i) -> {
            Tools.postDelayed(()-> {
                if (sharedPref.getCommentApproval().equals("yes")) {
                    sendCommentApproval();
                } else {
                    sendComment();
                }
            }, 100);
            hideKeyboard();
        });
        builder.setNegativeButton(getString(R.string.dialog_no), (dialog, which) -> {
        });
        AlertDialog alert = builder.create();
        alert.show();
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

    private void showFailedView(boolean flag, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed_category);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (flag) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item_category);
        ((TextView) findViewById(R.id.txt_no_comment)).setText(R.string.msg_no_comment);
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
            swipeRefreshLayout.setRefreshing(false);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            lytCommentHeader.setVisibility(View.VISIBLE);
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(false);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
            lytCommentHeader.setVisibility(View.INVISIBLE);
        });
    }

    private void swipeProgressOnRefresh(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            lytCommentHeader.setVisibility(View.VISIBLE);
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
            lytCommentHeader.setVisibility(View.INVISIBLE);
        });
    }

    public void sendComment() {

        progressDialog = new MaterialProgressDialog.Builder(this).build();
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.sending_comment));
        progressDialog.show();

        String content = edtCommentMessage.getText().toString();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date_time = simpleDateFormat.format(new Date());

        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        Call<Value> call = apiInterface.sendComment(nid, sharedPref.getUserId(), content, date_time);
        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(@NonNull Call<Value> call, @NonNull Response<Value> response) {

                assert response.body() != null;
                String value = response.body().value;

                new Handler().postDelayed(() -> {
                    progressDialog.dismiss();
                    if (value.equals("1")) {
                        Snackbar.make(parentView, getString(R.string.msg_comment_success), Snackbar.LENGTH_SHORT).show();
                        edtCommentMessage.setText("");
                        adapterComments.resetListData();
                        requestAction();
                        hideKeyboard();
                    } else {
                        Snackbar.make(parentView, getString(R.string.msg_comment_failed), Snackbar.LENGTH_SHORT).show();
                    }
                }, Constant.DELAY_REFRESH);

            }

            @Override
            public void onFailure(@NonNull Call<Value> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Snackbar.make(parentView, getString(R.string.msg_no_network), Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    public void sendCommentApproval() {

        progressDialog = new MaterialProgressDialog.Builder(this).build();
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.sending_comment));
        progressDialog.show();

        String content = edtCommentMessage.getText().toString();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date_time = simpleDateFormat.format(new Date());

        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        Call<Value> call = apiInterface.sendComment(nid, sharedPref.getUserId(), content, date_time);

        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(@NonNull Call<Value> call, @NonNull Response<Value> response) {

                assert response.body() != null;
                final String value = response.body().value;

                new Handler().postDelayed(() -> {
                    progressDialog.dismiss();
                    if (value.equals("1")) {
                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ActivityComments.this);
                        builder.setMessage(R.string.msg_comment_approval);
                        builder.setPositiveButton(getString(R.string.dialog_ok), (dialogInterface, i) -> {
                            Snackbar.make(parentView, getString(R.string.msg_comment_success), Snackbar.LENGTH_SHORT).show();
                            edtCommentMessage.setText("");
                            adapterComments.resetListData();
                            requestAction();
                            hideKeyboard();
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        Snackbar.make(parentView, getString(R.string.msg_comment_failed), Snackbar.LENGTH_SHORT).show();
                    }
                }, Constant.DELAY_REFRESH);

            }

            @Override
            public void onFailure(@NonNull Call<Value> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Snackbar.make(parentView, getString(R.string.msg_no_network), Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    @SuppressWarnings("deprecation")
    public void dialogUpdateComment(Comments obj) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ActivityComments.this);
        View view = layoutInflaterAndroid.inflate(R.layout.custom_dialog_comment, null);

        if (sharedPref.getIsDarkTheme()) {
            view.findViewById(R.id.header_update_comment).setBackgroundColor(getResources().getColor(R.color.color_dark_toolbar));
        } else {
            view.findViewById(R.id.header_update_comment).setBackgroundColor(getResources().getColor(R.color.color_light_primary));
        }

        EditText edt_id = view.findViewById(R.id.edt_id);
        edt_id.setText(obj.comment_id);

        EditText edt_date_time = view.findViewById(R.id.edt_date_time);
        edt_date_time.setText(obj.date_time);

        EditText edt_update_message = view.findViewById(R.id.edt_update_message);
        edt_update_message.setText(obj.content);
        edt_update_message.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(edt_update_message, InputMethodManager.SHOW_IMPLICIT);

        final MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(ActivityComments.this);
        alert.setView(view);
        alert.setCancelable(false);
        alert.setPositiveButton(getString(R.string.btn_update), (dialog, which) -> {

            if (edt_update_message.getText().toString().equals("")) {
                Snackbar.make(parentView, getString(R.string.msg_write_comment), Snackbar.LENGTH_SHORT).show();
            } else if (edt_update_message.getText().toString().length() <= 6) {
                Snackbar.make(parentView, getString(R.string.msg_write_comment_character), Snackbar.LENGTH_SHORT).show();
            } else {

                dialog.dismiss();
                hideKeyboard();

                progressDialog = new MaterialProgressDialog.Builder(this).build();
                progressDialog.setCancelable(false);
                progressDialog.setMessage(getResources().getString(R.string.updating_comment));
                progressDialog.show();

                String comment_id = edt_id.getText().toString();
                String date_time = edt_date_time.getText().toString();
                String content = edt_update_message.getText().toString();

                ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
                Call<Value> call = apiInterface.updateComment(comment_id, date_time, content);
                call.enqueue(new Callback<Value>() {
                    @Override
                    public void onResponse(@NonNull Call<Value> call, @NonNull Response<Value> response) {

                        assert response.body() != null;
                        String value = response.body().value;

                        new Handler().postDelayed(() -> {
                            progressDialog.dismiss();
                            if (value.equals("1")) {
                                Snackbar.make(parentView, getString(R.string.msg_comment_update), Snackbar.LENGTH_SHORT).show();
                                adapterComments.resetListData();
                                requestAction();
                                hideKeyboard();
                            } else {
                                Snackbar.make(parentView, getString(R.string.msg_update_comment_failed), Snackbar.LENGTH_SHORT).show();
                            }
                        }, Constant.DELAY_REFRESH);
                    }

                    @Override
                    public void onFailure(@NonNull Call<Value> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        progressDialog.dismiss();
                        Snackbar.make(parentView, getString(R.string.msg_no_network), Snackbar.LENGTH_SHORT).show();
                    }
                });

            }

        });
        alert.setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> {
            dialog.dismiss();
            hideKeyboard();
        });
        alert.show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            if (edtCommentMessage.length() > 0) {
                edtCommentMessage.setText("");
            } else {
                onBackPressedDispatcher.onBackPressed();
            }
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapterComments.resetListData();
        adsManager.resumeBannerAd(adsPref.getIsBannerComment());
        requestAction();
    }

}
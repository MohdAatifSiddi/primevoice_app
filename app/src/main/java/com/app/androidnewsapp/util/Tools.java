package com.app.androidnewsapp.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.app.androidnewsapp.BuildConfig;
import com.app.androidnewsapp.R;
import com.app.androidnewsapp.activity.ActivityNotificationDetails;
import com.app.androidnewsapp.activity.ActivityPostDetails;
import com.app.androidnewsapp.activity.ActivityUserLogin;
import com.app.androidnewsapp.activity.ActivityWebView;
import com.app.androidnewsapp.activity.ActivityWebViewImage;
import com.app.androidnewsapp.config.AppConfig;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.database.sqlite.DbHandler;
import com.app.androidnewsapp.listener.OnCompleteListener;
import com.app.androidnewsapp.listener.OnPositiveButtonListener;
import com.app.androidnewsapp.models.Post;
import com.app.androidnewsapp.models.Report;
import com.app.androidnewsapp.models.User;
import com.app.androidnewsapp.rest.ApiInterface;
import com.app.androidnewsapp.rest.RestAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.solodroid.push.sdk.provider.OneSignalPush;

import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("deprecation")
public class Tools {

    Activity activity;
    DbHandler dbHandler;
    SharedPref sharedPref;
    private BottomSheetDialog mBottomSheetDialog;

    String singleChoiceSelected;
    public static final int PERMISSIONS_REQUEST = 102;

    public Tools(Activity activity) {
        this.activity = activity;
        this.sharedPref = new SharedPref(activity);
        this.dbHandler = new DbHandler(activity);
    }

    public static void getTheme(Context context) {
        SharedPref sharedPref = new SharedPref(context);
        if (sharedPref.getIsDarkTheme()) {
            context.setTheme(R.style.AppDarkTheme);
        } else {
            context.setTheme(R.style.AppTheme);
        }
        EdgeToEdge.enable((ComponentActivity) context);
    }

    public static void setNavigation(Activity activity) {
        RelativeLayout rootView = activity.findViewById(R.id.root_view);
        RelativeLayout backgroundView = activity.findViewById(R.id.background_view);
        RelativeLayout navigationBarView = activity.findViewById(R.id.navigation_bar_view);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            navigationBarView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });
        SharedPref sharedPref = new SharedPref(activity);
        if (sharedPref.getIsDarkTheme()) {
            Tools.darkNavigation(activity);
            backgroundView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_dark_background));
            navigationBarView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_dark_bottom_navigation));
            rootView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_dark_status_bar));
            WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightStatusBars(false);
            WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightNavigationBars(false);
        } else {
            Tools.lightNavigation(activity);
            backgroundView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_light_background));
            navigationBarView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_light_bottom_navigation));
            if (AppConfig.ENABLE_NEW_APP_DESIGN) {
                rootView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_white));
                WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightStatusBars(true);
                WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightNavigationBars(true);
            } else {
                rootView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_light_status_bar));
                WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightStatusBars(false);
                WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightNavigationBars(true);
            }
        }
        getLayoutDirection(activity);
    }

    public static void setPlayerNavigation(Activity activity) {
        RelativeLayout rootView = activity.findViewById(R.id.root_view);
        RelativeLayout backgroundView = activity.findViewById(R.id.background_view);
        RelativeLayout navigationBarView = activity.findViewById(R.id.navigation_bar_view);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, 0);
            navigationBarView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });
        WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightStatusBars(false);
        rootView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_black));
        backgroundView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_black));
        navigationBarView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_black));
        WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightNavigationBars(false);
    }

    public static void setImageSliderNavigation(Activity activity) {
        RelativeLayout rootView = activity.findViewById(R.id.root_view);
        RelativeLayout backgroundView = activity.findViewById(R.id.background_view);
        RelativeLayout navigationBarView = activity.findViewById(R.id.navigation_bar_view);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            navigationBarView.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });
        WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightStatusBars(false);
        rootView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_black));
        backgroundView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_black));
        navigationBarView.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_black));
        WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightNavigationBars(false);
    }

    public static void setWindowInsetsLayout(Activity activity) {
        ViewCompat.setOnApplyWindowInsetsListener(activity.findViewById(R.id.background_view), (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), imeInsets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    public static void getLayoutDirection(Activity activity) {
        if (AppConfig.ENABLE_RTL_MODE) {
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public static void darkNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
        }
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
//            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.color_dark_bottom_navigation));
//        }
    }

    public static void lightNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        if (!NavigationModeChecker.isGestureNavigationEnabled(activity)) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.color_light_bottom_navigation));
            }
        }
    }

    public static void transparentStatusBarNavigation(Activity activity) {
        WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightStatusBars(false);
        WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView()).setAppearanceLightNavigationBars(false);
    }

    public static void notificationOpenHandler(Context context, Intent getIntent) {
        String id = getIntent.getStringExtra(OneSignalPush.EXTRA_ID);
        String title = getIntent.getStringExtra(OneSignalPush.EXTRA_TITLE);
        String message = getIntent.getStringExtra(OneSignalPush.EXTRA_MESSAGE);
        String bigImage = getIntent.getStringExtra(OneSignalPush.EXTRA_IMAGE);
        String launchUrl = getIntent.getStringExtra(OneSignalPush.EXTRA_LAUNCH_URL);
        String link = getIntent.getStringExtra(OneSignalPush.EXTRA_LINK);
        String uniqueId = getIntent.getStringExtra(OneSignalPush.EXTRA_UNIQUE_ID);

        SharedPref sharedPref = new SharedPref(context);
        String postId;
        if (sharedPref.getPushNotificationProvider().equals("onesignal")) {
            postId = OneSignalPush.AdditionalData.postId;
        } else {
            postId = getIntent.getStringExtra(OneSignalPush.EXTRA_POST_ID);
        }

        long _postId;
        if (postId != null && !postId.equals("")) {
            _postId = Long.parseLong(postId);
        } else {
            _postId = 0;
        }

        if (getIntent.hasExtra(OneSignalPush.EXTRA_UNIQUE_ID)) {
            if (_postId > 0) {
                Intent intent = new Intent(context, ActivityNotificationDetails.class);
                intent.putExtra("id", _postId);
                context.startActivity(intent);
                OneSignalPush.AdditionalData.postId = "0";
            }

            if (launchUrl != null && !launchUrl.equals("")) {
                Tools.openActivityLaunchUrl(context, title, launchUrl);
            } else {
                if (link != null && !link.equals("")) {
                    if (!link.equals("0")) {
                        Tools.openActivityLaunchUrl(context, title, link);
                    }
                }
            }

        }

        Log.d("Rawr", "long post id: " + _postId);
        Log.d("Rawr", "string post id: " + postId);

    }

    public static void openActivityLaunchUrl(Context context, String title, String url) {
        if (url.contains("play.google.com") || url.contains("?target=external")) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } else {
            Intent intent = new Intent(context, ActivityWebView.class);
            intent.putExtra("title", title);
            intent.putExtra("url", url);
            context.startActivity(intent);
        }
    }

    @SuppressWarnings("deprecation")
    public static boolean isConnect(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                return activeNetworkInfo.isConnected() || activeNetworkInfo.isConnectedOrConnecting();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static RequestBuilder<Drawable> RequestBuilder(Context context) {
        return Glide.with(context).asDrawable().sizeMultiplier(0.3f);
    }

    public static String decode(String code) {
        return decodeBase64(decodeBase64(decodeBase64(code)));
    }

    public static String decodeBase64(String code) {
        byte[] valueDecoded = Base64.decode(code.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        return new String(valueDecoded);
    }

    public static String withSuffix(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c", count / Math.pow(1000, exp), "KMGTPE".charAt(exp - 1));
    }

    public static long timeStringtoMilis(String time) {
        long milis = 0;

        try {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sd.parse(time);
            milis = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return milis;
    }

    public static String getFormatedDate(String date_str) {
        if (date_str != null && !date_str.trim().equals("")) {
            SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm");
            try {
                String newStr = newFormat.format(oldFormat.parse(date_str));
                return newStr;
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static String getFormatedDateSimple(String date_str) {
        if (date_str != null && !date_str.trim().equals("")) {
            SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy");
            try {
                String newStr = newFormat.format(oldFormat.parse(date_str));
                return newStr;
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static CharSequence getTimeAgo(String date_str) {
        if (date_str != null && !date_str.trim().equals("")) {
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //sdf.setTimeZone(TimeZone.getTimeZone("CET"));
            try {
                long time = sdf.parse(date_str).getTime();
                long now = System.currentTimeMillis();
                return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    public static void displayPostDescription(Activity activity, WebView webView, String htmlData) {
        SharedPref sharedPref = new SharedPref(activity);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.setFocusableInTouchMode(false);
        webView.setFocusable(false);

        if (!AppConfig.ENABLE_TEXT_SELECTION) {
            webView.setOnLongClickListener(v -> true);
            webView.setLongClickable(false);
        }

        webView.getSettings().setJavaScriptEnabled(true);

        WebSettings webSettings = webView.getSettings();
        if (sharedPref.getFontSize() == 0) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
        } else if (sharedPref.getFontSize() == 1) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
        } else if (sharedPref.getFontSize() == 2) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
        } else if (sharedPref.getFontSize() == 3) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
        } else if (sharedPref.getFontSize() == 4) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
        } else {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
        }

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";

        String bg_paragraph;
        if (sharedPref.getIsDarkTheme()) {
            bg_paragraph = "<style type=\"text/css\">body{color: #eeeeee;} a{color:#ffffff; font-weight:bold;}";
        } else {
            bg_paragraph = "<style type=\"text/css\">body{color: #000000;} a{color:#1e88e5; font-weight:bold;}";
        }

        String font_style_default = "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/font/custom_font.ttf\")}body {font-family: MyFont; font-size: medium; overflow-wrap: break-word; word-wrap: break-word; -ms-word-break: break-all; word-break: break-all; word-break: break-word; -ms-hyphens: auto; -moz-hyphens: auto; -webkit-hyphens: auto; hyphens: auto;}</style>";

        String text_default = "<html><head>"
                + font_style_default
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bg_paragraph
                + "</style></head>"
                + "<body>"
                + htmlData
                + "</body></html>";

        String text_rtl = "<html dir='rtl'><head>"
                + font_style_default
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bg_paragraph
                + "</style></head>"
                + "<body>"
                + htmlData
                + "</body></html>";

        if (AppConfig.ENABLE_RTL_MODE) {
            webView.loadDataWithBaseURL(null, text_rtl, mimeType, encoding, null);
        } else {
            webView.loadDataWithBaseURL(null, text_default, mimeType, encoding, null);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (AppConfig.OPEN_LINK_INSIDE_APP) {
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
                            Intent intent = new Intent(activity, ActivityWebViewImage.class);
                            intent.putExtra("image_url", url);
                            activity.startActivity(intent);
                        } else if (url.endsWith(".pdf")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            activity.startActivity(intent);
                        } else if (url.contains("play.google.com") || url.contains("?target=external")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url.replace("?target=external", "")));
                            activity.startActivity(intent);
                        } else if (url.contains("?target=custom_tabs")) {
                            CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
                            intent.launchUrl(activity, Uri.parse(url.replace("?target=custom_tabs", "")));
                        } else {
                            Intent intent = new Intent(activity, ActivityWebView.class);
                            intent.putExtra("url", url);
                            activity.startActivity(intent);
                        }
                    } else {
                        Toast.makeText(activity, activity.getString(R.string.msg_invalid_url), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    activity.startActivity(intent);
                }
                return true;
            }
        });

        FrameLayout customViewContainer = activity.findViewById(R.id.customViewContainer);
        webView.setWebChromeClient(new WebChromeClient() {
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                webView.setVisibility(View.INVISIBLE);
                customViewContainer.setVisibility(View.VISIBLE);
                customViewContainer.addView(view);
            }

            public void onHideCustomView() {
                super.onHideCustomView();
                webView.setVisibility(View.VISIBLE);
                customViewContainer.setVisibility(View.GONE);
            }
        });

    }

    public static String usernameFormatter(String name) {
        if (name != null && !name.trim().equals("")) {
            return name.replace("%20", " ");
        } else {
            return "";
        }
    }

    public void showBottomSheetDialogMoreOptions(View parentView, Post post, boolean isDetailView, OnCompleteListener onCompleteListener) {
        @SuppressLint("InflateParams") View view = activity.getLayoutInflater().inflate(R.layout.dialog_more_options, null);
        FrameLayout lytBottomSheet = view.findViewById(R.id.bottom_sheet);

        ImageView imgFavorite = view.findViewById(R.id.img_favorite);
        ImageView imgShare = view.findViewById(R.id.img_share);
        ImageView imgReport = view.findViewById(R.id.img_report);
        ImageView imgFeedback = view.findViewById(R.id.img_feedback);
        ImageView btnClose = view.findViewById(R.id.btn_close);

        if (sharedPref.getIsDarkTheme()) {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_sheet_dark));
            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            imgFeedback.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
            btnClose.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon));
        } else {
            lytBottomSheet.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_rounded_sheet_default));
            imgFavorite.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgShare.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgReport.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            imgFeedback.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
            btnClose.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon));
        }

        LinearLayout btnFavorite = view.findViewById(R.id.btn_favorite);
        LinearLayout btnShare = view.findViewById(R.id.btn_share);
        LinearLayout btnReport = view.findViewById(R.id.btn_report);
        LinearLayout btnFeedback = view.findViewById(R.id.btn_feedback);

        if (!sharedPref.getLoginFeature().equals("yes")) {
            btnReport.setVisibility(View.GONE);
            btnFeedback.setVisibility(View.GONE);
        }

        List<Post> posts = dbHandler.getFavRow(post.nid);
        if (posts.size() == 0) {
            imgFavorite.setImageResource(R.drawable.ic_favorite_outline_white);
            ((TextView) view.findViewById(R.id.txt_favorite)).setText(activity.getString(R.string.menu_favorite_add));
        } else {
            if (posts.get(0).getNid() == post.nid) {
                imgFavorite.setImageResource(R.drawable.ic_favorite_white);
                ((TextView) view.findViewById(R.id.txt_favorite)).setText(activity.getString(R.string.menu_favorite_remove));
            }
        }

        btnFavorite.setOnClickListener(v -> {
            if (isDetailView) {
                ((ActivityPostDetails) activity).onFavoriteClicked(post);
            } else {
                List<Post> _posts = dbHandler.getFavRow(post.nid);
                if (_posts.size() == 0) {
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
                    imgFavorite.setImageResource(R.drawable.ic_favorite_white);
                } else {
                    if (_posts.get(0).getNid() == post.nid) {
                        dbHandler.RemoveFav(new Post(post.nid));
                        Snackbar.make(parentView, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show();
                        imgFavorite.setImageResource(R.drawable.ic_favorite_outline_white);
                        onCompleteListener.onComplete();
                    }
                }
            }
            mBottomSheetDialog.dismiss();
        });

        btnShare.setOnClickListener(v -> {
            Tools.sharePost(activity, post.news_title);
            mBottomSheetDialog.dismiss();
        });

        btnReport.setOnClickListener(v -> {
            if (sharedPref.getIsLogin()) {
                String[] items = activity.getResources().getStringArray(R.array.dialog_report_post);
                singleChoiceSelected = items[0];
                int itemSelected = 0;
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(activity.getString(R.string.txt_dialog_title_report_post))
                        .setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> singleChoiceSelected = items[i])
                        .setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
                            Tools.sendReport(activity, "post", String.valueOf(post.nid), "0", sharedPref.getUserId(), "0", "0", post.news_title, singleChoiceSelected);
                            dialogInterface.dismiss();
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show();
            } else {
                activity.startActivity(new Intent(activity, ActivityUserLogin.class));
            }
            mBottomSheetDialog.dismiss();
        });

        btnFeedback.setOnClickListener(v -> {
            if (sharedPref.getIsLogin()) {
                LayoutInflater inflater = LayoutInflater.from(activity);
                @SuppressLint("InflateParams") View feedbackView = inflater.inflate(R.layout.custom_dialog_feedback, null);

                LinearLayout toolbar = feedbackView.findViewById(R.id.header_feedback);
                if (sharedPref.getIsDarkTheme()) {
                    toolbar.setBackgroundColor(activity.getResources().getColor(R.color.color_dark_toolbar));
                } else {
                    toolbar.setBackgroundColor(activity.getResources().getColor(R.color.color_light_primary));
                }

                EditText edtFeedback = feedbackView.findViewById(R.id.edt_feedback);
                TextView txtInfo = feedbackView.findViewById(R.id.txt_info);
                Button btnSendFeedback = feedbackView.findViewById(R.id.btn_send_feedback);
                ImageButton btnDismiss = feedbackView.findViewById(R.id.btn_dismiss);

                final MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(activity);
                alert.setView(feedbackView);

                final AlertDialog alertDialog = alert.create();
                btnSendFeedback.setOnClickListener(view1 -> {
                    String msgFeedback = edtFeedback.getText().toString();
                    if (!msgFeedback.equals("")) {
                        Tools.postDelayed(() -> {
                            Tools.sendFeedback(activity, "feedback", String.valueOf(post.nid), "0", sharedPref.getUserId(), "0", "0", post.news_title, msgFeedback);
                        }, 100);
                        Tools.hideKeyboard(activity);
                        alertDialog.dismiss();
                    } else {
                        txtInfo.setText(activity.getString(R.string.msg_complete_form));
                        txtInfo.setVisibility(View.VISIBLE);
                    }
                });
                btnDismiss.setOnClickListener(view1 -> new Handler().postDelayed(alertDialog::dismiss, 200));
                alertDialog.setCancelable(false);
                alertDialog.show();
            } else {
                activity.startActivity(new Intent(activity, ActivityUserLogin.class));
            }
            mBottomSheetDialog.dismiss();
        });

        btnClose.setOnClickListener(v -> mBottomSheetDialog.dismiss());

        if (AppConfig.ENABLE_RTL_MODE) {
            if (sharedPref.getIsDarkTheme()) {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDarkRtl);
            } else {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLightRtl);
            }
        } else {
            if (sharedPref.getIsDarkTheme()) {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogDark);
            } else {
                mBottomSheetDialog = new BottomSheetDialog(activity, R.style.SheetDialogLight);
            }
        }
        this.mBottomSheetDialog.setContentView(view);

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);

    }

    public static void sendReport(Activity activity, String type, String postId, String commentId, String userId, String reportedUserId, String reportedUserName, String reportedContent, String reason) {
        SharedPref sharedPref = new SharedPref(activity);
        MaterialProgressDialog.Builder progressDialog = new MaterialProgressDialog.Builder(activity).build();
        progressDialog.setMessage(activity.getResources().getString(R.string.waiting_message));
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        Call<Report> call = apiInterface.sendReport(type, postId, commentId, userId, reportedUserId, reportedUserName, reportedContent, reason);
        call.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(@NonNull Call<Report> call, @NonNull Response<Report> response) {
                Report report = response.body();
                if (report != null) {
                    new Handler().postDelayed(() -> {
                        showAlertDialog(activity, activity.getResources().getString(R.string.msg_success_report), () -> {
                        });
                        progressDialog.dismiss();
                    }, Constant.DELAY_PROGRESS_DIALOG);
                } else {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Report> call, @NonNull Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    public static void sendFeedback(Activity activity, String type, String postId, String commentId, String userId, String reportedUserId, String reportedUserName, String reportedContent, String reason) {
        SharedPref sharedPref = new SharedPref(activity);
        MaterialProgressDialog.Builder progressDialog = new MaterialProgressDialog.Builder(activity).build();
        progressDialog.setMessage(activity.getResources().getString(R.string.waiting_message));
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        Call<Report> call = apiInterface.sendReport(type, postId, commentId, userId, reportedUserId, reportedUserName, reportedContent, reason);
        call.enqueue(new Callback<Report>() {
            @Override
            public void onResponse(@NonNull Call<Report> call, @NonNull Response<Report> response) {
                Report report = response.body();
                if (report != null) {
                    new Handler().postDelayed(() -> {
                        showAlertDialog(activity, activity.getResources().getString(R.string.msg_success_feedback), () -> {
                        });
                        progressDialog.dismiss();
                    }, Constant.DELAY_PROGRESS_DIALOG);
                } else {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Report> call, @NonNull Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    public static void postAccountDeletionRequest(Activity activity, String message, String id, String account_deletion_request, String account_deletion_reason, String account_deletion_date, OnCompleteListener onCompleteListener) {
        SharedPref sharedPref = new SharedPref(activity);
        MaterialProgressDialog.Builder progressDialog = new MaterialProgressDialog.Builder(activity).build();
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();

        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        Call<User> call = apiInterface.postAccountDeletion(id, account_deletion_request, account_deletion_reason, account_deletion_date);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                User user = response.body();
                if (user != null) {
                    new Handler().postDelayed(() -> {
                        onCompleteListener.onComplete();
                        progressDialog.dismiss();
                    }, Constant.DELAY_PROGRESS_DIALOG);
                } else {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                progressDialog.dismiss();
            }
        });
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void showAlertDialog(Activity activity, String message, OnCompleteListener onCompleteListener) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        builder.setMessage(message);
        builder.setPositiveButton(activity.getResources().getString(R.string.dialog_ok), (dialog, i) -> onCompleteListener.onComplete());
        builder.setCancelable(false);
        builder.show();
    }

    public static void setupToolbar(AppCompatActivity activity, Toolbar toolbar, String title, boolean backButton) {
        SharedPref sharedPref = new SharedPref(activity);
        activity.setSupportActionBar(toolbar);
        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(activity.getResources().getColor(R.color.color_dark_toolbar));
        } else {
            if (AppConfig.ENABLE_NEW_APP_DESIGN) {
                toolbar.setBackgroundColor(activity.getResources().getColor(R.color.color_white));
            } else {
                toolbar.setBackgroundColor(activity.getResources().getColor(R.color.color_light_primary));
            }
        }
        final ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(backButton);
            activity.getSupportActionBar().setHomeButtonEnabled(backButton);
            activity.getSupportActionBar().setTitle(title);
        }
    }

    public static void setupAppBarLayout(AppCompatActivity activity) {
        ViewStub viewStub = activity.findViewById(R.id.header_view);
        if (AppConfig.ENABLE_NEW_APP_DESIGN) {
            viewStub.setLayoutResource(R.layout.include_appbar_light);
        } else {
            viewStub.setLayoutResource(R.layout.include_appbar_default);
        }
        viewStub.inflate();
    }

    public static void setupAppMainBarLayout(AppCompatActivity activity) {
        ViewStub viewStub = activity.findViewById(R.id.header_view);
        if (AppConfig.ENABLE_NEW_APP_DESIGN) {
            viewStub.setLayoutResource(R.layout.include_appbar_main_light);
        } else {
            viewStub.setLayoutResource(R.layout.include_appbar_main_default);
        }
        viewStub.inflate();
    }

    public static void setupAppDetailBarLayout(AppCompatActivity activity) {
        ViewStub viewStub = activity.findViewById(R.id.header_view);
        if (AppConfig.ENABLE_NEW_APP_DESIGN) {
            viewStub.setLayoutResource(R.layout.include_appbar_detail_light);
        } else {
            viewStub.setLayoutResource(R.layout.include_appbar_detail_default);
        }
        viewStub.inflate();
    }

    public static void setupAppSearchBarLayout(AppCompatActivity activity) {
        ViewStub viewStub = activity.findViewById(R.id.header_view);
        if (AppConfig.ENABLE_NEW_APP_DESIGN) {
            viewStub.setLayoutResource(R.layout.include_appbar_search_light);
        } else {
            viewStub.setLayoutResource(R.layout.include_appbar_search_default);
        }
        viewStub.inflate();
    }

    public static void postDelayed(OnCompleteListener onCompleteListener, int millisecond) {
        new Handler(Looper.getMainLooper()).postDelayed(onCompleteListener::onComplete, millisecond);
    }

    public static void downloadImage(Activity activity, String filename, String downloadUrlOfImage, String mimeType) {
        try {
            DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(downloadUrlOfImage);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(filename)
                    .setMimeType(mimeType)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + filename + ".jpg");
            dm.enqueue(request);
            Snackbar.make(activity.findViewById(android.R.id.content), "Image download started.", Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            Snackbar.make(activity.findViewById(android.R.id.content), "Image download failed.", Snackbar.LENGTH_SHORT).show();
        }
    }

    public static void nestedScrollTo(final NestedScrollView nested, final View targetView) {

    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void fullScreenMode(AppCompatActivity activity, boolean show) {
        SharedPref sharedPref = new SharedPref(activity);
        if (show) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide();
            }
            //activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (!sharedPref.getIsDarkTheme()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().show();
            }
            //activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public static void startExternalApplication(Context context, String url) {
        try {
            String[] results = url.split("package=");
            String packageName = results[1];
            boolean isAppInstalled = appInstalledOrNot(context, packageName);
            if (isAppInstalled) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage(packageName);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            } else {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(context, "Whoops! cannot handle this url.", Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("Error", "NameNotFoundException");
        }
        return false;
    }

    public static void rateApps(Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + Tools.getApplicationId())));
    }

    public static void shareApps(Context context) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_content) + "\n" + "https://play.google.com/store/apps/details?id=" + Tools.getApplicationId());
        intent.setType("text/plain");
        context.startActivity(intent);
    }

    public static void sharePost(Context context, String title) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + context.getString(R.string.share_content) + "\n" + "https://play.google.com/store/apps/details?id=" + Tools.getApplicationId());
        intent.setType("text/plain");
        context.startActivity(intent);
    }

    public static void moreApps(Context context, String url) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    public static void purchaseItem(Activity activity) {
        CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
        intent.launchUrl(activity, Uri.parse("https://codecanyon.net/item/android-news-app/10771397"));
    }

    public static void aboutDialog(Context context) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View view = layoutInflaterAndroid.inflate(R.layout.custom_dialog_about, null);
        ((TextView) view.findViewById(R.id.txt_app_version)).setText(Tools.getVersionName() + "");
        final MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(context);
        alert.setView(view);
        alert.setCancelable(false);
        alert.setPositiveButton(R.string.dialog_ok, (dialog, which) -> dialog.dismiss());
        alert.show();
    }

    public static void setNativeAdStyle(Context context, LinearLayout nativeAdView, String style) {
        switch (style) {
            case "small":
            case "radio":
                nativeAdView.addView(View.inflate(context, com.solodroidx.ads.R.layout.view_native_ad_radio, null));
                break;
            case "medium":
            case "news":
                nativeAdView.addView(View.inflate(context, com.solodroidx.ads.R.layout.view_native_ad_news, null));
                break;
            default:
                nativeAdView.addView(View.inflate(context, com.solodroidx.ads.R.layout.view_native_ad_medium, null));
                break;
        }
    }

    public static String nativeAdStyleFormatter(String style) {
        return switch (style) {
            case "small" -> "radio";
            case "medium" -> "news";
            case "large" -> "medium";
            default -> "default";
        };
    }

    public static String getApplicationId() {
        return BuildConfig.APPLICATION_ID;
    }

    public static int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    public static String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    public static boolean isDebug() {
        return BuildConfig.DEBUG;
    }

}

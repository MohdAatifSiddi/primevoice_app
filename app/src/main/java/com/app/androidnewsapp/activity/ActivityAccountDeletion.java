package com.app.androidnewsapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.util.Constant;
import com.app.androidnewsapp.util.Tools;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import id.solodroid.validationlibrary.Rule;
import id.solodroid.validationlibrary.Validator;
import id.solodroid.validationlibrary.annotation.Required;
import id.solodroid.validationlibrary.annotation.TextRule;

public class ActivityAccountDeletion extends AppCompatActivity {

    @Required(order = 1)
    @TextRule(order = 2, minLength = 1, maxLength = 500, trim = true, message = "Write your reason!")
    EditText edtReason;
    private Validator validator;

    WebView webView;
    Button btnDeleteAccount;
    SharedPref sharedPref;
    OnBackPressedDispatcher onBackPressedDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_account_deletion);
        sharedPref = new SharedPref(this);
        Tools.setNavigation(this);
        Tools.setupAppBarLayout(this);

        setupToolbar();
        edtReason = findViewById(R.id.edt_reason);
        webView = findViewById(R.id.web_view);
        btnDeleteAccount = findViewById(R.id.btn_delete_account);

        Tools.displayPostDescription(this, webView, sharedPref.getAccountDeletionMessage());

        btnDeleteAccount.setOnClickListener(v -> validator.validateAsync());

        validator = new Validator(this);
        validator.setValidationListener(new Validator.ValidationListener() {
            @Override
            public void onValidationSucceeded() {
                accountDeletionDialog(edtReason.getText().toString());
            }

            @Override
            public void onValidationFailed(View failedView, Rule<?> failedRule) {
                String message = failedRule.getFailureMessage();
                if (failedView instanceof EditText) {
                    failedView.requestFocus();
                    ((EditText) failedView).setError(message);
                }
            }
        });

        handleOnBackPressed();

        Tools.setWindowInsetsLayout(this);

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

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, getString(R.string.title_menu_delete_account), true);
    }

    public void accountDeletionDialog(String reason) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-m-d HH:mm:ss");
        String dateTime = dateFormat.format(Calendar.getInstance().getTime());

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ActivityAccountDeletion.this);
        builder.setTitle(R.string.delete_title);
        builder.setMessage(R.string.account_deletion_warnings);
        builder.setPositiveButton(R.string.dialog_yes, (di, i) -> {
            Tools.postAccountDeletionRequest(
                    ActivityAccountDeletion.this,
                    getResources().getString(R.string.delete_process),
                    sharedPref.getUserId(),
                    "1",
                    reason,
                    dateTime,
                    () -> {
                        sharedPref.setIsLogin(false);
                        sharedPref.setUserId("");
                        sharedPref.setUserName("");
                        sharedPref.setUserEmail("");
                        sharedPref.setUserImage("");
                        sharedPref.setUserPassword("");
                        MaterialAlertDialogBuilder alert = getMaterialAlertDialogBuilder();
                        alert.show();
                        Constant.isProfileUpdated = true;
                    }
            );
        });
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.setCancelable(false);
        builder.show();
    }

    private @NonNull MaterialAlertDialogBuilder getMaterialAlertDialogBuilder() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ActivityAccountDeletion.this);
        builder.setMessage(R.string.delete_success);
        builder.setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        builder.setCancelable(false);
        return builder;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
    }

}

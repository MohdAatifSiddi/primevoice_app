package com.app.androidnewsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.models.User;
import com.app.androidnewsapp.rest.ApiInterface;
import com.app.androidnewsapp.rest.RestAdapter;
import com.app.androidnewsapp.util.Constant;
import com.app.androidnewsapp.util.MaterialProgressDialog;
import com.app.androidnewsapp.listener.OnCompleteListener;
import com.app.androidnewsapp.util.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityEditProfile extends AppCompatActivity {

    EditText edtEmail, edtName, edtPassword;
    Button btnUpdate;
    Button btnChangePassword;
    Button btnLogout;
    Button btnDeleteAccount;
    RelativeLayout lytProfile;
    FloatingActionButton imgChange;
    Bitmap bitmap;
    ImageView profileImage;
    ImageView tmpImage;
    MaterialProgressDialog.Builder progressDialog;
    String userId, userName, userEmail, userImage, userPassword, userImageNew, userImageOld;
    private static final int IMAGE = 100;
    SharedPref sharedPref;
    OnBackPressedDispatcher onBackPressedDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_edit_profile);
        sharedPref = new SharedPref(this);
        Tools.setNavigation(this);
        Tools.setupAppBarLayout(this);
        Tools.setWindowInsetsLayout(this);
        setupToolbar();

        Intent intent = getIntent();
        userId = intent.getStringExtra("user_id");
        userName = intent.getStringExtra("user_name");
        userEmail = intent.getStringExtra("user_email");
        userImage = intent.getStringExtra("user_image");
        userPassword = intent.getStringExtra("user_password");

        progressDialog = new MaterialProgressDialog.Builder(ActivityEditProfile.this).build();
        progressDialog.setMessage(getResources().getString(R.string.logout_process));
        progressDialog.setCancelable(false);

        profileImage = findViewById(R.id.profile_image);
        tmpImage = findViewById(R.id.tmp_image);
        imgChange = findViewById(R.id.btn_change_image);

        lytProfile = findViewById(R.id.lyt_profile);

        edtEmail = findViewById(R.id.edt_email);
        edtName = findViewById(R.id.edt_user);
        edtPassword = findViewById(R.id.edt_password);

        edtName.setText(userName);
        edtEmail.setText(userEmail);
        edtPassword.setText(userPassword);
        displayProfileImage();

        imgChange.setOnClickListener(view -> selectImage());

        btnUpdate = findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(view -> updateUserData());

        btnChangePassword = findViewById(R.id.btn_change_password);
        btnChangePassword.setOnClickListener(view -> dialogChangePassword());

        btnLogout = findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(view -> logoutDialog());

        btnDeleteAccount = findViewById(R.id.btn_delete_account);
        btnDeleteAccount.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ActivityAccountDeletion.class)));

        handleOnBackPressed();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, getString(R.string.title_menu_edit_profile), true);
    }

    public void logoutDialog() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ActivityEditProfile.this);
        builder.setTitle(R.string.logout_title);
        builder.setMessage(R.string.logout_message);
        builder.setPositiveButton(R.string.dialog_yes, (di, i) -> {

            showProgressDialog(true, getResources().getString(R.string.logout_process));
            sharedPref.setIsLogin(false);
            sharedPref.setUserId("");
            sharedPref.setUserName("");
            sharedPref.setUserEmail("");
            sharedPref.setUserImage("");
            sharedPref.setUserPassword("");

            new Handler().postDelayed(() -> {
                showProgressDialog(false, getResources().getString(R.string.logout_process));
                MaterialAlertDialogBuilder builder1 = new MaterialAlertDialogBuilder(ActivityEditProfile.this);
                builder1.setMessage(R.string.logout_success);
                builder1.setPositiveButton(R.string.dialog_ok, (dialogInterface, i1) -> finish());
                builder1.setCancelable(false);
                builder1.show();
            }, Constant.DELAY_PROGRESS_DIALOG);

            Constant.isProfileUpdated = true;

        });
        builder.setNegativeButton(R.string.dialog_cancel, null);
        builder.show();

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

    public void accountDeletionDialog() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-m-d HH:mm:ss");
        String dateTime = dateFormat.format(Calendar.getInstance().getTime());

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ActivityEditProfile.this);
        builder.setTitle(R.string.delete_title);
        builder.setMessage(R.string.account_deletion_warnings);
        builder.setPositiveButton(R.string.dialog_yes, (di, i) -> {
            Tools.postAccountDeletionRequest(
                    ActivityEditProfile.this,
                    getResources().getString(R.string.delete_process),
                    sharedPref.getUserId(),
                    "1",
                    "deleted by user",
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
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ActivityEditProfile.this);
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

    private void displayProfileImage() {
        if (userImage.equals("")) {
            profileImage.setImageResource(R.drawable.ic_user_account);
        } else {
            Glide.with(this)
                    .load(sharedPref.getBaseUrl() + "/upload/avatar/" + userImage.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_user_account)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .apply(new RequestOptions().override(256, 256))
                    .centerCrop()
                    .into(profileImage);
        }
    }

    @SuppressWarnings("deprecation")
    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE);
    }

    private String convertToString() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imgByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgByte, Base64.DEFAULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE && resultCode == RESULT_OK && data != null) {
            Uri path = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                tmpImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void updateUserData() {

        showProgressDialog(true);

        userName = edtName.getText().toString();
        userEmail = edtEmail.getText().toString();
        userPassword = edtPassword.getText().toString();

        if (bitmap != null) {
            requestActionWithImage();
        } else {
            requestAction();
        }
    }

    private void requestAction() {

        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        Call<User> call = apiInterface.updateUserData(userId, userName, userEmail, userPassword);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                User user = response.body();
                if (user != null) {

                    sharedPref.setUserId(user.id);
                    sharedPref.setUserName(user.name);
                    sharedPref.setUserEmail(user.email);
                    sharedPref.setUserPassword(user.password);

                    new Handler().postDelayed(() -> {
                        showAlertDialog(getString(R.string.success_updating_profile), () -> finish());
                        showProgressDialog(false);
                    }, Constant.DELAY_PROGRESS_DIALOG);
                } else {
                    showProgressDialog(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                showProgressDialog(false);
            }
        });

    }

    private void requestActionWithImage() {

        userImageOld = userImage;
        userImageNew = convertToString();

        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        Call<User> call = apiInterface.updatePhotoProfile(userId, userName, userEmail, userPassword, userImageOld, userImageNew);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                User user = response.body();
                if (user != null) {
                    sharedPref.setUserId(user.id);
                    sharedPref.setUserName(user.name);
                    sharedPref.setUserEmail(user.email);
                    sharedPref.setUserImage(user.image);
                    sharedPref.setUserPassword(user.password);
                    Constant.isProfileUpdated = true;
                    new Handler().postDelayed(() -> {
                        showAlertDialog(getString(R.string.success_updating_profile), () -> finish());
                        showProgressDialog(false);
                    }, Constant.DELAY_PROGRESS_DIALOG);
                } else {
                    showProgressDialog(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                showProgressDialog(false);
            }
        });

    }

    @SuppressWarnings("deprecation")
    public void dialogChangePassword() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ActivityEditProfile.this);
        View view = layoutInflaterAndroid.inflate(R.layout.dialog_change_password, null);

        LinearLayout toolbar = view.findViewById(R.id.header_change_password);
        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.color_dark_toolbar));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.color_light_primary));
        }

        EditText edtOldPassword = view.findViewById(R.id.edt_old_password);

        EditText edtNewPassword = view.findViewById(R.id.edt_new_password);
        EditText edtConfirmNewPassword = view.findViewById(R.id.edt_confirm_new_password);
        TextView txtInfo = view.findViewById(R.id.txt_info);
        Button btnUpdate = view.findViewById(R.id.btn_update_password);
        ImageButton btnClose = view.findViewById(R.id.btn_close);

        final MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(ActivityEditProfile.this);
        alert.setView(view);

        final AlertDialog alertDialog = alert.create();

        btnUpdate.setOnClickListener(v -> {

            String oldPassword = edtOldPassword.getText().toString();
            String newPassword = edtNewPassword.getText().toString();
            String confirmNewPassword = edtConfirmNewPassword.getText().toString();

            if (!oldPassword.equals("") && !newPassword.equals("") && !confirmNewPassword.equals("")) {
                if (!oldPassword.equals(sharedPref.getUserPassword())) {
                    txtInfo.setText(getString(R.string.msg_old_password_invalid));
                    txtInfo.setVisibility(View.VISIBLE);
                } else {
                    if (oldPassword.equals(newPassword)) {
                        txtInfo.setText(getString(R.string.msg_password_same));
                        txtInfo.setVisibility(View.VISIBLE);
                    } else {
                        if (!newPassword.equals(confirmNewPassword)) {
                            txtInfo.setText(getString(R.string.msg_confirm_password_invalid));
                            txtInfo.setVisibility(View.VISIBLE);
                        } else {
                            alertDialog.dismiss();
                            showProgressDialog(true);
                            updatePassword(userId, newPassword);
                        }
                    }
                }
            } else {
                txtInfo.setText(getString(R.string.msg_complete_form));
                txtInfo.setVisibility(View.VISIBLE);
            }
        });

        btnClose.setOnClickListener(v -> new Handler().postDelayed(alertDialog::dismiss, 200));

        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void updatePassword(String userId, String newPassword) {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        Call<User> call = apiInterface.updatePassword(userId, newPassword);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                User user = response.body();
                if (user != null) {
                    sharedPref.setUserPassword(user.password);
                    new Handler().postDelayed(() -> {
                        showAlertDialog(getString(R.string.msg_password_changed), () -> finish());
                        showProgressDialog(false);
                    }, Constant.DELAY_PROGRESS_DIALOG);
                } else {
                    showProgressDialog(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                showProgressDialog(false);
            }
        });
    }

    private void showProgressDialog(boolean show) {
        if (show) {
            progressDialog = new MaterialProgressDialog.Builder(ActivityEditProfile.this).build();
            progressDialog.setMessage(getResources().getString(R.string.waiting_message));
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    private void showProgressDialog(boolean show, String message) {
        if (show) {
            progressDialog = new MaterialProgressDialog.Builder(ActivityEditProfile.this).build();
            progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else {
            progressDialog.dismiss();
        }
    }

    private void showAlertDialog(String message, OnCompleteListener onCompleteListener) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ActivityEditProfile.this);
        builder.setMessage(message);
        builder.setPositiveButton(getResources().getString(R.string.dialog_ok), (dialog, i) -> onCompleteListener.onComplete());
        builder.setCancelable(false);
        builder.show();
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

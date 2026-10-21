package com.app.androidnewsapp.util;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MaterialProgressDialog {

    public static class Builder {

        public Context context;
        public AlertDialog dialog;
        public String message = "";
        public boolean cancelable;
        public int textSize = 16;
        public TextView textView;

        public Builder(Context context) {
            this.context = context;
            this.textView = new TextView(context);
        }

        public Builder build() {
            loadProgressDialog();
            return this;
        }

        public Builder show() {
            showProgressDialog();
            return this;
        }

        public Builder dismiss() {
            dismissProgressDialog();
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder setTextSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        public void loadProgressDialog() {
            int llPadding = 60;
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setPadding(llPadding, llPadding * 2, llPadding, llPadding * 2);
            linearLayout.setGravity(Gravity.START);
            LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            llParam.gravity = Gravity.CENTER;
            linearLayout.setLayoutParams(llParam);

            ProgressBar progressBar = new ProgressBar(context);
            progressBar.setIndeterminate(true);
            progressBar.setPadding(0, 0, llPadding, 0);
            progressBar.setLayoutParams(llParam);

            llParam = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            llParam.gravity = Gravity.CENTER;

            textView.setTextSize(textSize);
            textView.setLayoutParams(llParam);

            linearLayout.addView(progressBar);
            linearLayout.addView(textView);

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            builder.setCancelable(cancelable);
            builder.setView(linearLayout);

            dialog = builder.create();

            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(dialog.getWindow().getAttributes());
                layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setAttributes(layoutParams);
            }
        }

        public void showProgressDialog() {
            textView.setText(message);
            dialog.show();
        }

        public void dismissProgressDialog() {
            dialog.dismiss();
        }

    }

}
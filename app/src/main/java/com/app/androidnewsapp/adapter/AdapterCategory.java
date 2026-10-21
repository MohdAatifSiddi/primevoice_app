package com.app.androidnewsapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.config.AppConfig;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.models.Category;
import com.app.androidnewsapp.util.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("NotifyDataSetChanged")
public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.ViewHolder> {

    Context context;
    private List<Category> items;
    private OnItemClickListener mOnItemClickListener;

    SharedPref sharedPref;

    public interface OnItemClickListener {
        void onItemClick(View view, Category obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterCategory(Context context, List<Category> items) {
        this.items = items;
        this.context = context;
        this.sharedPref = new SharedPref(context);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView categoryName;
        public TextView postCount;
        public LinearLayout lytParent;
        public ImageView categoryImage;

        public ViewHolder(View v) {
            super(v);
            categoryImage = v.findViewById(R.id.category_image);
            categoryName = v.findViewById(R.id.name);
            postCount = v.findViewById(R.id.post_count);
            lytParent = v.findViewById(R.id.lyt_parent);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Category c = items.get(position);
        holder.categoryName.setText(Html.fromHtml(c.category_name));

        if (AppConfig.SHOW_POST_COUNT_IN_CATEGORY) {
            holder.postCount.setText(Tools.withSuffix(c.post_count) + " " + context.getResources().getString(R.string.txt_posts));
        } else {
            holder.postCount.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(sharedPref.getBaseUrl() + "/upload/category/" + c.category_image.replace(" ", "%20"))
                .placeholder(R.drawable.ic_thumbnail)
                .thumbnail(Tools.RequestBuilder(context))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.categoryImage);

        holder.lytParent.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view, c, position);
            }
        });
    }

    public void setListData(List<Category> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void resetListData() {
        this.items = new ArrayList<>();
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

}
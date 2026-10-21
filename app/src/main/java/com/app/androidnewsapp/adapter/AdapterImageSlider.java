package com.app.androidnewsapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.models.Images;
import com.app.androidnewsapp.util.Constant;
import com.app.androidnewsapp.util.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

public class AdapterImageSlider extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    List<Images> images;
    private OnItemClickListener mOnItemClickListener;
    SharedPref sharedPref;

    public interface OnItemClickListener {
        void onItemClick(View view, Images obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterImageSlider(Context context, List<Images> images) {
        this.images = images;
        this.context = context;
        this.sharedPref = new SharedPref(context);
    }

    public static class OriginalViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public PhotoView postImage;

        public OriginalViewHolder(View v) {
            super(v);
            postImage = v.findViewById(R.id.image_detail);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_slider, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Images post = images.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            if (post.content_type != null && post.content_type.equals("youtube")) {
                Glide.with(context)
                        .load(Constant.YOUTUBE_IMG_FRONT + post.video_id + Constant.YOUTUBE_IMG_BACK)
                        .placeholder(R.drawable.ic_thumbnail)
                        .thumbnail(Tools.RequestBuilder(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(vItem.postImage);
            } else {
                Glide.with(context)
                        .load(sharedPref.getBaseUrl() + "/upload/" + post.image_name.replace(" ", "%20"))
                        .placeholder(R.drawable.ic_thumbnail)
                        .thumbnail(Tools.RequestBuilder(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(vItem.postImage);
            }

            vItem.postImage.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, post, position);
                }
            });

        }
    }

    public void insertData(List<Images> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.images.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    @SuppressWarnings("SuspiciousListRemoveInLoop")
    public void setLoaded() {
        for (int i = 0; i < getItemCount(); i++) {
            if (images.get(i) == null) {
                images.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void resetListData() {
        this.images.clear();
        notifyDataSetChanged();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return images.size();
    }

}
package com.app.androidnewsapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.app.androidnewsapp.R;
import com.app.androidnewsapp.config.AppConfig;
import com.app.androidnewsapp.database.prefs.AdsPref;
import com.app.androidnewsapp.database.prefs.SharedPref;
import com.app.androidnewsapp.models.Post;
import com.app.androidnewsapp.util.Constant;
import com.app.androidnewsapp.util.Tools;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

public class AdapterRelated extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;

    List<Post> posts;

    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    private OnItemClickListener mOnItemClickListener;
    boolean scrolling = false;

    SharedPref sharedPref;
    AdsPref adsPref;

    public interface OnItemClickListener {
        void onItemClick(View view, Post obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AdapterRelated(Context context, RecyclerView view, List<Post> posts) {
        this.posts = posts;
        this.context = context;
        this.sharedPref = new SharedPref(context);
        this.adsPref = new AdsPref(context);
        lastItemViewDetector(view);
        view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    scrolling = true;
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    scrolling = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    public static class OriginalViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView excerpt;
        public ImageView icDate;
        public TextView date;
        public TextView comment;
        public ImageView image;
        public ImageView thumbnailVideo;
        public LinearLayout lytParent;
        public LinearLayout lytComment;

        public OriginalViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            icDate = v.findViewById(R.id.ic_date);
            date = v.findViewById(R.id.date);
            excerpt = v.findViewById(R.id.excerpt);
            comment = v.findViewById(R.id.comment);
            image = v.findViewById(R.id.image);
            thumbnailVideo = v.findViewById(R.id.thumbnail_video);
            lytParent = v.findViewById(R.id.lyt_parent);
            lytComment = v.findViewById(R.id.lyt_comment);
        }

    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {

        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar1);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_default, parent, false);
            vh = new OriginalViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final Post p = (Post) posts.get(position);
            final OriginalViewHolder vItem = (OriginalViewHolder) holder;

            vItem.title.setText(Html.fromHtml(p.news_title));
            vItem.excerpt.setText(Html.fromHtml(p.news_description));

            if (AppConfig.SHOW_EXCERPT_IN_POST_LIST) {
                vItem.title.setMaxLines(2);
                vItem.excerpt.setVisibility(View.VISIBLE);
            } else {
                vItem.title.setMaxLines(4);
                vItem.excerpt.setVisibility(View.GONE);
            }

            if (AppConfig.SHOW_POST_DATE) {
                vItem.date.setVisibility(View.VISIBLE);
                vItem.icDate.setVisibility(View.VISIBLE);
            } else {
                vItem.date.setVisibility(View.GONE);
                vItem.icDate.setVisibility(View.GONE);
            }

            if (AppConfig.SHOW_DATE_DISPLAY_AS_TIME_AGO) {
                vItem.date.setText(Tools.getTimeAgo(p.news_date));
            } else {
                vItem.date.setText(Tools.getFormatedDateSimple(p.news_date));
            }

            if (!sharedPref.getLoginFeature().equals("yes")) {
                vItem.lytComment.setVisibility(View.GONE);
            }

            vItem.comment.setText(p.comments_count + "");

            if (p.content_type != null && p.content_type.equals("Post")) {
                vItem.thumbnailVideo.setVisibility(View.GONE);
            } else {
                vItem.thumbnailVideo.setVisibility(View.VISIBLE);
            }

            if (p.content_type != null && p.content_type.equals("youtube")) {
                Glide.with(context)
                        .load(Constant.YOUTUBE_IMG_FRONT + p.video_id + Constant.YOUTUBE_IMG_BACK)
                        .placeholder(R.drawable.ic_thumbnail)
                        .thumbnail(Tools.RequestBuilder(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(vItem.image);
            } else {
                Glide.with(context)
                        .load(sharedPref.getBaseUrl() + "/upload/" + p.news_image.replace(" ", "%20"))
                        .placeholder(R.drawable.ic_thumbnail)
                        .thumbnail(Tools.RequestBuilder(context))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(vItem.image);
            }

            vItem.lytParent.setOnClickListener(view -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(view, p, position);
                }
            });
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return posts.size();
    }

    @Override
    public int getItemViewType(int position) {
        Post post = posts.get(position);
        if (post != null) {
            return VIEW_ITEM;
        } else {
            return VIEW_PROG;
        }
    }

    public void insertData(List<Post> items) {
        setLoaded();
        int positionStart = getItemCount();
        int itemCount = items.size();
        this.posts.addAll(items);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    @SuppressWarnings("SuspiciousListRemoveInLoop")
    public void setLoaded() {
        loading = false;
        for (int i = 0; i < getItemCount(); i++) {
            if (posts.get(i) == null) {
                posts.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void setLoading() {
        if (getItemCount() != 0) {
            this.posts.add(null);
            notifyItemInserted(getItemCount() - 1);
            loading = true;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void resetListData() {
        this.posts.clear();
        notifyDataSetChanged();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    private void lastItemViewDetector(RecyclerView recyclerView) {

        if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            final StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int lastPos = getLastVisibleItem(layoutManager.findLastVisibleItemPositions(null));
                    if (!loading && lastPos == getItemCount() - 1 && onLoadMoreListener != null) {
                        int current_page = getItemCount() / (AppConfig.LOAD_MORE);
                        onLoadMoreListener.onLoadMore(current_page);
                        loading = true;
                    }
                }
            });
        }
    }

    public interface OnLoadMoreListener {
        void onLoadMore(int current_page);
    }

    private int getLastVisibleItem(int[] into) {
        int lastIdx = into[0];
        for (int i : into) {
            if (lastIdx < i) lastIdx = i;
        }
        return lastIdx;
    }

}
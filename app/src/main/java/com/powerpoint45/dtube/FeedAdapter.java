package com.powerpoint45.dtube;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.google.android.exoplayer2.ExoPlayer;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by michael on 5/11/17.
 */

class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private VideoArrayList videos;
    MainActivity c;
    private Drawable placeholderDrawable;
    private boolean tvMode;
    private int focusedItem;

    static final int TYPE_VIDEO = 1;
    static final int TYPE_LOADER = 2;

    int getFocusedItem() {
        return focusedItem;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView thumbView;
        TextView titleView;
        TextView priceView;
        RelativeTimeTextView timeView;
        TextView userView;
        LinearLayout itemView;
        ImageView removeButton;
        TextView durationText;
        ProgressBar progressBar;

        ViewHolder(View v, int type) {
            super(v);

            if (type == TYPE_VIDEO) {
                itemView = (LinearLayout)v;
                thumbView = v.findViewById(R.id.item_image);
                titleView = v.findViewById(R.id.item_title);
                timeView = v.findViewById(R.id.item_time);
                priceView = v.findViewById(R.id.item_value);
                userView = v.findViewById(R.id.item_user);
                removeButton = v.findViewById(R.id.item_remove);
                durationText = v.findViewById(R.id.duration_text);
            }else {
                progressBar = (ProgressBar)v;
            }
        }
    }


    // Provide a suitable constructor (depends on the kind of dataset)
    FeedAdapter(MainActivity c, boolean tvMode) {
        this.c = c;
        this.tvMode = tvMode;

        placeholderDrawable = c.getResources().getDrawable(R.drawable.ic_ondemand_video);
        setHasStableIds(true);
    }

    public void setVideos(VideoArrayList videos){
        this.videos = videos;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutid = tvMode ? R.layout.feed_item_tv : R.layout.feed_item;

        if (viewType == TYPE_VIDEO) {
            // create a new view
            LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(layoutid, parent, false);
            if (tvMode) {
                if (Preferences.darkMode)
                    v.findViewById(R.id.feed_item_desc_holder).setBackgroundColor(c.getResources().getColor(R.color.transparentBlack));
                v.setLayoutParams(new LinearLayout.LayoutParams(Tools.numtodp(400, c), ViewGroup.LayoutParams.WRAP_CONTENT));
            } else
                v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            // set the view's size, margins, paddings and layout parameters
            return new ViewHolder(v, TYPE_VIDEO);
        }else {

            ProgressBar pb = new ProgressBar(c);
            if (tvMode)
                pb.setLayoutParams(new LinearLayout.LayoutParams(Tools.numtodp(400, c), Tools.numtodp(267, c)));
            return new ViewHolder(pb, TYPE_LOADER);
        }
    }

    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            focusedItem = (int)v.getTag();
        }
    };

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if (getItemViewType(position)==TYPE_VIDEO) {

            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(v -> c.onItemClick((Integer) v.getTag()));

            holder.itemView.setOnLongClickListener(v -> {
                c.onItemLongClick((Integer) v.getTag());
                return true;
            });

            if (tvMode)
                holder.itemView.setFocusableInTouchMode(true);

            holder.itemView.setOnFocusChangeListener(onFocusChangeListener);


            if (videos.get(position).price == null)
                holder.priceView.setVisibility(View.GONE);
            else
                holder.priceView.setVisibility(View.VISIBLE);

            if (!tvMode && videos.get(position).categoryId == DtubeAPI.CAT_HISTORY) {
                holder.removeButton.setVisibility(View.VISIBLE);
                holder.removeButton.setTag(videos.get(position).permlink);
            } else
                holder.removeButton.setVisibility(View.GONE);

            holder.titleView.setText(videos.get(position).title);
            holder.timeView.setReferenceTime(videos.get(position).getDate());
            holder.priceView.setText(videos.get(position).price);
            holder.userView.setText(videos.get(position).user);


            if (videos.get(position).getDuration() != null) {
                holder.durationText.setVisibility(View.VISIBLE);
                holder.durationText.setText(videos.get(position).getDuration());
            } else
                holder.durationText.setVisibility(View.INVISIBLE);


            Picasso.get().load(videos.get(position).getImageURL()).placeholder(placeholderDrawable)
                    .resize(720, 720).centerInside()//prevents image to be shown to be larger than 720px w or h. Makes scrolling smoother
                    .noFade()
                    .into(holder.thumbView, new Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(videos.get(position).getBackupImageURL()).placeholder(placeholderDrawable)
                                    .resize(720, 720).centerInside()//prevents image to be shown to be larger than 720px w or h. Makes scrolling smoother
                                    .noFade()
                                    .into(holder.thumbView);
                        }
                    });
        }else {

            if (videos.size()>0
                    && (videos.get(videos.size()-1).categoryId==DtubeAPI.CAT_SUBSCRIBED
                    ||videos.get(videos.size()-1).categoryId==DtubeAPI.CAT_SUBSCRIBED
                    ||videos.get(videos.size()-1).categoryId==DtubeAPI.CAT_HISTORY)){
                holder.progressBar.setVisibility(View.GONE);
            }else
                holder.progressBar.setVisibility(View.VISIBLE);

        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (videos == null)
            return 0;
        else {
            return videos.size();
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == videos.size()-1)
            return 2;
        else
            return 1;
    }

    @Override
    public long getItemId(int position) {
        if (videos!=null)
            return videos.get(position).hashCode();
        return super.getItemId(position);
    }
}

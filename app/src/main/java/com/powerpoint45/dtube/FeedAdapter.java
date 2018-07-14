package com.powerpoint45.dtube;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.squareup.picasso.Picasso;

/**
 * Created by michael on 5/11/17.
 */

class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder> {

    private VideoArrayList videos;
    MainActivity c;
    private Drawable placeholderDrawable;
    boolean tvMode;

    static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        ImageView thumbView;
        TextView titleView;
        TextView priceView;
        RelativeTimeTextView timeView;
        TextView userView;
        LinearLayout itemView;
        ImageView removeButton;

        ViewHolder(LinearLayout v) {
            super(v);
            itemView = v;
            thumbView = (ImageView) v.findViewById(R.id.item_image);
            titleView = (TextView) v.findViewById(R.id.item_title);
            timeView = (RelativeTimeTextView) v.findViewById(R.id.item_time);
            priceView = (TextView) v.findViewById(R.id.item_value);
            userView = (TextView) v.findViewById(R.id.item_user);
            removeButton = (ImageView) v.findViewById(R.id.item_remove);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    FeedAdapter(MainActivity c, boolean tvMode) {
        this.c = c;
        this.tvMode = tvMode;

        placeholderDrawable = c.getResources().getDrawable(R.drawable.ic_ondemand_video);
    }

    public void setVideos(VideoArrayList videos){
        this.videos = videos;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FeedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.feed_item, parent, false);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        // set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {


        holder.itemView.setTag(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c.onItemClick((Integer)v.getTag());
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                c.onItemLongClick((Integer)v.getTag());
                return true;
            }
        });


        if (videos.get(position).price == null)
            holder.priceView.setVisibility(View.GONE);
        else
            holder.priceView.setVisibility(View.VISIBLE);

        if (!tvMode && videos.get(position).categoryId == DtubeAPI.CAT_HISTORY){
            holder.removeButton.setVisibility(View.VISIBLE);
            holder.removeButton.setTag(videos.get(position).permlink);
        }else
            holder.removeButton.setVisibility(View.GONE);

        holder.titleView.setText(videos.get(position).title);
        holder.timeView.setReferenceTime(videos.get(position).getDate());
        holder.priceView.setText(videos.get(position).price);
        holder.userView.setText(videos.get(position).user);




        Picasso.with(c).load(videos.get(position).getImageURL()).placeholder(placeholderDrawable)
                .resize(720,720).centerInside()//prevents image to be shown to be larger than 720px w or h. Makes scrolling smoother
                .noFade()
                .into(holder.thumbView);

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


}

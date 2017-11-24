package com.powerpoint45.dtube;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
            thumbView = ((ImageView)v.findViewById(R.id.item_image));
            titleView = ((TextView)v.findViewById(R.id.item_title));
            timeView = ((RelativeTimeTextView)v.findViewById(R.id.item_time));
            priceView = ((TextView)v.findViewById(R.id.item_value));
            userView = ((TextView)v.findViewById(R.id.item_user));
            removeButton = ((ImageView)v.findViewById(R.id.item_remove));
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    FeedAdapter(MainActivity c) {
        this.c = c;
    }

    public void setVideos(VideoArrayList videos){
        for (Video v: videos)
            Log.d("dtube","VIDS:"+ v.title +","+v.categoryId);
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


        if (videos.get(position).price == null)
            holder.priceView.setVisibility(View.GONE);
        else
            holder.priceView.setVisibility(View.VISIBLE);

        if (videos.get(position).categoryId == DtubeAPI.CAT_HISTORY){
            holder.removeButton.setVisibility(View.VISIBLE);
            holder.removeButton.setTag(videos.get(position).permlink);
        }else
            holder.removeButton.setVisibility(View.GONE);

        holder.titleView.setText(videos.get(position).title);
        holder.timeView.setReferenceTime(videos.get(position).getDate());
        holder.priceView.setText(videos.get(position).price);
        holder.userView.setText(videos.get(position).user);


        Log.d("dtube3", "picasso loading:"+videos.get(position).getImageURL());
        Picasso.with(c).load(videos.get(position).getImageURL()).placeholder(R.drawable.ic_ondemand_video).resize(720,720).centerInside().into(
                holder.thumbView);
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

package com.powerpoint45.dtube;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.squareup.picasso.Picasso;

/**
 * Created by michael on 12/11/17.
 */

class SuggestionAdapter extends BaseAdapter {

    private VideoArrayList videos;
    VideoPlayActivity c;

    SuggestionAdapter(VideoArrayList list, VideoPlayActivity activity){
        this.videos = list;
        c = activity;
    }

    public void setVideos(VideoArrayList videos){
        this.videos = videos;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (videos == null)
            return 0;
        else
            return videos.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ViewHolder{
        // each data item is just a string in this case
        private ImageView thumbView;
        private TextView titleView;
        private TextView priceView;
        private RelativeTimeTextView timeView;
        private TextView userView;
        private LinearLayout itemView;
        private TextView durationText;

        ViewHolder(LinearLayout v) {
            itemView = v;
            thumbView = v.findViewById(R.id.item_image);
            titleView = v.findViewById(R.id.item_title);
            timeView = v.findViewById(R.id.item_time);
            priceView = v.findViewById(R.id.item_value);
            userView = v.findViewById(R.id.item_user);
            durationText = v.findViewById(R.id.duration_text);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView!=null && convertView.getTag()!=null){
            holder = (ViewHolder) convertView.getTag();
        }else {
            LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.suggested_item, parent, false);
            v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            // set the view's size, margins, paddings and layout parameters
            holder = new ViewHolder(v);
        }



        if (!(holder.thumbView.getTag()!=null && holder.thumbView.getTag().equals(videos.get(position).permlink))) {
            holder.thumbView.setTag(videos.get(position).permlink);
            holder.titleView.setTag(position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    c.onItemClick((Integer) ((ViewHolder) v.getTag()).titleView.getTag());
                }
            });

            holder.titleView.setText(videos.get(position).title);
            holder.timeView.setReferenceTime(videos.get(position).getDate());
            holder.priceView.setText(videos.get(position).price);
            holder.userView.setText(videos.get(position).user);

            if (videos.get(position).getDuration()!=null) {
                holder.durationText.setVisibility(View.VISIBLE);
                holder.durationText.setText(videos.get(position).getDuration());
            }else
                holder.durationText.setVisibility(View.INVISIBLE);


            Picasso.with(c).load(videos.get(position).getImageURL()).placeholder(R.drawable.ic_ondemand_video).resize(400,400).centerInside().into(
                    holder.thumbView);
        }

        holder.itemView.setTag(holder);
        convertView = holder.itemView;

        return convertView;
    }
}

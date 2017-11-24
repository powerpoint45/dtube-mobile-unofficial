package com.powerpoint45.dtube;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;

/**
 * Created by michael on 9/11/17.
 */

class CommentsAdapter extends BaseAdapter {

    private ArrayList<Comment> comments;
    Context c;
    private Transformation transformation;
    boolean loggedIn;

    CommentsAdapter(ArrayList<Comment> comments, Context c, boolean loggedIn){
        this.comments = comments;
        this.c = c;
        this.loggedIn = loggedIn;

        transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(30)
                .oval(false)
                .build();
    }

    void setCommentsList(ArrayList<Comment> comments){
        this.comments = comments;
        notifyDataSetChanged();
    }


    private class ViewHolder{
        TextView usernameView;
        RelativeTimeTextView dateView;
        TextView priceView;
        TextView commentView;
        ImageView profileView;
        ImageView likeView;
        ImageView dislikeView;
        TextView likesView;
        TextView dislikesView;
        TextView replyButton;
        View indentView;
        FrameLayout commentReplyHolder;
        EditText replyEditText;
    }

    @Override
    public int getCount() {
        if(comments == null)
            return 0;
        else
            return comments.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View v, ViewGroup parent) {

        ViewHolder viewHolder;

        if (v!=null && v.getTag()!=null){
            viewHolder = (ViewHolder)v.getTag();
        }else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.comment_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.commentView = (TextView) v.findViewById(R.id.comment_comment);
            viewHolder.dateView = (RelativeTimeTextView) v.findViewById(R.id.comment_date);
            viewHolder.priceView = (TextView) v.findViewById(R.id.comment_price);
            viewHolder.usernameView = (TextView) v.findViewById(R.id.comment_username);
            viewHolder.profileView = (ImageView) v.findViewById(R.id.comment_image);
            viewHolder.likesView = (TextView) v.findViewById(R.id.text_likes);
            viewHolder.dislikesView = (TextView) v.findViewById(R.id.text_dislikes);
            viewHolder.indentView = v.findViewById(R.id.comment_indent);
            viewHolder.likeView = (ImageView) v.findViewById(R.id.comment_like);
            viewHolder.dislikeView = (ImageView) v.findViewById(R.id.comment_dislike);
            viewHolder.replyButton = (TextView) v.findViewById(R.id.comment_reply);
            viewHolder.commentReplyHolder = (FrameLayout) v.findViewById(R.id.comment_reply_holder);
            viewHolder.replyEditText = (EditText)v.findViewById(R.id.item_comment_reply_edittext);
        }

        if (!(viewHolder.likeView.getTag()!=null && comments.get(position).permlink!=null && viewHolder.likeView.getTag().equals(comments.get(position).permlink))){
            Spanned result;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                result = Html.fromHtml(comments.get(position).commentHTML, Html.FROM_HTML_MODE_LEGACY);
            } else {
                result = Html.fromHtml(comments.get(position).commentHTML);
            }
            viewHolder.commentView.setText(result);
            viewHolder.commentView.setMovementMethod(LinkMovementMethod.getInstance());
            viewHolder.usernameView.setText(comments.get(position).userName);
            viewHolder.dateView.setReferenceTime(comments.get(position).getDate());
            if (comments.get(position).indent>0)
                viewHolder.indentView.setVisibility(View.VISIBLE);
            else
                viewHolder.indentView.setVisibility(View.GONE);

            Picasso.with(c).load(comments.get(position).getImageURL()).placeholder(R.drawable.ic_account_circle).transform(transformation)
                    .into(viewHolder.profileView);
        }

        viewHolder.commentReplyHolder.setVisibility(View.GONE);
        viewHolder.replyEditText.setOnEditorActionListener(null);
        viewHolder.replyEditText.setOnKeyListener(null);



        viewHolder.priceView.setText(comments.get(position).price);
        viewHolder.likesView.setText(""+comments.get(position).likes);
        viewHolder.dislikesView.setText(""+comments.get(position).dislikes);

        viewHolder.likeView.setColorFilter(null);
        viewHolder.dislikeView.setColorFilter(null);

        viewHolder.likeView.setTag(comments.get(position).permlink);
        viewHolder.dislikeView.setTag(comments.get(position).permlink);
        viewHolder.replyButton.setTag(comments.get(position).permlink);

        if (!loggedIn) {
            viewHolder.replyButton.setEnabled(false);
            viewHolder.likeView.setEnabled(false);
            viewHolder.dislikeView.setEnabled(false);
        }

        if (comments.get(position).voteType == 1){
            viewHolder.likeView.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
        }else if(comments.get(position).voteType == -1)
            viewHolder.dislikeView.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);


        v.setTag(viewHolder);
        return v;
    }
}

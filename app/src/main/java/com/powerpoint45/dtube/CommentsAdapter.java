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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by michael on 9/11/17.
 */

class CommentsAdapter extends BaseAdapter {

    private CommentsList comments;
    Context c;
    private Transformation transformation;
    boolean loggedIn;
    boolean tvMode;

    CommentsAdapter(CommentsList comments, Context c, boolean loggedIn, boolean tvMode){
        this.comments = comments;
        this.c = c;
        this.loggedIn = loggedIn;
        this.tvMode = tvMode;

        transformation = new RoundedTransformationBuilder()
                .cornerRadiusDp(30)
                .oval(false)
                .build();
    }

    void setCommentsList(CommentsList comments){
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
        Button viewReplies;
    }

    @Override
    public int getCount() {
        if(comments == null)
            return 0;
        else
            return comments.getTotalComments();
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

        Comment currentComment = comments.getCommentByPosition(position, new AtomicInteger(0));

        ViewHolder viewHolder;

        if (v!=null && v.getTag()!=null){
            viewHolder = (ViewHolder)v.getTag();
        }else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.comment_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.commentView = v.findViewById(R.id.comment_comment);
            viewHolder.dateView = v.findViewById(R.id.comment_date);
            viewHolder.priceView = v.findViewById(R.id.comment_price);
            viewHolder.usernameView = v.findViewById(R.id.comment_username);
            viewHolder.profileView = v.findViewById(R.id.comment_image);
            viewHolder.likesView = v.findViewById(R.id.text_likes);
            viewHolder.dislikesView = v.findViewById(R.id.text_dislikes);
            viewHolder.indentView = v.findViewById(R.id.comment_indent);
            viewHolder.likeView = v.findViewById(R.id.comment_like);
            viewHolder.dislikeView = v.findViewById(R.id.comment_dislike);
            viewHolder.replyButton = v.findViewById(R.id.comment_reply);
            viewHolder.commentReplyHolder = v.findViewById(R.id.comment_reply_holder);
            viewHolder.replyEditText = v.findViewById(R.id.item_comment_reply_edittext);
            viewHolder.viewReplies = v.findViewById(R.id.view_replies);

        }

        if (!(viewHolder.likeView.getTag()!=null && currentComment.permlink!=null && viewHolder.likeView.getTag().equals(currentComment.permlink))){
            Spanned result;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                result = Html.fromHtml(currentComment.commentHTML, Html.FROM_HTML_MODE_LEGACY);
            } else {
                result = Html.fromHtml(currentComment.commentHTML);
            }
            viewHolder.commentView.setText(result);
            viewHolder.commentView.setMovementMethod(LinkMovementMethod.getInstance());
            viewHolder.usernameView.setText(currentComment.userName);
            viewHolder.dateView.setReferenceTime(currentComment.getDate());
            if (currentComment.indent>0)
                viewHolder.indentView.setVisibility(View.VISIBLE);
            else
                viewHolder.indentView.setVisibility(View.GONE);

            Picasso.with(c).load(currentComment.getImageURL()).placeholder(R.drawable.ic_account_circle).transform(transformation)
                    .into(viewHolder.profileView);
        }

        if (!viewHolder.replyEditText.hasFocus()) {
            viewHolder.commentReplyHolder.setVisibility(View.GONE);
            viewHolder.replyEditText.setOnEditorActionListener(null);
            viewHolder.replyEditText.setOnKeyListener(null);
        }

        if (currentComment.children>0 && currentComment.childComments==null){
            viewHolder.viewReplies.setVisibility(View.VISIBLE);
        }else if (currentComment.childComments!=null){
            viewHolder.viewReplies.setVisibility(View.GONE);
        }else {
            viewHolder.viewReplies.setVisibility(View.GONE);
        }

        viewHolder.priceView.setText(currentComment.price);
        viewHolder.likesView.setText(""+currentComment.likes);
        viewHolder.dislikesView.setText(""+currentComment.dislikes);

        viewHolder.likeView.setColorFilter(null);
        viewHolder.dislikeView.setColorFilter(null);

        viewHolder.replyButton.setTag(currentComment.permlink);
        viewHolder.likeView.setTag(currentComment.permlink);
        viewHolder.dislikeView.setTag(currentComment.permlink);
        viewHolder.viewReplies.setTag(currentComment.permlink);

        if (!loggedIn) {
            viewHolder.replyButton.setEnabled(false);
            viewHolder.likeView.setEnabled(false);
            viewHolder.dislikeView.setEnabled(false);
        }

        if (tvMode) {
            viewHolder.replyButton.setVisibility(View.INVISIBLE);
            viewHolder.replyButton.setEnabled(false);
        }

        if (currentComment.voteType == 1){
            viewHolder.likeView.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
        }else if(currentComment.voteType == -1)
            viewHolder.dislikeView.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);


        v.setTag(viewHolder);
        return v;
    }
}

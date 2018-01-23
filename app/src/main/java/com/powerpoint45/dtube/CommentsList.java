package com.powerpoint45.dtube;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by michael on 9/11/17.
 */

public class CommentsList extends ArrayList<Comment> {

    public boolean contains(Comment comment){
        for (Comment c: this){
            if (c.permlink.equals(comment.permlink))
                return true;
        }
        return false;
    }

    public Comment getCommentByID(String id){
        for (Comment c: this){

            if (c.permlink.equals(id))
                return c;

            if (c.childComments!=null)
                if (c.childComments.getCommentByID(id)!=null)
                    return c.childComments.getCommentByID(id);

        }

        return null;
    }

    public int getTotalComments(){
        int numberOfComments = 0;
        for (Comment c: this){
            numberOfComments ++;
            if (c.childComments!=null)
                numberOfComments+=c.childComments.getTotalComments();
        }

        return numberOfComments;
    }

    //AtomicInteger is used to find comment by position since in java you can't pass integer by reference
    public Comment getCommentByPosition(int pos, AtomicInteger currentIndex){
        for (int i =0; i<size(); i++){
            if (currentIndex.get() == pos) {
                Log.d("DT","R:"+currentIndex.get()+","+get(i).commentHTML);
                return get(i);
            }

            currentIndex.set(currentIndex.get()+1);

            if (get(i).childComments!=null){
                Comment nestedComment = get(i).childComments.getCommentByPosition(pos,currentIndex);
                if (nestedComment!=null) {
                    Log.d("DT","C:"+currentIndex.get()+","+nestedComment.commentHTML);
                    return nestedComment;
                }
            }
        }
        return null;
    }


    public int getCommentPosition(String id){
        int totalComments = getTotalComments();
        AtomicInteger atomicInteger = new AtomicInteger(0);
        for (int i =0; i<totalComments; i++){
            if (getCommentByPosition(i, atomicInteger).permlink.equals(id))
                return i;
        }

        return 0;
    }

}

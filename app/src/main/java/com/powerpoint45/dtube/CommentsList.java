package com.powerpoint45.dtube;

import java.util.ArrayList;

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
        }

        return null;
    }

}

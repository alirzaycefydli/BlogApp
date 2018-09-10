package com.example.alirz.blogapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private List<Comments> commentsList;

    public CommentsAdapter(List<Comments> commentsList){
        this.commentsList=commentsList;
    }


    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.single_comment_layout,parent,false);

        return new CommentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder holder, int position) {

        String comment=commentsList.get(position).getComment();
        holder.user_comment.setText(comment);



    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{
        private TextView user_name, user_comment;
        private CircleImageView user_image;

        public CommentsViewHolder(View itemView) {
            super(itemView);
            user_image=itemView.findViewById(R.id.comment_user_image);
            user_name=itemView.findViewById(R.id.comment_user_name);
            user_comment=itemView.findViewById(R.id.single_comment);
        }
    }
}

package com.example.alirz.blogapp;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {


    private List<Post> postList;
    private List<User> userList;
    private DocumentReference postFirebase;
    private DocumentReference likesRef;
    private FirebaseAuth mAuth;
    private Context context;


    public PostAdapter(List<Post> postList, List<User> userList) {
        this.postList = postList;
        this.userList = userList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_post_layout, parent, false);

        context = parent.getContext();
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostViewHolder holder, final int position) {

        mAuth = FirebaseAuth.getInstance();
        final String post_id = postList.get(position).PostId;

        String current_user_id = mAuth.getCurrentUser().getUid();
        String blog_user_id=postList.get(position).getUser_id();


        holder.post_descp.setText(postList.get(position).getDescription());
        holder.post_date.setText(postList.get(position).getDate());

        String image_url = postList.get(position).getImage();
        Picasso.get().load(image_url).placeholder(R.drawable.u).into(holder.post_image);


        postFirebase=FirebaseFirestore.getInstance().collection("Posts").document(post_id);

        String user_name = userList.get(position).getName();
        holder.post_user_name.setText(user_name);

        String user_profile_image = userList.get(position).getImage();

        Picasso.get().load(user_profile_image).placeholder(R.drawable.u).into(holder.post_user_image);

        likesRef = FirebaseFirestore.getInstance().collection("Posts").document(post_id);

        likesRef.collection("Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    int likesCount = queryDocumentSnapshots.size();
                    holder.post_like_count.setText(likesCount + " Likes");

                } else {
                    holder.post_like_count.setText("0 Likes");
                }

            }
        });


        likesRef.collection("Likes")
                .document(current_user_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.post_likes.setImageDrawable(context.getDrawable(R.drawable.ic_like_red));
                    } else {
                        holder.post_likes.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like_red));
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.post_likes.setImageDrawable(context.getDrawable(R.drawable.ic_like_grey));
                    } else {
                        holder.post_likes.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like_grey));
                    }

                }
            }
        });

        holder.post_likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                likesRef.collection("Posts").document(post_id).collection("Likes")
                        .document(mAuth.getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (task.isSuccessful()) {
                            if (!task.getResult().exists()) {

                                Map<String, Object> likesMap = new HashMap<>();
                                likesMap.put("timestamp", FieldValue.serverTimestamp());

                                likesRef.collection("Posts").document(post_id).collection("Likes")
                                        .document(mAuth.getCurrentUser().getUid()).set(likesMap);

                            } else {

                                likesRef.collection("Posts").document(post_id).collection("Likes")
                                        .document(mAuth.getCurrentUser().getUid()).delete();
                            }
                        }
                    }
                });


            }
        });

        holder.post_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, CommentActivity.class);
                i.putExtra("post_id", post_id);
                context.startActivity(i);
            }
        });


      // holder.delete_post.setOnClickListener(new View.OnClickListener() {
      //     @Override
      //     public void onClick(View view) {

      //         postFirebase.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
      //             @Override
      //             public void onSuccess(Void aVoid) {

      //                 postList.remove(position);
      //                 userList.remove(position);
      //             }
      //         });
      //     }
      // });


    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        private TextView post_user_name, post_date, post_descp, post_like_count, post_comment_count;
        private CircleImageView post_user_image;
        private ImageView post_image;
        private ImageButton post_likes, post_comment;

        public PostViewHolder(View itemView) {
            super(itemView);

            post_user_name = itemView.findViewById(R.id.single_user_name);
            post_date = itemView.findViewById(R.id.single_post_date);
            post_descp = itemView.findViewById(R.id.single_post_descp);
            post_user_image = itemView.findViewById(R.id.single_user_image);
            post_image = itemView.findViewById(R.id.single_post_image);
            post_likes = itemView.findViewById(R.id.single_like);
            post_like_count = itemView.findViewById(R.id.single_like_count);
            post_comment_count = itemView.findViewById(R.id.single_comment_count);
            post_comment = itemView.findViewById(R.id.single_comment);
        }
    }
}

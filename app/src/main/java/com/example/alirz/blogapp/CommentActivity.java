package com.example.alirz.blogapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class CommentActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText comment_field;
    private RecyclerView recyclerView;
    private ImageButton send_comments;

    private DocumentReference commentRef;
    private FirebaseAuth mAuth;
    private List<Comments> commentsList;
    private CommentsAdapter adapter;


    private String post_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        toolbar = findViewById(R.id.comment_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        comment_field = findViewById(R.id.comment_edit_text);
        send_comments = findViewById(R.id.comment_send);
        recyclerView = findViewById(R.id.comment_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        commentsList=new ArrayList<>();
        adapter=new CommentsAdapter(commentsList);
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();

        post_id = getIntent().getStringExtra("post_id");


        commentRef.collection("Posts").document(post_id).collection("Comments")
                .addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    if (e != null) {
                        Toast.makeText(CommentActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {



                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String commentId = doc.getDocument().getId();
                                Comments comments=doc.getDocument().toObject(Comments.class);
                                commentsList.add(comments);

                                adapter.notifyDataSetChanged();

                            }
                        }

                    }
                }

            }
        });


        send_comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = comment_field.getText().toString();

                if (!TextUtils.isEmpty(comment)) {

                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("comment", comment);
                    commentMap.put("user_id", mAuth.getCurrentUser().getUid());
                    commentMap.put("timestamp", FieldValue.serverTimestamp());

                    commentRef.collection("Posts").document(post_id).collection("Comments").add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(CommentActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            comment_field.setText("");
                        }


                    });

                }
            }
        });



    }
}

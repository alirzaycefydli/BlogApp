package com.example.alirz.blogapp;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;

    private List<Post> postList;
    private List<User> userList;
    private PostAdapter adapter;
    private FirebaseFirestore postRef;
    private DocumentSnapshot lastVisible;
    private Boolean isFirst = true;
    private FirebaseAuth mAuth;
    private DocumentReference userRef;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        postRef = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.post_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        postList = new ArrayList<>();
        userList=new ArrayList<>();
        adapter = new PostAdapter(postList,userList);
        recyclerView.setAdapter(adapter);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {


            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean bottom = !recyclerView.canScrollVertically(1);

                    if (bottom) {

                        displayMore();
                    }
                }
            });


            Query query = postRef.collection("Posts").orderBy("date", Query.Direction.DESCENDING).limit(5); // for pagination


            query.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if (!queryDocumentSnapshots.isEmpty()) {

                        if (e != null) {
                            Toast.makeText(container.getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {

                            if (isFirst) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                postList.clear();
                                userList.clear();
                            }


                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    String postId = doc.getDocument().getId();

                                    final Post post = doc.getDocument().toObject(Post.class).withId(postId);

                                    String blogUserId=doc.getDocument().getString("user_id");

                                    userRef=FirebaseFirestore.getInstance().collection("Users").document(blogUserId);
                                    userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if (task.isSuccessful()){

                                                User user=task.getResult().toObject(User.class);

                                                if (isFirst) {

                                                    userList.add(user);
                                                    postList.add(post);
                                                } else {
                                                    userList.add(0,user);
                                                    postList.add(0, post);
                                                }

                                                adapter.notifyDataSetChanged();

                                            }
                                        }
                                    });


                                }
                            }

                            isFirst = false;


                        }
                    }

                }
            });


        }


        return view;
    }

    private void displayMore() {

        Query nextQuery = postRef.collection("Posts")
                .orderBy("date", Query.Direction.DESCENDING).startAfter(lastVisible).limit(5); // for pagination

        nextQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {

                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);

                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String postId = doc.getDocument().getId();

                                final Post post = doc.getDocument().toObject(Post.class).withId(postId);
                                String blogUserId=doc.getDocument().getString("user_id");
                                userRef=FirebaseFirestore.getInstance().collection("Users").document(blogUserId);
                                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()){

                                            User user=task.getResult().toObject(User.class);

                                                userList.add(user);
                                                postList.add(post);


                                            adapter.notifyDataSetChanged();

                                        }
                                    }
                                });
                            }
                        }
                    }

                }

            }
        });
    }

}

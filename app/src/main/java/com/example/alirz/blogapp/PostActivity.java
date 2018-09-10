package com.example.alirz.blogapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static io.opencensus.tags.TagValue.MAX_LENGTH;

public class PostActivity extends AppCompatActivity {

    //vidgets
    private Toolbar mToolbar;
    private EditText post_descp;
    private ImageView post_image;
    private Button post_button;
    private ProgressBar progressBar;

    //vars
    private static final int PICK_IMAGE_REQUEST = 11;
    private Uri resultUri;
    private StorageReference storageReference;

    private FirebaseAuth mAuth;
    private String current_user_id;

    private CollectionReference postRef;
    private String description;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mToolbar = findViewById(R.id.post_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();

        postRef = FirebaseFirestore.getInstance().collection("Posts");

        post_descp = findViewById(R.id.post_descp);
        post_image = findViewById(R.id.post_image);
        post_button = findViewById(R.id.post_button);
        progressBar=findViewById(R.id.post_progress);

        post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(PostActivity.this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(PostActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
                    } else {
                        pickImageFromGallery();
                    }
                } else {
                    pickImageFromGallery();
                }
            }
        });

        post_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressBar.setVisibility(View.VISIBLE);
                description = post_descp.getText().toString();
                if (TextUtils.isEmpty(description)){
                    description=" ";
                }

                if (resultUri != null) {

                    String post_name= UUID.randomUUID().toString();

                    final StorageReference reference = storageReference.child("user_posts").child(post_name + ".jpg");

                    reference.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()) {

                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        String download_url = uri.toString();

                                        DateFormat format = DateFormat.getTimeInstance();
                                        String date=format.format(Calendar.getInstance().getTime());

                                        Map<String, String> postMap = new HashMap<>();
                                        postMap.put("image", download_url);
                                        postMap.put("date", date);
                                        postMap.put("description",description);
                                        postMap.put("user_id",current_user_id);

                                        postRef.add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()) {

                                                    Intent i = new Intent(PostActivity.this, MainActivity.class);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(i);
                                                    finish();

                                                }
                                            }
                                        });

                                    }
                                });


                            }
                        }
                    });

                    progressBar.setVisibility(View.INVISIBLE);

                }
            }
        });


    }
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    private void pickImageFromGallery() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select an image!"), PICK_IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data.getData() != null) {
            resultUri = data.getData();
            post_image.setImageURI(resultUri);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PICK_IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                pickImageFromGallery();

            } else {
                Toast.makeText(PostActivity.this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

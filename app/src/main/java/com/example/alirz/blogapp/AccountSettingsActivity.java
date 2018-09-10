package com.example.alirz.blogapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettingsActivity extends AppCompatActivity {

    //vidgets
    private Toolbar toolbar;
    private EditText name_input;
    private CircleImageView profile_image;
    private Button save_button;
    private ProgressBar mProgress;


    //vars
    private static final int PICK_IMAGE_REQUEST = 14;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private String current_user_id;
    private DocumentReference userRef;

    private Uri resultUri;
    private String user_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accaunt_settings);

        toolbar = findViewById(R.id.account_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Account Settings");

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        userRef = FirebaseFirestore.getInstance().collection("Users").document(current_user_id);

        name_input = findViewById(R.id.account_name);
        profile_image = findViewById(R.id.account_image);
        save_button = findViewById(R.id.account_save_button);
        mProgress=findViewById(R.id.account_progress);


        user_name=getIntent().getStringExtra("user_name");
        name_input.setText(user_name);

        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    String image = task.getResult().getString("image");
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.u).into(profile_image);
                }
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(AccountSettingsActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(AccountSettingsActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE_REQUEST);
                    } else {
                        pickPhotoFromGallery();
                    }
                } else {
                    pickPhotoFromGallery();
                }


            }
        });

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String user_name = name_input.getText().toString();

                if (!TextUtils.isEmpty(user_name) && resultUri != null) {

                    mProgress.setVisibility(View.VISIBLE);


                    final StorageReference reference = storageReference.child("Profile_images").child(current_user_id + ".jpg");
                    reference.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()) {

                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        String downloadUrl = uri.toString();
                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put("image", downloadUrl);

                                        userRef.update(userMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()) {
                                                            Intent i = new Intent(AccountSettingsActivity.this, MainActivity.class);
                                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(i);
                                                            finish();

                                                        } else {
                                                            Toast.makeText(AccountSettingsActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                });


                            }
                            mProgress.setVisibility(View.INVISIBLE);
                        }
                    });

                }

            }
        });


    }

    private void pickPhotoFromGallery() {
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
            profile_image.setImageURI(resultUri);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PICK_IMAGE_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                pickPhotoFromGallery();

            } else {
                Toast.makeText(AccountSettingsActivity.this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

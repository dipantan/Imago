package me.dipantan.imago.View;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import de.hdodenhof.circleimageview.CircleImageView;
import me.dipantan.imago.Models.UserDetailsModel;
import me.dipantan.imago.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private CircleImageView circleImageView;
    public static final String TAG = "TAG";
    public static final int IMAGE_REQUEST = 1;
    private DatabaseReference reference1, reference2;
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String name, uid;
    private String[] mimeTypes;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        circleImageView = findViewById(R.id.imageButton);
        String path = getApplicationInfo().dataDir + "/" + "files" + "/" + "profile.png";
        name = user.getDisplayName();
        uid = user.getUid();
        Log.d(TAG, path);
//        Glide.with(this).load(path).into(circleImageView);
        try {
            reference1 = FirebaseDatabase.getInstance().getReference("users").child(uid);
            reference1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserDetailsModel model = snapshot.getValue(UserDetailsModel.class);
                    try {
                        Glide.with(ProfileActivity.this)
                                .load(Objects.requireNonNull(model).getPhotoUrl())
                                .into(circleImageView);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        circleImageView.setOnClickListener((v) -> {
            ImagePicker.with(this)
                    .cropSquare()
                    .compress(100)
                    .maxResultSize(400, 400)
                    .galleryMimeTypes(
                            mimeTypes = new String[]{
                                    "image/png",
                                    "image/jpg",
                                    "image/jpeg"
                            })
                    .start();
//            Intent intent = new Intent(Intent.ACTION_PICK);
//            intent.setType("image/*");
//            startActivityForResult(intent, IMAGE_REQUEST);
        });
        //   UserDetailsModel model = new UserDetailsModel();
        //Log.d(TAG, "onCreate: "+model.getPhotoUrl());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Log.d(TAG, "onActivityResult: " + resultCode);
//        Log.d(TAG, "onActivityResult: " + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "ok ");
            try {
                if (data != null) {
                    uri = data.getData();
//               upload image to firebase
                    storageReference = FirebaseStorage.getInstance().getReference("images/profiles/" + uid + ".png");
                    storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                        if (taskSnapshot.getMetadata() != null) {
                            if (taskSnapshot.getMetadata().getReference() != null) {
                                Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                result.addOnSuccessListener(uri -> {
                                    String path = uri.toString();
                                    try {
                                        Log.d(TAG, "onActivityResult: " + path);
                                        reference1 = FirebaseDatabase.getInstance().getReference("users").child(uid);
                                        reference1.child("photoUrl").setValue(path);
                                        reference2 = (DatabaseReference) FirebaseDatabase.getInstance().getReference("posts").orderByChild("author").equalTo(name);
                                        Map<String,String> map = new HashMap<>();
                                        map.put("authorIconUrl",path);
//                                        reference2.updateChildren(map);
                                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
//                                        reference = (DatabaseReference) FirebaseDatabase.getInstance().getReference("posts").orderByChild("author").equalTo(name);
//                                        reference.child("authorIconUrl").setValue(path);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

//                                    Log.d(TAG, "onActivityResult: " + path);
                                });
                            }
                        }
                    });

                }
            } catch (Exception e) {
                Log.d(TAG, "onActivityResult: " + e.getMessage());
            }

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Log.d(TAG, "error ");

        } else {
            Log.d(TAG, "cancelled ");

        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

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
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.StorageReference;
import de.hdodenhof.circleimageview.CircleImageView;
import me.dipantan.imago.Models.UserDetailsModel;
import me.dipantan.imago.R;

import java.io.File;
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
    private String email, uid;
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
        email = user.getEmail();
        uid = user.getUid();
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.progress_animation)
                //.error(R.drawable.user_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(com.bumptech.glide.Priority.HIGH)
                .dontAnimate()
                .dontTransform();
//        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(this);
//        circularProgressDrawable.setStrokeWidth(5f);
//        circularProgressDrawable.setCenterRadius(30f);
//        circularProgressDrawable.start();

        AndroidNetworking.initialize(getApplicationContext());

        // Log.d(TAG, path);
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
                                .apply(options)
                                .apply(RequestOptions.skipMemoryCacheOf(true))
                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
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
                    .compress(20)

                    .maxResultSize(80, 80)
                    .galleryMimeTypes(
                            mimeTypes = new String[]{
                                    "image/png",
                                    "image/jpg",
                                    "image/jpeg"
                            })
                    .start();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            try {
                if (data != null) {
                    uri = data.getData();
                    assert uri != null;
                    File file = new File(Objects.requireNonNull(uri.getPath()));
                    AndroidNetworking.upload("https://dipantan.me/upload/upload.php")
                            .addMultipartFile("fileToUpload", file)
                            .addMultipartParameter("name", user.getUid())
                            .setTag("uploadTest")
                            .setPriority(Priority.HIGH)
                            .build()
                            .setUploadProgressListener((bytesUploaded, totalBytes) -> {
//                                Log.d(TAG, "bytesUploaded: " + bytesUploaded);
//                                Log.d(TAG, "totalBytes: " + totalBytes);
                            })
                            .getAsString(new StringRequestListener() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d(TAG, "onResponse: " + response);
                                    reference1 = FirebaseDatabase.getInstance().getReference("users").child(uid);
                                    reference1.child("photoUrl").setValue(response);
                                    Toast.makeText(ProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                                    finish();
                                    overridePendingTransition(0, 0);
                                    startActivity(getIntent());
                                    overridePendingTransition(0, 0);
                                }

                                @Override
                                public void onError(ANError anError) {
//                                    Log.d(TAG, "onError: " + anError);
                                    Toast.makeText(ProfileActivity.this, anError.getMessage(), Toast.LENGTH_SHORT).show();
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

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        this.finish();
//        startActivity(new Intent(this,FeedActivity.class));
//    }
}

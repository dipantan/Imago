package me.dipantan.imago.View;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import de.hdodenhof.circleimageview.CircleImageView;
import me.dipantan.imago.Controller.OnPostClick;
import me.dipantan.imago.Controller.PostAdapter;
import me.dipantan.imago.Models.PostModel;
import me.dipantan.imago.Models.UserDetailsModel;
import me.dipantan.imago.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class FeedActivity extends AppCompatActivity implements ImageChooser.OnFragmentInteractionListener, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "TAG";
    Button logoutBtn;
    private CircleImageView profileImage;
    private GoogleApiClient googleApiClient;
    private DatabaseReference mDatabase, mDatabase2;
    private FirebaseUser user;
    private ShimmerRecyclerView recyclerView;
    private PostAdapter adapter;
    private ArrayList<PostModel> models;
    private EditText textView;
    private Button button;
    private GoogleSignInAccount account;
    private ImageButton imageButton;
    private Uri uri;
    private int IMAGE_REQUEST = 1;
    private String[] array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setVisibility(View.GONE);
        imageButton = findViewById(R.id.btnCamera);
        profileImage = findViewById(R.id.profileImage);
        recyclerView = findViewById(R.id.recycler_view);
        textView = findViewById(R.id.txt_post);
        button = findViewById(R.id.btn_post);
        // mDatabase = FirebaseDatabase.getInstance().getReference().child("likes");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        models = new ArrayList<>();
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        //_____
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_REQUEST);
//                final Uri filePath = uri;
//                if (filePath != null) {
//                    StorageReference reference = FirebaseStorage.getInstance().getReference("images/profiles/" + user.getUid() + ".jpg");
//                    reference.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                            Log.d(TAG, "onSuccess: uploaded");
//                        }
//                    });
//                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] authorIcon = new String[1];
                String post = textView.getText().toString();
                if (post.equals("")) {
                    Toast.makeText(FeedActivity.this, "Please write something and try again", Toast.LENGTH_SHORT).show();
                    return;
                }
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
                String strDate = dateFormat.format(date);
                // Log.d(TAG, "onClick: " + strDate);
                String author = user.getDisplayName();
                // mDatabase2 = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());

                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserDetailsModel model = snapshot.getValue(UserDetailsModel.class);
                        if (model != null) {
                            authorIcon[0] = model.getPhotoUrl();
                            PostModel postModel = new PostModel();
                            postModel.setAuthorIconUrl(authorIcon[0]);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                mDatabase = FirebaseDatabase.getInstance().getReference().child("posts");
                String key = mDatabase.push().getKey();
                String postImage = "";
//                try {
//                    Log.d(TAG, authorIcon);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                PostModel models = new PostModel(post, author, strDate, authorIcon[0], key, postImage);
                mDatabase.keepSynced(true);
                assert key != null;
                mDatabase.child(key).setValue(models, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        Toast.makeText(FeedActivity.this, "Posted", Toast.LENGTH_SHORT).show();
                        //dismiss keyboard
                        hideSoftKeyBoard();
                        textView.getText().clear();
                    }
                });

            }
        });
        loadPost();
        adapter = new PostAdapter(models, FeedActivity.this, new OnPostClick() {
            @Override
            public void OnClick(View v, int position) {
                Log.d(TAG, "OnClick: " + position);
            }

            @Override
            public void OnToggleClick(CompoundButton button, boolean checked, int position) {
                PostModel model = models.get(position);
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("likes").child(model.getPostKey());
                if (checked) {
                    reference.child(user.getUid()).setValue("true");
                } else {
                    reference.child(user.getUid()).removeValue();
                }

            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.showShimmerAdapter();
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (status.isSuccess()) {
                                    gotoMainActivity();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Session not close", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
        profileImage.setOnClickListener((v) -> {
            Log.d(TAG, "profileImage ");
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");
        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();//image path
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);//picasso or glide cam be used here instead
//                Fragment fragment = ImageChooser.newInstance("","");


                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserDetailsModel model = snapshot.getValue(UserDetailsModel.class);
                        PostModel postModel = new PostModel();
                        if (model != null) {
                            postModel.setAuthorIconUrl(model.getPhotoUrl());
                        }
                        Bundle bundle = new Bundle();
                        String author = user.getDisplayName();
                        String authorIcon = null;
                        if (model != null) {
                            authorIcon = String.valueOf(model.getPhotoUrl());
                        }
                        Date date = Calendar.getInstance().getTime();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.getDefault());
                        String strDate = dateFormat.format(date);
                        String key = mDatabase.push().getKey();
                        String postImage = uri.toString();
                        array = new String[]{author, authorIcon, strDate, key, postImage};
                        bundle.putStringArray("bundle", array);
                        ImageChooser fragment = new ImageChooser();
                        fragment.setArguments(bundle);
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.rr, fragment).addToBackStack(TAG).commit();
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void hideSoftKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if (imm != null && imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        }
    }

    private void loadPost() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("posts");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    models.clear();
                    Log.d(TAG, "onDataChange: cleared");
                    adapter.notifyDataSetChanged();
                }
                try {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        PostModel model = dataSnapshot.getValue(PostModel.class);
                        models.add(model);
                        recyclerView.hideShimmerAdapter();
                        Collections.reverse(models);
                    }
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.d(TAG, "onDataChange: " + e.getMessage());
                    Toast.makeText(FeedActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            account = result.getSignInAccount();
            assert account != null;
            mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());


            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    UserDetailsModel userDetailsModel1 = snapshot.getValue(UserDetailsModel.class);
                    assert userDetailsModel1 != null;
                    if (userDetailsModel1.getPhotoUrl().isEmpty()) {
                        UserDetailsModel userDetailsModel = new UserDetailsModel(account.getDisplayName(), account.getEmail(), Objects.requireNonNull(account.getPhotoUrl()).toString());
                        mDatabase.setValue(userDetailsModel);
                    }
                    adapter.notifyDataSetChanged();
                    Glide
                            .with(FeedActivity.this)
                            .asBitmap()
                            .load(userDetailsModel1.getPhotoUrl())
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    profileImage.setImageBitmap(resource);
                                    try {
                                        Log.d(TAG, "onResourceReady: ");
                                        FileOutputStream fileOutputStream = FeedActivity.this.openFileOutput("profile.png", Context.MODE_PRIVATE);
                                        resource.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                                        fileOutputStream.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {

                                }
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "loadPost:onCancelled", error.toException());
                }
            });

        } else {
            gotoMainActivity();
        }
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}

package me.dipantan.imago.View;


import android.content.Intent;
import android.graphics.Bitmap;
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
import com.androidnetworking.AndroidNetworking;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import de.hdodenhof.circleimageview.CircleImageView;
import me.dipantan.imago.Controller.OnPostClick;
import me.dipantan.imago.Controller.PostAdapter;
import me.dipantan.imago.Models.PostModel;
import me.dipantan.imago.Models.UserDetailsModel;
import me.dipantan.imago.R;

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
    GoogleSignInAccount account;
    private ImageButton imageButton;
    private Uri uri;
    private int IMAGE_REQUEST = 1;
    private String[] array;
    private String authorIcons = null;
    private int positions;
    private String key;
    private RequestOptions options;
    private GoogleSignInResult result;

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
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        models = new ArrayList<>();
        AndroidNetworking.initialize(getApplicationContext());
        user = FirebaseAuth.getInstance().getCurrentUser();
        options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.progress_animation)
                //.error(R.drawable.user_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(com.bumptech.glide.Priority.HIGH)
                .dontAnimate()
                .dontTransform();
        //mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
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
            result = opr.get();
            handleSignInResult(result);
        } else {
            opr.setResultCallback(this::handleSignInResult);
        }

        imageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, IMAGE_REQUEST);
        });
        button.setOnClickListener(v -> {
            String post = textView.getText().toString();
            if (post.equals("")) {
                Toast.makeText(FeedActivity.this, "Please write something and try again", Toast.LENGTH_SHORT).show();
                return;
            }
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
            String strDate = dateFormat.format(date);
            String author = user.getDisplayName();

            mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            mDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserDetailsModel model = snapshot.getValue(UserDetailsModel.class);
                    authorIcons = model != null ? model.getPhotoUrl() : null;
                    mDatabase = FirebaseDatabase.getInstance().getReference().child("posts");
                    key = mDatabase.push().getKey();
                    String postImage = "";
                    Log.d(TAG, "authorIcons: " + authorIcons);
                    PostModel models = new PostModel(post, author, strDate, authorIcons, key, postImage, user.getEmail());
                    mDatabase.child(key).setValue(models, (error, ref) -> {
                        Toast.makeText(FeedActivity.this, "Posted", Toast.LENGTH_SHORT).show();
                        //dismiss keyboard
                        hideSoftKeyBoard();
                        textView.getText().clear();
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        });
        loadPost();
        adapter = new PostAdapter(models, FeedActivity.this, new OnPostClick() {
            @Override
            public void OnClick(View v, int position) {
                positions = position;
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
                        status -> {
                            if (status.isSuccess()) {
                                gotoMainActivity();
                            } else {
                                Toast.makeText(getApplicationContext(), "Session not close", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
        profileImage.setOnClickListener((v) -> {
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

                mDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserDetailsModel model = snapshot.getValue(UserDetailsModel.class);
                        // PostModel postModel = new PostModel();
//                        if (model != null) {
//                            postModel.setAuthorIconUrl(model.getPhotoUrl());
//                        }
                        Bundle bundle = new Bundle();
                        String author = user.getDisplayName();
                        String authorIcon = null;
                        if (model != null) {
                            authorIcon = String.valueOf(model.getPhotoUrl());
                        }
                        Date date = Calendar.getInstance().getTime();
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
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
                    adapter.notifyDataSetChanged();
                }
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    PostModel model = dataSnapshot.getValue(PostModel.class);
                    models.add(model);
                    recyclerView.hideShimmerAdapter();
                    Collections.reverse(models);
                    //adapter.notifyItemChanged(positions);
                    //adapter.notifyDataSetChanged();
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
                    //  assert userDetailsModel1 != null;
                    if (userDetailsModel1 == null) {
                        UserDetailsModel userDetailsModel = new UserDetailsModel(account.getDisplayName(), account.getEmail(), Objects.requireNonNull(account.getPhotoUrl()).toString());
                        mDatabase.setValue(userDetailsModel);
                    }
                    adapter.notifyDataSetChanged();
                    Glide
                            .with(getApplicationContext())
                            .load(userDetailsModel1.getPhotoUrl())
                            .apply(options)
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .into(profileImage);
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
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        handleSignInResult(result);
        loadPost();
    }
}

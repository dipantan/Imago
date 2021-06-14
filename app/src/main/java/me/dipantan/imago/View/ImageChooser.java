package me.dipantan.imago.View;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Objects;

import me.dipantan.imago.Models.PostModel;
import me.dipantan.imago.R;

public class ImageChooser extends Fragment {
    private DatabaseReference reference;
    private static final String TAG = "TAG";
    private ProgressDialog dialog;
    private OnFragmentInteractionListener mListener;

    public ImageChooser() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_image_chooser, container, false);
        dialog = new ProgressDialog(getActivity());
        ImageView imageView = view.findViewById(R.id.image);
        EditText editText = view.findViewById(R.id.caption);
        Button button = view.findViewById(R.id.post);
        final Bundle bundle = this.getArguments();
        assert bundle != null;
        String[] array = bundle.getStringArray("bundle");
        reference = FirebaseDatabase.getInstance().getReference().child("posts");
        final CharSequence post = editText.getText();
        assert array != null;
        final String author = array[0];
        final String authorIcon = array[1];
        final String date = array[2];
        final String key = array[3];
        final String postImage = array[4];
        Glide.with(this).load(postImage).into(imageView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                dialog.setCancelable(false);
                final StorageReference reference1 = FirebaseStorage.getInstance().getReference("images/posts/" + key + ".jpg");
                reference1.putFile(Uri.parse(postImage)).addOnSuccessListener(taskSnapshot -> {
                    if (taskSnapshot.getMetadata() != null) {
                        if (taskSnapshot.getMetadata().getReference() != null) {
                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            result.addOnSuccessListener(uri -> {
                                String path = uri.toString();
                                PostModel model = new PostModel(post.toString(), author, date, authorIcon, key, path);
                                reference.child(key).setValue(model);
                                dialog.dismiss();
                                Intent intent = new Intent(getActivity(), FeedActivity.class);
                                startActivity(intent);
                                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().remove(ImageChooser.this).commit();
                            });
                        }
                    }
                }).addOnProgressListener(snapshot -> {
                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                    dialog.setMessage("Uploaded " + ((int) progress) + "%...");
                });
            }
        });
        Toolbar toolbar = view.findViewById(R.id.profileToolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).onBackPressed();
            }
        });
        return view;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
       // getFragmentManager().beginTransaction().remove((Fragment) ImageChooser).commitAllowingStateLoss();
        Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
}

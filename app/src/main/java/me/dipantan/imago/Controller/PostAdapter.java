package me.dipantan.imago.Controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import de.hdodenhof.circleimageview.CircleImageView;
import me.dipantan.imago.Models.PostModel;
import me.dipantan.imago.R;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {
    private static final String TAG = "TAG";
    private ArrayList<PostModel> models;
    private Context context;
    private OnPostClick click;
    private DatabaseReference reference;
    private FirebaseUser auth;


    public PostAdapter(ArrayList<PostModel> models, Context context, OnPostClick click) {
        this.models = models;
        this.context = context;
        this.click = click;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_posts, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final PostModel model = models.get(position);
        auth = FirebaseAuth.getInstance().getCurrentUser();
        holder.author.setText(model.getAuthor());
        holder.date.setText(model.getDate());
        holder.expandableTextView.setText(model.getPostText());
//        holder.setIsRecyclable(false);
        reference = FirebaseDatabase.getInstance().getReference().child("likes").child(model.getPostKey());
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String likes = String.valueOf(snapshot.getChildrenCount());
                holder.likes.setText(likes + " liked");
                //if liked
                //                    holder.toggleButton.setBackgroundResource(R.drawable.ic_favorite);
                //if not liked
                //                    holder.toggleButton.setBackgroundResource(R.drawable.ic_favorite_border);
                holder.toggleButton.setChecked(snapshot.hasChild(auth.getUid()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Glide.with(context)
                .load(model.getAuthorIconUrl())
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                .into(holder.imageView);
        try {
            if (model.getPostImage().equals("")) {
                holder.postImage.setVisibility(View.GONE);
                holder.expandableTextView.setGravity(Gravity.CENTER);
                holder.expandableText.setTextSize(18);
                holder.expandableText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                holder.expandableText.setGravity(Gravity.CENTER);
//                holder.post.setBackgroundResource(R.color.colorAccent);
                ViewGroup.LayoutParams params = holder.ll0.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.ll0.setLayoutParams(params);
//            holder.ll0.setHe
//            holder.post.setTextAlignment(View.ALIGN_CENTER);
            } else {
                holder.postImage.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params = holder.ll0.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.ll0.setLayoutParams(params);
                Glide.with(context)
                        .load(model.getPostImage())
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                        .into(holder.postImage);
            }
        } catch (Exception e) {
            Log.d(TAG, "onBindViewHolder: " + e);
        }

    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView author, date, likes, expandableText;
        ExpandableTextView expandableTextView;
        CircleImageView imageView;
        ToggleButton toggleButton;
        ImageView postImage;
        LinearLayout ll0;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.author);
            date = itemView.findViewById(R.id.date);
            expandableTextView = (ExpandableTextView) itemView.findViewById(R.id.expand_text_view)
                    .findViewById(R.id.expand_text_view);
            expandableText = itemView.findViewById(R.id.expandable_text);
            likes = itemView.findViewById(R.id.likes);
            imageView = itemView.findViewById(R.id.authorIcon);
            postImage = itemView.findViewById(R.id.postImage);
            toggleButton = itemView.findViewById(R.id.button_favorite);
            ll0 = itemView.findViewById(R.id.ll0);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    click.OnClick(v, getAbsoluteAdapterPosition());
                }
            });
            toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    click.OnToggleClick(buttonView, isChecked, getAbsoluteAdapterPosition());
                }
            });
        }
    }
}

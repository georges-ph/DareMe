package ga.jundbits.dareme.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.dareme.Activities.ProfileActivity;
import ga.jundbits.dareme.Models.ChallengeCommentsBottomSheetModel;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.TimeAgo;

public class ChallengeCommentsBottomSheetRecyclerAdapter extends FirestoreRecyclerAdapter<ChallengeCommentsBottomSheetModel, ChallengeCommentsBottomSheetRecyclerAdapter.ChallengeCommentsBottomSheetViewHolder> {

    Context context;
    TimeAgo timeAgo;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String currentUserID;
    DocumentReference currentUserDocument;

    public ChallengeCommentsBottomSheetRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChallengeCommentsBottomSheetModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull final ChallengeCommentsBottomSheetViewHolder holder, int position, @NonNull ChallengeCommentsBottomSheetModel model) {

        // Retrieving
        final String userID = model.getUser_id();
        String image = model.getImage();
        String username = model.getUsername();
        String comment = model.getComment();
        long dateTimeMillis = model.getDate_time_millis();

        // Applying
        if (image.equals("default")) {
            holder.userImage.setImageResource(R.mipmap.no_image);
        } else {
            Glide.with(context).load(image).into(holder.userImage);
        }
        holder.userUsername.setText(username);
        holder.comment.setText(String.valueOf(comment));
        holder.commentTimeAgo.setText(timeAgo.getTimeAgo(context, dateTimeMillis));

        holder.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.openProfile(userID);
            }
        });

        holder.userUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.openProfile(userID);
            }
        });

    }

    @NonNull
    @Override
    public ChallengeCommentsBottomSheetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.challenge_comments_bottom_sheet_list_item, parent, false);
        context = parent.getContext();
        timeAgo = new TimeAgo();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserID = firebaseUser.getUid();
        currentUserDocument = firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(currentUserID);

        return new ChallengeCommentsBottomSheetViewHolder(view);

    }

    public class ChallengeCommentsBottomSheetViewHolder extends RecyclerView.ViewHolder {

        CircleImageView userImage;
        TextView userUsername, comment, commentTimeAgo;

        public ChallengeCommentsBottomSheetViewHolder(@NonNull View itemView) {
            super(itemView);

            userImage = itemView.findViewById(R.id.challenge_comments_bottom_sheet_list_item_user_image);
            userUsername = itemView.findViewById(R.id.challenge_comments_bottom_sheet_list_item_user_username);
            comment = itemView.findViewById(R.id.challenge_comments_bottom_sheet_list_item_comment_text);
            commentTimeAgo = itemView.findViewById(R.id.challenge_comments_bottom_sheet_list_item_comment_time_ago);

        }

        public void openProfile(String userID) {

            Intent profileIntent = new Intent(context, ProfileActivity.class);
            profileIntent.putExtra("user_id", userID);
            context.startActivity(profileIntent);

        }

    }

}

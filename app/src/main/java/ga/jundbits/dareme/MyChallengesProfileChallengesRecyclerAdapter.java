package ga.jundbits.dareme;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyChallengesProfileChallengesRecyclerAdapter extends FirestorePagingAdapter<MyChallengesProfileChallengesModel, MyChallengesProfileChallengesRecyclerAdapter.MyChallengesProfileChallengesViewHolder> {

    Context context;
    ListItemButtonClick listItemButtonClick;

    TimeAgo timeAgo;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String currentUserID;
    DocumentReference currentUserDocument;

    public MyChallengesProfileChallengesRecyclerAdapter(@NonNull FirestorePagingOptions<MyChallengesProfileChallengesModel> options, Context context, ListItemButtonClick listItemButtonClick) {
        super(options);
        this.context = context;
        this.listItemButtonClick = listItemButtonClick;
    }

    @Override
    protected void onBindViewHolder(@NonNull final MyChallengesProfileChallengesViewHolder holder, int position, @NonNull MyChallengesProfileChallengesModel model) {

        // Retrieving everything
        final String image = model.getImage();
        final String username = model.getUsername();
        final String challengesUsername = model.getChallenges_username();
        long dateTimeMillis = model.getDate_time_millis();

        // Applying
        if (image.equals("default")) {
            holder.myChallengesUserImage.setImageResource(R.mipmap.no_image);
        } else {
            Glide.with(context).load(image).into(holder.myChallengesUserImage);
        }

        currentUserDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String currentUserType = documentSnapshot.getString("type");

                if (currentUserType.equals("player")) {
                    holder.myChallengesUserUsername.setText(username);
                } else if (currentUserType.equals("watcher")) {
                    holder.myChallengesUserUsername.setText(challengesUsername);
                }

            }
        });

        holder.myChallengesTimeAgo.setText(timeAgo.getTimeAgo(context, dateTimeMillis));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent challengeIntent = new Intent(context, ChallengeActivity.class);
                challengeIntent.putExtra("challenge_id", getItem(holder.getAdapterPosition()).getId());
                context.startActivity(challengeIntent);

            }
        });

    }

    @NonNull
    @Override
    public MyChallengesProfileChallengesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_my_challenges_list_item, parent, false);
        context = parent.getContext();

        timeAgo = new TimeAgo();

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        currentUserID = firebaseUser.getUid();
        currentUserDocument = firebaseFirestore.collection(context.getString(R.string.app_name_no_spaces)).document("AppCollections").collection("Users").document(currentUserID);

        return new MyChallengesProfileChallengesViewHolder(view);

    }

    public class MyChallengesProfileChallengesViewHolder extends RecyclerView.ViewHolder {

        CircleImageView myChallengesUserImage;
        TextView myChallengesUserUsername, myChallengesTimeAgo;

        public MyChallengesProfileChallengesViewHolder(@NonNull View itemView) {
            super(itemView);

            myChallengesUserImage = itemView.findViewById(R.id.main_my_challenges_list_item_user_image);
            myChallengesUserUsername = itemView.findViewById(R.id.main_my_challenges_list_item_user_username);
            myChallengesTimeAgo = itemView.findViewById(R.id.main_my_challenges_list_item_time_ago);

        }

    }

    public interface ListItemButtonClick {
        void onListItemButtonClick(String buttonName, String username, String challengeID, int challengePosition);
    }

}

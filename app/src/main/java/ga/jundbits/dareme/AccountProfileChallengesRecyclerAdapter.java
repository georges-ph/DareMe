package ga.jundbits.dareme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;

public class AccountProfileChallengesRecyclerAdapter extends FirestorePagingAdapter<AccountProfileChallengesModel, AccountProfileChallengesRecyclerAdapter.ProfileChallengesViewHolder> {

    OnListItemClick onListItemClick;
    Context context;

    public AccountProfileChallengesRecyclerAdapter(@NonNull FirestorePagingOptions<AccountProfileChallengesModel> options, OnListItemClick onListItemClick, Context context) {
        super(options);
        this.onListItemClick = onListItemClick;
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ProfileChallengesViewHolder holder, int position, @NonNull AccountProfileChallengesModel model) {

        String videoThumbnail = model.getVideo_thumbnail();
        Glide.with(context).load(videoThumbnail).into(holder.accountProfileChallengesListItemImage);

    }

    @NonNull
    @Override
    public ProfileChallengesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_account_profile_challenges_list_item, parent, false);
        return new ProfileChallengesViewHolder(view);
    }

    public class ProfileChallengesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView accountProfileChallengesListItemImage;

        public ProfileChallengesViewHolder(@NonNull View itemView) {
            super(itemView);

            accountProfileChallengesListItemImage = itemView.findViewById(R.id.account_profile_challenges_list_item_image);

            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int imageWidth = screenWidth / 3;

            ViewGroup.LayoutParams params = accountProfileChallengesListItemImage.getLayoutParams();
            params.width = imageWidth;
            params.height = imageWidth;
            accountProfileChallengesListItemImage.setLayoutParams(params);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onListItemClick.onItemClick(getItem(getAdapterPosition()).getId());
        }

    }

    public interface OnListItemClick {
        void onItemClick(String challengeID);
    }

}

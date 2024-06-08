package ga.jundbits.dareme.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;

import ga.jundbits.dareme.Callbacks.OnChallengeClick;
import ga.jundbits.dareme.Models.Challenge;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.ViewHolders.AccountProfileChallengesViewHolder;

public class AccountProfileChallengesRecyclerAdapter extends FirestorePagingAdapter<Challenge, AccountProfileChallengesViewHolder> {

    private final OnChallengeClick onChallengeClick;
    private Context context;

    public AccountProfileChallengesRecyclerAdapter(@NonNull FirestorePagingOptions<Challenge> options, Context context, OnChallengeClick onChallengeClick) {
        super(options);
        this.context = context;
        this.onChallengeClick = onChallengeClick;
    }

    @NonNull
    @Override
    public AccountProfileChallengesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.main_account_profile_challenges_list_item, parent, false);
        return new AccountProfileChallengesViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull AccountProfileChallengesViewHolder holder, int position, @NonNull Challenge challenge) {
        holder.setImage((Activity) context);
        Glide.with(context).load(challenge.getVideo_url()).into(holder.getImage());
        holder.itemView.setOnClickListener(view -> onChallengeClick.onClick(getItem(position).getId(), challenge));
    }

}

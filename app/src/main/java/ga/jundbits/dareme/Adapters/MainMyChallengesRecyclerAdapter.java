package ga.jundbits.dareme.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.gson.Gson;

import ga.jundbits.dareme.Activities.ChallengeActivity;
import ga.jundbits.dareme.Models.Challenge;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.HelperMethods;
import ga.jundbits.dareme.ViewHolders.ChallengeViewHolder;

public class MainMyChallengesRecyclerAdapter extends FirestorePagingAdapter<Challenge, ChallengeViewHolder> {

    private Context context;

    public MainMyChallengesRecyclerAdapter(@NonNull FirestorePagingOptions<Challenge> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.main_my_challenges_list_item, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ChallengeViewHolder holder, int i, @NonNull Challenge challenge) {

        if (HelperMethods.getCurrentUser().getType().equals("player")) {

            holder.getUsername().setText(challenge.getWatcher_username());

            if (challenge.getWatcher_image() == null) {
                holder.getImage().setImageResource(R.mipmap.no_image);
            } else {
                Glide.with(context).load(challenge.getWatcher_image()).into(holder.getImage());
            }

        } else if (HelperMethods.getCurrentUser().getType().equals("watcher")) {

            holder.getUsername().setText(challenge.getPlayer_username());

            if (challenge.getPlayer_image() == null) {
                holder.getImage().setImageResource(R.mipmap.no_image);
            } else {
                Glide.with(context).load(challenge.getPlayer_image()).into(holder.getImage());
            }

        }

        holder.getTimeAgo().setText(TimeAgo.using(challenge.getTimestamp().getTime()));

        holder.itemView.setOnClickListener(v -> {

            Bundle bundle = new Bundle();
            bundle.putString("challenge", new Gson().toJson(challenge));
            bundle.putString("challenge_id", getItem(i).getId());

            Intent challengeIntent = new Intent(context, ChallengeActivity.class);
            challengeIntent.putExtras(bundle);
            context.startActivity(challengeIntent);

        });

    }

}

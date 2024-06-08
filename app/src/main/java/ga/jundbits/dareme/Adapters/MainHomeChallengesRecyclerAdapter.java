package ga.jundbits.dareme.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.gson.Gson;

import ga.jundbits.dareme.Activities.ChallengeAcceptedActivity;
import ga.jundbits.dareme.Activities.ChallengeActivity;
import ga.jundbits.dareme.Callbacks.OnCommentsClick;
import ga.jundbits.dareme.Models.Challenge;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.FirebaseHelper;
import ga.jundbits.dareme.Utils.HelperMethods;
import ga.jundbits.dareme.ViewHolders.MainHomeChallengesViewHolder;

public class MainHomeChallengesRecyclerAdapter extends FirestorePagingAdapter<Challenge, MainHomeChallengesViewHolder> {

    private Context context;
    private final OnCommentsClick onCommentsClick;

    public MainHomeChallengesRecyclerAdapter(@NonNull FirestorePagingOptions<Challenge> options, Context context, OnCommentsClick onCommentsClick) {
        super(options);
        this.context = context;
        this.onCommentsClick = onCommentsClick;
    }

    @NonNull
    @Override
    public MainHomeChallengesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.main_home_challenges_list_item, parent, false);
        return new MainHomeChallengesViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull final MainHomeChallengesViewHolder holder, int position, @NonNull Challenge challenge) {

        DocumentReference challengeDocument = FirebaseHelper.documentReference("Challenges/" + getItem(position).getId());

        if (challenge.getWatcher_image() == null) {
            holder.getUserImage().setImageResource(R.mipmap.no_image);
        } else {
            Glide.with(context).load(challenge.getWatcher_image()).into(holder.getUserImage());
        }

        holder.getUserUsername().setText(challenge.getWatcher_username());

        if (HelperMethods.getCurrentUser().getUsername().equals(challenge.getPlayer_username())) {
            holder.getChallengesUsername().setText(R.string.challenges_you);
            holder.getChallengeAcceptRejectButtonsLayout().setVisibility(challenge.isCompleted() || challenge.isFailed() ? View.GONE : View.VISIBLE);
        } else if (HelperMethods.getCurrentUser().getUsername().equals(challenge.getWatcher_username())) {
            holder.getChallengesUsername().setText(context.getString(R.string.challenges) + " " + challenge.getPlayer_username());
            holder.getChallengeCompletedFailedButtonsLayout().setVisibility(challenge.isCompleted() || challenge.isFailed() ? View.GONE : View.VISIBLE);
        } else {
            holder.getChallengesUsername().setText(context.getString(R.string.challenges) + " " + challenge.getPlayer_username());
        }

        holder.getChallengesTimeAgo().setText(TimeAgo.using(challenge.getTimestamp().getTime()));
        holder.getChallengeText().setBackgroundColor(Color.parseColor(challenge.getColor()));
        holder.getChallengeText().setText(challenge.getDescription());
        holder.getRewards().setText(context.getString(R.string.rewards) + "\n" + challenge.getRewards());
        holder.getChallengeStatusCompletedLayout().setVisibility(challenge.isCompleted() ? View.VISIBLE : View.GONE);
        holder.getChallengeStatusFailedLayout().setVisibility(challenge.isFailed() ? View.VISIBLE : View.GONE);

        holder.getLikeButton().setImageResource(challenge.getLikes().contains(FirebaseHelper.getCurrentUser().getUid()) ? R.drawable.ic_like_red_32dp : R.drawable.ic_like_grey_32dp);
        holder.getLikeCounter().setText(String.valueOf(challenge.getLikes().size()));
        holder.getShareCounter().setText(String.valueOf(challenge.getShare_count()));

        FirebaseHelper.collectionReference("Challenges/" + getItem(position).getId() + "/Comments")
                .count().get(AggregateSource.SERVER)
                .addOnSuccessListener((Activity) context, aggregateQuerySnapshot -> holder.getCommentCounter().setText(String.valueOf(aggregateQuerySnapshot.getCount())));

        holder.getUserImage().setOnClickListener(v -> holder.openProfile(challenge.getWatcher_id()));
        holder.getUserUsername().setOnClickListener(v -> holder.openProfile(challenge.getWatcher_id()));
        holder.getChallengesUsername().setOnClickListener(v -> holder.openProfile(challenge.getPlayer_id()));

        holder.getCardView().setOnClickListener(view -> openActivity(getItem(position).getId(), challenge, position, ChallengeActivity.class));
        holder.getAcceptButton().setOnClickListener(view -> openActivity(getItem(position).getId(), challenge, position, ChallengeAcceptedActivity.class));

        holder.getRejectButton().setOnClickListener(view -> showDialog(context.getString(R.string.reject_challenge),
                context.getString(R.string.rejecting_the_challenge_will_mark_this_challenge_as_failed_this_action_cannot_be_undone_do_you_want_to_proceed),
                context.getString(R.string.reject),
                (dialogInterface, i) -> {
                    challenge.setFailed(true);
                    challengeDocument.update("failed", true);
                    holder.getChallengeCompletedFailedButtonsLayout().setVisibility(View.GONE);
                    holder.getChallengeAcceptRejectButtonsLayout().setVisibility(View.GONE);
                    holder.getChallengeStatusFailedLayout().setVisibility(View.VISIBLE);
                }));

        holder.getCompletedButton().setOnClickListener(view -> showDialog(context.getString(R.string.confirm_challenge_completion),
                context.getString(R.string.do_you_want_to_confirm_that_the_challenge_is_completed),
                context.getString(R.string.yes),
                (dialogInterface, i) -> {
                    challenge.setCompleted(true);
                    challengeDocument.update("completed", true);
                    holder.getChallengeCompletedFailedButtonsLayout().setVisibility(View.GONE);
                    holder.getChallengeAcceptRejectButtonsLayout().setVisibility(View.GONE);
                    holder.getChallengeStatusCompletedLayout().setVisibility(View.VISIBLE);
                }));

        holder.getFailedButton().setOnClickListener(view -> showDialog(context.getString(R.string.confirm_challenge_failure),
                context.getString(R.string.do_you_want_to_confirm_that_the_challenge_is_failed),
                context.getString(R.string.yes),
                (dialogInterface, i) -> {
                    challenge.setFailed(true);
                    challengeDocument.update("failed", true);
                    holder.getChallengeCompletedFailedButtonsLayout().setVisibility(View.GONE);
                    holder.getChallengeAcceptRejectButtonsLayout().setVisibility(View.GONE);
                    holder.getChallengeStatusFailedLayout().setVisibility(View.VISIBLE);
                }));

        holder.getLikeButton().setOnClickListener(view -> {
            if (challenge.getLikes().contains(FirebaseHelper.getCurrentUser().getUid())) {
                challenge.getLikes().remove(FirebaseHelper.getCurrentUser().getUid());
                challengeDocument.update("likes", FieldValue.arrayRemove(FirebaseHelper.getCurrentUser().getUid()));
                FirebaseHelper.documentReference("Users/" + challenge.getPlayer_id()).update("likes", FieldValue.arrayRemove(FirebaseHelper.getCurrentUser().getUid()));
            } else {
                challenge.getLikes().add(FirebaseHelper.getCurrentUser().getUid());
                challengeDocument.update("likes", FieldValue.arrayUnion(FirebaseHelper.getCurrentUser().getUid()));
                FirebaseHelper.documentReference("Users/" + challenge.getPlayer_id()).update("likes", FieldValue.arrayUnion(FirebaseHelper.getCurrentUser().getUid()));
            }
            holder.getLikeButton().setImageResource(challenge.getLikes().contains(FirebaseHelper.getCurrentUser().getUid()) ? R.drawable.ic_like_red_32dp : R.drawable.ic_like_grey_32dp);
            holder.getLikeCounter().setText(String.valueOf(challenge.getLikes().size()));
        });

        holder.getCommentButton().setOnClickListener(view -> onCommentsClick.onClick(getItem(position).getId()));

        holder.getShareButton().setOnClickListener(view -> {
            // TODO: 25-May-24 implement using another service (NOT Firebase)
            FirebaseHelper.createShareUrl(getItem(position).getId(), challenge)
                    .addOnFailureListener((Activity) context, e -> Toast.makeText(context, context.getString(R.string.error_sharing_the_challenge), Toast.LENGTH_SHORT).show())
                    .addOnSuccessListener((Activity) context, shortDynamicLink -> {

                        challenge.setShare_count(challenge.getShare_count() + 1);
                        holder.getShareCounter().setText(String.valueOf(challenge.getShare_count()));
                        challengeDocument.update("share_count", FieldValue.increment(1));

                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, shortDynamicLink.getShortLink().toString());
                        sendIntent.setType("text/plain");

                        Intent shareIntent = Intent.createChooser(sendIntent, "Share challenge");
                        context.startActivity(shareIntent);

                    });
        });

    }

    private void openActivity(String challengeID, Challenge challenge, int challengePosition, Class<? extends Activity> activity) {

        SharedPreferences challengePreferences = context.getSharedPreferences("ChallengePreferences", Context.MODE_PRIVATE);

        // Save position to scroll to it later when restored
        SharedPreferences.Editor editor = challengePreferences.edit();
        editor.putInt("position", challengePosition);
        editor.apply();

        // Navigate to target activity
        Bundle bundle = new Bundle();
        bundle.putString("challenge", new Gson().toJson(challenge));
        bundle.putString("challenge_id", challengeID);

        Intent challengeIntent = new Intent(context, activity);
        challengeIntent.putExtras(bundle);
        context.startActivity(challengeIntent);

    }

    private void showDialog(String title, String message, String button, DialogInterface.OnClickListener onClickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(button, onClickListener);
        builder.setNegativeButton(context.getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

}

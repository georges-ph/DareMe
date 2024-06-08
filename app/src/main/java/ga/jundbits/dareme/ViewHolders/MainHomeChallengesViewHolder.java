package ga.jundbits.dareme.ViewHolders;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.dareme.Activities.ProfileActivity;
import ga.jundbits.dareme.R;

public class MainHomeChallengesViewHolder extends RecyclerView.ViewHolder {

    private CardView cardView;
    private CircleImageView userImage;
    private TextView userUsername, challengesUsername, challengesTimeAgo;
    private CardView challengeTextCardView;
    private TextView challengeText;
    private TextView prizeList;
    private ConstraintLayout challengeStatusCompletedLayout, challengeStatusFailedLayout, challengeAcceptRejectButtonsLayout, challengeCompletedFailedButtonsLayout;
    private Button acceptButton, rejectButton, completedButton, failedButton;
    private ImageButton likeButton, commentButton, shareButton;
    private TextView likeCounter, commentCounter, shareCounter;

    public MainHomeChallengesViewHolder(@NonNull View itemView) {
        super(itemView);

        cardView = itemView.findViewById(R.id.main_home_challenge_list_item_card_view);
        userImage = itemView.findViewById(R.id.main_home_challenge_list_item_user_image);
        userUsername = itemView.findViewById(R.id.main_home_challenge_list_item_user_username);
        challengesUsername = itemView.findViewById(R.id.main_home_challenge_list_item_challenges_username);
        challengesTimeAgo = itemView.findViewById(R.id.main_home_challenge_list_item_challenges_time_ago);
        challengeTextCardView = itemView.findViewById(R.id.main_home_challenge_list_item_challenge_text_card_view);
        challengeText = itemView.findViewById(R.id.main_home_challenge_list_item_challenge_text);
        prizeList = itemView.findViewById(R.id.main_home_challenge_list_item_prize_list);
        challengeStatusCompletedLayout = itemView.findViewById(R.id.main_home_challenge_list_item_challenge_status_completed_layout);
        challengeStatusFailedLayout = itemView.findViewById(R.id.main_home_challenge_list_item_challenge_status_failed_layout);
        challengeAcceptRejectButtonsLayout = itemView.findViewById(R.id.main_home_challenge_list_item_challenge_accept_reject_buttons_layout);
        challengeCompletedFailedButtonsLayout = itemView.findViewById(R.id.main_home_challenge_list_item_challenge_completed_failed_buttons_layout);
        acceptButton = itemView.findViewById(R.id.main_home_challenge_list_item_accept_button);
        rejectButton = itemView.findViewById(R.id.main_home_challenge_list_item_reject_button);
        completedButton = itemView.findViewById(R.id.main_home_challenge_list_item_completed_button);
        failedButton = itemView.findViewById(R.id.main_home_challenge_list_item_failed_button);
        likeButton = itemView.findViewById(R.id.main_home_challenge_list_item_like_button);
        commentButton = itemView.findViewById(R.id.main_home_challenge_list_item_comment_button);
        shareButton = itemView.findViewById(R.id.main_home_challenge_list_item_share_button);
        likeCounter = itemView.findViewById(R.id.main_home_challenge_list_item_like_counter);
        commentCounter = itemView.findViewById(R.id.main_home_challenge_list_item_comment_counter);
        shareCounter = itemView.findViewById(R.id.main_home_challenge_list_item_share_counter);

    }

    public void openProfile(String userID) {

        Intent profileIntent = new Intent(itemView.getContext(), ProfileActivity.class);
        profileIntent.putExtra("user_id", userID);
        itemView.getContext().startActivity(profileIntent);

    }

    public CardView getCardView() {
        return cardView;
    }

    public CircleImageView getUserImage() {
        return userImage;
    }

    public TextView getUserUsername() {
        return userUsername;
    }

    public TextView getChallengesUsername() {
        return challengesUsername;
    }

    public TextView getChallengesTimeAgo() {
        return challengesTimeAgo;
    }

    public CardView getChallengeTextCardView() {
        return challengeTextCardView;
    }

    public TextView getChallengeText() {
        return challengeText;
    }

    public TextView getRewards() {
        return prizeList;
    }

    public ConstraintLayout getChallengeStatusCompletedLayout() {
        return challengeStatusCompletedLayout;
    }

    public ConstraintLayout getChallengeStatusFailedLayout() {
        return challengeStatusFailedLayout;
    }

    public ConstraintLayout getChallengeAcceptRejectButtonsLayout() {
        return challengeAcceptRejectButtonsLayout;
    }

    public ConstraintLayout getChallengeCompletedFailedButtonsLayout() {
        return challengeCompletedFailedButtonsLayout;
    }

    public Button getAcceptButton() {
        return acceptButton;
    }

    public Button getRejectButton() {
        return rejectButton;
    }

    public Button getCompletedButton() {
        return completedButton;
    }

    public Button getFailedButton() {
        return failedButton;
    }

    public ImageButton getLikeButton() {
        return likeButton;
    }

    public ImageButton getCommentButton() {
        return commentButton;
    }

    public ImageButton getShareButton() {
        return shareButton;
    }

    public TextView getLikeCounter() {
        return likeCounter;
    }

    public TextView getCommentCounter() {
        return commentCounter;
    }

    public TextView getShareCounter() {
        return shareCounter;
    }

}
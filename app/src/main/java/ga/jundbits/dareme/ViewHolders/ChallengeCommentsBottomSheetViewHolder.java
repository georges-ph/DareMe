package ga.jundbits.dareme.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.dareme.R;

public class ChallengeCommentsBottomSheetViewHolder extends RecyclerView.ViewHolder {

    private CircleImageView image;
    private TextView username, comment, timeAgo;

    public ChallengeCommentsBottomSheetViewHolder(@NonNull View itemView) {
        super(itemView);

        image = itemView.findViewById(R.id.challenge_comments_bottom_sheet_list_item_user_image);
        username = itemView.findViewById(R.id.challenge_comments_bottom_sheet_list_item_user_username);
        comment = itemView.findViewById(R.id.challenge_comments_bottom_sheet_list_item_comment_text);
        timeAgo = itemView.findViewById(R.id.challenge_comments_bottom_sheet_list_item_comment_time_ago);

    }

    public CircleImageView getImage() {
        return image;
    }

    public TextView getUsername() {
        return username;
    }

    public TextView getComment() {
        return comment;
    }

    public TextView getTimeAgo() {
        return timeAgo;
    }

}
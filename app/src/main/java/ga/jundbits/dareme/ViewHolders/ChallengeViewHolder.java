package ga.jundbits.dareme.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.dareme.R;

public class ChallengeViewHolder extends RecyclerView.ViewHolder {

    private CircleImageView image;
    private TextView username, timeAgo;

    public ChallengeViewHolder(@NonNull View itemView) {
        super(itemView);

        image = itemView.findViewById(R.id.main_my_challenges_list_item_user_image);
        username = itemView.findViewById(R.id.main_my_challenges_list_item_user_username);
        timeAgo = itemView.findViewById(R.id.main_my_challenges_list_item_time_ago);

    }

    public CircleImageView getImage() {
        return image;
    }

    public TextView getUsername() {
        return username;
    }

    public TextView getTimeAgo() {
        return timeAgo;
    }

}

package ga.jundbits.dareme.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.dareme.R;

public class PlayersViewHolder extends RecyclerView.ViewHolder {

    private CircleImageView imageView;
    private TextView usernameView;

    public PlayersViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.new_challenge_list_item_user_image);
        usernameView = itemView.findViewById(R.id.new_challenge_list_item_user_username);

    }

    public CircleImageView getImageView() {
        return imageView;
    }

    public TextView getUsernameView() {
        return usernameView;
    }

}
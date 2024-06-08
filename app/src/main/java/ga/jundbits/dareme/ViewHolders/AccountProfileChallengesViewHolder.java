package ga.jundbits.dareme.ViewHolders;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ga.jundbits.dareme.R;
import ga.jundbits.dareme.Utils.HelperMethods;

public class AccountProfileChallengesViewHolder extends RecyclerView.ViewHolder {

    private ImageView image;

    public AccountProfileChallengesViewHolder(@NonNull View itemView) {
        super(itemView);
        image = itemView.findViewById(R.id.account_profile_challenges_list_item_image);
    }

    public ImageView getImage() {
        return image;
    }

    public void setImage(Activity activity) {
        ViewGroup.LayoutParams params = image.getLayoutParams();
        params.width = HelperMethods.getScreenWidth(activity) / 3;
        params.height = HelperMethods.getScreenWidth(activity) / 3;
        image.setLayoutParams(params);
    }

}
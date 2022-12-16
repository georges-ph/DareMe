package ga.jayp.dareme;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewChallengePlayersRecyclerAdapter extends RecyclerView.Adapter<NewChallengePlayersRecyclerAdapter.NewChallengePlayersViewHolder> {

    List<NewChallengePlayersModel> newChallengePlayersModelList;
    Context context;
    ListItemButtonClick listItemButtonClick;

    public NewChallengePlayersRecyclerAdapter(Context context, List<NewChallengePlayersModel> newChallengePlayersModelList, ListItemButtonClick listItemButtonClick) {

        this.context = context;
        this.newChallengePlayersModelList = newChallengePlayersModelList;
        this.listItemButtonClick = listItemButtonClick;

    }


    public void updateList(List<NewChallengePlayersModel> list) {
        newChallengePlayersModelList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NewChallengePlayersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_challenge_players_list_item, parent, false);
        context = parent.getContext();
        return new NewChallengePlayersViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final NewChallengePlayersViewHolder holder, int position) {

        holder.setIsRecyclable(true);

        String image = newChallengePlayersModelList.get(position).getImage();
        final String username = newChallengePlayersModelList.get(position).getUsername();

        if (image.equals("default")) {
            holder.playerUserImage.setImageResource(R.mipmap.no_image);
        } else {
            Glide.with(context).load(image).into(holder.playerUserImage);
        }

        holder.playerUserUsername.setText(username);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                listItemButtonClick.onListItemButtonClick(username);

            }
        });

    }

    @Override
    public int getItemCount() {
        return newChallengePlayersModelList.size();
    }

    public class NewChallengePlayersViewHolder extends RecyclerView.ViewHolder {

        CircleImageView playerUserImage;
        TextView playerUserUsername;

        public NewChallengePlayersViewHolder(@NonNull View itemView) {
            super(itemView);

            playerUserImage = itemView.findViewById(R.id.new_challenge_list_item_user_image);
            playerUserUsername = itemView.findViewById(R.id.new_challenge_list_item_user_username);

        }

    }

    public interface ListItemButtonClick {
        void onListItemButtonClick(String username);
    }

}

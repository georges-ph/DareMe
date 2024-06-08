package ga.jundbits.dareme.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ga.jundbits.dareme.Callbacks.OnPlayerClick;
import ga.jundbits.dareme.Models.User;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.ViewHolders.PlayersViewHolder;

public class PlayersRecyclerAdapter extends RecyclerView.Adapter<PlayersViewHolder> {

    private List<User> playersList;
    private Context context;
    private final OnPlayerClick onPlayerClick;

    public PlayersRecyclerAdapter(Context context, List<User> playersList, OnPlayerClick onPlayerClick) {
        this.context = context;
        this.playersList = playersList;
        this.onPlayerClick = onPlayerClick;
    }

    @NonNull
    @Override
    public PlayersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.new_challenge_players_list_item, parent, false);
        return new PlayersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlayersViewHolder holder, int position) {

        User player = playersList.get(position);

        if (player.getImage() == null) {
            holder.getImageView().setImageResource(R.mipmap.no_image);
        } else {
            Glide.with(context).load(player.getImage()).into(holder.getImageView());
        }

        holder.getUsernameView().setText(player.getUsername());

        holder.itemView.setOnClickListener(v -> onPlayerClick.onClick(player));

    }

    public void update(List<User> list) {
        playersList = list;
        notifyItemRangeChanged(0, playersList.size());
    }

    @Override
    public int getItemCount() {
        return playersList.size();
    }

}

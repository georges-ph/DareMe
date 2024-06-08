package ga.jundbits.dareme.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;

import java.util.Date;
import java.util.List;

import ga.jundbits.dareme.Activities.ProfileActivity;
import ga.jundbits.dareme.Models.Comment;
import ga.jundbits.dareme.R;
import ga.jundbits.dareme.ViewHolders.ChallengeCommentsBottomSheetViewHolder;

public class ChallengeCommentsBottomSheetRecyclerAdapter extends RecyclerView.Adapter<ChallengeCommentsBottomSheetViewHolder> {

    private Context context;
    private List<Comment> commentsList;

    public ChallengeCommentsBottomSheetRecyclerAdapter(Context context, List<Comment> commentsList) {
        this.context = context;
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public ChallengeCommentsBottomSheetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.challenge_comments_bottom_sheet_list_item, parent, false);
        return new ChallengeCommentsBottomSheetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeCommentsBottomSheetViewHolder holder, int position) {

        Comment comment = commentsList.get(position);

        if (comment.getImage() == null) {
            holder.getImage().setImageResource(R.mipmap.no_image);
        } else {
            Glide.with(context).load(comment.getImage()).into(holder.getImage());
        }
        holder.getUsername().setText(comment.getUsername());
        holder.getComment().setText(comment.getComment());
        holder.getTimeAgo().setText(TimeAgo.using((comment.getTimestamp() != null ? comment.getTimestamp() : new Date()).getTime()));

        holder.getImage().setOnClickListener(v -> openProfile(comment.getUser_id()));
        holder.getUsername().setOnClickListener(v -> openProfile(comment.getUser_id()));

    }

    public void update(List<Comment> list) {
        commentsList = list;
        notifyItemRangeChanged(0, commentsList.size());
    }

    private void openProfile(String userID) {

        Intent profileIntent = new Intent(context, ProfileActivity.class);
        profileIntent.putExtra("user_id", userID);
        context.startActivity(profileIntent);

    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

}

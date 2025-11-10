package micheal.must.signuplogin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import micheal.must.signuplogin.R;
import micheal.must.signuplogin.models.CommunityPost;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<CommunityPost> postList;
    private Context context;
    private OnCommentClickListener commentClickListener;
    private OnShareClickListener shareClickListener;

    public interface OnCommentClickListener {
        void onCommentClick(CommunityPost post, int position);
    }

    public interface OnShareClickListener {
        void onShareClick(CommunityPost post);
    }

    public PostAdapter(List<CommunityPost> postList, Context context) {
        this.postList = postList;
        this.context = context;
    }

    public void setOnCommentClickListener(OnCommentClickListener listener) {
        this.commentClickListener = listener;
    }

    public void setOnShareClickListener(OnShareClickListener listener) {
        this.shareClickListener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        CommunityPost post = postList.get(position);

        holder.tvAuthor.setText(post.getUserName());
        holder.tvGroup.setText("Group: " + post.getGroup());
        holder.tvContent.setText(post.getContent());
        holder.tvLikes.setText("Likes: " + post.getLikeCount());
        holder.tvComments.setText("Comments: " + post.getCommentCount());

        holder.btnComment.setOnClickListener(v -> {
            if (commentClickListener != null) {
                commentClickListener.onCommentClick(post, position);
            }
        });

        holder.btnShare.setOnClickListener(v -> {
            if (shareClickListener != null) {
                shareClickListener.onShareClick(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvGroup, tvContent, tvLikes, tvComments;
        ImageButton btnComment, btnShare;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthor = itemView.findViewById(R.id.tv_post_author);
            tvGroup = itemView.findViewById(R.id.tv_post_group);
            tvContent = itemView.findViewById(R.id.tv_post_content);
            tvLikes = itemView.findViewById(R.id.tv_post_likes);
            tvComments = itemView.findViewById(R.id.tv_post_comments);
            btnComment = itemView.findViewById(R.id.btn_comment);
            btnShare = itemView.findViewById(R.id.btn_share);
        }
    }
}

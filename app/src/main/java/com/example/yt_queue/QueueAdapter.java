package com.example.yt_queue;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {
    private Context context;
    private List<VideoItem> videos = new ArrayList<>();
    private QueueListener listener;

    public QueueAdapter(Context context) {
        this.context = context;
    }

    // handler for adding a video to a previously empty queue
    public interface QueueListener {
        public void onVideoAddedToEmptyQueue(VideoItem video);
    }

    public void setQueueListener(QueueListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public QueueAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_queue, parent, false);

        return new QueueAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueAdapter.ViewHolder holder, int position) {
        VideoItem video = videos.get(position);

        holder.videoTitle.setText(video.getTitle());

        Glide.with(context)
                .load(video.getThumbnailUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public void playNext(VideoItem video) {
        System.out.println("QueueAdapter handling play next");
        videos.add(0, video);
        notifyItemInserted(0);

        System.out.println(videos.size() == 1);
        System.out.println(listener != null);

        if (videos.size() == 1 && listener != null) {
            System.out.println("We get here");
            listener.onVideoAddedToEmptyQueue(video);
        }
    }

    public void addToQueue(VideoItem video) {
        videos.add(video);
        notifyItemInserted(videos.size() - 1);

        if (videos.size() == 1 && listener != null) {
            listener.onVideoAddedToEmptyQueue(video);
        }
    }

    public void remove(int position) {
        videos.remove(position);
        notifyItemRemoved(position);
    }

    @Nullable
    public VideoItem getNextVideo() {
        if (!videos.isEmpty()) {
            return videos.get(0);
        }
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout video;
        private ImageButton btnChangePos;
        private TextView videoTitle;
        private ImageView thumbnail;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            video = itemView.findViewById(R.id.queue_video);
            videoTitle = itemView.findViewById(R.id.queue_title);
            thumbnail = itemView.findViewById(R.id.queue_thumbnail);
            btnChangePos = itemView.findViewById(R.id.btn_change_pos);

            // set focus listener for the queue_video item
            video.setOnFocusChangeListener((v, hasFocus) -> {
                // make the button visible/gone depending on focus
                if (hasFocus || btnChangePos.hasFocus() && btnChangePos.getVisibility() == View.GONE) {
                    btnChangePos.setVisibility(View.VISIBLE);
                } else if (!btnChangePos.hasFocus()) {
                    btnChangePos.setVisibility(View.GONE);
                }
            });

            btnChangePos.setOnFocusChangeListener((v, hasFocus) -> {
                if (!video.hasFocus()) {
                    btnChangePos.setVisibility(View.GONE);
                }
            });
        }
    }
}

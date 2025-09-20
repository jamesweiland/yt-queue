package com.example.yt_queue;

import android.content.Context;
import android.provider.MediaStore;
import android.text.Layout;
import android.transition.ChangeBounds;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    private List<VideoItem> videos = new ArrayList<>();
    private Context context;

    private SearchResultButtonListener listener;

    public SearchResultAdapter(Context context) {
        this.context = context;
    }

    public interface SearchResultButtonListener {
        public void onPlayNext(VideoItem video);
        public void onAddToQueue(VideoItem video);
    }


    @NonNull
    @Override
    public SearchResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.search_result, parent, false);

        return new SearchResultAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultAdapter.ViewHolder holder, int position) {
        System.out.println("Binding ViewHolder");
        VideoItem video = videos.get(position);

        holder.videoTitle.setText(video.getTitle());
        holder.channelName.setText(video.getChannelName());

        // load thumbnail with glide
        Glide.with(context)
                .load(video.getThumbnailUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .centerCrop()
                .into(holder.thumbnail);

        if (listener != null) {

            holder.btnAddToQueue.setOnClickListener((v) -> listener.onAddToQueue(video));
            holder.btnPlayNext.setOnClickListener((v) -> listener.onPlayNext(video));
        }

        System.out.println("ViewHolder bound");
    }

    public void setOnButtonClickListener(SearchResultButtonListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public void clear() {
        videos.clear();
        notifyDataSetChanged();
    }

    public void add(VideoItem video) {
        videos.add(video);
        notifyItemInserted(videos.size() - 1);
    }
    public void addAll(List<VideoItem> videos) {
        int startPosition = this.videos.size();
        this.videos.addAll(videos);
        notifyItemRangeInserted(startPosition, videos.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout parent;
        private ConstraintLayout video;
        private ImageView thumbnail;
        private TextView videoTitle;
        private TextView channelName;
        private ConstraintLayout btnContainer;
        private ImageButton btnPlayNext;
        private ImageButton btnAddToQueue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            parent = (ConstraintLayout) itemView;
            video = itemView.findViewById(R.id.video_item);
            thumbnail = itemView.findViewById(R.id.video_thumbnail);
            videoTitle = itemView.findViewById(R.id.video_title);
            channelName = itemView.findViewById(R.id.channel_name);
            btnContainer = itemView.findViewById(R.id.button_container);
            btnPlayNext = itemView.findViewById(R.id.btn_play_next);
            btnAddToQueue = itemView.findViewById(R.id.btn_add_to_queue);

            // handle focus changes
            video.setOnFocusChangeListener((v, hasFocus) -> {
                System.out.println("Video focused");
                if (hasFocus) {
                    showButtons();
                } else if (!btnPlayNext.hasFocus() && !btnAddToQueue.hasFocus()) {
                    hideButtons();
                }
            });

            btnPlayNext.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && !video.hasFocus() && !btnAddToQueue.hasFocus()) {
                    hideButtons();
                }
            });

            btnAddToQueue.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus && !video.hasFocus() && !btnAddToQueue.hasFocus()) {
                    hideButtons();
                }
            });

            System.out.println("ViewHolder created");
        }

        private void showButtons() {
            // post so it doesn't interfere with any recyclerview stuff
            btnContainer.post(new Runnable() {
                @Override
                public void run() {
                    // delay the transition so it looks nice

                    // the buttons will slide in from the right
                    TransitionSet transition = new TransitionSet();
                    transition.addTransition(new ChangeBounds());

                    Slide slide = new Slide(Gravity.END);
                    slide.addTarget(btnContainer.getId());
                    transition.addTransition(slide);
                    transition.setDuration(200);

                    TransitionManager.beginDelayedTransition(parent, transition);

                    // constrain video item end to btn container start
                    ConstraintSet set = new ConstraintSet();
                    set.clone(parent);
                    set.connect(video.getId(), ConstraintSet.END, btnContainer.getId(), ConstraintSet.START);

                    // apply constraint changes
                    set.applyTo(parent);

                    // show buttons
                    btnContainer.setVisibility(View.VISIBLE);
                }
            });

        }

        private void hideButtons() {
            // have to post so it doesn't interfere with any recyclerview animation
            btnContainer.post(new Runnable() {
                @Override
                public void run() {
                    // delay the transition so it looks nice
                    TransitionSet transition = new TransitionSet();
                    transition.addTransition(new ChangeBounds());

                    Slide slide = new Slide(Gravity.END);
                    slide.addTarget(btnContainer.getId());
                    transition.addTransition(slide);
                    transition.setDuration(200);

                    TransitionManager.beginDelayedTransition(parent, transition);

                    // constrain video item end to parent end
                    ConstraintSet set = new ConstraintSet();
                    set.clone(parent);
                    set.connect(video.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

                    // apply constraint changes
                    set.applyTo(parent);

                    // hide buttons
                    btnContainer.setVisibility(View.GONE);
                }
            });
        }
    }
}

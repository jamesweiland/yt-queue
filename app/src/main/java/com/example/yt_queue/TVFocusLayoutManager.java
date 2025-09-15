package com.example.yt_queue;

import android.content.Context;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

public class TVFocusLayoutManager extends LinearLayoutManager {
    private Context context;

    public TVFocusLayoutManager(Context context) {
        super(context);
    }

    @Override
    @Nullable
    public View onInterceptFocusSearch(@Nullable View focused, int direction) {
        System.out.println("intercepting focus search");
        assert focused != null;
        System.out.println(focused.getId());
        ConstraintLayout root = (ConstraintLayout) findContainingItemView(focused);
        assert root != null;
        System.out.println(root.getId());

        int position = getPosition(root);
        int count = getItemCount();

        // handle downward navigation
        if (direction == View.FOCUS_DOWN) {
            if (position < count - 1) {
                // if it's not the last item in the list, get the next item
                scrollToPosition(position + 1);
                return findViewByPosition(position + 1);
            } else {
                // we're at the last item -- trap focus
                return focused;
            }
        }

        // handle upward navigation when it's not the first item
        // (we want the first item to be default behavior -- go to search bar)
        else if (direction == View.FOCUS_UP && position > 0) {
            scrollToPosition(position - 1);
            return findViewByPosition(position - 1);
        }

        // default for everything else
        return super.onInterceptFocusSearch(focused, direction);
    }
}

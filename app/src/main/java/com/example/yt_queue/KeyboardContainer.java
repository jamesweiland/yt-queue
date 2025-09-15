package com.example.yt_queue;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

// a layout that inherits from ConstraintLayout to allow for custom focus management
public class KeyboardContainer extends ConstraintLayout {

    private final int[][] keyboardGrid = {
            {R.id.key_q, R.id.key_w, R.id.key_e, R.id.key_r, R.id.key_t, R.id.key_y, R.id.key_u, R.id.key_i, R.id.key_o, R.id.key_p},
            {R.id.key_a, R.id.key_s, R.id.key_d, R.id.key_f, R.id.key_g, R.id.key_h, R.id.key_j, R.id.key_k, R.id.key_l},
            {R.id.key_z, R.id.key_x, R.id.key_c, R.id.key_v, R.id.key_b, R.id.key_n, R.id.key_m},
            {R.id.key_space, R.id.key_backspace, R.id.key_search, R.id.key_dismiss} };

    private final int numRows = keyboardGrid.length;

    public KeyboardContainer(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public View focusSearch(View focused, int direction) {
        int [] pos = findKeyPosition(focused.getId());
        if (pos == null) {
            // default behavior for when the view isn't a key
            return super.focusSearch(focused, direction);
        }

        // calculate next position based on direction
        int nextRow = pos[0];
        int nextCol = pos[1];
        switch (direction) {
            case View.FOCUS_UP: nextRow--; break;
            case View.FOCUS_DOWN:
                // wrap to first row if on last
                nextRow = (nextRow + 1) % numRows;
                nextCol = Math.min(pos[1], keyboardGrid[nextRow].length - 1);
                break;

            case View.FOCUS_LEFT:
                // wrap right if necessary
                int numColsLeft = keyboardGrid[pos[0]].length;
                nextCol = (nextCol - 1 + numColsLeft) % numColsLeft;
                break;
            case View.FOCUS_RIGHT:
                // wrap left if necessary
                int numColsRight = keyboardGrid[pos[0]].length;
                nextCol = (nextCol + 1) % numColsRight;
                break;
        }

        // extra handling for up direction: if we were already on the top row then exit
        // the keyboard
        if (direction == View.FOCUS_UP && nextRow < 0) {
            setVisibility(View.GONE);
            return super.focusSearch(focused, direction);
        }

        return findViewById(keyboardGrid[nextRow][nextCol]);
    }

    // returns a [row, col] coordinate
    private int[] findKeyPosition(int id) {
        for (int i = 0; i < keyboardGrid.length; i++) {
            for (int j = 0; j < keyboardGrid[i].length; j++) {
                if (keyboardGrid[i][j] == id) {
                    return new int[]{i, j};
                }
            }
        }

        return null; // not found
    }
}

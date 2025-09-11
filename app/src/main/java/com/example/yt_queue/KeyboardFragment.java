package com.example.yt_queue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class KeyboardFragment extends Fragment {

    public interface KeyboardListener {
        void onKeyPressed(String key);
        void onBackspacePressed();
        void onSpacePressed();
        void onSearchPressed();
        void onDismissPressed();
    }

    private KeyboardListener keyboardListener;

    public void setKeyboardListener(KeyboardListener listener) {
        this.keyboardListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_keyboard, container, false);
        setupKeyboardButtons(root);
        return root;
    }

    private void setupKeyboardButtons(View root) {
        // setup letter keys
        setupLetterKeys(root);

        // setup special keys
        Button spaceButton = root.findViewById(R.id.key_space);
        Button backspaceButton = root.findViewById(R.id.key_backspace);
        Button searchButton = root.findViewById(R.id.key_search);
        Button dismissButton = root.findViewById(R.id.key_dismiss);

        // setup listeners
        spaceButton.setOnClickListener(v -> {
            if (keyboardListener != null) {
                keyboardListener.onSpacePressed();
            }
        });
        backspaceButton.setOnClickListener(v -> {
            if (keyboardListener != null) {
                keyboardListener.onBackspacePressed();
            }
        });
        searchButton.setOnClickListener(v -> {
            if (keyboardListener != null) {
                keyboardListener.onSearchPressed();
            }
        });
        dismissButton.setOnClickListener(v -> {
            if (keyboardListener != null) {
                keyboardListener.onDismissPressed();
            }
        });
    }


    private void setupLetterKeys(View root) {
        // define all letter keys and their IDs
        String[] letters = {
                "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P",
                "A", "S", "D", "F", "G", "H", "J", "K", "L",
                "Z", "X", "C", "V", "B", "N", "M"
        };

        int[] keyIds = {
                R.id.key_q, R.id.key_w, R.id.key_e, R.id.key_r, R.id.key_t,
                R.id.key_y, R.id.key_u, R.id.key_i, R.id.key_o, R.id.key_p,
                R.id.key_a, R.id.key_s, R.id.key_d, R.id.key_f, R.id.key_g,
                R.id.key_h, R.id.key_j, R.id.key_k, R.id.key_l,
                R.id.key_z, R.id.key_x, R.id.key_c, R.id.key_v, R.id.key_b,
                R.id.key_n, R.id.key_m
        };

        // set click listeners for all letter keys
        for (int i = 0; i < letters.length; i++) {
            Button button = root.findViewById(keyIds[i]);
            String letter = letters[i];

            button.setOnClickListener(v -> {
                if (keyboardListener != null) {
                    keyboardListener.onKeyPressed(letter.toLowerCase());
                }
            });
        }
    }
}

package com.google.engedu.wordstack;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private static final int WORD_LENGTH = 5;
    public static final int LIGHT_BLUE = Color.rgb(176, 200, 255);
    public static final int LIGHT_GREEN = Color.rgb(200, 255, 200);
    private ArrayList<String> words = new ArrayList<>();
    private Random random = new Random();
    private StackedLayout stackedLayout;
    private String word1, word2, scrambledWord;
    private TextView messageBox;
    private Stack<LetterTile> placedTiles = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("words.txt");
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while((line = in.readLine()) != null) {
                String word = line.trim();
                if (word.length() == WORD_LENGTH) {
                    words.add(word);
                }
            }
        } catch (IOException e) {
            Toast toast = Toast.makeText(this, "Could not load dictionary", Toast.LENGTH_LONG);
            toast.show();
        }


        LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_layout);
        stackedLayout = new StackedLayout(this);
        verticalLayout.addView(stackedLayout, 3);
        startGame();

        View word1LinearLayout = findViewById(R.id.word1);
        word1LinearLayout.setOnTouchListener(new TouchListener());
        word1LinearLayout.setOnDragListener(new DragListener());
        View word2LinearLayout = findViewById(R.id.word2);
        word2LinearLayout.setOnTouchListener(new TouchListener());
        word2LinearLayout.setOnDragListener(new DragListener());
    }

    private void startGame() {
        // Choose Randomly 2 words
        int index1, index2;
        index1 = random.nextInt(words.size());
        index2 = random.nextInt(words.size());
        while (index1 == index2){
            // Index must be different
            index1 = random.nextInt(words.size());
            index2 = random.nextInt(words.size());
        }
        word1 = words.get(index1);
        word2 = words.get(index2);

        // Scramble the word
        scrambledWord = getScrambledWord();

        // Write the scrambled word on the messageBox
        messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText(scrambledWord);

        for (int i = 0; i < WORD_LENGTH*2; i++) {
            LetterTile charTile = new LetterTile(getApplicationContext(),
                    scrambledWord.charAt(WORD_LENGTH*2 - 1 - i));
            stackedLayout.push(charTile);
        }
    }

    private String getScrambledWord() {
        String word = "";
        int pointer1 = 0;
        int pointer2 = 0;
        for (int i = 0; i < WORD_LENGTH*2; i++) {
            if (pointer1 == WORD_LENGTH) {
                word = word + word2.substring(pointer2);
                break;
            }
            if (pointer2 == WORD_LENGTH) {
                word = word + word1.substring(pointer1);
                break;
            }
            if (random.nextInt() % 2 == 1) {
                word = word + word2.charAt(pointer2);
                pointer2++;
            } else {
                word = word + word1.charAt(pointer1);
                pointer1++;
            }
        }
        return word;
    }

    private class TouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN && !stackedLayout.empty()) {
                LetterTile tile = (LetterTile) stackedLayout.peek();
                tile.moveToViewGroup((ViewGroup) v);
                if (stackedLayout.empty()) {
                    TextView messageBox = (TextView) findViewById(R.id.message_box);
                    messageBox.setText(word1 + " " + word2);
                }
                placedTiles.push(tile);
                return true;
            }
            return false;
        }
    }

    private class DragListener implements View.OnDragListener {

        public boolean onDrag(View v, DragEvent event) {
            int action = event.getAction();
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(LIGHT_GREEN);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(LIGHT_BLUE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundColor(Color.WHITE);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign Tile to the target Layout
                    LetterTile tile = (LetterTile) event.getLocalState();
                    tile.moveToViewGroup((ViewGroup) v);
                    placedTiles.push(tile);
                    if (stackedLayout.empty()) {
                        TextView messageBox = (TextView) findViewById(R.id.message_box);
                        messageBox.setText(word1 + " " + word2);
                    }

                    return true;
            }
            return false;
        }
    }

    public boolean onStartGame(View view) {
        TextView messageBox = (TextView) findViewById(R.id.message_box);
        messageBox.setText("Game started");
        LinearLayout word1Layout = (LinearLayout)findViewById(R.id.word1);
        LinearLayout word2Layout = (LinearLayout)findViewById(R.id.word2);
        word1Layout.removeAllViews();
        word2Layout.removeAllViews();
        stackedLayout.clear();
        startGame();
        return true;
    }

    public boolean onUndo(View view) {
        if (!placedTiles.empty()) {
            LetterTile tile = placedTiles.pop();
            tile.moveToViewGroup(stackedLayout);
        } else {
            Toast.makeText(this, "No moves in hsitory", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}

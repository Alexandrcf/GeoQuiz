package com.bignerdranch.android.geoquiz;

import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ProgressBar progressBar;
    private int progressStatus = 1;
    private Handler handler = new Handler();
    private TextView textView;

    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final int REQUEST_CODE_CHEAT = 0;

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mCheatButton;
    private Button mSkipButton;
    private TextView mQuestionTextView;
    private TextView mAnswerCounter;

    private int mCurrentIndex = 0;
    double countCorrectAnswer = 0;

    private boolean mIsCheater;
    int counter = 0;
    int answerCounter = 3;
    boolean isButtonOff = false;
    double countPercentAnswer;

    private Question[] mQuestionBank = new Question[] {
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
            new Question(R.string.question_rusRivers, true)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle) called");
        setContentView(R.layout.activity_main);

        //Проверка сохраненных данных в onCreate(Bundle)
        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
        }

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);
        mAnswerCounter = (TextView) findViewById(R.id.tvCountAnswer);
        mTrueButton = (Button) findViewById(R.id.true_button);
        mFalseButton = (Button) findViewById(R.id.false_button);
        mSkipButton = (Button) findViewById(R.id.skip_button);
        mCheatButton = (Button) findViewById(R.id.cheat_button);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        textView = (TextView) findViewById(R.id.textView);

        updateQuestion();
    }

    public void newGame () {
        progressStatus = 1;
        progressBar.setProgress(progressStatus);
        textView.setText(progressStatus + "/" + progressBar.getMax());
        mCurrentIndex = 0;
        countCorrectAnswer = 0;
        counter = 0;
        answerCounter = 3;
        isButtonOff = false;
        mCheatButton.setEnabled(true);
        mAnswerCounter.setText(Integer.toString(answerCounter));
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);

        updateQuestion();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            mIsCheater = CheatActivity.wasAnswerShown(data);
        }
    }

    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);

        mTrueButton.setEnabled(true);
        mFalseButton.setEnabled(true);
        isButtonOff = false;
    }

    public void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
        int messageResId = 0;
        if (mIsCheater) {
            messageResId = R.string.judgment_toast;
        }
        else {
            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_answer;
                countCorrectAnswer++;
            }
            else {
                messageResId = R.string.incorrect_answer;
            }
        }
        buttonOff();
        isButtonOff = true;
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.answer)
                .setMessage(messageResId)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (counter < mQuestionBank.length -1){
                                    counter++;
                                    mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                                    mIsCheater = false;
                                    updateQuestion();
                                }
                                else {
                                    finishGame();
                                }
                            }
                        });
        dialog.show();
    }

    public void ProgressBar() {
        progressBar.setMax(mQuestionBank.length);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (progressStatus < mQuestionBank.length) {
                    progressStatus++;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(progressStatus);
                            textView.setText(progressStatus + "/" + progressBar.getMax());
                        }
                    });
                }
            }
        }).start();
    }

    public void buttonOff() {
        mTrueButton.setEnabled(false);
        mFalseButton.setEnabled(false);
    }

    public void finishGame(){
        countPercentAnswer =  100.0 / mQuestionBank.length * countCorrectAnswer;
        String formattedСountPercentAnswer = String.format("%.2f", countPercentAnswer);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.game_over)
                .setTitle(R.string.percent)
                .setMessage(String.valueOf(formattedСountPercentAnswer  + " %"))
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newGame();
                    }
                });
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isButtonOff == true) {
            buttonOff();
        }
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressBar();
                checkAnswer(true);
            }
        });
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProgressBar();
                checkAnswer(false);
            }
        });
        mSkipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (counter < mQuestionBank.length -1){
                    counter++;
                    mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                    mIsCheater = false;
                    ProgressBar();
                    updateQuestion();
                }
                else {
                    ProgressBar();
                    finishGame();
                }
            }
        });

        if (answerCounter == 0) {
            mCheatButton.setEnabled(false);
        }

        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (answerCounter > 0){
                    answerCounter--;
                    mAnswerCounter.setText(Integer.toString(answerCounter));
                    boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                    Intent intent = CheatActivity.newIntent(MainActivity.this, answerIsTrue);
                    startActivityForResult(intent, REQUEST_CODE_CHEAT);
                }
                else if(answerCounter == 0) {
                    mCheatButton.setEnabled(false);
                    mAnswerCounter.setText(Integer.toString(answerCounter));
                }
            }
        });

        progressBar.setMax(mQuestionBank.length);
        progressBar.setProgress(progressStatus);
        textView.setText(progressStatus + "/" + progressBar.getMax());

        mAnswerCounter.setText(Integer.toString(answerCounter));
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt("currentIndex", mCurrentIndex);
        savedInstanceState.putDouble("countCorrectAnswer", countCorrectAnswer);
        savedInstanceState.putBoolean("mIsCheater", mIsCheater);
        savedInstanceState.putInt("answerCounter", answerCounter);
        savedInstanceState.putBoolean("buttonOff", isButtonOff);
        savedInstanceState.putInt("counter", counter);
        savedInstanceState.putInt("progressStatus", progressStatus);
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentIndex = savedInstanceState.getInt("currentIndex");
        countCorrectAnswer = savedInstanceState.getDouble("countCorrectAnswer");
        mIsCheater = savedInstanceState.getBoolean("mIsCheater");
        answerCounter = savedInstanceState.getInt("answerCounter");
        isButtonOff = savedInstanceState.getBoolean("buttonOff");
        counter = savedInstanceState.getInt("counter");
        progressStatus = savedInstanceState.getInt("progressStatus");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    @Override
    public void onClick(View v) {

    }
}

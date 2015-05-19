package com.tinvillanueva.marslander;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;


public class GameActivity extends Activity implements View.OnTouchListener{

    private GameView gameView;
    private ImageButton leftThruster;
    private ImageButton rightThruster;
    private ImageButton mainThruster;
    private ImageButton reset;
    private ProgressBar fuelGauge;

    //sound effect
    private SoundPool soundPool;
    private int thrusterSoundID;
    private int thrusterStreamID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //removes title bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.game_screen);

        gameView = (GameView) findViewById(R.id.gameView);

        leftThruster = (ImageButton) findViewById(R.id.btnLeftThruster);
        leftThruster.setOnTouchListener(this);

        rightThruster = (ImageButton) findViewById(R.id.btnRightThruster);
        rightThruster.setOnTouchListener(this);

        mainThruster = (ImageButton) findViewById(R.id.btnMainThruster);
        mainThruster.setOnTouchListener(this);

        reset = (ImageButton) findViewById(R.id.btnReset);
        reset.setOnTouchListener(this);

        fuelGauge = (ProgressBar) findViewById(R.id.fuelGauge);
        gameView.setFuelGauge(fuelGauge);

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        thrusterSoundID = soundPool.load(this, R.raw.thruster, 1);

    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int id = view.getId();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
//                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (id == R.id.btnMainThruster) {
                    gameView.mainThrusterOn = true;
                    thrusterStreamID = soundPool.play(thrusterSoundID, 0.4f, 0.4f, 1, -1, 1f);
                }
                if (id == R.id.btnLeftThruster){
                    gameView.leftThrusterOn = true;
                    thrusterStreamID = soundPool.play(thrusterSoundID, 0.1f, 0.1f, 1, -1, 1f);
                }
                if (id == R.id.btnRightThruster) {
                    gameView.rightThrusterOn = true;
                    thrusterStreamID = soundPool.play(thrusterSoundID, 0.1f, 0.1f, 1, -1, 1f);
                }
                if(id == R.id.btnReset)
                    gameView.reset();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
//                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (id == R.id.btnLeftThruster){
                    gameView.leftThrusterOn = false;
                    soundPool.stop(thrusterStreamID);
                }
                if (id == R.id.btnRightThruster) {
                    gameView.rightThrusterOn = false;
                    soundPool.stop(thrusterStreamID);
                }
                if (id == R.id.btnMainThruster) {
                    gameView.mainThrusterOn = false;
                    soundPool.stop(thrusterStreamID);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.startGameThread();
    }
}

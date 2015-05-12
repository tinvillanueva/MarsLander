package com.tinvillanueva.marslander;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
    private ImageButton pause;
    private ProgressBar fuelGauge;

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

        pause = (ImageButton) findViewById(R.id.btnPause);
        pause.setOnTouchListener(this);

        fuelGauge = (ProgressBar) findViewById(R.id.fuelGauge);
        gameView.setFuelGauge(fuelGauge);

    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int id = view.getId();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:

//                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (id == R.id.btnLeftThruster){
                    gameView.leftThrusterOn = true;
                }
                if (id == R.id.btnRightThruster) {
                    gameView.rightThrusterOn = true;
                }
                if (id == R.id.btnMainThruster) {
                    gameView.mainThrusterOn = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:

                break;
            case MotionEvent.ACTION_UP:

//                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (id == R.id.btnLeftThruster){
                    gameView.leftThrusterOn = false;
                }
                if (id == R.id.btnRightThruster) {
                    gameView.rightThrusterOn = false;
                }
                if (id == R.id.btnMainThruster) {
                    gameView.mainThrusterOn = false;
                }
                if (id == R.id.btnPause) {
                    gameView.togglePause();
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

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pauseGame();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return false;
    }
}

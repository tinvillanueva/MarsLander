package com.tinvillanueva.marslander;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Path;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Region;
import android.graphics.Shader;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class Game extends SurfaceView implements Runnable, SurfaceHolder.Callback{

    //variables declaration
    SurfaceHolder holder;
    private Thread gameThread;
    private float x;
    private float y;
    private int screenWidth;
    private int screenHeight;
    private boolean running;
    private Canvas canvas;
    private Bitmap bitmap;
    private Bitmap rocket;
    private Paint paint;

    //terrain
    private int maxTerrainHeight;
    private final int TERRAIN_POINTS = 5;

    private Paint terrainPaint;
    private Path terrainPath;
    private Region terrainRegion;
    private Bitmap terrainTexture;
    private BitmapShader terrainShade;

    private Path platformPath;
    private Region platformRegion;
    private int platformWidth;
    private int platformX;
    private int platformY;


    //default constructor
    public Game(Context context) {
        super(context);
        setupGame();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //startGame()
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        //pauseGame()
    }

    @Override
    public void run() {
        while (running) {
            if (!holder.getSurface().isValid()){
                continue;
            }
            synchronized (holder){
                canvas = holder.lockCanvas();
                //drawCanvas()
            }

            try {
                Thread.sleep(50);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }

            holder.unlockCanvasAndPost(canvas);
        }
    }

    //setup game
    private void setupGame(){
        running = false;
        holder = getHolder();
        holder.addCallback(this);
        screenHeight = getHeight();
        screenWidth = getWidth();
        maxTerrainHeight = screenHeight/3;
        
        marsTerrain();
    }

    private void marsTerrain() {
        //defining terrain texture
        terrainTexture = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.mars);
        terrainShade = new BitmapShader(terrainTexture, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        terrainPaint = new Paint();
        terrainPaint.setColor(0xFFFFFFFF);
        terrainPaint.setStyle(Paint.Style.FILL);
        terrainPaint.setShader(terrainShade);
           //todo good night!

    }

    //start game thread
    public void startGame(){
        if (gameThread != null){
            if (!running){
                gameThread.start();
            }
        }
        else {
            gameThread = new Thread(this);
            gameThread.start();
        }
        running = true;
    }

    //pause game thread
    public void pauseGame(){
        running = false;
        boolean retry = true;
        while (retry){
            try {
                gameThread.join();  //finishes game thread and let it die a natural death
                retry = false;
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        gameThread = null;
    }
}

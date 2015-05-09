package com.tinvillanueva.marslander;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback{

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
    private final int MIN_TERRAIN_POINTS = 5;

    private Paint terrainPaint;
    private Path terrainPath;
    private Region terrainRegion;
    private Bitmap terrainTexture;
    private BitmapShader terrainShade;
    private Random randomTerrain;

    private Path landingPadPath;
    private Region landingPadRegion;
    private int landingPadWidth;
    private int landingPadX;
    private int landingPadY;

    //default constructor


    public GameView(Context context) {
        super(context);
        setupGame();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupGame();
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
            canvas = holder.lockCanvas();
            synchronized (holder){
                doDraw(canvas);
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

    private void doDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);

        paint.setColor(Color.GREEN);
        canvas.drawPath(terrainPath, paint);

        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath( landingPadPath, paint);

//        paint.setColor(Color.YELLOW);
//        canvas.drawPath(terrainPath, paint);
    }

    //setup game
    private void setupGame(){
        running = false;
        holder = getHolder();
        holder.addCallback(this);

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;
        maxTerrainHeight = screenHeight/2;

        setFocusable(true);

        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);

        Path test;
        test = new Path();
        test.moveTo(0, screenHeight);
        marsTerrain();
    }

    private void marsTerrain() {
        //terrain texture
        terrainTexture = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.mars);
        terrainShade = new BitmapShader(terrainTexture, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        terrainPaint = new Paint();
        terrainPaint.setColor(0xFFFFFFFF);
        terrainPaint.setStyle(Paint.Style.FILL);
        terrainPaint.setShader(terrainShade);
        //random terrain path
        randomTerrain = new Random();
        terrainPath = new Path();
        terrainPath.setFillType(Path.FillType.WINDING);
        terrainPath.setLastPoint(0, screenHeight);
        int lastX = 0;
        int lastY = screenHeight - randomTerrain.nextInt(screenHeight/2);
        terrainPath.lineTo(lastX, lastY);

        int newX = lastX;
        int newY = lastY;

        boolean landingPadExists = false;

        while (newX < screenWidth){
            lastX = newX;
            lastY = newY;
            newX += randomTerrain.nextInt(screenWidth/MIN_TERRAIN_POINTS);
            newY += screenHeight - randomTerrain.nextInt(maxTerrainHeight);
            terrainPath.cubicTo(interpolateLinear(lastX, newX, 0.333f), lastY,
                    interpolateLinear(lastX, newX,0.666f), newY, newX, newY);
            if (newX > (screenWidth/2) && (!landingPadExists)) {
                //draw landing area
                landingPadWidth = (screenWidth/5);
                landingPadX = newX + randomTerrain.nextInt(screenWidth/MIN_TERRAIN_POINTS);
                landingPadY = screenHeight - randomTerrain.nextInt(screenWidth/MIN_TERRAIN_POINTS);
                terrainPath.lineTo(landingPadX, landingPadY);
                terrainPath.lineTo((landingPadX +landingPadWidth), landingPadY);
                newX = landingPadX + landingPadWidth;
                newX = landingPadY;
                createLandingArea();
                landingPadExists = true;
            }
            terrainPath.lineTo(screenWidth, screenHeight);
            terrainPath.close();
        }



    }

    private void createLandingArea() {
         landingPadPath = new Path();
         landingPadPath.moveTo(landingPadX, landingPadY);
         landingPadPath.lineTo(landingPadX + MIN_TERRAIN_POINTS, landingPadY - MIN_TERRAIN_POINTS);
         landingPadPath.lineTo(landingPadX + landingPadWidth - MIN_TERRAIN_POINTS,
                landingPadY - MIN_TERRAIN_POINTS);
         landingPadPath.lineTo(landingPadX + landingPadWidth, landingPadY);
         landingPadPath.close();
    }


    private int interpolateLinear(int start, int end, float part){
        return (int) (start*(1-part) + end*part);
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
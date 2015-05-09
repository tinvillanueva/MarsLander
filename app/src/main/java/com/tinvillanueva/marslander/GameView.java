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
    private Paint paint;

    //terrain
    private int maxTerrainHeight;
    private final int MIN_TERRAIN_POINTS = 6;

    private Paint terrainPaint;
    private Path terrainPath;
    private Region terrainRegion;
    private Bitmap terrainTexture;
    private BitmapShader terrainShade;
    private Random randomTerrain;

    private Path landingPadPath;
    private Region landingPadRegion;
    private Region clip;
    private int landingPadWidth;
    private int landingPadX;
    private int landingPadY;

    //flying object variables
    private Bitmap rocket;
    private Bitmap mainFlame;
    private Bitmap leftFlame;
    private Bitmap rightFlame;
    private float rocketX;
    private float rocketY;
    private Path rocketPath;



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
        canvas.drawColor(Color.BLACK);  //background color
        //terrain
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
        canvas.drawPath(terrainPath,terrainPaint);
        paint.setColor(Color.DKGRAY);
        canvas.drawPath(terrainPath, paint);

//        paint.setColor(Color.GREEN);
//        canvas.drawPath(terrainPath, paint);
        //landing pad
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath( landingPadPath, paint);

        //rocket
        canvas.drawBitmap(rocket, rocketX, rocketY, null);
    }

    //setup game
    private void setupGame(){
        holder = getHolder();
        holder.addCallback(this);
        running = false;
        setFocusable(true);

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;

        clip = new Region(0, 0, screenWidth, screenHeight);

        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5);


        createMarsTerrain();
        createRocketShip();
    }

    private void createMarsTerrain() {
        //terrain texture
        terrainTexture = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.mars);
        terrainShade = new BitmapShader(terrainTexture, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        terrainPaint = new Paint();
        terrainPaint.setColor(0xFFFFFFFF);
        terrainPaint.setStyle(Paint.Style.FILL);
        terrainPaint.setShader(terrainShade);
        //terrain path
        terrainPath = new Path();
        terrainPath.setFillType(Path.FillType.WINDING);
        terrainPath.setLastPoint(0, screenHeight);
        //random terrain path
        randomTerrain = new Random();

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
            newY += screenHeight - randomTerrain.nextInt(screenHeight/2);

            terrainPath.cubicTo(interpolateLinear(lastX, newX, 0.333f), lastY,
                    interpolateLinear(lastX, newX,0.666f), newY, newX, newY);

            if (newX > (screenWidth/2) && (!landingPadExists)) {
                //draw landing area
                landingPadWidth = (screenWidth/5);
                landingPadX = newX + randomTerrain.nextInt(screenWidth/MIN_TERRAIN_POINTS);
                landingPadY = screenHeight - randomTerrain.nextInt(screenWidth/MIN_TERRAIN_POINTS);

                terrainPath.cubicTo(interpolateLinear(newX, landingPadX, 0.333f), newY,
                        interpolateLinear(newX, landingPadX, 0.666f), landingPadY, landingPadX, landingPadY);

                terrainPath.lineTo((landingPadX + landingPadWidth), landingPadY);

                newX = landingPadX + landingPadWidth;
                newY = landingPadY;
                createLandingPad();
                landingPadExists = true;
            }
            terrainPath.lineTo(screenWidth, screenHeight);
            terrainPath.close();

            terrainRegion = new Region();
            terrainRegion.setPath(terrainPath, clip);
        }
    }

    //support method for mars terrain
    private int interpolateLinear(int start, int end, float part){
        return (int) (start*(1-part) + end*part);
    }

    //landing pad where the rocket lands safely
    private void createLandingPad() {
        landingPadPath = new Path();
        landingPadPath.moveTo(landingPadX, landingPadY);
        landingPadPath.lineTo(landingPadX + 5, landingPadY - 5);
        landingPadPath.lineTo(landingPadX + landingPadWidth - 5, landingPadY - 5);
        landingPadPath.lineTo(landingPadX + landingPadWidth, landingPadY);
        landingPadPath.close();
        
        landingPadRegion = new Region();
        landingPadRegion.setPath(landingPadPath, clip);
    }

    //create rocketShip
    private void createRocketShip() {
        rocket = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.rocket);
        rocketX = (screenWidth/8);
        rocketY = (screenHeight/4) - (rocket.getHeight()/2);
        mainFlame = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.main_flame);
        leftFlame = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.left_flame);
        rightFlame = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.right_flame);
        rocketPath = new Path();
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
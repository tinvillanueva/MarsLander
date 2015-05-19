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
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ProgressBar;

import java.util.Random;

public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    //variables declaration
    SurfaceHolder holder;
    private Thread gameThread;
    private boolean paused;
    private boolean gameDone;
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
    private BitmapShader terrainShader;
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
    private Bitmap explosion;
    private float rocketX;
    private float rocketY;
    private Path rocketPath;
    private Region rocketRegion;
    public boolean mainThrusterOn;
    public boolean leftThrusterOn;
    public boolean rightThrusterOn;
    private int mainThrusterPower = 3;
    private int minorThrusterPower = 2;

    //variables that affect rocket movement
    private final int GRAVITY = 1;
    private final int TERMINAL_VELOCITY = 50;
    private int speedX = 0;
    private int speedY = 0;
    //fuel variables
    public int fuel;
    private ProgressBar fuelGauge;
    private boolean win;
    private String resultMessage;
    private String score;
    private Paint textPaint;

    //background variables
    private Paint backgroundPaint;
    private Bitmap backgroundTexture;
    private BitmapShader backgroundShader;

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
            synchronized (holder) {
                if (!gameDone) {
                    update();
                }
                doDraw(canvas);

                holder.unlockCanvasAndPost(canvas);
                try {
                    Thread.sleep(50);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //game initialization
    private void setupGame(){
        holder = getHolder();
        holder.addCallback(this);
        running = false;
        setFocusable(true);

        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;

        clip = new Region(0, 0, screenWidth, screenHeight);
        explosion = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.explosion);

        backgroundTexture = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.space);
        backgroundShader = new BitmapShader(backgroundTexture, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        backgroundPaint = new Paint();
        backgroundPaint.setColor(0xFFFFFFFF);
        backgroundPaint.setShader(backgroundShader);

//        paint = new Paint();
//        paint.setColor(Color.BLUE);
//        paint.setStrokeWidth(5);

        createMarsTerrain();
        createRocketShip();

        fuel = 100;
        paused = false;
        gameDone = false;

        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size));
//        textPaint.setColor(Color.WHITE);

        paint = new Paint();
        paint.setColor(Color.RED);
    }

    private void doDraw(Canvas canvas) {
//        canvas.drawColor(Color.BLACK);  //background color
        canvas.drawRect(0, 0, screenWidth, screenHeight, backgroundPaint);
//        canvas.drawPath(terrainPath,terrainPaint);
//        paint.setColor(Color.DKGRAY);
        canvas.drawPath(terrainPath, terrainPaint);

//        paint.setColor(Color.GREEN);
//        canvas.drawPath(terrainPath, paint);
        //landing pad
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath( landingPadPath, paint);

        //rocket & thrusters
        canvas.drawBitmap(rocket, rocketX, rocketY, null);

        if (fuel > 0) {
            if (mainThrusterOn) {
                canvas.drawBitmap(mainFlame, rocketX, rocketY, null);
            }
            if (leftThrusterOn) {
                canvas.drawBitmap(leftFlame, rocketX, rocketY, null);
            }
            if (rightThrusterOn) {
                canvas.drawBitmap(rightFlame, rocketX, rocketY, null);
            }
        }
        
        //message output if rocket has been landed or crashed
        if (gameDone) {
            int messageLength = 0;
            if (win) {

                textPaint.setColor(Color.WHITE);
                canvas.drawText(resultMessage, screenWidth/2, screenHeight/3, textPaint);
                canvas.drawText(score, screenWidth/2, (screenHeight/3)+textPaint.descent()*5f, textPaint);
            }
            else {
                textPaint.setColor(Color.RED);
//                textPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawBitmap(explosion, rocketX, rocketY, null);
                canvas.drawText("GAME OVER", screenWidth/2, (screenHeight/3)-textPaint.descent()*5f, textPaint);
                canvas.drawText(resultMessage, screenWidth/2, screenHeight/3, textPaint);
            }
        }
    }

    private void createMarsTerrain() {
        //terrain texture
        terrainTexture = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.mars);
        terrainShader = new BitmapShader(terrainTexture, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        terrainPaint = new Paint();
        terrainPaint.setColor(0xFFFFFFFF);
        terrainPaint.setStyle(Paint.Style.FILL);
        terrainPaint.setShader(terrainShader);
        //random terrain path
        randomTerrain = new Random();

        terrainPath = new Path();
        terrainPath.setFillType(Path.FillType.WINDING);
        terrainPath.setLastPoint(0, screenHeight);

        int lastX = 0;
        int lastY = screenHeight - randomTerrain.nextInt(screenHeight/2);

        terrainPath.lineTo(lastX, lastY);

        int newX = lastX;
        int	newY = lastY;

        boolean landingPadExists = false;

        while(newX < screenWidth) {
            lastX = newX;
            lastY = newY;
            newX += randomTerrain.nextInt(screenWidth/MIN_TERRAIN_POINTS);
            newY = screenHeight - randomTerrain.nextInt(screenHeight/2);

            terrainPath.cubicTo(interpolateLinear(lastX, newX, 0.333f), lastY,
                    interpolateLinear(lastX, newX, 0.666f), newY, newX, newY);

            //draw some flat land on second half of the screen but only draw it once. then create a separate path for landing pad at the cords
            if(newX > (screenWidth/2) && !landingPadExists) {
                landingPadWidth = (screenWidth/5);
                landingPadX = newX + randomTerrain.nextInt(screenWidth/MIN_TERRAIN_POINTS);
                landingPadY = screenHeight - randomTerrain.nextInt(screenWidth/MIN_TERRAIN_POINTS);

                terrainPath.cubicTo(interpolateLinear(newX, landingPadX, 0.333f), newY,
                        interpolateLinear(newX, landingPadX, 0.666f),
                        landingPadY, landingPadX, landingPadY);
                //terrainPath.lineTo(landingPadX,landingPadY);
                terrainPath.lineTo(landingPadX + landingPadWidth, landingPadY);

                newX = landingPadX + landingPadWidth;
                newY = landingPadY;
                createLandingPad();
                landingPadExists = true;
            }
        }

        terrainPath.lineTo(screenWidth, screenHeight);
        terrainPath.close();

        terrainRegion = new Region();
        terrainRegion.setPath(terrainPath, clip);

    }

    //support method for mars terrain
    private int interpolateLinear(int start, int end, float part){
        return (int) (start*(1-part) + end*part);
    }

    //landing pad where the rocket lands safely
    private void createLandingPad() {
        int landingPadHalfWidth = 5;
        landingPadPath = new Path();
        landingPadPath.moveTo(landingPadX, landingPadY);
        landingPadPath.lineTo(landingPadX + landingPadHalfWidth, landingPadY - landingPadHalfWidth);
        landingPadPath.lineTo(landingPadX + landingPadWidth - landingPadHalfWidth, landingPadY - landingPadHalfWidth);
        landingPadPath.lineTo(landingPadX + landingPadWidth, landingPadY);
        landingPadPath.close();
        
        landingPadRegion = new Region();
        landingPadRegion.setPath(landingPadPath, clip);
    }

    //create rocketShip
    private void createRocketShip() {
        rocket = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.rocket);
        rocketX = (screenWidth/6);
        rocketY = (screenHeight/4) - (rocket.getHeight()/2);
        mainFlame = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.main_flame);
        leftFlame = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.left_flame);
        rightFlame = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.right_flame);
        rocketPath = new Path();
    }

    //rocketPosition
    private void rocketPosition() {
        //check for boundaries. if off side of the screen it will be repositioned
        if (rocketX > screenWidth) {
            rocketX = 0;
        }
        else if (rocketX < 0) {
            rocketX = screenWidth;
        }
//        int positionY = (int) rocketY;
        //vertical position
        if (speedY >= 0) {
            //rocket is falling
            speedY += GRAVITY;
        }
        else {
            //rocket is rising/going up
            speedY = speedY + GRAVITY;
        }


        if (speedY > TERMINAL_VELOCITY) {
            speedY = TERMINAL_VELOCITY;
        }
        rocketY += speedY;

        if (mainThrusterOn && fuel > 0) {
            speedY -= mainThrusterPower;
        }

        //horizontal position
        //gravity is not application here
        if (speedX > 0) {
            speedX = speedX - 1;
        }
        if (speedX < 0) {
            speedX = speedX + 1;
        }
        else {
            //if speedX = 0, no horizontal movement
        }
        rocketX += speedX;

        if (fuel > 0){
            if (leftThrusterOn) {
                speedX += minorThrusterPower;
            }
            if (rightThrusterOn) {
                speedX -= minorThrusterPower;
            }
        }

        //draw rocket path
        rocketPath.reset();
        rocketPath.moveTo((rocketX + (rocket.getWidth()/2)), rocketY);
        rocketPath.lineTo(rocketX, rocketY + rocket.getHeight());
        rocketPath.lineTo(rocketX + rocket.getWidth(), rocketY + rocket.getHeight());
        rocketPath.close();
    }

    //fuel gauge
    public void setFuelGauge(ProgressBar progressBar) {
        //use to update progress bar on game thread
        fuelGauge = progressBar;
    }


    //fuel level
    private void fuelLevel() {
        if (mainThrusterOn || leftThrusterOn || rightThrusterOn) {
            fuel -= 1;
        }
        fuelGauge.setProgress(fuel);
    }

    //collision detection
    private void collisionDetection() {
        rocketRegion = new Region();
        rocketRegion.setPath(rocketPath, clip);
        if (!rocketRegion.quickReject(landingPadRegion) && rocketRegion.op(landingPadRegion, Region.Op.INTERSECT)){
            Log.e("collision", "Landed!");
            gameResult(true);
        }
        if (!rocketRegion.quickReject(terrainRegion) && rocketRegion.op(terrainRegion, Region.Op.INTERSECT)){
            Log.e("collision", "Crashed!");
            gameResult(false);
        }
//        clip = new Region(0, 0, screenWidth, screenHeight);
//        Region region1 = new Region();
//        region1.setPath(testCircle, clip);
//        Region region2 = new Region();
//        region2.setPath(landingPadPath, clip);
//        if (!region1.quickReject(region2) && region1.op(region2, Region.Op.INTERSECT)){
//            Log.e("collision", "Landed");
//        }


    }

    //methods that need updating
    private void update() {
        fuelLevel();
        rocketPosition();
        collisionDetection();
    }

    //start game thread
    public void startGameThread(){
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

    public void reset() {
        win = false;
        gameDone = false;
        createRocketShip();
        createMarsTerrain(); // to create a new random terrain
        speedX = 0;
        speedY = 0;
        fuel = 100;
    }

    //adding game score
    public void gameResult(boolean win) {
        gameDone = true;
        if (win) {
            this.win = true;
            if (fuel > 120) {
                score = "Your score: 5";
            }
            else if (fuel <= 120 && fuel > 90) {
                score = "Your score: 4";
            }
            else if (fuel <=90 && fuel >50) {
                score = "Your score: 3";
            }
            else if (fuel <= 50 && fuel > 30){
                score = "Your score: 2";
            }
            else {
                score = "Your score: 1";
            }

            resultMessage = "Good Job! You are a certified pilot!";
        }
        else {
            resultMessage = "You need more practice flying a rocket! Try again";
        }
    }
}
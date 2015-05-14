package com.tinvillanueva.marslander;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * Created by tinvillanueva on 14/05/15.
 */
public class IntroPage extends Activity{

    MediaPlayer introMusic;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_page);

        introMusic = MediaPlayer.create(this, R.raw.transformers_3);
        introMusic.start();

        Thread introTimer = new Thread() {
            public void run() {
                try {
                    sleep(4000);
                    startActivity(new Intent(context, MainMenu.class));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    finish();
                }

            }
        };
        introTimer.start();

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        Bitmap introImage = BitmapFactory.decodeResource(getResources(), R.drawable.intro_image);
        imageView.setImageBitmap(introImage);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_rotator);
        imageView.startAnimation(animation);
    }

}

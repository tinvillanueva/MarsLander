package com.tinvillanueva.marslander;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainMenu extends Activity implements OnClickListener {

    private Button btnNewGame;
    private Button btnGameHelp;
    private Button btnExit;
    private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_menu);

        btnNewGame = (Button) findViewById(R.id.btnNewGame);
        btnNewGame.setOnClickListener(this);

//        btnGameHelp = (Button) findViewById(R.id.btnHelp);
//        btnGameHelp.setOnClickListener(this);

        btnExit = (Button) findViewById(R.id.btnExit);
        btnExit.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.btnNewGame:
                startActivity(new Intent(this, GameActivity.class));
                break;

            case R.id.btnExit:
                AlertDialog.Builder exitDialog = new AlertDialog.Builder(context);
                exitDialog.setTitle("Exit App");
                exitDialog.setMessage("Do you want to exit 'Mars Lander?' ");
                exitDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                        finish();
                        dialog.dismiss();
                    }
                });

                exitDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                exitDialog.show();

                break;
        }

    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        //getMenuInflater().inflate(R.menu.main, menu);
//        return false;
//    }

}

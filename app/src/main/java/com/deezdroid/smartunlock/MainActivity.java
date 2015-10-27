package com.deezdroid.smartunlock;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nvanbenschoten.motion.ParallaxImageView;
import com.squareup.picasso.Picasso;

public class MainActivity extends Activity {

    NfcAdapter mAdapter;
    PendingIntent mPendingIntent;
    ParallaxImageView background;
    ImageView watch;


    //for twice pressed
    private static long pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set up our Lockscreen
        makeFullScreen();
        startService(new Intent(this, LockScreenService.class));

        setContentView(R.layout.activity_main);
        TextView modelNameView = (TextView) findViewById(R.id.phone_model);
        TextView timeView = (TextView) findViewById(R.id.phone_time);
        TextView batteryView = (TextView) findViewById(R.id.phone_battery);
        TextView info = (TextView) findViewById(R.id.instruction);
        TextView copyright = (TextView) findViewById(R.id.copyright);

        watch = (ImageView) findViewById(R.id.watch);
        background = (ParallaxImageView) findViewById(R.id.background);
        background.registerSensorManager();

        Picasso.with(this).load(R.drawable.background).transform(new BlurTransformation(this, 25)).fit().centerCrop().into(background);


        modelNameView.setText(Build.BRAND + "\n" + Build.MODEL);

        Time today = new Time(Time.getCurrentTimezone());
        today.setToNow();

        timeView.setText(today.format("%k:%M"));

        Typeface light = Typeface.createFromAsset(getApplication().getAssets(), "fonts/MontserratLight.otf");
        Typeface black = Typeface.createFromAsset(getApplication().getAssets(), "fonts/MontserratBlack.otf");
        Typeface hairline = Typeface.createFromAsset(getApplication().getAssets(), "fonts/MontserratHairline.otf");

        modelNameView.setTypeface(light);
        timeView.setTypeface(hairline);
        batteryView.setTypeface(hairline);
        info.setTypeface(hairline);

        Intent intent  = getApplication().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int    level   = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int    scale   = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int    percent = (level*100)/scale;
        batteryView.setText(String.valueOf(percent) + "%");

        //NFC STUFFS////////////////////

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            //nfc not support your device.
            Toast.makeText(this, "NFC Not supported", Toast.LENGTH_SHORT).show();
            return;
        }
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        copyright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pressed + 1000 > System.currentTimeMillis()) finish();
                pressed =  System.currentTimeMillis();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);


        new Handler().post(new Runnable() {
            @Override
            public void run() {
                // Code here will run in UI thread
                final Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.login_logo_hearbeat);
                watch.startAnimation(animation);


            }
        });
    }

    /**
     * A simple method that sets the screen to fullscreen.  It removes the Notifications bar,
     * the Actionbar and the virtual keys (if they are on the phone)
     */
    public void makeFullScreen() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT < 19) { //View.SYSTEM_UI_FLAG_IMMERSIVE is only on API 19+
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else {
            this.getWindow().getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
    }

    @Override
    public void onBackPressed() {
        return; //Do nothing!
    }

    @Override
    protected void onPause() {
        super.onPause();
        background.unregisterSensorManager();
        if (mAdapter != null) {
            mAdapter.disableForegroundDispatch(this);
        }

    }


    @Override
    protected void onNewIntent(Intent intent){
        getTagInfo(intent);
    }
    private void getTagInfo(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        finish();
    }


    public void unlockScreen(View view) {
        finish();
    }




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_HOME)
        {
            Log.i("Home Button","Clicked");
        }
        if(keyCode==KeyEvent.KEYCODE_BACK)
        {
        }
        return false;
    }
//
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//
//        if(!hasFocus) {
//            Log.d("Focus debug", "Lost focus !");
//
//            Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
//            sendBroadcast(closeDialog);
//        }
//
//    }
}

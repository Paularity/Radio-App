package com.example.christian.radioapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Button b_play,btnfb,btntwitter,btnRefresh;
    private ImageView imageView;
    private ViewFlipper viewFlipper;
    private String imagesArr[];

    boolean prepared = false;
    boolean started = false;

    String stream = "http://us2.amfmph.com:8222/live.mp3";

    public static MediaPlayer mediaPlayer;
    private SeekBar volumeSeekbar = null;
    private AudioManager audioManager = null;
    private ProgressBar pbLoading= null;
    private SwipeRefreshLayout swipelayout = null;
    private Animation bounce = null;
    TextView elapseTime;

    private static final int uniqueID = 5139;


    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    try {
        initControls();
        b_play = findViewById(R.id.b_play);
        btnRefresh = findViewById(R.id.refresh);
        btnfb = findViewById(R.id.fbbtn);
        btntwitter = findViewById(R.id.twitterbtn);
        pbLoading = findViewById(R.id.pbLoading);
        viewFlipper = findViewById(R.id.viewFlipper);
        swipelayout = findViewById(R.id.swipelayout);
        imagesArr = new String[]{"https://goo.gl/6YXsmX", "https://goo.gl/aDPxFo", "https://goo.gl/bk8jNB"};

        if(isNetworkAvailable())
        {
            for (int i = 0; i < imagesArr.length; i++) {
                flipperImages(imagesArr[i]);
            }


        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext());
        notification.setAutoCancel(true);



        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipelayout.setRefreshing(true);
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipelayout.setRefreshing(false);
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                    }
                },2000);
            }
        });
        b_play.setEnabled(false);
        pbLoading.setVisibility(View.VISIBLE);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
        mediaPlayer.setLooping(false);
        mediaPlayer.setVolume(100, 100);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        new PlayerTask().execute(stream);

        b_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (started) {
                    started = false;
                  stopService(new Intent(getApplicationContext(),MyServices.class));

                    b_play.setBackgroundResource(R.drawable.rippleplay);
                    //Animate
                    bounce = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.smallbounce);
                    b_play.startAnimation(bounce);
                    // Use bounce interpolator with amplitude 0.2 and frequency 20
                    MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 10);
                    bounce.setInterpolator(interpolator);
                    b_play.startAnimation(bounce);

                    pbLoading.setVisibility(View.INVISIBLE);
                    b_play.setText("");
                } else {
                    started = true;
                    startService(new Intent(getApplicationContext(),MyServices.class));
                    b_play.setBackgroundResource(R.drawable.ripplepause);

                    //Animate
                    bounce = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.smallbounce);
                    b_play.startAnimation(bounce);
                    // Use bounce interpolator with amplitude 0.2 and frequency 20
                    MyBounceInterpolator interpolator = new MyBounceInterpolator(0.1, 10);
                    bounce.setInterpolator(interpolator);
                    b_play.startAnimation(bounce);

                    pbLoading.setVisibility(View.INVISIBLE);
                    b_play.setText("");

                }
            }
        });

        btnfb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/helloasiaradio/"));
                startActivity(intent);
            }
        });

        btntwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/HelloAsiaRadio"));
                startActivity(intent);
            }
        });

            btnRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    stopService(new Intent(getApplicationContext(),MyServices.class));
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);



                }
            });

        }

        else
        {
            viewFlipper.setBackgroundResource(R.drawable.dino);
            b_play.setBackgroundResource(R.drawable.nonetwork);
            Toast.makeText(this,"No internet connection",Toast.LENGTH_SHORT).show();

            btnRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout);



                }
            });
        }

    }
    catch (Exception ex)
    {
        ex.printStackTrace();
    }
    }





    private class PlayerTask extends AsyncTask<String, Void, Boolean>{


        @Override
        protected Boolean doInBackground(String... strings) {

            try {
                mediaPlayer.setDataSource(strings[0]);
                mediaPlayer.prepare();
                prepared = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            super.onPostExecute(aBoolean);
            b_play.setEnabled(true);
            b_play.setBackgroundResource(R.drawable.rippleplay);
            //Animate
            bounce = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.bounce);
            b_play.startAnimation(bounce);
            // Use bounce interpolator with amplitude 0.2 and frequency 20
            MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
            bounce.setInterpolator(interpolator);
            b_play.startAnimation(bounce);
            pbLoading.setVisibility(View.INVISIBLE);
            b_play.setText("");

        }
    }

    @Override
    public void onBackPressed() {
        started = true;
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext());
                notification.setSmallIcon(R.drawable.smallicon);
                notification.setTicker("Hello Asia is now ON AIR!");
                notification.setWhen(System.currentTimeMillis());
                notification.setContentTitle("Hello Asia!");
                notification.setContentText("[Now Playing]");
                notification.setSmallIcon(R.drawable.smallicon);
                notification.setOngoing(false);
                notification.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.ic_launcher));

                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
                notification.setContentIntent(pendingIntent);

                NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(uniqueID,notification.build());


                intent = new Intent(getApplicationContext(),MainActivity.class);
                intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
                startActivity(intent);
                finish();
               System.exit(0);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isNetworkAvailable() == true) {
        if(!started)
        {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext());
                notification.setSmallIcon(R.drawable.smallicon);
                notification.setTicker("Hello Asia is now ON AIR!");
                notification.setWhen(System.currentTimeMillis());
                notification.setContentTitle("Hello Asia!");
                notification.setContentText("[Now Playing]");
                notification.setSmallIcon(R.drawable.smallicon);
                notification.setOngoing(false);
                notification.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.ic_launcher));

                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notification.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(uniqueID, notification.build());
            super.onPause();

            }
            else
            {
                super.onPause();
            }


        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (started)
        {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext());
            notification.setSmallIcon(R.drawable.smallicon);
            notification.setTicker("Hello Asia is now ON AIR!");
            notification.setWhen(System.currentTimeMillis());
            notification.setContentTitle("Hello Asia!");
            notification.setContentText("[Now Playing]");
            notification.setSmallIcon(R.drawable.smallicon);
            notification.setOngoing(false);
            notification.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                    R.mipmap.ic_launcher));

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(uniqueID,notification.build());
            mediaPlayer.start();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(prepared)
        {
            mediaPlayer.release();
        }
    }


    private void initControls()
    {
        try
        {
            volumeSeekbar = (SeekBar)findViewById(R.id.seekbar);
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volumeSeekbar.setMax(audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            volumeSeekbar.setProgress(audioManager
                    .getStreamVolume(AudioManager.STREAM_MUSIC));


            volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onStopTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onStartTrackingTouch(SeekBar arg0)
                {
                }

                @Override
                public void onProgressChanged(SeekBar arg0, int progress, boolean arg2)
                {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                            progress, 0);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void flipperImages(String imgUrl)
    {
        imageView = new ImageView(this);
        Picasso.get().load(imgUrl).memoryPolicy(MemoryPolicy.NO_CACHE).into(imageView);
        viewFlipper.addView(imageView);
        viewFlipper.setFlipInterval(6000);
        viewFlipper.setAutoStart(true);
        viewFlipper.setInAnimation(this, R.anim.slide_right);
        viewFlipper.setOutAnimation(this, R.anim.slide_left);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }






}

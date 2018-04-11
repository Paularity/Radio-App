package com.example.christian.radioapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button b_play,btnfb,btntwitter,btnRefresh;

    boolean prepared = false;
    boolean started = false;

    String stream = "http://us2.amfmph.com:8222/live.mp3";

    MediaPlayer mediaPlayer;
    private SeekBar volumeSeekbar = null;
    private AudioManager audioManager = null;
    private ProgressBar pbLoading= null;
    private SwipeRefreshLayout swipelayout = null;
    private Animation bounce = null;
    TextView elapseTime;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControls();
    try {
        b_play = (Button) findViewById(R.id.b_play);
        btnRefresh = (Button) findViewById(R.id.refresh);
        btnfb = (Button) findViewById(R.id.fbbtn);
        btntwitter = (Button) findViewById(R.id.twitterbtn);
        pbLoading = (ProgressBar) findViewById(R.id.pbLoading);
        swipelayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);

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
                    mediaPlayer.pause();
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
                    mediaPlayer.start();
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
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

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
    protected void onPause() {
        super.onPause();
        if(started)
        {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (started)
        {
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
}

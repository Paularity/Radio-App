package com.example.christian.radioapp;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button b_play,btnfb,btntwitter;

    boolean prepared = false;
    boolean started = false;

    String stream = "http://shaincast.caster.fm:13297/listen.mp3?authn2d9e4cbfaf0b01a3a4ca95e752e9ff43";

    MediaPlayer mediaPlayer;
    private SeekBar volumeSeekbar = null;
    private AudioManager audioManager = null;
    private ProgressBar pbLoading= null;
    TextView elapseTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initControls();
    try {
        b_play = (Button) findViewById(R.id.b_play);
        btnfb = (Button) findViewById(R.id.fbbtn);
        btntwitter = (Button) findViewById(R.id.twitterbtn);
        pbLoading = (ProgressBar) findViewById(R.id.pbLoading);
        b_play.setEnabled(false);
        pbLoading.setVisibility(View.VISIBLE);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.5f, 0.5f);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        new PlayerTask().execute(stream);

        b_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (started) {
                    started = false;
                    mediaPlayer.pause();
                    b_play.setBackgroundResource(R.drawable.rippleplay);
                    pbLoading.setVisibility(View.INVISIBLE);
                    b_play.setText("");
                } else {
                    started = true;
                    mediaPlayer.start();
                    b_play.setBackgroundResource(R.drawable.ripplepause);
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

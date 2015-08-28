package com.beak.gifmaker;

import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.beak.gifmakerlib.GifMaker;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button mMainBtn;
    private TextView mLogTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainBtn = (Button)findViewById(R.id.main_start_btn);
        mLogTv = (TextView)findViewById(R.id.main_log_tv);

        mMainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        final File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Movies" + File.separator + "0.gif");
                        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.kof_13);
                        GifMaker maker = new GifMaker(2);
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.v(TAG, "make gif start...data source res/raw/kof_13.mp4");
                                mLogTv.append("make gif start...data source res/raw/kof_13.mp4" + "\n");
                            }
                        });
                        maker.setOnGifListener(new GifMaker.OnGifListener() {
                            @Override
                            public void onMake(final int current, final int total) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLogTv.append("make gif current=" + current + " total=" + total + "\n");
                                        Log.v(TAG, "make gif current=" + current + " total=" + total);
                                    }
                                });
                            }
                        });
                        final long start = System.currentTimeMillis();
                        final boolean success = maker.makeGifFromVideo(MainActivity.this, uri, 0, 10 * 1000, 200, file.getAbsolutePath());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "make gif success ? " + success, Toast.LENGTH_SHORT).show();
                                final long now = System.currentTimeMillis();
                                mLogTv.append("make gif success ? " + success + " at:" + file.getAbsolutePath() + " cost time:" + ((now - start)/1000) + "s" + "\n");
                                Log.v(TAG, "make gif success ? " + success + " at:" + file.getAbsolutePath() + " cost time:" + ((now - start)/1000) + "s");
                            }
                        });
                    }
                }.start();
            }
        });

    }

}

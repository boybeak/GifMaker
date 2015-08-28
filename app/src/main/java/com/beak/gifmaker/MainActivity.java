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
import android.widget.Toast;

import com.beak.gifmakerlib.GifMaker;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread() {
            @Override
            public void run() {
                super.run();
                File file = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Movies" + File.separator + "0.gif");
                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.kof_13);
                GifMaker maker = new GifMaker(2);
                maker.setOnGifListener(new GifMaker.OnGifListener() {
                    @Override
                    public void onMake(int current, int total) {
                        Log.v(TAG, "make gif current=" + current + " total=" + total);
                    }
                });
                final boolean success = maker.makeGifFromVideo(MainActivity.this, uri, 0, 10 * 1000, 200, file.getAbsolutePath());
                Log.v(TAG, "make gif success ? " + success + " at:" + file.getAbsolutePath());
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "make gif success ? " + success, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }.start();

    }

}

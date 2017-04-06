package com.beak.gifmaker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button mMainBtn;
    private TextView mLogTv;
    private ImageView mShowCaseIv;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mLogTv.append(intent.getStringExtra("log") + "\n");
            boolean success = intent.getBooleanExtra(GifMakeService.EXTRA_SUCCESS, false);
            if (success) {
                mLogTv.append("trying to show this gif");
                String file = intent.getStringExtra(GifMakeService.EXTRA_FILE);
                Glide.with(MainActivity.this).load(new File(file)).asGif().placeholder(android.R.color.holo_green_dark).into(mShowCaseIv);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainBtn = (Button)findViewById(R.id.main_start_btn);
        mLogTv = (TextView)findViewById(R.id.main_log_tv);
        mShowCaseIv = (ImageView)findViewById(R.id.show_case);

        mMainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PackageManager.PERMISSION_GRANTED !=
                        ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                } else {
                    tryMake();
                }
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(GifMakeService.ACTION_MAKE_GIF));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            tryMake();
        }
    }

    private void tryMake () {
        final File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Movies" + File.separator + "0.gif");
        GifMakeService.startMaking(this, "android.resource://" + getPackageName() + "/" + R.raw.kof_13, file.getAbsolutePath(), 0, 10 * 1000, 200);
    }

}

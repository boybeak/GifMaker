package com.beak.gifmaker;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.beak.gifmakerlib.GifMaker;

public class GifMakeService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    public static final String ACTION_MAKE_GIF = "com.beak.gifmaker.action.MAKE_GIF";

    private static final String EXTRA_FROM_FILE = "com.beak.gifmaker.extra.FROM_FILE";
    private static final String EXTRA_TO_FILE = "com.beak.gifmaker.extra.TO_FILE";
    private static final String EXTRA_FROM_POSITION = "com.beak.gifmaker.extra.FROM_POSITION";
    private static final String EXTRA_TO_POSITION = "com.beak.gifmaker.extra.TO_POSITION";
    private static final String EXTRA_PERIOD = "com.beak.gifmaker.extra.PERIOD";

    public static final String EXTRA_FILE = "file", EXTRA_SUCCESS = "success";

    public GifMakeService() {
        super("GifMakeService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startMaking(Context context, String fromFile, String toFile, int fromPosition, int toPosition, int period) {
        Intent intent = new Intent(context, GifMakeService.class);
        intent.setAction(ACTION_MAKE_GIF);
        intent.putExtra(EXTRA_FROM_FILE, fromFile);
        intent.putExtra(EXTRA_TO_FILE, toFile);
        intent.putExtra(EXTRA_FROM_POSITION, fromPosition);
        intent.putExtra(EXTRA_TO_POSITION, toPosition);
        intent.putExtra(EXTRA_PERIOD, period);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MAKE_GIF.equals(action)) {
                final String fromFile = intent.getStringExtra(EXTRA_FROM_FILE);
                final String toFile = intent.getStringExtra(EXTRA_TO_FILE);
                final int fromPosition = intent.getIntExtra(EXTRA_FROM_POSITION, 0);
                final int toPosition = intent.getIntExtra(EXTRA_TO_POSITION, 0);
                final int period = intent.getIntExtra(EXTRA_PERIOD, 200);
                handleTask(fromFile, toFile, fromPosition, toPosition, period);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleTask(String fromFile, String toFile, int fromPosition, int toPosition, int period) {
        GifMaker maker = new GifMaker(2);
        maker.setOnGifListener(new GifMaker.OnGifListener() {
            @Override
            public void onMake(int current, int total) {
                LocalBroadcastManager.getInstance(GifMakeService.this).sendBroadcast(new Intent(ACTION_MAKE_GIF).putExtra("log", "executing " + current + "/" + total));
            }
        });
        long startAt = System.currentTimeMillis();
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_MAKE_GIF).putExtra("log", "start making at " + startAt));
        final boolean success = maker.makeGifFromVideo(this, Uri.parse(fromFile), fromPosition, toPosition, period, toFile);
        long now = System.currentTimeMillis();
        LocalBroadcastManager.getInstance(GifMakeService.this).sendBroadcast(new Intent(ACTION_MAKE_GIF)
                .putExtra("log", "Done! " + (success ? " success " : " failed ") + " cost time=" + ((now - startAt)/1000) + " seconds "
                + " \nsave at=" + toFile)
                .putExtra(EXTRA_FILE, toFile)
                .putExtra(EXTRA_SUCCESS, true));
    }
}

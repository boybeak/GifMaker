package com.beak.gifmakerlib;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.DrawableRes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beak on 2015/8/27.
 */
public class GifMaker {

    public boolean makeGif (List<Bitmap> source, String outputPath) throws IOException {
        AnimatedGifEncoder encoder = new AnimatedGifEncoder();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        encoder.start(bos);
        encoder.setRepeat(0);
        final int length = source.size();
        for (int i = 0; i < length; i++) {
            Bitmap bmp = source.get(i);
            if (bmp == null) {
                continue;
            }
            //Bitmap thumb = ThumbnailUtils.extractThumbnail(bmp, bmp.getWidth() / 8, bmp.getHeight() / 8);
            try {
                encoder.addFrame(bmp);
            } catch (Exception e) {
                e.printStackTrace();
                System.gc();
                break;
            }

            bmp.recycle();
            //thumb.recycle();
            //TODO how about releasing bitmap after addFrame
        }
        encoder.finish();
        source.clear();
        byte[] data = bos.toByteArray();
        File file = new File(outputPath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(data);
        fileOutputStream.flush();
        fileOutputStream.close();
        return file.exists();
    }

    public boolean makeGifFromPath (List<String> sourcePathList, String outputPath) throws IOException {
        List<Bitmap> bitmaps = new ArrayList<Bitmap>();
        final int length = sourcePathList.size();
        for (int i = 0; i < length; i++) {
            bitmaps.add(BitmapFactory.decodeFile(sourcePathList.get(i)));
        }
        return makeGif(bitmaps, outputPath);
    }

    public boolean makeGifFromFile (List<File> sourceFileList, String outputPath) throws IOException {
        List<String> pathArray = new ArrayList<String>();
        final int length = sourceFileList.size();
        for (int i = 0; i < length; i++) {
            pathArray.add(sourceFileList.get(i).getAbsolutePath());
        }
        return makeGifFromPath(pathArray, outputPath);
    }

    public boolean makeGif (Resources resources, @DrawableRes int[] sourceDrawableId, String outputPath) throws IOException {
        List<Bitmap> bitmaps = new ArrayList<Bitmap>();
        for (int i = 0; i < sourceDrawableId.length; i++) {
            bitmaps.add(BitmapFactory.decodeResource(resources, sourceDrawableId[i]));
        }
        return makeGif(bitmaps, outputPath);
    }

    public boolean makeGifFromVideo (String videoPath, long startMillSeconds, long endMillSeconds, long periodMillSeconds, String outputPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        return makeGifWithMediaMetadataRetriever(retriever, startMillSeconds, endMillSeconds, periodMillSeconds, outputPath);
    }

    public boolean makeGifFromVideo (Context context, Uri uri, long startMillSeconds, long endMillSeconds, long periodMillSeconds, String outputPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, uri);
        return makeGifWithMediaMetadataRetriever(retriever, startMillSeconds, endMillSeconds, periodMillSeconds, outputPath);
    }

    private boolean makeGifWithMediaMetadataRetriever (MediaMetadataRetriever retriever, long startMillSeconds, long endMillSeconds, long periodMillSeconds, String outputPath) {
        if (startMillSeconds <= 0 || endMillSeconds <= 0 || periodMillSeconds <= 0 || endMillSeconds <= startMillSeconds) {
            throw new IllegalArgumentException("startMillSecodes, endMillSeconds or periodMillSeconds may <= 0, or endMillSeconds <= startMillSeconds");
        }
        try {
            List<Bitmap> bitmaps = new ArrayList<Bitmap>();
            String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            long duration = Long.parseLong(durationStr);
            long minDuration = Math.min(duration, endMillSeconds);
            for (long time = startMillSeconds; time < minDuration; time += periodMillSeconds) {
                bitmaps.add(retriever.getFrameAtTime(time * 1000, MediaMetadataRetriever.OPTION_CLOSEST));
            }
            retriever.release();
            return makeGif(bitmaps, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public class VideoOption {
        public long startMillSeconds;
        public long endMillSeconds;
        long periodMillSeconds;
        public String path;


        public boolean canMakeGif () {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);

            int width = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            int height = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            long frameByteLength = width * height * 4;
            int frameCount = (int)((endMillSeconds - startMillSeconds) / periodMillSeconds);
            long totalByteLength = frameByteLength * frameCount;

            long memoryAvailable = Runtime.getRuntime().maxMemory();
            return memoryAvailable > totalByteLength;
        }
    }

}

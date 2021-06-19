package me.dipantan.imago.Controller;

import android.content.Context;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import me.dipantan.imago.R;

public class ImageDownloader {
//    static void downloader(Context context){
//        AndroidNetworking.initialize(context);
//        AndroidNetworking.download(context.getResources().getString(R.string.url),dirPath,fileName)
//                .setTag("downloadTest")
//                .setPriority(Priority.MEDIUM)
//                .build()
//                .setDownloadProgressListener(new DownloadProgressListener() {
//                    @Override
//                    public void onProgress(long bytesDownloaded, long totalBytes) {
//                        // do anything with progress
//                    }
//                })
//                .startDownload(new DownloadListener() {
//                    @Override
//                    public void onDownloadComplete() {
//                        // do anything after completion
//                    }
//                    @Override
//                    public void onError(ANError error) {
//                        // handle error
//                    }
//                });
//    }
}

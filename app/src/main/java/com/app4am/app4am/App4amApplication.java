package com.app4am.app4am;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by dino on 2014/10/20.
 */
public class App4amApplication extends Application {


    private ImageLoaderConfiguration mImageLoaderConfiguration;

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();
        mImageLoaderConfiguration = new ImageLoaderConfiguration.Builder(this)
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(mImageLoaderConfiguration);
    }
}

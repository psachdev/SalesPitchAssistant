package com.oracle.play.pptviewer;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

public class CacheManager {
	//Singleton class
	private static CacheManager CacheManagerInstance;
	private static LruCache<String, Bitmap> mMemoryCache; //for cache
	
	private static String logtag = "CacheManager";//for use as the tag when logging
	private static String urlPrefix = "http://slcai764.us.oracle.com:8081/PPTRestService";
	
	//Init the instance
	public static void initInstance(){
		if (CacheManagerInstance == null)
		{
		  // Create the instance
			CacheManagerInstance = new CacheManager();
			configureCache();
		}
	}
	
	//Reinstantitate
	public static void reInitInstance(){
		  // Create the instance
			CacheManagerInstance = new CacheManager();
			configureCache();
		
	}
	//Get the instance
	public static CacheManager getInstance()
	{
	 // Return the instance
	 return CacheManagerInstance;
	 }
	
	private static void configureCache() {
		Log.d(logtag,"************Configuring cache*************");
		//For cache
		// Get max available VM memory, exceeding this amount will throw an
		// OutOfMemory exception. Stored in kilobytes as LruCache takes an
		// int in its constructor.
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

		// Use 1/16th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 8;

		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				// The cache size will be measured in kilobytes rather than
				// number of items.
				return bitmap.getByteCount() / 1024;
			}
		};

	}
	
	//This class is the cache manager that uses android's LRU Cache.
	public static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
        	Log.d(logtag, "Adding to cache :" + key);
            mMemoryCache.put(key, bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
    
    public static Bitmap getImage(String postfix){
    	String path = urlPrefix + postfix; 
    	try {
      	  Bitmap bitmap = getBitmapFromMemCache(path);
          if (bitmap != null) {
        	  //Exists in cache
          	Log.d(logtag, "Retrieving from cache :" + path);          
              
          } else {
        	  //Does not exist in cache. Retrieve it using REST call
          	bitmap = new ImageRetriever().execute(path).get();
          	if(bitmap!=null)
          		addBitmapToMemoryCache(path, bitmap);        	
          }		
          return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
		}     
    	return null;
    }
}

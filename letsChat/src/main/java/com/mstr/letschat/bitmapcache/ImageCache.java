package com.mstr.letschat.bitmapcache;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.StatFs;
import android.util.Log;
import android.util.LruCache;

import com.mstr.letschat.R;
import com.mstr.letschat.utils.FileUtils;
import com.mstr.letschat.utils.Utils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ImageCache {
	private static final String LOG_TAG = "ImageCache";
	
	public static final String AVATAR_DIR = "avatar";
	
	private static final int DEFAULT_MEM_CACHE_SIZE = 1024 * 5; // 5MB

    // Default disk cache size in bytes
    private static final int DEFAULT_DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    
    private static final CompressFormat DEFAULT_COMPRESS_FORMAT = CompressFormat.PNG;
    private static final int DEFAULT_COMPRESS_QUALITY = 100;
    private static final int DISK_CACHE_INDEX = 0;
    
    private LruCache<String, BitmapDrawable> memoryCache;
    private DiskLruCache diskLruCache;
    private ImageCacheParams cacheParams;
    
    private final Object diskCacheLock = new Object();
    private boolean diskCacheStarting = true;
    
    private Set<SoftReference<Bitmap>> reusableBitmaps;
    
    private ImageCache(ImageCacheParams cacheParams) {
    	init(cacheParams);
    }
    
    public static ImageCache getInstance(FragmentManager fragmentManager, ImageCacheParams cacheParams) {
    	// Search for, or create an instance of the non-UI RetainFragment
        final RetainFragment retainFragment = RetainFragment.findOrCreateRetainFragment(fragmentManager);

        // See if we already have an ImageCache stored in RetainFragment
        ImageCache imageCache = (ImageCache)retainFragment.getImageCache();

        // No existing ImageCache, create one and store it in RetainFragment
        if (imageCache == null) {
            imageCache = new ImageCache(cacheParams);
            retainFragment.setImageCache(imageCache);
        }

        return imageCache;
    }
    
    private void init(ImageCacheParams cacheParams) {
    	this.cacheParams = cacheParams;

		reusableBitmaps = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
    	
    	memoryCache = new LruCache<String, BitmapDrawable>(cacheParams.memCacheSize) {
			@Override
			protected int sizeOf(String key, BitmapDrawable value) {
			    final int bitmapSize = getBitmapSize(value) / 1024;
			    return bitmapSize == 0 ? 1 : bitmapSize;
			}
			
			@Override
			protected void entryRemoved(boolean evicted, String key, BitmapDrawable oldValue, BitmapDrawable newValue) {
				if (Utils.hasHoneycomb()) {
					reusableBitmaps.add(new SoftReference<Bitmap>(oldValue.getBitmap()));
				}
			}
		};
    }
    
    public void initDiskCache() {
    	synchronized(diskCacheLock) {
    		if (diskLruCache == null || diskLruCache.isClosed()) {
    			File diskCacheDir = cacheParams.diskCacheDir;
    			if (diskCacheDir != null) {
    				if (!diskCacheDir.exists()) {
    					diskCacheDir.mkdirs();
    				}
    				
    				if (getUsableSpace(diskCacheDir) > cacheParams.diskCacheSize) {
    					try {
                            diskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, cacheParams.diskCacheSize);
                        } catch (final IOException e) {
                        	cacheParams.diskCacheDir = null;
                        	Log.e(LOG_TAG, "initDiskCache - " + e);
                        }
    				}
    			}
    		}
    		
    		diskCacheStarting = false;
    		diskCacheLock.notifyAll();
    	}
    }
    
    public void flush() {
		synchronized (diskCacheLock) {
			if (diskLruCache != null) {
				try {
					diskLruCache.flush();
		        } catch (IOException e) {
		        	Log.e(LOG_TAG, "flush - " + e);
		        }
			}
		}
    }

    /**
     * Closes the disk cache associated with this ImageCache object. Note that this includes
     * disk access so this should not be executed on the main/UI thread.
     */
    public void close() {
    	synchronized (diskCacheLock) {
    		if (diskLruCache != null) {
    			try {
    				if (!diskLruCache.isClosed()) {
    					diskLruCache.close();
    				}
    			} catch (IOException e) {
    				Log.e(LOG_TAG, "close - " + e);
    			}
    		}
    	}
    }
    
    public void addBitmapToCache(String data, BitmapDrawable value) {
    	if (data == null || value == null) {
    		return;
    	}
    	
    	if (memoryCache != null) {
    		memoryCache.put(data, value);
    	}
    	
    	synchronized(diskCacheLock) {
    		if (diskLruCache != null) {
    			final String key = hashKeyForDisk(data);
    			OutputStream out = null;
    			
    			try {
					DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
					if (snapshot == null) {
						DiskLruCache.Editor editor = diskLruCache.edit(key);
						if (editor != null) {
							out = editor.newOutputStream(DISK_CACHE_INDEX);
							value.getBitmap().compress(cacheParams.compressFormat, cacheParams.compressQuality, out);
							editor.commit();
							out.close();
						}
					} else {
						snapshot.getInputStream(DISK_CACHE_INDEX).close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (out != null) {
							out.close();
						}
					} catch(IOException e) {}
				}
    		}
    	}
    }
    
    
    /**
	 * Get from memory cache.
	 *
	 * @param data Unique identifier for which item to get
	 * @return The bitmap drawable if found in cache, null otherwise
	 */
    public BitmapDrawable getBitmapFromMemCache(String data) {
    	//BEGIN_INCLUDE(get_bitmap_from_mem_cache)
    	BitmapDrawable memValue = null;
	    
	    if (memoryCache != null) {
	    	memValue = memoryCache.get(data);
	    }
	
	    if (memValue != null) {
	    	Log.d(LOG_TAG, "Memory cache hit");
	    }
	    
	    return memValue;
    }
    
    public Bitmap getBitmapFromDiskCache(String data) {
    	final String key = hashKeyForDisk(data);
        Bitmap bitmap = null;
    	
    	synchronized (diskCacheLock) {
    		while (diskCacheStarting) {
    			try {
    				diskCacheLock.wait();
    			} catch(InterruptedException e) {}
    		}
    		
    		if (diskLruCache != null) {
    			InputStream inputStream = null;
    			try {
		    		DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
		    		if (snapshot != null) {
		    			Log.d(LOG_TAG, "Disk cache hit");
		    			
		    			inputStream = snapshot.getInputStream(DISK_CACHE_INDEX);
		    			if (inputStream != null) {
		    				FileDescriptor fd = ((FileInputStream) inputStream).getFD();

                            // Decode bitmap, but we don't want to sample so give
                            // MAX_VALUE as the target dimensions
                            bitmap = BitmapUtils.decodeSampledBitmapFromDescriptor(
                                    fd, Integer.MAX_VALUE, Integer.MAX_VALUE, this);
		    			}
		    		}
    			} catch(IOException e) {
    				Log.e(LOG_TAG, "getBitmapFromDiskCache - " + e);
    			} finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {}
                }
    		}
    	}
    	
    	return bitmap;
    }
    
    protected Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
    	//BEGIN_INCLUDE(get_bitmap_from_reusable_set)
    	Bitmap bitmap = null;
		
		if (reusableBitmaps != null && !reusableBitmaps.isEmpty()) {
		    synchronized (reusableBitmaps) {
		        final Iterator<SoftReference<Bitmap>> iterator = reusableBitmaps.iterator();
		        Bitmap item;
		
		        while (iterator.hasNext()) {
		            item = iterator.next().get();
		            
		            if (null != item && item.isMutable()) {
		                // Check to see it the item can be used for inBitmap
		                if (canUseForInBitmap(item, options)) {
		                    bitmap = item;
		
		                    // Remove from reusable set so it can't be used again
		                    iterator.remove();
		                    break;
		                }
		            } else {
		                // Remove from the set if the reference has been cleared.
		                iterator.remove();
		            }
		        }
		    }
		}
		
		return bitmap;
    }
    
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static boolean canUseForInBitmap(Bitmap candidate, BitmapFactory.Options targetOptions) {
		//BEGIN_INCLUDE(can_use_for_inbitmap)
    	if (!Utils.hasKitKat()) {
			// On earlier versions, the dimensions must match exactly and the inSampleSize must be 1
			return candidate.getWidth() == targetOptions.outWidth
					&& candidate.getHeight() == targetOptions.outHeight
					&& targetOptions.inSampleSize == 1;
		}
		
		// From Android 4.4 (KitKat) onward we can re-use if the byte size of the new bitmap
		// is smaller than the reusable bitmap candidate allocation byte count.
		int width = targetOptions.outWidth / targetOptions.inSampleSize;
		int height = targetOptions.outHeight / targetOptions.inSampleSize;
		int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
		return byteCount <= candidate.getAllocationByteCount();
        //END_INCLUDE(can_use_for_inbitmap)
	}
    
    /**
     * Return the byte usage per pixel of a bitmap based on its configuration.
     * @param config The bitmap configuration.
     * @return The byte usage per pixel.
     */
    private static int getBytesPerPixel(Config config) {
        if (config == Config.ARGB_8888) {
            return 4;
        } else if (config == Config.RGB_565) {
            return 2;
        } else if (config == Config.ARGB_4444) {
            return 2;
        } else if (config == Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }
    
    /**
	 * A hashing method that changes a string (like a URL) into a hash suitable for using as a
	 * disk filename.
	 */
	public static String hashKeyForDisk(String key) {
	    String cacheKey;
	    try {
	        final MessageDigest mDigest = MessageDigest.getInstance("MD5");
	        mDigest.update(key.getBytes());
	        cacheKey = bytesToHexString(mDigest.digest());
	    } catch (NoSuchAlgorithmException e) {
	        cacheKey = String.valueOf(key.hashCode());
	    }
	    return cacheKey;
	}
	
	private static String bytesToHexString(byte[] bytes) {
	    // http://stackoverflow.com/questions/332079
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < bytes.length; i++) {
	        String hex = Integer.toHexString(0xFF & bytes[i]);
	        if (hex.length() == 1) {
	            sb.append('0');
	        }
	        sb.append(hex);
	    }
	    return sb.toString();
	}
	
	public static void addAvatarToFile(Context context, String user, Bitmap avatar) {
		// cache avatar
		if (avatar != null) {
			File dir = FileUtils.getDiskCacheDir(context, AVATAR_DIR);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			
			File file = new File(dir, hashKeyForDisk(user));
			FileOutputStream out = null;
			try {
			    out = new FileOutputStream(file);
			    avatar.compress(Bitmap.CompressFormat.PNG, 100, out);
			} catch (IOException e) {
			    e.printStackTrace();
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {}
			}
		}
	}
	
	public static Bitmap getAvatarFromFile(Context context, String user) {
		File file = new File(FileUtils.getDiskCacheDir(context, AVATAR_DIR), hashKeyForDisk(user));
		if (!file.exists()) {
			return null;
		}
		
		FileDescriptor fd = null;
		try {
			FileInputStream inputStream = new FileInputStream(file);
			fd = ((FileInputStream)inputStream).getFD();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (fd != null) {
			int size = context.getResources().getDimensionPixelSize(R.dimen.default_avatar_size);
			return BitmapUtils.decodeSampledBitmapFromDescriptor(fd, size, size, null);
		} else {
			return null;
		}
	}
    
    public static class ImageCacheParams {
		public int memCacheSize = DEFAULT_MEM_CACHE_SIZE;
		public int diskCacheSize = DEFAULT_DISK_CACHE_SIZE;
		public File diskCacheDir;
		public CompressFormat compressFormat = DEFAULT_COMPRESS_FORMAT;
		public int compressQuality = DEFAULT_COMPRESS_QUALITY;

    	public ImageCacheParams(Context context, String diskCacheDirectoryName) {
    		diskCacheDir = FileUtils.getDiskCacheDir(context, diskCacheDirectoryName);
    	}
    }
    
	@TargetApi(VERSION_CODES.GINGERBREAD)
	public static long getUsableSpace(File path) {
		if (Utils.hasGingerbread()) {
			return path.getUsableSpace();
		}
		
		final StatFs stats = new StatFs(path.getPath());
		return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
	}
    
	/**
	 * Get the size in bytes of a bitmap in a BitmapDrawable. Note that from Android 4.4 (KitKat)
	 * onward this returns the allocated memory size of the bitmap which can be larger than the
	 * actual bitmap data byte count (in the case it was re-used).
	 *
	 * @param value
	 * @return size in bytes
	 */
    @TargetApi(VERSION_CODES.KITKAT)
    public static int getBitmapSize(BitmapDrawable value) {
    	Bitmap bitmap = value.getBitmap();
	
	    // From KitKat onward use getAllocationByteCount() as allocated bytes can potentially be
	    // larger than bitmap byte count.
	    if (Utils.hasKitKat()) {
	        return bitmap.getAllocationByteCount();
	    }
	
	    if (Utils.hasHoneycombMR1()) {
	        return bitmap.getByteCount();
	    }
	    
	    // Pre HC-MR1
	    return bitmap.getRowBytes() * bitmap.getHeight();
	}
    
    public static class RetainFragment extends Fragment {
    	public static final String TAG = "RetainFragment";
    	private ImageCache imageCache;
    	
    	private static RetainFragment findOrCreateRetainFragment(FragmentManager fragmentManager) {
    		RetainFragment fragment = (RetainFragment)fragmentManager.findFragmentByTag(TAG);
    		if (fragment == null) {
    			fragment = new RetainFragment();
    			fragmentManager.beginTransaction().add(fragment, TAG).commit();
    		}
    		
    		return fragment;
        }
    	
    	public void onCreate(Bundle savedInstanceState) {
    		super.onCreate(savedInstanceState);
    		
    		setRetainInstance(true);
    	}

		public ImageCache getImageCache() {
			return imageCache;
		}

		public void setImageCache(ImageCache imageCache) {
			this.imageCache = imageCache;
		}
    }
}
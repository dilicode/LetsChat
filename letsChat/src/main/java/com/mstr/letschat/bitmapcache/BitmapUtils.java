package com.mstr.letschat.bitmapcache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.mstr.letschat.utils.Utils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class BitmapUtils {
	private static final String TAG = "ImageCache";
	
	public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth, int reqHeight, ImageCache cache) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
		
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		
		Bitmap inBitmap = null;
		if (Utils.hasHoneycomb()) {
			inBitmap = addInBitmapOptions(options, cache);
		}
		
		Bitmap result = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
		if (inBitmap != null && result == inBitmap) {
			Log.d(TAG, "reuse bitmap");
		}
		
		return result;
	}
	
	public static Bitmap decodeSampledBitmapFromByteArray(byte[] data, int reqWidth, int reqHeight, ImageCache cache) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		
		Bitmap inBitmap = null;
		if (Utils.hasHoneycomb()) {
			inBitmap = addInBitmapOptions(options, cache);
		}
		
		Bitmap result =  BitmapFactory.decodeByteArray(data, 0, data.length, options);
		if (inBitmap != null && result == inBitmap) {
			Log.d(TAG, "reuse bitmap");
		}
		
		return result;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// BEGIN_INCLUDE (calculate_sample_size)
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
	
	private static Bitmap addInBitmapOptions(BitmapFactory.Options options, ImageCache cache) {
        //BEGIN_INCLUDE(add_bitmap_options)
        // inBitmap only works with mutable bitmaps so force the decoder to
        // return mutable bitmaps.
        options.inMutable = true;

        if (cache != null) {
            // Try and find a bitmap to use for inBitmap
            Bitmap inBitmap = cache.getBitmapFromReusableSet(options);

            if (inBitmap != null) {
                options.inBitmap = inBitmap;
                return inBitmap;
            }
        }
        
        return null;
    }

	public static Bitmap decodeSampledBitmapFromStream(Context context, Uri uri, int reqWidth, int reqHeight) {
		InputStream inputStream;
		try {
			inputStream = context.getContentResolver().openInputStream(uri);
		} catch(FileNotFoundException e) {
			return null;
		}
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(inputStream, null, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		try {
			inputStream = context.getContentResolver().openInputStream(uri);
		} catch(FileNotFoundException e) {
			return null;
		}
		return BitmapFactory.decodeStream(inputStream, null, options);
	}
}
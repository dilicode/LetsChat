package com.mstr.letschat.bitmapcache;

import java.lang.ref.WeakReference;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.mstr.letschat.bitmapcache.AvatarImageView.BitmapWorkerTask;

public class CacheableBitmapDrawable extends BitmapDrawable {
	private WeakReference<BitmapWorkerTask> taskReference;
	
	public CacheableBitmapDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask task) {
		super(res, bitmap);
		
		taskReference = new WeakReference<BitmapWorkerTask>(task);
	}
	
	public BitmapWorkerTask getBitmapWorkerTask() {
		return taskReference.get();
	}
}
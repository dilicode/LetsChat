package com.mstr.letschat.bitmapcache;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.mstr.letschat.R;
import com.mstr.letschat.SmackInvocationException;
import com.mstr.letschat.model.UserProfile;
import com.mstr.letschat.utils.AppLog;
import com.mstr.letschat.xmpp.SmackHelper;

public class AvatarImageView extends ImageView {
	
	public AvatarImageView(Context context) {
		super(context);
	}
	
	public AvatarImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public AvatarImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public boolean loadImage(String jid) {
		if (cancelPotentialWork(jid)) {
			BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(this);
			CacheableBitmapDrawable drawable = new CacheableBitmapDrawable(getResources(), 
					BitmapFactory.decodeResource(getResources(), R.drawable.ic_default_avatar), bitmapWorkerTask);
			setImageDrawable(drawable);
			bitmapWorkerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, jid);
		}
		
		return true;
	}
	
	private boolean cancelPotentialWork(String jid) {
		BitmapWorkerTask task = getBitmapWorkerTask();
		if (task != null) {
			String taskJid = task.jid;
			if (taskJid == null || !taskJid.equals(jid)) {
				task.cancel(true);
			} else {
				return false;
			}
		}
		
		return true;
	}
	
	private BitmapWorkerTask getBitmapWorkerTask() {
		Drawable drawable = getDrawable();
		if (drawable instanceof CacheableBitmapDrawable) {
			return ((CacheableBitmapDrawable)drawable).getBitmapWorkerTask();
		}
		
		return null;
	}
	
	public static class BitmapWorkerTask extends AsyncTask<String, Void, CacheableBitmapDrawable> {
		private WeakReference<AvatarImageView> imageViewReference;
		String jid;
		
		public BitmapWorkerTask(AvatarImageView imageView) {
			imageViewReference = new WeakReference<AvatarImageView>(imageView);
		}
		
		@Override
		protected CacheableBitmapDrawable doInBackground(String... params) {
			AvatarImageView imageView = imageViewReference.get();
			if (imageView == null || isCancelled()) {
				return null;
			}
			
			jid = params[0];
			UserProfile user = null;
			try {
				user = SmackHelper.getInstance(imageView.getContext()).search(jid);
			} catch (SmackInvocationException e) {
				AppLog.e(String.format("get user avatar error %s", jid), e);
			}
			
			if (user != null) {
				byte[] avatar = user.getAvatar();
				if (avatar != null) {
					return new CacheableBitmapDrawable(imageView.getResources(), 
							BitmapFactory.decodeByteArray(avatar, 0, avatar.length), this);
				}
			}
		
			return null;
		}
		
		@Override
		protected void onPostExecute(CacheableBitmapDrawable bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}
			
			AvatarImageView imageView = imageViewReference.get();
			if (imageView != null && bitmap != null) {
				BitmapWorkerTask bitmapWorkerTask = imageView.getBitmapWorkerTask();
				if (bitmapWorkerTask == this) {
					imageView.setImageDrawable(bitmap);
				}
			}
		}
	}
}
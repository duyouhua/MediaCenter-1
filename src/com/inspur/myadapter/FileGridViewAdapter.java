package com.inspur.myadapter;

import java.io.File;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.files.FileItem;
import com.inspur.files.FileItemForOperation;
import com.inspur.files.FileItemSet;
import com.inspur.files.FileManager.ViewMode;
import com.inspur.mediacenter.MApplication;
import com.inspur.mediacenter.R;
import com.inspur.mediacenter.filebrowser.FileBrowser;
import com.inspur.mediacenter.filebrowser.ImageFileBrowser;

import com.inspur.utils.Helper;

public class FileGridViewAdapter extends BaseAdapter{
    private LayoutInflater mInflater;
    private FileItemSet mdata;
    private Context mContext;
    ViewHolder holder = null;
    private ViewMode mViewMode;
    private static Bitmap defaultImaPic;
    private static Bitmap defaultVidPic;
    public void setViewMode(ViewMode mode){
    	mViewMode = mode;
    }
    
    public FileGridViewAdapter(Context context, FileItemSet data){
        super();
        this.mContext = context;
        this.mdata = data;
        mInflater = LayoutInflater.from(this.mContext);
        defaultImaPic=BitmapFactory.decodeResource(this.mContext.getResources(), R.drawable.picture); 
        defaultImaPic =ThumbnailUtils.extractThumbnail(defaultImaPic, 100,100,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        defaultVidPic=BitmapFactory.decodeResource(this.mContext.getResources(), R.drawable.videofile); 
        defaultVidPic =ThumbnailUtils.extractThumbnail(defaultVidPic, 100,100,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
    }

    @Override
    public int getCount() {
        return mdata.getFileItems().size();
    }

    @Override
    public Object getItem(int position) {
        return mdata.getFileItems().get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
          return getGridViewItem(position, convertView);
    }
   
    private View getGridViewItem(int position, View convertView){
        if (convertView == null) { 
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.grid_view_item, null);
            holder.img_head = (ImageView)convertView.findViewById(R.id.ivOfGVItem);
            holder.title = (TextView)convertView.findViewById(R.id.tvOfGVItem);
            convertView.setTag(holder);   
        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        FileItemForOperation file = mdata.getFileItems().get(position);
        FileItem fileItem = file.getFileItem();
        String url = fileItem.getFilePath();
        int iconId = fileItem.getIconId();
        if (fileItem.getIcon() != null) {
        	holder.img_head.setImageBitmap(fileItem.getIcon());
		}
        if(iconId > 0){
       	holder.img_head.setImageResource(iconId);
        }
		switch (iconId) {
		case R.drawable.app_default_icon:
			new AsyncLoadApkicon().execute(fileItem);
			break;
		case R.drawable.picture:
			//new AsyncLoadImage().execute(fileItem);
			if (cancelPotentialLoadImage(url, holder.img_head)) {
				AsyncLoadImage task = new AsyncLoadImage(holder.img_head);
				 task.execute(position);
		         LoadedImageDrawable loadedDrawable = new LoadedImageDrawable(task);
		         holder.img_head.setImageDrawable(loadedDrawable);	        
		     }
			break;
		case R.drawable.videofile:
			//new AsyncLoadVideoImage().execute(fileItem);
			String extraName=fileItem.getExtraName();
	        if(canCatchImage(extraName)){
				if (cancelPotentialLoadVideo(url, holder.img_head)) {
					AsyncLoadVideoImage task = new AsyncLoadVideoImage(holder.img_head);
			         LoadedVideoDrawable loadedDrawable = new LoadedVideoDrawable(task);
			         holder.img_head.setImageDrawable(loadedDrawable);
			         task.execute(position);
			     }
	        }
	        else
	        	holder.img_head.setImageBitmap(defaultVidPic);
			break;
		default:
			break;
		}
        String displayName = fileItem.getFileName();
        holder.title.setText(displayName);     
        switch (file.getSelectState()) {
            case FileItemForOperation.SELECT_STATE_CUT:
                holder.title.setTextAppearance(mContext, R.style.tvInGridViewCut);
                break;
            case FileItemForOperation.SELECT_STATE_NOR:
                holder.title.setTextAppearance(mContext, R.style.tvInGridView);
                break;  
            case FileItemForOperation.SELECT_STATE_SEL:
                holder.title.setTextAppearance(mContext, R.style.tvInGridViewSelected);
                break;
            default:
                break;
        }
        return convertView;
    }
    
    
    private final class ViewHolder{
        public ImageView img_head;
        public TextView title;
    }
    private boolean canCatchImage(String extraName){
		if(extraName.equalsIgnoreCase("3gp")||extraName.equalsIgnoreCase("avi")
    			||extraName.equalsIgnoreCase("mp4")||extraName.equalsIgnoreCase("mkv")
    			||extraName.equalsIgnoreCase("mov")||extraName.equalsIgnoreCase("wmv"))
			return true;
		else
			return false;
	}
  //加载图片
    class AsyncLoadImage extends AsyncTask<Integer, Void, Bitmap> {
    	private String url = null;
		private final WeakReference<ImageView> imageViewReference;
		public AsyncLoadImage(ImageView imageview) {
			super();
			// TODO Auto-generated constructor stub
			imageViewReference = new WeakReference<ImageView>(imageview);
		}
		@Override
		protected Bitmap doInBackground(Integer... params) {
			// TODO Auto-generated method stub
			Bitmap bitmap = null;
			this.url = mdata.getFileItems().get(params[0]).getFileItem().getFilePath();			
			bitmap = getImageBitmapFromUrl(url);
			ImageFileBrowser.gridviewBitmapCaches.put(mdata.getFileItems().get(params[0]).getFileItem().getFilePath(), bitmap);			
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap resultBitmap) {
			// TODO Auto-generated method stub
			if(isCancelled()){
				resultBitmap = null;
			}
			if(imageViewReference != null){
				ImageView imageview = imageViewReference.get();
				AsyncLoadImage loadImageTask = getAsyncLoadImageTask(imageview);
			    // Change bitmap only if this process is still associated with it
			    if (this==loadImageTask) {
			    	imageview.setImageBitmap(resultBitmap);
			    	//imageview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			    }
			}
			super.onPostExecute(resultBitmap);
		}	
                

        @Override
        public void onProgressUpdate(Void... value) {
        	FileGridViewAdapter.this.notifyDataSetChanged();
        }
    }
    	
	private Bitmap getImageBitmapFromUrl(String url){
		Bitmap bitmap = null;
		bitmap = FileBrowser.gridviewBitmapCaches.get(url);
		if(bitmap != null){
			return bitmap;
		}
		bitmap = BitmapFactory.decodeFile(url);
		bitmap =ThumbnailUtils.extractThumbnail(bitmap, 100,100,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
	
	private boolean cancelPotentialLoadImage(String url,ImageView imageview){
		AsyncLoadImage loadImageTask = getAsyncLoadImageTask(imageview);

	    if (loadImageTask != null) {
	        String bitmapUrl = loadImageTask.url;
	        if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
	        	loadImageTask.cancel(true);	        	
	        } else {
	            // 相同的url已经在加载中.
	            return false;
	        }
	    }
	    return true;

	}
	
	//当 loadImageTask.cancel(true)被执行的时候，则AsyncLoadImageTask 就会被取消，
	//当AsyncLoadImageTask 任务执行到onPostExecute的时候，如果这个任务加载到了图片，
	//它也会把这个bitmap设为null了。 
	//getAsyncLoadImageTask代码如下：
	private AsyncLoadImage getAsyncLoadImageTask(ImageView imageview){
		if (imageview != null) {
	        Drawable drawable = imageview.getDrawable();
	        if (drawable instanceof LoadedImageDrawable) {
	        	LoadedImageDrawable loadedDrawable = (LoadedImageDrawable)drawable;
	            return loadedDrawable.getLoadImageTask();
	        }
	    }
	    return null;
	}
	
	//该类功能为：记录imageview加载任务并且为imageview设置默认的drawable
		public static class LoadedImageDrawable extends BitmapDrawable{
			private final WeakReference<AsyncLoadImage> loadImageTaskReference;

		    public LoadedImageDrawable(AsyncLoadImage loadImageTask) {
		        super(defaultImaPic);
		        loadImageTaskReference =
		            new WeakReference<AsyncLoadImage>(loadImageTask);
		    }

		    public AsyncLoadImage getLoadImageTask() {
		        return loadImageTaskReference.get();
		    }

		}
		
		
		
		
		
		
		
		
		
		
		
		
		//视频抽帧
	    class AsyncLoadVideoImage extends AsyncTask<Integer, Void, Bitmap> {
	    	private String url = null;
			private final WeakReference<ImageView> imageViewReference;
			public AsyncLoadVideoImage(ImageView imageview) {
				super();
				// TODO Auto-generated constructor stub
				imageViewReference = new WeakReference<ImageView>(imageview);
			}
			@Override
			protected Bitmap doInBackground(Integer... params) {
				// TODO Auto-generated method stub
				Bitmap bitmap = null;
				this.url = mdata.getFileItems().get(params[0]).getFileItem().getFilePath();			
				bitmap = getVideoBitmapFromUrl(url);
				FileBrowser.gridviewBitmapCaches.put(mdata.getFileItems().get(params[0]).getFileItem().getFilePath(), bitmap);			
				return bitmap;
			}

			@Override
			protected void onPostExecute(Bitmap resultBitmap) {
				// TODO Auto-generated method stub
				if(isCancelled()){
					resultBitmap = null;
				}
				if(imageViewReference != null){
					ImageView imageview = imageViewReference.get();
					AsyncLoadVideoImage loadImageTask = getAsyncLoadVideoTask(imageview);
				    // Change bitmap only if this process is still associated with it
				    if (this==loadImageTask) {
				    	imageview.setImageBitmap(resultBitmap);
				    	//imageview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				    }
				}
				super.onPostExecute(resultBitmap);
			}	
	                

	        @Override
	        public void onProgressUpdate(Void... value) {
	        	FileGridViewAdapter.this.notifyDataSetChanged();
	        }
	    }
		
		private Bitmap getVideoBitmapFromUrl(String url){
			Bitmap bitmap = null;
			bitmap = FileBrowser.gridviewBitmapCaches.get(url);
			if(bitmap != null){
				return bitmap;
			}
			bitmap =ThumbnailUtils.createVideoThumbnail(url,3);
			if(bitmap==null)
				bitmap=defaultVidPic;
			return bitmap;
		}

		
		
		
		private boolean cancelPotentialLoadVideo(String url,ImageView imageview){
			AsyncLoadVideoImage loadImageTask = getAsyncLoadVideoTask(imageview);

		    if (loadImageTask != null) {
		        String bitmapUrl = loadImageTask.url;
		        if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
		        	loadImageTask.cancel(true);	        	
		        } else {
		            // 相同的url已经在加载中.
		            return false;
		        }
		    }
		    return true;

		}
		
		//当 loadImageTask.cancel(true)被执行的时候，则AsyncLoadImageTask 就会被取消，
		//当AsyncLoadImageTask 任务执行到onPostExecute的时候，如果这个任务加载到了图片，
		//它也会把这个bitmap设为null了。 
		//getAsyncLoadImageTask代码如下：
		private AsyncLoadVideoImage getAsyncLoadVideoTask(ImageView imageview){
			if (imageview != null) {
		        Drawable drawable = imageview.getDrawable();
		        if (drawable instanceof LoadedVideoDrawable) {
		        	LoadedVideoDrawable loadedDrawable = (LoadedVideoDrawable)drawable;
		            return loadedDrawable.getLoadImageTask();
		        }
		    }
		    return null;
		}

		//该类功能为：记录imageview加载任务并且为imageview设置默认的drawable
		public static class LoadedVideoDrawable extends BitmapDrawable{
			private final WeakReference<AsyncLoadVideoImage> loadImageTaskReference;

		    public LoadedVideoDrawable(AsyncLoadVideoImage loadImageTask) {
		        super(defaultVidPic);
		        loadImageTaskReference =
		            new WeakReference<AsyncLoadVideoImage>(loadImageTask);
		    }

		    public AsyncLoadVideoImage getLoadImageTask() {
		        return loadImageTaskReference.get();
		    }

		}		
    
    
    class AsyncLoadApkicon extends AsyncTask<FileItem, Void, Object> {
        @Override
        protected Object doInBackground(FileItem... params) {
            String path = MApplication.CACHE_PATH;
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            } else {
                Bitmap bm;
            	FileItem item = params[0];
            	File thumbFile = new File(path + item.getFileName().replace(".", ""));
            	if (thumbFile.exists()) {
            		bm = BitmapFactory.decodeFile(thumbFile.getAbsolutePath());
            		bm =ThumbnailUtils.extractThumbnail(bm, 100,100,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
					item.setIcon(bm);
					publishProgress();
				}
            	else {
            		try {
            			Drawable dw = Helper.showUninstallAPKIcon(mContext, item.getFilePath());
                		if(dw!=null){
                			BitmapDrawable bd = (BitmapDrawable)dw;
                			bm = bd.getBitmap();
                			bm =ThumbnailUtils.extractThumbnail(bm, 100,100,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                			item.setIcon(bm);
                			publishProgress();
                			//Thread.sleep(50);
                		}
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
				}
            }
            return null;
        }

        @Override
        public void onProgressUpdate(Void... value) {
        	//FileGridViewAdapter.this.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Object result) {
        	
        }
    }
}
package com.inspur.myadapter;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.files.FileItem;
import com.inspur.files.FileItemForOperation;
import com.inspur.files.FileItemSet;
import com.inspur.mediacenter.MainActivity;
import com.inspur.mediacenter.R;
import com.inspur.mediacenter.filebrowser.ImageFileBrowser;



public class ImageGridViewAdapter extends BaseAdapter{
	
	
	private LayoutInflater mInflater;
    private FileItemSet mdata;
    private Context mContext;
    ViewHolder holder = null;
    private static Bitmap defaultPic;
    private int numColumns;
    public int getNumColumns() {
		return numColumns;
	}

	public void setNumColumns(int numColumns) {
		this.numColumns = numColumns;
	}

	public ImageGridViewAdapter(Context context, FileItemSet data){
        super();
        this.mContext = context;
        this.mdata = data;
        mInflater = LayoutInflater.from(this.mContext);
        defaultPic=BitmapFactory.decodeResource(this.mContext.getResources(), R.drawable.picture); 
        defaultPic =ThumbnailUtils.extractThumbnail(defaultPic, 100,100,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
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
        //holder.img_head.setImageResource(fileItem.getIconId());
		//首先我们先通过cancelPotentialLoad方法去判断imageview是否有线程正在为它加载图片资源，
		//如果有现在正在加载，那么判断加载的这个图片资源（url）是否和现在的图片资源一样，不一样则取消之前的线程（之前的下载线程作废）。
		//见下面cancelPotentialLoad方法代码
        
		if (cancelPotentialLoad(url, holder.img_head)) {
			AsyncLoadImage task = new AsyncLoadImage(holder.img_head);
			 task.execute(position);
	         LoadedDrawable loadedDrawable = new LoadedDrawable(task);
	         holder.img_head.setImageDrawable(loadedDrawable);	        
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
			bitmap = getBitmapFromUrl(url);
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
        	ImageGridViewAdapter.this.notifyDataSetChanged();
        }
    }
    
   
	
	
	
	
	
	
	
	
	
	
	private Bitmap getBitmapFromUrl(String url){
		Bitmap bitmap = null;
		bitmap = ImageFileBrowser.gridviewBitmapCaches.get(url);
		if(bitmap != null){
			return bitmap;
		}
		bitmap = BitmapFactory.decodeFile(url);
		bitmap =ThumbnailUtils.extractThumbnail(bitmap, 100,100,ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	
	
	
	private boolean cancelPotentialLoad(String url,ImageView imageview){
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
	        if (drawable instanceof LoadedDrawable) {
	        	LoadedDrawable loadedDrawable = (LoadedDrawable)drawable;
	            return loadedDrawable.getLoadImageTask();
	        }
	    }
	    return null;
	}

	/*
	//该类功能为：记录imageview加载任务并且为imageview设置默认的drawable
	public static class LoadedDrawable extends ColorDrawable{
		private final WeakReference<AsyncLoadImage> loadImageTaskReference;

	    public LoadedDrawable(AsyncLoadImage loadImageTask) {
	        super(R.drawable.picture);
	        loadImageTaskReference =
	            new WeakReference<AsyncLoadImage>(loadImageTask);
	    }

	    public AsyncLoadImage getLoadImageTask() {
	        return loadImageTaskReference.get();
	    }

	}*/
	
	
	//该类功能为：记录imageview加载任务并且为imageview设置默认的drawable
		public static class LoadedDrawable extends BitmapDrawable{
			private final WeakReference<AsyncLoadImage> loadImageTaskReference;

		    public LoadedDrawable(AsyncLoadImage loadImageTask) {
		        super(defaultPic);
		        loadImageTaskReference =
		            new WeakReference<AsyncLoadImage>(loadImageTask);
		    }

		    public AsyncLoadImage getLoadImageTask() {
		        return loadImageTaskReference.get();
		    }

		}

}
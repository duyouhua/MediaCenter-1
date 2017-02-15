package com.inspur.mediacenter.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;

import com.inspur.files.FileItem;
import com.inspur.files.FileItemForOperation;
import com.inspur.files.FileItemSet;
import com.inspur.files.FileManager;
import com.inspur.files.FileManager.FileFilter;
import com.inspur.files.FileManager.OnFileSetUpdated;
import com.inspur.files.FileManager.OnWhichOperation;
import com.inspur.files.FileManager.ViewMode;
import com.inspur.files.FilesAdapter;
import com.inspur.mediacenter.ImageActivity;
import com.inspur.mediacenter.MusicActivity;
import com.inspur.mediacenter.PreparedResource;
import com.inspur.mediacenter.R;
import com.inspur.mediacenter.VideoActivity;
import com.inspur.music.Music;

public abstract class Browser implements OnItemClickListener, OnClickListener,OnLongClickListener,OnWhichOperation,OnFileSetUpdated {
	protected static String TAG = "";
	protected FileManager mFileManager;
	protected LayoutInflater mInflater; 
	protected Context mContext;
	protected FileItemSet mData;
	protected FileItemSet mShowData;
	protected int curShowUpIndex;
	protected int curShowLowIndex;
	protected BaseAdapter mItemsAdapter;
	protected boolean pickPath = false;
	/**
     * 
     */
	protected ViewMode mViewMode;
	protected View mView;
	
	private final int SUB_MENU_TXT      = Menu.FIRST + 10;
    private final int SUB_MENU_AUDIO    = Menu.FIRST + 11;
    private final int SUB_MENU_VIDEO    = Menu.FIRST + 12;
    private final int SUB_MENU_PIC      = Menu.FIRST + 13;
	/**
     * 
     */
	protected PreparedResource preResource;

	public View getView(){
		return mView;
	}
	
	public abstract boolean onPrepareOptionsMenu(Menu menu);
	public abstract void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo);
	public abstract void onContextMenuClosed(Menu menu);
	public abstract boolean onContextItemSelected(MenuItem item);
	public abstract boolean onOptionsItemSelected(MenuItem item);
	public abstract boolean onBackPressed();
	public abstract void recycleBitmapCaches();

	
	public abstract void onResume();
	
	protected Browser(Context context) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
        preResource = new PreparedResource(context);
        mData = new FileItemSet();
        mShowData=new FileItemSet();
        mFileManager = new FileManager(context, mData);
        mFileManager.setOnWhichoperation(this);
	}
	protected void QueryData(File preFile,boolean clear,FileFilter filter){
		if (clear) {
			mData.clear();
			mShowData.clear();
		}
        mFileManager.setOnFileSetUpdated(this);
        mFileManager.query(preFile.getAbsolutePath(),filter);
    }
	/**
     * refresh adapter
     */
	protected void refreshData() {
		int mDataSize=mData.getFileItems().size();
		mShowData.clear();
		curShowUpIndex=0;
		List<FileItemForOperation> fileItems=new ArrayList<FileItemForOperation>();
		if(mDataSize-curShowUpIndex<=12){
			for(int i=curShowUpIndex;i<mDataSize+curShowUpIndex;i++){
				fileItems.add(mData.getFileItems().get(i));
			}
		}
		else{
			for(int i=curShowUpIndex;i<12+curShowUpIndex;i++){
				fileItems.add(mData.getFileItems().get(i));
			}
		}
		mShowData.setFileItems(fileItems);
		curShowLowIndex=curShowUpIndex;
		curShowUpIndex+=fileItems.size();
		if (mItemsAdapter != null) {
			recycleBitmapCaches();
	    	mItemsAdapter.notifyDataSetChanged();
		}
		if(mView!=null&&mView.getVisibility()==View.VISIBLE)
			reflashTipImage();
    }
	
	public void nextPage(){
		int mDataSize=mData.getFileItems().size();
		if(mDataSize-curShowUpIndex<=0)
			return;
		List<FileItemForOperation> fileItems=new ArrayList<FileItemForOperation>();
		if(mDataSize-curShowUpIndex>12){
			for(int i=curShowUpIndex;i<12+curShowUpIndex;i++){
				fileItems.add(mData.getFileItems().get(i));
			}
			curShowLowIndex=curShowUpIndex;
			curShowUpIndex+=fileItems.size();
			mShowData.clear();
			mShowData.setFileItems(fileItems);
			if (mItemsAdapter != null) {
				recycleBitmapCaches();
		    	mItemsAdapter.notifyDataSetChanged();
			}
			reflashTipImage();
			mView.setAnimation(AnimationUtils.loadAnimation(  
					mContext.getApplicationContext(), R.anim.in_right_translate));
		}
		else if(mDataSize-curShowUpIndex<=12){
			for(int i=curShowUpIndex;i<mDataSize;i++){
				fileItems.add(mData.getFileItems().get(i));
			}
			curShowLowIndex=curShowUpIndex;
			curShowUpIndex+=fileItems.size();
			mShowData.clear();
			mShowData.setFileItems(fileItems);
			if (mItemsAdapter != null) {
				recycleBitmapCaches();
		    	mItemsAdapter.notifyDataSetChanged();
			}
			reflashTipImage();
			mView.setAnimation(AnimationUtils.loadAnimation(  
					mContext.getApplicationContext(), R.anim.in_right_translate));
		}
	}
	public void prePage(){
		if(curShowLowIndex<=0)
			return;
		List<FileItemForOperation> fileItems=new ArrayList<FileItemForOperation>();
		if(curShowLowIndex>=12){
			for(int i=curShowLowIndex-12;i<curShowLowIndex;i++){
				fileItems.add(mData.getFileItems().get(i));
			}
			curShowUpIndex=curShowLowIndex;
			curShowLowIndex-=fileItems.size();
			mShowData.clear();
			mShowData.setFileItems(fileItems);
			if (mItemsAdapter != null) {
				recycleBitmapCaches();
		    	mItemsAdapter.notifyDataSetChanged();
			}
			reflashTipImage();
			mView.setAnimation(AnimationUtils.loadAnimation(  
					mContext.getApplicationContext(),R.anim.in_left_translate));
		}		
	}
	//刷新指示箭头
	protected void reflashTipImage(){
		int max=mData.getFileItems().size();
		if(curShowLowIndex==0){
			if(curShowUpIndex==max){
				sendBoardcast(1);
			}else{
				sendBoardcast(2);
			}
		}
		else{
			if(curShowUpIndex==max){
				sendBoardcast(3);	
			}else{
				sendBoardcast(4);	
			}
		}
	}
	protected void sendBoardcast(int index){
		Intent intent=new Intent("com.uvchip.reflashImagePage");
		intent.putExtra("index", index);
		mContext.sendBroadcast(intent);
	}
	protected void clickFileItem(FileItemForOperation fileItem) {
		if (pickPath){
        	Intent intent = new Intent();
            Uri uri = getContentUri(fileItem.getFileItem());
            intent.setData(uri);
            Log.i(TAG, "uri:" + uri);
            ((Activity)mContext).setResult(Activity.RESULT_OK, intent);
            ((Activity)mContext).finish();
        }else {
            doOpenFile(null,fileItem.getFileItem());
        }
	}
	
	/**
     * open file depending on file type
     */
	protected void doOpenFile(String type,FileItem fileItem) {
		
		
		
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse("file://" + fileItem.getFilePath());
        if (type == null) {
            //type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileItem.getExtraName().toLowerCase());
        	if(preResource.isAudioFile(fileItem.getExtraName().toLowerCase())){			
    			List<Music> L=new ArrayList<Music>();
    			Music m=new Music();
    			m.setUrl(fileItem.getFilePath());
    			m.setSize(fileItem.getFileSize());
    			m.setName(fileItem.getFileName());
    			L.add(m);
    			Intent intent1=new Intent(mContext,MusicActivity.class);
    			intent1.putExtra("musicList", L.toArray());
    			intent1.putExtra("id", 0);
    			mContext.startActivity(intent1);
    			return;
    		}else if(preResource.isVideoFile(fileItem.getExtraName().toLowerCase())){
    			Intent intent1 = new Intent(mContext,VideoActivity.class);
    			intent1.putExtra("url", fileItem.getFilePath());
    			mContext.startActivity(intent1);
    			return;
    		}else if(preResource.isImageFile(fileItem.getExtraName().toLowerCase())){
    			List<String> iPath=new ArrayList<String>();
    			iPath.add(fileItem.getFilePath());
    			Bundle bundle=new Bundle();
    			bundle.putStringArrayList("iPath", (ArrayList<String>) iPath);
    			bundle.putInt("currentIndex", 0);
    			Intent intent1=new Intent(mContext,ImageActivity.class);
    			intent1.putExtra("bundle",bundle);
    			mContext.startActivity(intent1);
    			return;
    		}else{
    			openAsDialog(fileItem).show();
    		}
        }
        else if (type != null) {
            intent.setDataAndType(uri, type);
            try {
                ((Activity)mContext).startActivityForResult(intent, 1);
            } catch (ActivityNotFoundException e) {
                //ViewEffect.showToast(this, formatStr(R.string.toast_cont_open_file,fileItem.getFileName()));
                openAsDialog(fileItem).show();
            }
        } 
        //else {
        //    openAsDialog(fileItem).show();
        //}

        /** */
    }
	
	private void openAs(int id,FileItem fileItem){
        String type = null;
        switch (id) {
            case SUB_MENU_TXT:
                type = "text/plain";
                break;
            case SUB_MENU_AUDIO:
                type = "audio/*";
                break;
            case SUB_MENU_VIDEO:
                type = "video/*";
                break;
            case SUB_MENU_PIC:
                type = "image/*";
                break;
            default:
                break;
        }
        doOpenFile(type, fileItem);
    }
    protected Dialog openAsDialog(final FileItem fileItem){
        return new AlertDialog.Builder(mContext)
        .setTitle(R.string.menu_open_as)
        .setItems(R.array.open_as_items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                openAs(Menu.FIRST + 10 + which,fileItem);
            }
        })
        .create();
    }
    
    protected void toggleMode() {
		if (mViewMode == ViewMode.LISTVIEW) {
			mViewMode = ViewMode.GRIDVIEW;
		}else {
			mViewMode = ViewMode.LISTVIEW;
		}
	}
    
	protected Uri getContentUri(FileItem item){
        Uri uri = null;
        String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(item.getExtraName().toLowerCase());
        ContentResolver cr = mContext.getContentResolver();
        if (type != null) {
            if (type.startsWith("image/")  ) {
                final String[] BUCKET_PROJECTION_IMAGES = new String[] { Images.ImageColumns._ID };
                final String where = Images.ImageColumns.DATA + " = '" + item.getFilePath() + "'";
                final Cursor cursor = cr.query(Images.Media.EXTERNAL_CONTENT_URI, BUCKET_PROJECTION_IMAGES, where, null, null);
                if (null != cursor && cursor.moveToFirst()) {
                    int mediaId = cursor.getInt(0);
                    uri = ContentUris.withAppendedId(Images.Media.EXTERNAL_CONTENT_URI,mediaId);
                }
            }else if (type.startsWith("video/")) {
                final String[] BUCKET_PROJECTION_VIDEO = new String[] { Video.VideoColumns._ID };
                final String where = Video.VideoColumns.DATA + " = '" + item.getFilePath() + "'";
                final Cursor cursor = cr.query(Video.Media.EXTERNAL_CONTENT_URI, BUCKET_PROJECTION_VIDEO, where, null, null);
                if (null != cursor && cursor.moveToFirst()) {
                    int mediaId = cursor.getInt(0);
                    uri = ContentUris.withAppendedId(Video.Media.EXTERNAL_CONTENT_URI,mediaId);
                }
            }else if (type.startsWith("audio/")) {
                final String[] BUCKET_PROJECTION_AUDIO = new String[] { Audio.AudioColumns._ID };
                final String where = Audio.AudioColumns.DATA + " = '" + item.getFilePath() + "'";
                final Cursor cursor = cr.query(Audio.Media.EXTERNAL_CONTENT_URI, BUCKET_PROJECTION_AUDIO, where, null, null);
                if (null != cursor && cursor.moveToFirst()) {
                    int mediaId = cursor.getInt(0);
                    uri = ContentUris.withAppendedId(Audio.Media.EXTERNAL_CONTENT_URI,mediaId);
                }
            }
        }
        
        if (uri == null) {
            File file = new File(item.getFilePath());
            uri = Uri.fromFile(file);
        }
        return uri;
    }
}

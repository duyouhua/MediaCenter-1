package com.inspur.mediacenter.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.inspur.files.FileManager.FileFilter;
import com.inspur.files.FileManager.FilesFor;
import com.inspur.files.FileManager.ViewMode;
import com.inspur.files.FileItemForOperation;
import com.inspur.files.FilesAdapter;
import com.inspur.mediacenter.MainActivity;
import com.inspur.mediacenter.MusicActivity;
import com.inspur.mediacenter.R;
import com.inspur.mediacenter.VideoActivity;
import com.inspur.myadapter.VideoGridViewAdapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.animation.AnimationUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

public class VideoFileBrowser extends Browser{
	static{
		TAG = VideoFileBrowser.class.getCanonicalName();
		
	}
	private GridView mGridView;
	private boolean onResume = false;
	public static Map<String,Bitmap> gridviewBitmapCaches = new HashMap<String,Bitmap>();
	private VideoGridViewAdapter videoadapter=null;
	public VideoFileBrowser(Context context) {
		super(context);
		initView();
		mItemsAdapter=new VideoGridViewAdapter(mContext,mShowData);
		QueryData(new File("/mnt/"), true, FileFilter.VIDEO);
	}
	
	public void onResume(){
		if (!onResume) {
			onResume = true;
		}
		reflashTipImage();
	}
	public void reflashSDcardChanged(){
		QueryData(new File("/mnt/"), true, FileFilter.VIDEO);
	}
	private void initView() {
		mView = mInflater.inflate(R.layout.video_browser, null);
		mGridView = (GridView)mView.findViewById(R.id.gvVideoList);
		mGridView.setOnItemClickListener(this);
		mGridView.setNumColumns(4);
	}	
	@Override
	public void QueryData(File preFile,boolean clear,FileFilter filter) {
		super.QueryData(preFile,clear,filter);
		mGridView.setAdapter(mItemsAdapter);
		mGridView.invalidate();
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position=curShowLowIndex+position;
		FileItemForOperation fileItem = mData.getFileItems().get(position);
		Intent intent = new Intent(mContext,
				VideoActivity.class);
		intent.putExtra("url", fileItem.getFileItem().getFilePath());
		mContext.startActivity(intent);
	}
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}
  //卸载图片的函数
  	public void recycleBitmapCaches(){		
  		Bitmap delBitmap = null;
  		for(int del=0;del<mShowData.getFileItems().size();del++){
  			delBitmap = gridviewBitmapCaches.get(mShowData.getFileItems().get(del).getFileItem().getFilePath());	
  			if(delBitmap != null){	
  				//如果非空则表示有缓存的bitmap，需要清理	
  				Log.d(TAG, "release position:"+ del);		
  				//从缓存中移除该del->bitmap的映射		
  				gridviewBitmapCaches.remove(mShowData.getFileItems().get(del).getFileItem().getFilePath());		
  				delBitmap.recycle();	
  				delBitmap = null;
  			} 			
  		}		
  	}
	@Override
	public void onClick(View v) {
		
	}

	@Override
	public boolean onLongClick(View v) {
		return false;
	}

	@Override
	public void whichOperation(FilesFor filesFor, int size) {
		
	}

	@Override
	public void queryFinished() {
		refreshData();
	}

	@Override
	public void queryMatched() {
		//refreshData();
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
	}
	@Override
	public void onContextMenuClosed(Menu menu) {
		
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return false;
	}
	@Override
	public boolean onBackPressed() {
		return false;
	}
	

}

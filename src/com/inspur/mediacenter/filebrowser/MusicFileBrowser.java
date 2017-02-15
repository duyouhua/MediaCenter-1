package com.inspur.mediacenter.filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;





import com.inspur.files.FileItem;
import com.inspur.files.FilesAdapter;
import com.inspur.files.FileManager.FileFilter;
import com.inspur.files.FileManager.FilesFor;
import com.inspur.files.FileManager.ViewMode;
import com.inspur.mediacenter.MainActivity;
import com.inspur.mediacenter.MusicActivity;
import com.inspur.mediacenter.R;
import com.inspur.music.Music;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;



public class MusicFileBrowser extends Browser{
	private GridView mGridView;
	private boolean onResume = false;
	private List<Music> musicList;
	public MusicFileBrowser(Context context) {
		super(context);
		initView();
		mItemsAdapter = new FilesAdapter(mContext, mShowData);
		QueryData(new File("/mnt/"), true, FileFilter.MUSIC);		
	}
	public void onResume(){
		if (!onResume) {
			onResume = true;
		}
		reflashTipImage();
	}
	public void reflashSDcardChanged(){
		QueryData(new File("/mnt/"), true, FileFilter.MUSIC);			
	}
	private void initView() {
		mView = mInflater.inflate(R.layout.music_browser, null);
		mGridView = (GridView)mView.findViewById(R.id.gvMusicList);
		mGridView.setOnItemClickListener(this);	
		//mGridView.setNumColumns(MainActivity.mScreenWidth / 320); 
		mGridView.setNumColumns(4);
	}	
	@Override
	public void QueryData(File preFile,boolean clear,FileFilter filter) {
		super.QueryData(preFile,clear,filter);
		//mItemsAdapter.setViewMode(ViewMode.GRIDVIEW);
		mGridView.setAdapter(mItemsAdapter);
		mGridView.invalidate();
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		position=curShowLowIndex+position;
		Intent intent=new Intent(mContext,MusicActivity.class);
		intent.putExtra("musicList", musicList.toArray());
		intent.putExtra("id", position);
		mContext.startActivity(intent);
	}	
	private List<Music> getMusicList(){
		List<Music> L=new ArrayList<Music>();
		for(int i=0;i<mData.size();i++){
			FileItem fi=mData.getFileItems().get(i).getFileItem();
			Music m=new Music();
			m.setUrl(fi.getFilePath());
			m.setSize(fi.getFileSize());
			m.setName(fi.getFileName());
			L.add(m);
		}
		return L;
	}		
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
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
		musicList=getMusicList();
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
	
	//卸载图片的函数
	public void recycleBitmapCaches(){		
  				
  	}	
}

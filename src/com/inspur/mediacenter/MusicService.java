package com.inspur.mediacenter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import com.inspur.music.LrcProcess;
import com.inspur.music.LrcContent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;



public class MusicService extends Service{
	private MediaPlayer player ;
	public LrcProcess mLrcProcess;
	private int duration=0;
	private SeekBarBroadcastReceiver receiver;
	private boolean getAudioId=false;
	private String uri;
	private boolean isError;


	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return new MsgBinder();  
	}
	
      
    public class MsgBinder extends Binder{  
        /** 
         * 获取当前Service的实例 
         * @return 
         */  
        public MusicService getService(){  
            return MusicService.this;  
        }  
    }  
	@Override
	public void onCreate() {
		//注册点击进度条触发广播
		receiver = new SeekBarBroadcastReceiver();
		IntentFilter filter = new IntentFilter("com.uvchip.seekBar");
		this.registerReceiver(receiver, filter);
		super.onCreate();
	}

	@Override
	public void onDestroy(){
		if (player != null) {
			player.stop();
			player.release();
			player = null;
			}
		this.unregisterReceiver(receiver);
		mHandler.removeCallbacks(mRunnable);
		super.onDestroy();
	}
	
	
	@Override
	public void onStart(Intent intent, int startId) {

	    String play = intent.getStringExtra("play");
		uri=intent.getStringExtra("uri"); 
			if (play.equals("play")) {
				playMusic();
			} else if (play.equals("pause")) {
				if (null != player) {
					player.pause();
				}
			} else if (play.equals("playing")) {
				if (player != null) {
					player.start();
				} else {
					playMusic();
				}
			}
	}

	private Runnable LrcListRunnable=new Runnable(){
		@Override
		public void run() {
			// TODO Auto-generated method stub
			String song_name="";
			String singer="";
			Cursor cursor = MusicService.this.getContentResolver().query(
	                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
	                new String[] {  MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ARTIST}
	                , MediaStore.Audio.Media.DATA+"=?", new String[]{uri},null);
		      if (cursor.moveToFirst()) {
		         song_name=cursor.getString(0);
		          singer=cursor.getString(1);
		      }	
		      if(cursor!=null)
		    	  cursor.close();
		    Log.v("song_name",song_name);
		    Log.v("singer",singer);
			mLrcProcess = new LrcProcess(MusicService.this); 
			// 读取歌词文件 
			mLrcProcess.readLRC(uri,song_name,singer);
			// 传回处理后的歌词文件
			lrcList = mLrcProcess.getLrcContent();
			if(lrcList.size()==0){
				LrcContent lc=new LrcContent();
				lc.setLrc_time(0);
				lc.setLrc("无法搜索到歌词");
				lrcList.add(lc);
			}
			Intent lrcintent = new Intent("com.uvchip.lrcLists");
			lrcintent.putExtra("lrcList", lrcList.toArray());
			sendBroadcast(lrcintent);
			// 启动线程
			mHandler.post(mRunnable); 
			// /////////////////////// 初始化歌词配置 /////////////////////////			
		}
		
	};
	private void playMusic() {

		// /////////////////////// 初始化歌词配置 /////////////////////// //
		isError=false;
		new Thread(LrcListRunnable).start();
		Uri myUri = Uri.parse(uri);
		if(player==null)
			player= new MediaPlayer();
		player.reset();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			player.setDataSource(getApplicationContext(), myUri);
			player.prepareAsync();
			player.setOnPreparedListener(new OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // 装载完毕 开始播放流媒体
                	player.start();
                	duration=player.getDuration();
                	Intent it=new Intent("com.uvchip.audioid");
                	if(!getAudioId){
                		it.putExtra("audioid", player.getAudioSessionId());
                		getAudioId=true;
                	}
                	it.putExtra("duration",duration);
                	sendBroadcast(it);
            		player.setOnCompletionListener(new OnCompletionListener() {

            			@Override
            			public void onCompletion(MediaPlayer mp) {
            				if(!isError){
	            				Intent intent = new Intent("com.uvchip.completion");
	            				sendBroadcast(intent);
            				}
            			}
            		});
            		
            		player.setOnErrorListener(new OnErrorListener() {

            			@Override
            			public boolean onError(MediaPlayer mp, int what, int extra) {
            				if(!isError){
	            				isError=true;
	            				if(player!=null)
	            					player.reset();
			            		Intent intent = new Intent("com.uvchip.error");
			            		sendBroadcast(intent);
            				}
            				return false;
            			}
            		});
            		
                }
            });
		} catch (Exception e) {
			// TODO Auto-generated catch block
			if(!isError){
				isError=true;
				if(player!=null)
					player.reset();
        		Intent intent = new Intent("com.uvchip.error");
        		sendBroadcast(intent);
			}
			e.printStackTrace();			
		} 
	}

	private class SeekBarBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int seekBarPosition = intent.getIntExtra("seekBarPosition", 0);
			//player.pause();
			player.seekTo(seekBarPosition * duration / 100);
			//player.start();			
		}
	}
	Handler mHandler = new Handler();
	// 歌词滚动线程
	Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (null != player&&player.isPlaying()) {
				int position = player.getCurrentPosition();
				int total = player.getDuration();
				Intent intent = new Intent("com.uvchip.progress");
				intent.putExtra("position", position);
				intent.putExtra("total", total);
				intent.putExtra("lrcIndex", LrcIndex());
				sendBroadcast(intent);	
				mHandler.postDelayed(mRunnable, 200);
			}			
		} 
	};

	// 创建对象
	private List<LrcContent> lrcList = new ArrayList<LrcContent>();
	// 初始化歌词检索值
	private int index = 0;
	// 初始化歌曲播放时间的变量
	private int CurrentTime = 0;
	// 初始化歌曲总时间的变量
	private int CountTime = 0;

	/**
	 * 歌词同步处理类
	 */
	public int LrcIndex() {
		if (player!=null&&player.isPlaying()) {
			// 获得歌曲播放在哪的时间
			CurrentTime = player.getCurrentPosition();
			// 获得歌曲总时间长度
			CountTime = player.getDuration();
		}
		if (CurrentTime < CountTime) {

			for (int i = 0; i < lrcList.size(); i++) {
				if (i < lrcList.size() - 1) {
					if (CurrentTime < lrcList.get(i).getLrc_time() && i == 0) {
						index = i;
					}
					if (CurrentTime > lrcList.get(i).getLrc_time()
							&& CurrentTime < lrcList.get(i + 1).getLrc_time()) {
						index = i;
					}
				}
				if (i == lrcList.size() - 1
						&& CurrentTime > lrcList.get(i).getLrc_time()) {
					index = i;
				}
			}
		}
		return index;
	}
}
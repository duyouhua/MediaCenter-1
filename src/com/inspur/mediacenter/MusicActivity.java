package com.inspur.mediacenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



import com.inspur.files.FileItem;
import com.inspur.mediacenter.filebrowser.MusicFileBrowser;
import com.inspur.music.LrcContent;
import com.inspur.music.Music;
import com.inspur.music.LrcView;
import com.inspur.music.MusicList;
import com.inspur.music.MusicListAdapter;
import com.inspur.music.VisualizerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MusicActivity extends Activity {

	private TextView textName;
	private TextView textStartTime;
	private TextView textEndTime;
	private ImageButton imageBtnRewind;
	private ImageButton imageBtnPlay;
	private ImageButton imageBtnForward;
	private ImageButton imageBtnLoop;
	private ImageButton imageBtnCloseList;
	private LrcView lrc_view;
	private SeekBar seekBar1;
	private List<Music> musicList;
	private Boolean isPlaying = false;
	private Boolean isReplaying = false;
	private int id = 0;
	private MyProgressBroadCastReceiver receiver;
	private MyCompletionListner completionListner;			
	private LinearLayout mLinearLayout;
	private Visualizer mVisualizer;
	private VisualizerView mVisualizerView;
	private AudioSessionIdListner audioSessionIdListner;
	public Intent musicSerInt;
	private LrcListsListner lrcListsListner;
	private ErrorListner errorListner;
	private String playMode;
	private ListView lvMusicList;
	private int rightDown=1;
	private int leftDown=1;
	private int ceterDown=1;
	private boolean isCloseList=false;
	private int screenWidth;
	private LinearLayout llMusicList;
	private boolean canSeek=true;
	private int currLrcIndex=-1;
	private MusicListAdapter musicListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.musicplayer);
		screenWidth=getWindowManager().getDefaultDisplay().getWidth();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		/*
		Cursor cursor = MusicActivity.this.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null
                ,null,null,null);
	      if (cursor.moveToFirst()) {
	    	  do{
	         Log.v("title",cursor.getString(0));
	         Log.v("singer",cursor.getString(1));
	    	  }while(cursor.moveToNext());
	      }
	      if(cursor!=null)
	    	  cursor.close();
	    */
		Intent intent=this.getIntent();
		musicList=new ArrayList<Music>();
		Object[] mobjs = (Object[]) intent.getSerializableExtra("musicList"); 
		//Log.v("mobjs",Integer.toString(mobjs.length));
	    for (int i = 0; i < mobjs.length; i++) {   
	        Music m = (Music) mobjs[i];   
	        musicList.add(m);   
	    }	    
	    id=intent.getIntExtra("id", 0);
		playMode="LOOP";
		textName = (TextView) findViewById(R.id.music_name);
		textStartTime = (TextView) findViewById(R.id.music_start_time);
		textEndTime = (TextView)findViewById(R.id.music_end_time);
		seekBar1 = (SeekBar) findViewById(R.id.music_seekBar);
		imageBtnRewind = (ImageButton) findViewById(R.id.music_rewind); 
		imageBtnPlay = (ImageButton) findViewById(R.id.music_play);
		imageBtnForward = (ImageButton) findViewById(R.id.music_foward);		
		imageBtnLoop = (ImageButton) findViewById(R.id.music_loop);	
		imageBtnCloseList= (ImageButton) findViewById(R.id.music_closelist);	
		lrc_view = (LrcView)findViewById(R.id.LyricShow);
		lrc_view.setFocusable(false);		
		mLinearLayout=(LinearLayout)findViewById(R.id.VisualizerView);
		llMusicList=(LinearLayout)findViewById(R.id.llMusicList);
		lvMusicList=(ListView)findViewById(R.id.lvMusicList);
		imageBtnRewind.setOnClickListener(new MyListener());
		imageBtnPlay.setOnClickListener(new MyListener());
		imageBtnForward.setOnClickListener(new MyListener());
		imageBtnLoop.setOnClickListener(new MyListener());
		imageBtnCloseList.setOnClickListener(new MyListener());
		seekBar1.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				int value;
				switch(keyCode){
				case KeyEvent.KEYCODE_DPAD_LEFT:
					if(leftDown==1){
						leftDown++;
						canSeek=false;
						value=seekBar1.getProgress();
						if(value>0)
							value--;
						seekBar1.setProgress(value);
					}
					else 
						leftDown=1;
					break;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					if(rightDown==1){
						rightDown++;
						canSeek=false;
						value=seekBar1.getProgress();
						if(value<seekBar1.getMax())
							value++;
						seekBar1.setProgress(value);
					}						
					else
						rightDown=1;
					break;
				case KeyEvent.KEYCODE_DPAD_CENTER:	
					if(ceterDown==1){
						ceterDown++;
						value=seekBar1.getProgress();
						Intent intent=new Intent("com.uvchip.seekBar");
						intent.putExtra("seekBarPosition", value);
						sendBroadcast(intent);
					}
					else{
						ceterDown=1; 
						canSeek=true;
					}
					break;
				}
				return false;
			}			
		});/*
		seekBar1.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(!hasFocus&&!canSeek){
					canSeek=true; 
				}
			}			
		});*/
		musicListAdapter=new MusicListAdapter(MusicActivity.this,musicList);
		lvMusicList.setAdapter(musicListAdapter);
		lvMusicList.setLayoutParams(new LinearLayout.LayoutParams(screenWidth/5, LayoutParams.MATCH_PARENT));
		lvMusicList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if(arg2!=id||!isPlaying){
					setListItemNonePlaying(id);
					id=arg2;
					setPlay("play");
				}
			}			
		});
		
		receiver=new MyProgressBroadCastReceiver();
		IntentFilter filter1=new IntentFilter("com.uvchip.progress");
		registerReceiver(receiver, filter1);
		completionListner=new MyCompletionListner();
		IntentFilter filter=new IntentFilter("com.uvchip.completion");
		registerReceiver(completionListner, filter);	
		audioSessionIdListner=new AudioSessionIdListner();
		IntentFilter filter2=new IntentFilter("com.uvchip.audioid");
		registerReceiver(audioSessionIdListner, filter2);	
		lrcListsListner=new LrcListsListner();
		IntentFilter filter3=new IntentFilter("com.uvchip.lrcLists");
		registerReceiver(lrcListsListner, filter3);	
		errorListner=new ErrorListner();
		IntentFilter filter4=new IntentFilter("com.uvchip.error");
		registerReceiver(errorListner, filter4);	
		setPlay("play");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	protected void onPause() {
		if(musicSerInt!=null)
			this.stopService(musicSerInt);
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		this.unregisterReceiver(receiver);
		this.unregisterReceiver(completionListner);
		this.unregisterReceiver(audioSessionIdListner);
		this.unregisterReceiver(lrcListsListner);
		this.unregisterReceiver(errorListner);
		if(musicSerInt!=null){
			this.stopService(musicSerInt);
		}
		finish();
		super.onDestroy();
	}
	private void setNonePlay(){
		textName.setText("");
		textStartTime.setText("00:00");
		textEndTime.setText("00:00");
		seekBar1.setProgress(0);
		lrc_view.setNonePlay();
		imageBtnPlay.setImageResource(R.drawable.btn_play);
		isPlaying=false;
		isReplaying=true;
	}
	private void setPlay(String play){
		musicSerInt=new Intent(MusicActivity.this,MusicService.class);
		musicSerInt.putExtra("play", play);
		musicSerInt.putExtra("uri", musicList.get(id).getUrl());
		this.startService(musicSerInt);
		Music m=musicList.get(id);
		textName.setText(m.getName());
		imageBtnPlay.setImageResource(R.drawable.btn_pause);
		isPlaying = true;
		lrc_view.setSearchingLrc();
	}
	private void setListItemPlaying(int index){
		lvMusicList.setSelection(index);
		musicListAdapter.setSelectedId(index);
	}
	private void setListItemNonePlaying(int index){
		currLrcIndex=-1;
		musicListAdapter.setSelectedId(-1);
	}
	private class MyListener implements OnClickListener {

		@Override 
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (v == imageBtnRewind) {
				// 前一首
				setListItemNonePlaying(id);
				id--;
				if(id>=musicList.size()-1){
					id=musicList.size()-1;
				}else if(id<=0){
					id=0;
				}
				setPlay("play");
			} else if (v == imageBtnPlay) {
				// 正在播放
				if (isPlaying == true) {
					musicSerInt=new Intent(MusicActivity.this,MusicService.class);
					musicSerInt.putExtra("play", "pause");
					musicSerInt.putExtra("uri", musicList.get(id).getUrl());
					startService(musicSerInt);
					isPlaying = false;
					imageBtnPlay.setImageResource(R.drawable.btn_play);
				} else {
					musicSerInt=new Intent(MusicActivity.this,MusicService.class);
					if(isReplaying){
						musicSerInt.putExtra("play", "play");
						setListItemPlaying(id);
					}
					else
						musicSerInt.putExtra("play", "playing");
					musicSerInt.putExtra("uri", musicList.get(id).getUrl());
					startService(musicSerInt);
					isPlaying = true;
					imageBtnPlay.setImageResource(R.drawable.btn_pause);
				}
			} else if (v == imageBtnForward) {
				// 下一首
				setListItemNonePlaying(id);
				id++;
				if(id>=musicList.size()-1){
					id=musicList.size()-1;
				}else if(id<=0){
					id=0;
				}
				setPlay("play");
			} else if (v == imageBtnLoop) {
				if(playMode.equalsIgnoreCase("SINGLE")){
					playMode="SINGLELOOP";
					imageBtnLoop.setImageResource(R.drawable.btn_singleloop);
				}else if(playMode.equalsIgnoreCase("SINGLELOOP")){
					playMode="ORDER";
					imageBtnLoop.setImageResource(R.drawable.btn_order);
				}else if(playMode.equalsIgnoreCase("ORDER")){
					playMode="LOOP";
					imageBtnLoop.setImageResource(R.drawable.btn_loop);
				}else if(playMode.equalsIgnoreCase("LOOP")){
					playMode="RANDOM";
					imageBtnLoop.setImageResource(R.drawable.btn_random);
				}else if(playMode.equalsIgnoreCase("RANDOM")){
					playMode="SINGLE";
					imageBtnLoop.setImageResource(R.drawable.btn_single);
				}
			}else if(v==imageBtnCloseList){
				if(!isCloseList){
					llMusicList.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.out_right_translate));
					llMusicList.setVisibility(View.GONE);					
					imageBtnCloseList.setImageResource(R.drawable.btn_showlist);
					isCloseList=true;
				}else{
					llMusicList.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.in_right_translate));
					llMusicList.setVisibility(View.VISIBLE);
					imageBtnCloseList.setImageResource(R.drawable.btn_closelist);
					isCloseList=false;
				}
			}

		}
	}
   private class MyCompletionListner extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("complete","complete");
		setListItemNonePlaying(id);
		if(playMode.equalsIgnoreCase("SINGLE")){
			setNonePlay();			
		}else if(playMode.equalsIgnoreCase("SINGLELOOP")){
			setPlay("play");
		}else if(playMode.equalsIgnoreCase("ORDER")){
			id++;
			if(id<=musicList.size()-1){
				setPlay("play");
			}
			else{
				id=0;
				setNonePlay();
				setListItemNonePlaying(id);
			}
		}else if(playMode.equalsIgnoreCase("LOOP")){
			id++;
			if(id>musicList.size()-1){
				id=0;
			}
			setPlay("play");
		}else if(playMode.equalsIgnoreCase("RANDOM")){
			id=new Random().nextInt(musicList.size());
			setPlay("play");
		}
	}
	   
   }
	/**
	 * 时间格式转换
	 * 
	 * @param time
	 * @return
	 */
	public String toTime(int time) {

		time /= 1000;
		int minute = time / 60;
		int hour = minute / 60;
		int second = time % 60;
		minute %= 60;
		return String.format("%02d:%02d", minute, second);
	}
	
    private boolean getAudioId=false;
    private class AudioSessionIdListner extends BroadcastReceiver{

    	@Override
    	public void onReceive(Context context, Intent intent) {
    		if(!getAudioId){
	    		int audio=intent.getIntExtra("audioid",0);
	    		setupVisualizerFxAndUI(audio);
	    		mVisualizer.setEnabled(true);
	    		getAudioId=true;
    		}
    		int duration=intent.getIntExtra("duration",0);
    		textEndTime.setText(toTime(duration));
    		setListItemPlaying(id);
    		isPlaying = true;
    	}  	   
       }
    public class MyProgressBroadCastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			
			int position=intent.getIntExtra("position", 0);
			int total=intent.getIntExtra("total", 0);
			int progress = position * 100 / total;
			textStartTime.setText(toTime(position));
			textStartTime.invalidate();
			if(canSeek){
				seekBar1.setProgress(progress);
				seekBar1.invalidate();	
			}
			int lrcIndex=intent.getIntExtra("lrcIndex", 0);
			if(currLrcIndex!=lrcIndex){				
				currLrcIndex=lrcIndex;
			    lrc_view.SetIndex(lrcIndex);
			    lrc_view.invalidate();  
			}
		}    	
    }
    private class LrcListsListner extends BroadcastReceiver{

    	@Override
    	public void onReceive(Context context, Intent intent) {
    		List<LrcContent> lrcList = new ArrayList<LrcContent>();
   		    Object[] lobjs = (Object[]) intent.getSerializableExtra("lrcList");   
   	        for (int i = 0; i < lobjs.length; i++) {   
   	        	LrcContent lrc = (LrcContent) lobjs[i];   
   	        	lrcList.add(lrc);   
   	        }
    		lrc_view.setSentenceEntities(lrcList);
    		// 切换带动画显示歌词
    		lrc_view.setAnimation(AnimationUtils.loadAnimation(
    				MusicActivity.this, R.anim.alpha_z));
    	}    	   
     }
    
    private class ErrorListner extends BroadcastReceiver{

    	@Override
    	public void onReceive(Context context, Intent intent) {
    		Log.v("error", "error");
    		dialog();
    	}    	   
     }
    private void setupVisualizerFxAndUI(int audioSessionId)
	{
		if(mVisualizerView!=null){
			mLinearLayout.removeAllViewsInLayout();
		}
		mVisualizerView = new VisualizerView(MusicActivity.this);
		mVisualizerView.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT));
		mLinearLayout.addView(mVisualizerView);	
		mVisualizerView.setFocusable(false);
		final int maxCR = Visualizer.getMaxCaptureRate();				
		mVisualizer = new Visualizer(audioSessionId);
		mVisualizer.setCaptureSize(1024);
		mVisualizer.setDataCaptureListener(
				new Visualizer.OnDataCaptureListener()
				{
					public void onWaveFormDataCapture(Visualizer visualizer,
							byte[] bytes, int samplingRate)
					{
						mVisualizerView.updateVisualizer(bytes);
					}

					public void onFftDataCapture(Visualizer visualizer,
							byte[] fft, int samplingRate)
					{
						mVisualizerView.updateVisualizer(fft);
					}
				}, maxCR / 3, false, true);
	}
    
    
    protected void dialog() {
		  AlertDialog.Builder builder = new Builder(MusicActivity.this);
		  builder.setMessage("抱歉！无法播放该音乐");

		  builder.setTitle("错误"); 

		  builder.setPositiveButton("确认",new DialogInterface.OnClickListener() {

		   @Override
		   public void onClick(DialogInterface dialog, int which) {
		    dialog.dismiss();
		    musicList.remove(id);
		    lvMusicList.setAdapter(new MusicListAdapter(MusicActivity.this,musicList));
		    setNonePlay();
		   }
		  });
		  builder.create().show();
	}
}

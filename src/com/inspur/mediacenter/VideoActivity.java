package com.inspur.mediacenter;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;




public class VideoActivity extends Activity{
	private ImageButton bt;  //用于开始和暂停的按钮
	private SurfaceView pView;   //绘图容器对象，用于把视频显示在屏幕上
	private String url;   //视频播放地址
	private MediaPlayer mediaPlayer;    //播放器控件
	private int postSize;    //保存义播视频大小
	private SeekBar seekbar;   //进度条控件
	private boolean flag = true;   //用于判断视频是否在播放中
	private RelativeLayout rl;   
	private boolean display;   //用于是否显示其他按钮
	private upDateSeekBar update;   //更新进度条用
	private ImageButton btn_rewind;
	private ImageButton btn_forward;
	private TextView startTime;
	private TextView endTime;
	private int disTime=0;
	private int rightDown=1;
	private int leftDown=1;
	private int centerDown=1;
	private boolean canSeek=true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);   //全屏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 应用运行时，保持屏幕高亮，不锁屏
        setContentView(R.layout.videoplayer);

        //Set the screen to landscape.
        Intent intent=this.getIntent();
        url=intent.getStringExtra("url");
        super.onCreate(savedInstanceState);		
		init();  //初始化数据		
		setListener();   //绑定相关事件      
    }

    public boolean onKeyDown(int keyCode,KeyEvent event){
    	if(keyCode!=KeyEvent.KEYCODE_BACK){
	    	disTime=0;		
	    	if (!display) {
	    		rl.setVisibility(View.VISIBLE);
				bt.setVisibility(View.VISIBLE);
				bt.requestFocus();
				pView.setVisibility(View.VISIBLE);
				/**
				 * 设置播放为全屏
				 */
				ViewGroup.LayoutParams lp = pView.getLayoutParams();
				lp.height = LayoutParams.FILL_PARENT;
				lp.width = LayoutParams.FILL_PARENT;
				pView.setLayoutParams(lp);
				display = true;
	     	}
    	}
		return super.onKeyDown(keyCode, event);
	}
    /**
     * 更新进度条
     */
    private Runnable myRunnable =new Runnable(){
		public void run(){
			if (mediaPlayer == null) {
				flag = false;
			} else if (mediaPlayer.isPlaying()) {
				if(display){
					disTime++;					
					if(disTime>=7){
						disTime=0;
						bt.setVisibility(View.GONE);
						rl.setVisibility(View.GONE);
						flag = true;
						int position = mediaPlayer.getCurrentPosition();
						int mMax = mediaPlayer.getDuration();
						int sMax = seekbar.getMax();
						seekbar.setProgress(position * sMax / mMax);
						startTime.setText(toTime(position));
						endTime.setText(toTime(mMax));
						display = false;
					}
				}
				if(canSeek){
					flag = true;
					int position = mediaPlayer.getCurrentPosition();
					int mMax = mediaPlayer.getDuration();
					int sMax = seekbar.getMax();
					seekbar.setProgress(position * sMax / mMax);
					startTime.setText(toTime(position));
					endTime.setText(toTime(mMax));
				}
				mhandler.postDelayed(myRunnable, 1000);
			} else {
				mhandler.postDelayed(myRunnable, 1000);
				return;
			}
		}
	};
    
    private void init() {
		mediaPlayer = new MediaPlayer();   //创建一个播放器对象
		update = new upDateSeekBar();  //创建更新进度条对象
		setContentView(R.layout.videoplayer);   //加载布局文件
		
		seekbar = (SeekBar) findViewById(R.id.seekbar);  //进度条
		seekbar.setFocusable(true);
		seekbar.setFocusableInTouchMode(true);
		seekbar.requestFocus();
		bt = (ImageButton) findViewById(R.id.play);
		bt.setEnabled(false); //刚进来，设置其不可点击
		pView = (SurfaceView) findViewById(R.id.mSurfaceView);
		pView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);   //不缓冲
		pView.getHolder().setKeepScreenOn(true);   //保持屏幕高亮
		pView.getHolder().addCallback(new surFaceView());   //设置监听事件
		rl = (RelativeLayout) findViewById(R.id.rl2);  
		btn_rewind=(ImageButton)findViewById(R.id.rewind);
		btn_forward=(ImageButton)findViewById(R.id.forward);
		startTime=(TextView)findViewById(R.id.video_start_time);
		endTime=(TextView)findViewById(R.id.video_end_time);
	}

	class PlayMovie extends Thread {   //播放视频的线程

		int post = 0;

		public PlayMovie(int post) {
			this.post = post;

		}

		@Override
		public void run() {
			Message message = Message.obtain();
			try {
				mediaPlayer.reset();    //回复播放器默认
				mediaPlayer.setDataSource(url);   //设置播放路径
				mediaPlayer.setDisplay(pView.getHolder());  //把视频显示在SurfaceView上
				mediaPlayer.setOnPreparedListener(new Ok(post));  //设置监听事件
				mediaPlayer.prepare();  //准备播放
			} catch (Exception e) {
				message.what = 2;
				errorHandler.post(errorRunnable);
			}

			super.run();
		}
	}
	private Runnable errorRunnable=new Runnable(){
		public void run(){
			dialog();
		}
	};
    private Handler errorHandler=new Handler();
	class Ok implements OnPreparedListener {
		int postSize;

		public Ok(int postSize) {
			this.postSize = postSize;
		}

		@Override
		public void onPrepared(MediaPlayer mp) {
			//准备完成后，隐藏控件
			//bt.setVisibility(View.GONE);
			//rl.setVisibility(View.GONE);
			//bt.setEnabled(true);  
			display = true;
			if (mediaPlayer != null) { 
				mediaPlayer.start();  //开始播放视频
				int mMax = mediaPlayer.getDuration();
				endTime.setText(toTime(mMax));
			} else {
				return;
			}
			if (postSize > 0&&canSeek) {  //说明中途停止过（activity调用过pase方法，不是用户点击停止按钮），跳到停止时候位置开始播放
				mediaPlayer.seekTo(postSize);   //跳到postSize大小位置处进行播放
			}
			//new Thread(update).start();   //启动线程，更新进度条
			mhandler.post(myRunnable);
		}
	}

	private class surFaceView implements Callback {     //上面绑定的监听的事件

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {   //创建完成后调用
			if (postSize > 0 && url!= null) {    //说明，停止过activity调用过pase方法，跳到停止位置播放
				new PlayMovie(postSize).start();
				flag = true;
				if(canSeek){
					int sMax = seekbar.getMax();
					int mMax = mediaPlayer.getDuration();
					seekbar.setProgress(postSize * sMax / mMax);
				}
				postSize = 0;
			}
			else {
				new PlayMovie(0).start();   //表明是第一次开始播放
			}
		}
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) { //activity调用过pase方法，保存当前播放位置
			if (mediaPlayer != null && mediaPlayer.isPlaying()) {
				postSize = mediaPlayer.getCurrentPosition();
				mediaPlayer.stop();
				flag = false;				
			}
		}
	}

	private void setListener() {
		mediaPlayer
				.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
					@Override
					public void onBufferingUpdate(MediaPlayer mp, int percent) {
					}
				});

		mediaPlayer
				.setOnCompletionListener(new MediaPlayer.OnCompletionListener() { //视频播放完成
					@Override
					public void onCompletion(MediaPlayer mp) {
						Log.v("movie","complete");
						flag = false;
						bt.setImageResource(R.drawable.btn_play);
						seekbar.setProgress(0);
						startTime.setText(toTime(0));
						endTime.setText(toTime(0));
					}
				});

		/*
		mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {

			}
		});*/
		
		mediaPlayer.setOnErrorListener(new OnErrorListener(){

			@Override
			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
				// TODO Auto-generated method stub
				Log.v("movie","error");
				dialog();
				return false;
			}
			
		});
		
		mediaPlayer.setOnSeekCompleteListener(new OnSeekCompleteListener(){

			@Override
			public void onSeekComplete(MediaPlayer mp) {
				// TODO Auto-generated method stub
				mediaPlayer.start();
			}			
		});
/**
 * 如果视频在播放，则调用mediaPlayer.pause();，停止播放视频，反之，mediaPlayer.start()  ，同时换按钮背景
 */
		bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disTime=0;
				if (mediaPlayer.isPlaying()) {    
					bt.setImageResource(R.drawable.btn_play);
					mediaPlayer.pause();
					postSize = mediaPlayer.getCurrentPosition();
				} else {
					if (flag == false) {
						flag = true;
						//new Thread(update).start();
					}
					mediaPlayer.start();
					bt.setImageResource(R.drawable.btn_pause);
				}
			}
		});
		btn_rewind.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disTime=0;
				if (mediaPlayer.isPlaying()) {    
					int value=seekbar.getProgress();
	 	    		if(value>0){
	 	    			value-=2;
	 	    			value = value * mediaPlayer.getDuration()  //计算进度条需要前进的位置数据大小
								/ seekbar.getMax();
		 	    		mediaPlayer.seekTo(value);
	 	    		}	 	    		
				} 
			}
		});
		btn_forward.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disTime=0;
				if (mediaPlayer.isPlaying()) {    
					int value=seekbar.getProgress();
	 	    		if(value<seekbar.getMax()){
	 	    			value+=2;
	 	    		    value = value * mediaPlayer.getDuration()  //计算进度条需要前进的位置数据大小
							/ seekbar.getMax();
	 	    		    mediaPlayer.seekTo(value);
	 	    		}
				} 
			}
		});
		seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				disTime=0;
				int value = seekbar.getProgress() * mediaPlayer.getDuration()  //计算进度条需要前进的位置数据大小
						/ seekbar.getMax();
				mediaPlayer.seekTo(value);
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}
		});
		seekbar.setKeyProgressIncrement(0);
		seekbar.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				disTime=0;
				int value;
				switch(keyCode){
				case KeyEvent.KEYCODE_DPAD_LEFT:
					if(leftDown==1){
						leftDown++;
						canSeek=false;
						value=((SeekBar)v).getProgress();
						if(value>0){
							value--;
							((SeekBar)v).setProgress(value); 
						}	
					}
					else{
						leftDown=1;
					}
					break;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					
					if(rightDown==1){
						rightDown++;
						canSeek=false;
						value=((SeekBar)v).getProgress();
						if(value<((SeekBar)v).getMax()){
							value++;
							((SeekBar)v).setProgress(value); 
						}
					}
					else{
						rightDown=1;
					}
					break;
				case KeyEvent.KEYCODE_DPAD_CENTER:
					if(centerDown==1){
						centerDown++;
						value = seekbar.getProgress() * mediaPlayer.getDuration()  //计算进度条需要前进的位置数据大小
								/ seekbar.getMax();
						mediaPlayer.seekTo(value);
					}
					else{
						centerDown=1;
						canSeek=true;
					}
					break;
				}
				
				return false;
			}			
		});
		
/**
 * 点击屏幕，切换控件的显示，显示则应藏，隐藏，则显示
 */
		pView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (display) {
					bt.setVisibility(View.GONE);
					rl.setVisibility(View.GONE);
					display = false;
				} else {
					rl.setVisibility(View.VISIBLE);
					bt.setVisibility(View.VISIBLE);
					pView.setVisibility(View.VISIBLE);
					/**
					 * 设置播放为全屏
					 */
					ViewGroup.LayoutParams lp = pView.getLayoutParams();
					lp.height = LayoutParams.FILL_PARENT;
					lp.width = LayoutParams.FILL_PARENT;
					pView.setLayoutParams(lp);
					display = true;
				}

			}
		});
	}

    private Handler mhandler=new Handler();
	class upDateSeekBar implements Runnable {

		@Override
		public void run() {
			mhandler.post(myRunnable);
			if (flag) {
				mhandler.postDelayed(myRunnable, 1000);
			}
		}
	}

	@Override
	protected void onDestroy() {   //activity销毁后，释放资源
		super.onDestroy();
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
			mediaPlayer = null;
		}
		System.gc();
	}
	/**
	 * 时间格式转换
	 * 
	 * @param time
	 * @return
	 */
	public String toTime(int time) {
		int second = time / 1000;
		int minute = second / 60;
		int second_left = second % 60;
		int hour = minute / 60;
		int minute_left = minute % 60;
		return String.format("%02d:%02d:%02d",hour, minute_left, second_left);
	}
	
	
	protected void dialog() {
		  AlertDialog.Builder builder = new Builder(VideoActivity.this);
		  builder.setMessage("抱歉！无法播放该视频");

		  builder.setTitle("错误"); 

		  builder.setPositiveButton("确认",new DialogInterface.OnClickListener() {

		   @Override
		   public void onClick(DialogInterface dialog, int which) {
			   if(mediaPlayer!=null)
				   mediaPlayer.reset();
			   dialog.dismiss();
			   VideoActivity.this.finish();
		   }
		  });
		  builder.create().show();
	}
	
}

package com.inspur.mediacenter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.inspur.music.Music;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher.ViewFactory;

public class ImageActivity extends Activity{
	private String TAG = "ImageActivity";
	public static int mScreenWidth;
	private int currentIndex=0;
	private ImageSwitcher imageSwitcher;
	private List<String> iPath;
	private ImageButton btn_rewind;
	private ImageButton btn_forward;
	private ImageButton btn_play;
	private Timer myTimer;
	private boolean isPlaying=false;
	private int showTime;
	private RelativeLayout llbtn;
	private boolean isShow=true;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
	                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.imageplayer);
		//获取屏幕宽度 
		mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
		Intent intent=this.getIntent();
		Bundle bundle=intent.getBundleExtra("bundle");
		iPath=bundle.getStringArrayList("iPath"); 
		currentIndex=bundle.getInt("currentIndex");
		initView();//初始化界面	
	}
	@Override
	public void onDestroy(){
		myHandler.removeCallbacks(myRunnable);
		super.onDestroy();
	}
	private Handler handler=new Handler();
	public void startAutoPlay() {
		myTimer=new Timer();
		myTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (currentIndex == iPath.size() - 1) {
					currentIndex = 0;
					handler.post(new Runnable() {
						@Override
						public void run() {
							showImageNext(currentIndex);  
						}
					});
				} else {
					currentIndex++;
					handler.post(new Runnable() {
						@Override
						public void run() {
							showImageNext(currentIndex);  
						}
					});
				}
			}
		}, 3000, 3000);
	}   			
	//初始化界面
	private void initView() {
		imageSwitcher=(ImageSwitcher)findViewById(R.id.imageSwitcher);
		btn_rewind=(ImageButton)findViewById(R.id.rewind);
		btn_forward=(ImageButton)findViewById(R.id.forward);
		btn_rewind.setOnClickListener(new BtnListener());
		btn_forward.setOnClickListener(new BtnListener());
		btn_play=(ImageButton)findViewById(R.id.play);
		btn_play.setOnClickListener(new BtnListener());
		llbtn=(RelativeLayout)findViewById(R.id.llbtn);
		imageSwitcher.setFactory(new ViewFactory() {  
			  
            public View makeView() {  
            	ImageView iv=new ImageView(ImageActivity.this);
            	iv.setScaleType(ScaleType.CENTER_INSIDE);
            	iv.setImageURI(Uri.parse(iPath.get(currentIndex)));
                return iv;  
            }  
        }); 
		//showImageNext(currentIndex);
		myTimer=new Timer();
		myHandler.post(myRunnable);
		//startAutoPlay();		
	}
	private Handler myHandler = new Handler();
	private Runnable myRunnable =new Runnable(){
		public void run(){
			synchronized(this){
				showTime++;
				myHandler.postDelayed(myRunnable, 1000);
				if(showTime==10){
					showTime=0;
					llbtn.setVisibility(View.GONE);
					isShow=false;
				} 
			}
		}
	};
	private void showImageNext(int index){
		imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(),  
	                R.anim.in_right_translate));  
		// 设置切出动画  
		imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(  
					 getApplicationContext(), R.anim.out_left_translate));  
		imageSwitcher.setImageURI(Uri.parse(iPath.get(index)));    
	}
	private void showImagePre(int index){
		imageSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplicationContext(),  
                R.anim.in_left_translate));  
		// 设置切出动画  
		imageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(  
			          getApplicationContext(),R.anim.out_right_translate));  	
		imageSwitcher.setImageURI(Uri.parse(iPath.get(index)));    
	}
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event){
		synchronized(this){
			showTime=0;
		}
		if(!isShow){
			llbtn.setVisibility(View.VISIBLE);
        	isShow=true;
		}
		return super.onKeyDown(keyCode, event);
	}
	private void DisplayToast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}
	private class BtnListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			synchronized(this){
				showTime=0;
			}
			if (v == btn_rewind) {
				if(currentIndex>0){
					currentIndex--;
					showImagePre(currentIndex);
				}
				else if(currentIndex==0){
					DisplayToast("这是第一张图片了");
				}
			} else if (v == btn_forward) {
				if(currentIndex<iPath.size()-1){
					currentIndex++;
					showImageNext(currentIndex);
				}else if(currentIndex==iPath.size()-1){
					DisplayToast("这是最后一张图片了");
				}
			} else if (v == btn_play) {
				if(isPlaying){
					myTimer.cancel();
					isPlaying=false;
					btn_play.setImageResource(R.drawable.btn_play);
					btn_rewind.setEnabled(true);
					btn_forward.setEnabled(true);
					btn_rewind.setFocusable(true);
					btn_forward.setFocusable(true);
				}
				else{
					startAutoPlay();
					isPlaying=true;
					btn_play.setImageResource(R.drawable.btn_pause);
					btn_rewind.setEnabled(false);
					btn_forward.setEnabled(false);
					btn_rewind.setFocusable(false);
					btn_forward.setFocusable(false);
				}
			} 

		}
	}
}





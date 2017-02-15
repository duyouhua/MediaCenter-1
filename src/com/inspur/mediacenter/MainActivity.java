package com.inspur.mediacenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.inspur.mediacenter.filebrowser.Browser;
import com.inspur.mediacenter.filebrowser.FileBrowser;
import com.inspur.mediacenter.filebrowser.ImageFileBrowser;
import com.inspur.mediacenter.filebrowser.MusicFileBrowser;
import com.inspur.mediacenter.filebrowser.VideoFileBrowser;
import com.inspur.utils.AnimTabLayout;
import com.inspur.utils.AnimTabLayout.OnTabChangeListener;
import com.inspur.utils.MyViewPager;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainActivity extends Activity implements OnTabChangeListener{
	private String TAG = "MainActivity";
	private MyViewPager mViewPager;
	private FileBrowser mFileBrowser;
	private MusicFileBrowser mMusicFileBrowser;
	private VideoFileBrowser mVideoFileBrowser;
	private ImageFileBrowser mImageFileBrowser;
	private List<View> mViews;
	private List<String> mTitles;
	public static int mScreenWidth;
	private SDCardChangeReceiver mReceiver;
	private ReflashImagePageReceiver reflashImagePageReceiver;
	private int currentIndex=0;
	private AnimTabLayout titleList;
	private int screenWidth;
	private int screenHeight;
	private LinearLayout lltitleList;
	public static int gridViewWidth;
	public static int gridViewHeight;
	private ImageView image_pre;
	private ImageView image_next;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		screenWidth=getWindowManager().getDefaultDisplay().getWidth();
		screenHeight=getWindowManager().getDefaultDisplay().getHeight();
		setContentView(R.layout.activity_main);
		//RelativeLayout rl=(RelativeLayout)findViewById(R.id.main);
		//rl.setBackgroundColor(Color.BLUE);
		//获取屏幕宽度
		mScreenWidth = getWindowManager().getDefaultDisplay().getWidth();
		mFileBrowser = new FileBrowser(this);//文件浏览
		mMusicFileBrowser = new MusicFileBrowser(this);//音乐浏览
		mVideoFileBrowser = new VideoFileBrowser(this);//视频浏览
		mImageFileBrowser = new ImageFileBrowser(this);//图片浏览
		initView();//初始化界面
		mReceiver = new SDCardChangeReceiver();
		registerSDCardChangeReceiver();
		reflashImagePageReceiver=new ReflashImagePageReceiver();
		IntentFilter filter=new IntentFilter("com.uvchip.reflashImagePage");
		this.registerReceiver(reflashImagePageReceiver, filter);
	}
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event){
		View focusView;
		switch(keyCode){
			case KeyEvent.KEYCODE_DPAD_LEFT:
				focusView=titleList.findFocus();
				if(focusView==null){
					Browser current=getCurrBrowser();
					current.prePage();
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				focusView=titleList.findFocus();
				if(focusView==null){
					Browser current=getCurrBrowser();
					current.nextPage();
					titleList.setIsKey_Down(true);
				}
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				titleList.setIsKey_Down(true);
				break;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	protected void onPause() {		
		super.onPause(); 		
	}
	@Override
	protected void onDestroy() {
		this.unregisterReceiver(reflashImagePageReceiver);
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		Browser browser = getCurrBrowser();
		if (browser != null) {
			if(!browser.onBackPressed())
				super.onBackPressed();
		}
	}

    private void registerSDCardChangeReceiver(){ 
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);//usb插入完毕
        filter.addAction(Intent.ACTION_MEDIA_EJECT);//usb拔出
        filter.addDataScheme("file");
        registerReceiver(mReceiver, filter);
    }
	
	BaseAdapter tabAdapter = new BaseAdapter() {

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = new Button(MainActivity.this);
			}
			((Button)convertView).setText(mTitles.get(position));
			((Button)convertView).setTextAppearance(MainActivity.this, R.style.tvInBtn);
			((Button)convertView).setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
			((Button)convertView).setHeight(75);
			return convertView; 
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public int getCount() {
			return mTitles.size();
		}
	};

	//初始化界面
	private void initView() {
		//初始化ViewPager控件
		mViewPager = (MyViewPager) findViewById(R.id.viewpager);
		//设置页面视图
		mViews = new ArrayList<View>();
		mFileBrowser.getView().setTag(0);
		mImageFileBrowser.getView().setTag(1);		
		mVideoFileBrowser.getView().setTag(2);
		mMusicFileBrowser.getView().setTag(3);
		mViews.add(mFileBrowser.getView());
		mViews.add(mImageFileBrowser.getView());		
		mViews.add(mVideoFileBrowser.getView());
		mViews.add(mMusicFileBrowser.getView());
		mViewPager.setViews(mViews);
		mViewPager.setCurrentItem(0);
		image_pre=(ImageView)findViewById(R.id.image_pre);
		image_next=(ImageView)findViewById(R.id.image_next);
		//设置页面标题
		mTitles = new ArrayList<String>();
		mTitles.add(getString(R.string.file_browser));
		mTitles.add(getString(R.string.image_browser));
		mTitles.add(getString(R.string.video_browser));		
		mTitles.add(getString(R.string.music_browser));
		titleList=(AnimTabLayout)findViewById(R.id.titleList);
		lltitleList=(LinearLayout)findViewById(R.id.lltitleList);
		
		//lltitleList.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,screenHeight/5));
		titleList.setAdapter(tabAdapter);
		titleList.setOnTabChangeListener(this);
		//titleList.setLayoutParams(new RelativeLayout.LayoutParams(screenWidth/5,LayoutParams.WRAP_CONTENT));
		titleList.moveTo(0);
		gridViewHeight=screenHeight-lltitleList.getHeight();
	}	
	//获取当前ViewPager中的对象
	private Browser getCurrBrowser(){
		int index = mViewPager.getCurrentItem()%mViews.size();
		switch (index) {
		case 0:
			return mFileBrowser;			
		case 1:
			return mImageFileBrowser;			
		case 2:
			return mVideoFileBrowser;
		case 3:	
			return mMusicFileBrowser;
		default:
			return null;
		}		
	}

	private void DisplayToast(String str) {
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}
	@Override
	public void tabChange(int index) {
		// TODO Auto-generated method stub
		int curr = mViewPager.getCurrentItem();
		int realIndex = curr % mViews.size();
		int toIndex = curr + (index - realIndex);
		mViewPager.setCurrentItem(toIndex);
		switch (toIndex) {
		case 0:
			mFileBrowser.onResume();
			break;
		case 1:
			mImageFileBrowser.onResume();			
			break;
		case 2:
			mVideoFileBrowser.onResume();
			break;
		case 3:			
			mMusicFileBrowser.onResume();
			break;
		default:
			break;
		}
	}
	//刷新指示箭头
	private class ReflashImagePageReceiver extends BroadcastReceiver{

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	int index=intent.getIntExtra("index", 0);
	    	switch(index){
	    	case 1:
	    		image_pre.setImageResource(R.drawable.btn_noneprepage);
	    		image_next.setImageResource(R.drawable.btn_nonenextpage);
	    		break;
	    	case 2:
	    		image_pre.setImageResource(R.drawable.btn_noneprepage);
	    		image_next.setImageResource(R.drawable.btn_hasnextpage);
	    		break;
	    	case 3:
	    		image_pre.setImageResource(R.drawable.btn_hasprepage);
	    		image_next.setImageResource(R.drawable.btn_nonenextpage);
	    		break;
	    	case 4:
	    		image_pre.setImageResource(R.drawable.btn_hasprepage);
	    		image_next.setImageResource(R.drawable.btn_hasnextpage);
	    		break;
	    	}
	    }
	}
	private class SDCardChangeReceiver extends BroadcastReceiver{

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if(intent.getAction().equalsIgnoreCase(intent.ACTION_MEDIA_EJECT)){
	    		try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
	    	mFileBrowser.reflashSDcardChanged();
	    	mMusicFileBrowser.reflashSDcardChanged();
	    	mVideoFileBrowser.reflashSDcardChanged();
	    	mImageFileBrowser.reflashSDcardChanged();
	    }
	}
}

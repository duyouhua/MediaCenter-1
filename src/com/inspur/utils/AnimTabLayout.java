package com.inspur.utils;

import com.inspur.mediacenter.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;


public class AnimTabLayout extends LinearLayout implements OnClickListener{
	private LinearLayout mTabContainer,mIndicatorLayout;
	private Context mContext;
	private ImageView mIndicator;
	private int mTabCount = 5;
	private int mCurrIndex = 0;
	private BaseAdapter mAdapter;
	private int mIndicatorWidth = 0;
	private OnTabChangeListener mChangeListener;
	private final int selected;
	private final int unfocused;
	private final int focused;
	private int CurrentId;
	private View currentView;
	private boolean isKey_Down=false;
	public boolean getIsKey_Down() {
		return isKey_Down;
	}
	public void setIsKey_Down(boolean isKey_Down) {
		this.isKey_Down = isKey_Down;
	}
	public AnimTabLayout(Context context) {
		this(context,null);
	}
	public AnimTabLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
		//this.selected=R.drawable.tab_selected;
		this.selected=R.drawable.leftfocusbgmenufocus;
		this.unfocused=R.drawable.bg_unfocused;
		this.focused=Color.GRAY;
	}
	
	
	private void initView(){
		setOrientation(LinearLayout.VERTICAL);
		mTabContainer = new LinearLayout(mContext);
		mTabContainer.setOrientation(LinearLayout.HORIZONTAL);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		params.weight = 1;
		addView(mTabContainer,params); 		
		//mIndicatorLayout = new LinearLayout(mContext);
//		mIndicatorLayout.setBackgroundColor(Color.GRAY);
		//addView(mIndicatorLayout,new LayoutParams(LayoutParams.FILL_PARENT,5));
		
		//mIndicator = new ImageView(mContext);
		//mIndicator.setBackgroundColor(Color.GREEN);
		//mIndicatorLayout.addView(mIndicator,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//final int width = MeasureSpec.getSize(widthMeasureSpec);
		//mIndicatorWidth = width / mTabCount;
		//int childWidthSpec = getChildMeasureSpec(
         //       MeasureSpec.makeMeasureSpec(mIndicatorWidth, MeasureSpec.EXACTLY), 0, 
         //       mIndicator.getLayoutParams().width);
		//mIndicator.measure(childWidthSpec, heightMeasureSpec);
	}

	public void setAdapter(BaseAdapter adapter){
		Log.i(VIEW_LOG_TAG, "setAdapter IndicatorWidth:" + mIndicatorWidth);
		mAdapter = adapter;
		mTabCount = mAdapter.getCount();
		if (mTabCount <= 0) {
			return;
		}
		for (int i = 0; i < mTabCount; i++) {
			RelativeLayout layout = new RelativeLayout(mContext);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
					LayoutParams.MATCH_PARENT);
			layoutParams.weight = 1;
			layout.setBackgroundColor(Color.TRANSPARENT);
			//layout.getBackground().setAlpha(0);
			//layout.setBackgroundResource(R.drawable.bg_title);
			mTabContainer.addView(layout, layoutParams);
			View child = adapter.getView(i, null, layout);
			RelativeLayout.LayoutParams childParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
					LayoutParams.MATCH_PARENT);
			//childParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			child.setTag(R.drawable.topbar_bg, i);
			child.getBackground().setAlpha(0);
			layout.addView(child, childParams);
			child.setOnClickListener(this);
			if(i==0){
				currentView=child;
				child.setBackgroundResource(R.drawable.tab_selected);
				Button btn=(Button)currentView;
				btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
			}
			child.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					// TODO Auto-generated method stub
					if(hasFocus){
						if(!isKey_Down){
							int index = (Integer) v.getTag(R.drawable.topbar_bg);
							if(mCurrIndex!=index){
							//RelativeLayout tabIndex = (RelativeLayout)mTabContainer.getChildAt(index);
							//tabIndex.setBackgroundColor(focused);
								//v.setBackgroundResource(selected);
								if (mChangeListener != null) {
									mChangeListener.tabChange(index);
								}
								Button btn=(Button)currentView;
								btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
								btn.setBackgroundResource(R.drawable.tab_unselected);
								moveTo(index);
								v.setBackgroundResource(R.drawable.tab_selected);
								CurrentId=v.getId();
								currentView=v;
							}
						}
						else{
							currentView.requestFocus();
							currentView.setBackgroundResource(R.drawable.tab_selected);
							isKey_Down=false;
						} 
					}
					else{
						//int index = (Integer) v.getTag(R.drawable.topbar_bg);
						//if(mCurrIndex!=index){
						//RelativeLayout tabIndex = (RelativeLayout)mTabContainer.getChildAt(index);
						//tabIndex.setBackgroundResource(unfocused);
						//}  
						if(!isKey_Down){
							Button btn=(Button)v;
							btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
						}
						else{
						}
					}
					Button btn=(Button)currentView;
					btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
				}
				
			});
		}

	}
	
	public void setIndicatorBackgroundColor(int color){
		mIndicator.setBackgroundColor(color);
		invalidate();
	}
	public void setIndicatorBackgroundRe(int resid){
		mIndicator.setBackgroundResource(resid);
		invalidate();
	}
	
	@Override
	public void onClick(View v) {
		final int index = (Integer) v.getTag(R.drawable.topbar_bg);
		if (mChangeListener != null) {
			mChangeListener.tabChange(index);
		}
		moveTo(index);		
	}
	public int getCurrentId(){
		return CurrentId;
	}
	public void moveTo(int index){
		if (index < 0 || index >= mTabCount) {
			return;
		}
		RelativeLayout tabCurr = (RelativeLayout)mTabContainer.getChildAt(mCurrIndex);
		RelativeLayout tabIndex = (RelativeLayout)mTabContainer.getChildAt(index);
		//tabCurr.setBackgroundResource(unfocused);
		//tabIndex.setBackgroundResource(selected);
		mCurrIndex = index;
	}
	
	public OnTabChangeListener getOnTabChangeListener() {
		return mChangeListener;
	}
	public void setOnTabChangeListener(OnTabChangeListener mChangeListener) {
		this.mChangeListener = mChangeListener;
	}

	public interface OnTabChangeListener{
		public void tabChange(int index);
	}

}

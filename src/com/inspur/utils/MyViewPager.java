package com.inspur.utils;

import java.util.List;

import com.inspur.mediacenter.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

public class MyViewPager extends RelativeLayout{

	private List<View> mViews;
	private int currentIndex;
	private Context context;
	public MyViewPager(Context context) {
		super(context);
		this.context=context;
		// TODO Auto-generated constructor stub
	}
	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
	}
	public void setViews(List<View> mViews){
		this.mViews=mViews;
		for(View v:mViews){
			RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
			this.addView(v, params);
			v.setVisibility(View.GONE);
		}
	} 
	public void setCurrentItem(int index){
		if(index>=currentIndex){
			mViews.get(currentIndex).setAnimation(AnimationUtils.loadAnimation(  
					context.getApplicationContext(), R.anim.out_left_translate));
			mViews.get(currentIndex).setVisibility(View.GONE);
			currentIndex=index;
			mViews.get(currentIndex).setAnimation(AnimationUtils.loadAnimation(  
					context.getApplicationContext(), R.anim.in_right_translate));
			mViews.get(currentIndex).setVisibility(View.VISIBLE);
		}else{
			mViews.get(currentIndex).setAnimation(AnimationUtils.loadAnimation(  
					context.getApplicationContext(), R.anim.out_right_translate));
			mViews.get(currentIndex).setVisibility(View.GONE);
			currentIndex=index;
			mViews.get(currentIndex).setAnimation(AnimationUtils.loadAnimation(  
					context.getApplicationContext(), R.anim.in_left_translate));
			mViews.get(currentIndex).setVisibility(View.VISIBLE);
		}
	}
	public int getCurrentItem(){
		return currentIndex;
	}

}

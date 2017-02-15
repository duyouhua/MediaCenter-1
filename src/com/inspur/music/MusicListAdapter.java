package com.inspur.music;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.inspur.mediacenter.R;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MusicListAdapter extends BaseAdapter{

	private Context mContext;
	private List<Music> musicList;
	private LayoutInflater mInflater;
	private int selectedId;
	private Drawable d;
	public int getSelectedId() {
		return selectedId;
	}
	public void setSelectedId(int selectedId) {
		this.selectedId = selectedId;
		MusicListAdapter.this.notifyDataSetChanged();
	}
	public MusicListAdapter(Context mContext,List<Music> musicList){
		this.mContext=mContext;
		this.musicList=musicList;
		mInflater = LayoutInflater.from(this.mContext);
		this.d = this.mContext.getResources().getDrawable(R.drawable.btn_play);
		d.setBounds(0, 0, 20, 20);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return musicList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView=mInflater.inflate(R.layout.musiclist_item_file, null);
        }
		TextView title = (TextView)convertView.findViewById(R.id.title);
		if(position==selectedId){
			title.setTextColor(Color.WHITE);
			SpannableString spanText = new SpannableString("   "+musicList.get(position).getName());
			spanText.setSpan(new ImageSpan(d), 1, 2, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			title.setText(spanText);
		}
		else{
			title.setTextColor(Color.BLACK);
			title.setText("  "+Integer.toString(position+1)+".  "+musicList.get(position).getName());
		}
		return convertView;
	}	
}

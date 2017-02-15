package com.inspur.music;

import java.io.Serializable;

/**
 * 获得歌词和时间并返回的类
 */
public class LrcContent implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	private String Lrc;
	private int Lrc_time;

	public String getLrc() {
		return Lrc;
	}

	public void setLrc(String lrc) {
		Lrc = lrc;
	}

	public int getLrc_time() {
		return Lrc_time;
	}

	public void setLrc_time(int lrc_time) {
		Lrc_time = lrc_time;
	}
}

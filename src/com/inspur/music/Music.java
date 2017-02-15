package com.inspur.music;

import java.io.Serializable;

public class Music implements Serializable {
	  
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String title;//歌曲名称
	private String singer;//歌手
	private String album;//专辑
	private String url;//文件路径
	private long size;//大小
	private long time;//时间
	private String name;//
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSinger() {
		return singer;
	}
	public void setSinger(String singer) {
		this.singer = singer;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
}

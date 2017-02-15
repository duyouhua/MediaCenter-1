package com.inspur.music;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.util.Xml;

/**
 * 处理歌词文件的类
 */
public class LrcProcess {

	private List<LrcContent> LrcList;

	private LrcContent mLrcContent;
	private String currentTime = null;//存放临时时间  
    private String currentContent = null;//存放临时歌词
    private Context context;
	public LrcProcess(Context context) {
		this.context=context;
		mLrcContent = new LrcContent();
		LrcList = new ArrayList<LrcContent>();
	}
	private int LrcSearch(String musicName, String singerName) {
		int idnumber=0;
		musicName = musicName.replace(' ', '+');
		singerName = singerName.replace(' ', '+');
		URL url=null;
		int eventType = 0;
		try {
			musicName = URLEncoder.encode(musicName, "UTF-8");
			singerName = URLEncoder.encode(singerName, "UTF-8");			
			musicName = musicName.replaceAll("%2B", "+");
			singerName = singerName.replaceAll("%2B", "+");
			String strUrl = "http://box.zhangmen.baidu.com/x?op=12&count=1&title="
					+ musicName + "$$" + singerName + "$$$$";
			url = new URL(strUrl);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		HttpURLConnection conn = null;
		
		try {
				conn = (HttpURLConnection) url.openConnection();
				conn.setReadTimeout(5 * 1000);
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}			
			try {
				conn.setRequestMethod("GET");
			} catch (ProtocolException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}catch (IllegalArgumentException e3){
				e3.printStackTrace();
			}
		
		InputStream inStream = null;
		try {
			inStream = conn.getInputStream();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}catch (IllegalArgumentException e3){
			e3.printStackTrace();
		}
		if(inStream==null)
			return 0;
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(inStream, "UTF-8");
			eventType = parser.getEventType();
		} catch (XmlPullParserException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}catch (IllegalArgumentException e3){
			e3.printStackTrace();
		} 

		while (eventType != XmlPullParser.END_DOCUMENT) {
			//Log.v("eventType",Integer.toString(eventType));
			switch (eventType) {
			case XmlPullParser.START_DOCUMENT:			
				break;
			case XmlPullParser.START_TAG:
				// 获取解析器当前指向的元素的名称
				String name = parser.getName();
				if ("lrcid".equals(name)) {
					try {
						idnumber = Integer.parseInt(parser.nextText());
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (XmlPullParserException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}catch (IllegalArgumentException e3){
						e3.printStackTrace();
					}
				}
				break;
			}
			try {
				eventType = parser.next();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (IllegalArgumentException e3){
				e3.printStackTrace();
			}

		}
		return idnumber;
	}
	private InputStreamReader fetchLyric(int idnumber) {
		//Log.v("idnumber",Integer.toString(idnumber));
		InputStreamReader is=null;
		if (idnumber == 0) {
			return null;
		}
		String geciURL = "http://box.zhangmen.baidu.com/bdlrc/" + idnumber
				/ 100 + "/" + idnumber + ".lrc";
		URL url=null;
			try {
				url = new URL(geciURL);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (IllegalArgumentException e3){
				e3.printStackTrace();
			}
		try {
			is=new InputStreamReader(url.openStream(),"GB2312");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IllegalArgumentException e3){
			e3.printStackTrace();
		}
		
		return is;		
	}
	/**
	 * 读取歌词文件的内容
	 * 
	 */
	public String readLRC(String song_path,String song_name,String singer){
		// public void Read(String file){
		StringBuilder stringBuilder = new StringBuilder();
		String lrcPath=song_path.replace(".mp3", ".lrc");
		File f = new File(lrcPath);
		FileInputStream fis=null;
		InputStreamReader isr=null;
		BufferedReader br=null;
		try {
			fis = new FileInputStream(f);
			isr = new InputStreamReader(fis, "GB2312");			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			if(singer.equals("<unknown>"))
				return null;
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context  
		               .getSystemService(Context.CONNECTIVITY_SERVICE);  
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();  
			if (mNetworkInfo != null) {  
				 if(mNetworkInfo.isAvailable()){
					 int idnumber=LrcSearch(song_name,singer);
					 isr=fetchLyric(idnumber);
				 }
			}	
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e3){
			e3.printStackTrace();
		}
		try {
			if(isr==null){
				 mLrcContent.setLrc_time(0);
	             mLrcContent.setLrc("无法搜索到歌词");
			     // 添加进列表数组
				LrcList.add(mLrcContent);
				return null;
			}
			br = new BufferedReader(isr);
			String s = "";
			while ((s = br.readLine()) != null) {
				/*
				// 替换字符
				s = s.replace("[", "");
				s = s.replace("]", "@");
				// 分离"@"字符
				String splitLrc_data[] = s.split("@");
				if (splitLrc_data.length > 1) {
					mLrcContent.setLrc(splitLrc_data[1]);

					// 处理歌词取得歌曲时间
					int LrcTime = TimeStr(splitLrc_data[0]);

					mLrcContent.setLrc_time(LrcTime);

					// 添加进列表数组
					LrcList.add(mLrcContent);

					// 创建对象
					mLrcContent = new LrcContent();
				}*/
				 String reg = "\\[(\\d{2}:\\d{2}\\.\\d{2})\\]";  
				// 编译  
		            Pattern pattern = Pattern.compile(reg);  
		            Matcher matcher = pattern.matcher(s);  
		  
		            // 如果存在匹配项，则执行以下操作  
		            while (matcher.find()) {  
		                // 得到匹配的所有内容  
		                String msg = matcher.group();  
		                // 得到这个匹配项开始的索引  
		                int start = matcher.start();  
		                // 得到这个匹配项结束的索引  
		                int end = matcher.end();  
		  
		                // 得到这个匹配项中的组数  
		                int groupCount = matcher.groupCount();  
		                // 得到每个组中内容  
		                for (int i = 0; i <= groupCount; i++) {  
		                    String timeStr = matcher.group(i);  
		                    if (i == 1) {  
		                        // 将第二组中的内容设置为当前的一个时间点  
		                        currentTime = timeStr;  
		                       //Log.v("currentTime",currentTime);
		                    }  
		                }  
		                int LrcTime = TimeStr(currentTime);						
		                // 得到时间点后的内容  
		                String[] content = pattern.split(s);  
		                // 输出数组内容  
		                if(content.length>0){
			                currentContent = content[content.length - 1];
			                mLrcContent.setLrc_time(LrcTime);
			                mLrcContent.setLrc(currentContent);
					        // 添加进列表数组
							LrcList.add(mLrcContent);
							// 创建对象
							mLrcContent = new LrcContent();
		                }
		            }

			}
			if(br!=null)
				br.close();
			if(isr!=null)
				isr.close();
			if(fis!=null)
				fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IllegalArgumentException e3){
			e3.printStackTrace();
		}
		
		
		return stringBuilder.toString();
	}

	/**
	 * 解析歌曲时间处理类
	 */
	public int TimeStr(String timeStr) {

		timeStr = timeStr.replace(":", ".");
		timeStr = timeStr.replace(".", "@");

		String timeData[] = timeStr.split("@");

		// 分离出分、秒并转换为整型
		int minute = Integer.parseInt(timeData[0]);
		int second = Integer.parseInt(timeData[1]);
		int millisecond = Integer.parseInt(timeData[2]);

		// 计算上一行与下一行的时间转换为毫秒数
		int currentTime = (minute * 60 + second) * 1000 + millisecond * 10;

		return currentTime;
	}

	public List<LrcContent> getLrcContent() {
		Comparator<LrcContent> comparator = new Comparator<LrcContent>(){
			   public int compare(LrcContent l1, LrcContent l2) {
				   if(l1.getLrc_time()>l2.getLrc_time())
					   return 1;
				   else if(l1.getLrc_time()<l2.getLrc_time())
					   return -1;
				   else
					   return 0;
			    
			   }
		}; 
		Collections.sort(LrcList, comparator);
		return LrcList;
	}

	

}
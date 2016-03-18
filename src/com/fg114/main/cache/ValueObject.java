package com.fg114.main.cache;

import java.util.*;

import android.util.Log;

/*
 * Creator: xu jianjun, 2011-11-15
 * 
 * 此类的对象表示一个被缓存的“值”对象。
 *
 */
public class ValueObject extends CacheableObject {
	public int id;
	public String dir;

	// 文件的key
	public String key;
	public String value;
	public String update_stamp;
	public int expire_minute;
	public Date save_time;
	public String version;
	public int read_count;
	public Date read_time;
	//表示“值”对象在内存中占用的字节数大小
	private long size=-1;

	public int getId() {
		return id;
	}

	void setId(int id) {
		this.id = id;
	}

	//
	public String getDir() {
		return dir;
	}

	void setDir(String dir) {
		this.dir = dir;
	}

	//
	public String getKey() {
		return key;
	}

	void setKey(String key) {
		this.key = key;
	}

	//
	public String getValue() {
		return value==null?"":value;
	}

	void setValue(String value) {
		this.value = value;
	}

	//
	public String getUpdate_stamp() {
		return update_stamp;
	}

	void setUpdate_stamp(String update_stamp) {
		this.update_stamp = update_stamp;
	}

	//
	public int getExpire_minute() {
		return expire_minute;
	}

	void setExpire_minute(int expire_minute) {
		this.expire_minute = expire_minute;
	}

	//
	public Date getSave_time() {
		return save_time;
	}

	void setSave_time(Date save_time) {
		this.save_time = save_time;
	}

	//
	public String getVersion() {
		return version;
	}

	void setVersion(String version) {
		this.version = version;
	}

	//
	public int getRead_count() {
		return read_count;
	}

	void setRead_count(int read_count) {
		this.read_count = read_count;
	}

	//
	public Date getRead_time() {
		return read_time;
	}

	void setRead_time(Date read_time) {
		this.read_time = read_time;
	}
	public boolean isExpired(){
		if((System.currentTimeMillis()-save_time.getTime())<expire_minute*60*1000){
			
			return false;
		}
		else {
			return true;
		}
	}
	//
	ValueObject() {

	}
	public String toString(){
		
		StringBuffer sb=new StringBuffer();
		sb.append("id="+id+", \n");
		sb.append("dir="+dir+", \n");

		sb.append("key="+key+", \n");
		sb.append("value="+value+", \n");
		sb.append("update_stamp="+update_stamp+", \n");
		sb.append("expire_minute="+expire_minute+", \n");
		sb.append("save_time="+save_time+", \n");
		sb.append("version="+version+", \n");
		sb.append("read_count="+read_count+", \n");
		sb.append("read_time="+read_time+", \n");
		return sb.toString();
	}

	@Override
	public long size() {
		
		if(this.size==-1){			
			this.size=this.value==null?0:this.value.getBytes().length;		
		}
		return this.size;
	}

	@Override
	public String identity() {
		
		return CacheableObject.constructIdentity(new String[]{this.dir,this.key});
	}

	@Override
	protected long hit() {
		
		return super.hit();
	}

	@Override
	protected long weight() {
		
		return super.weight();
	}

	@Override
	public byte[] getContent() {
		//缺省字符集
		return this.getValue().getBytes();
	}

	@Override
	public void setContent(byte[] bytes) {
		//缺省字符集
		if(bytes==null){
			this.setValue("");
		}
		else{
			this.setValue(new String(bytes));
		}
		
		
	}

	@Override
	void dump() {
				
	}
	
}

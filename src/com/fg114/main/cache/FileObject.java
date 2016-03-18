package com.fg114.main.cache;
import static com.fg114.main.app.Settings.DEBUG;

import java.util.*;

import com.fg114.main.app.view.MyImageView;
import com.fg114.main.util.ContextUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


/*
 * Creator: xu jianjun, 2011-11-15
 * 
 * 此类的对象表示一个可以自动利用缓存机制的“文件”对象。
 *
 */
/**
 * @author Administrator
 *
 */
public class FileObject extends CacheableObject {
	

	private int id;
	//文件所在的目录名
	private String dir;
	
	//文件的原始url
	private String key;
	private String local_file_name;
	private Date save_time;
	private String version;
	private int read_count;
	private Date read_time;
	//文件缓冲内容
	private byte[] buffer;
	
	//缓存图片文件的对象，用于进一步优化速度
	private Bitmap bmp=null;
	private int width=-1;
	private int height=-1;
	BitmapFactory.Options options = new BitmapFactory.Options();
	{
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
	}
	

	
	//文件的全路径名
	private String fullFileName;
	//文件全路径
	private String fullPath;
	
	//
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
	public String getLocal_file_name() {
		return local_file_name;
	}
	void setLocal_file_name(String local_file_name) {
		this.local_file_name = local_file_name;
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
	//
	public String getFullFileName() {
		return fullFileName;
	}
	void setFullFileName(String fullFileName) {
		this.fullFileName = fullFileName;
	}
	//
	public String getFullPath() {
		return fullPath;
	}
	void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}
	//

	FileObject(){}
	
	/**
	 * 返回表示该缓存文件的File对象
	 * @return bytes[] 存储文件的内容的字节数组。
	 */
	public byte[] getContent(){
		
/*		File file=new File(fullFileName);
		
		//文件存在，直接返回
		if(file.exists()&& file.isFile()){
			Log.d("FileObject","read from disk: "+this.fullFileName+",cached from: "+this.key);
			buffer=file.
		}
		
		//不存在文件时，尝试下载文件，如果下载失败，则返回null。
		Log.d("FileObject","missing file! Try to download again...");
		if(fileCache.downloadFileToDisk(this.fullPath, this.local_file_name, this.key)){
			
			return file;
		}*/
		
		return this.buffer;		
	}

	/**
	 * 以图片形式返回文件的内容。
	 * @return 表示文件内容的图片。如果文件不是有效的图片，则返回null  
	 */
	public Bitmap getContentAsBitmap(){
		if(this.buffer!=null && this.bmp==null && this.width==-1 && this.height==-1||this.bmp!=null&&this.bmp.isRecycled()){
			
			synchronized(this){
				if(this.buffer!=null && this.bmp==null && this.width==-1 && this.height==-1||this.bmp!=null&&this.bmp.isRecycled()){
					try{
						this.bmp=BitmapFactory.decodeByteArray(this.buffer, 0, this.buffer.length, options);
					}catch(OutOfMemoryError e){		
						this.isValid=false;	
						return null;
					}
			    	this.width=options.outWidth;
			    	this.height=options.outHeight;
			    	//如果发现是无效图片，清除无效的缓存，使下次请求可以从网络重新下载图片
			    	if(this.bmp==null||this.width==-1||this.height==-1){
			    		Log.e("getContentAsBitmap()","无效图片！从内存缓存中和文件系统中清除！"+this.key+",["+this.buffer.length+"]["+this.buffer+"]");
			    		//先清除文件缓存
			    		FileCacheUtil.getInstance().remove(this.dir, this.key);
			    		//再使内存缓存失效
			    		this.isValid=false;			    		
			    	}
			    	//Log.w("XXXXX decodeByteArray","this.buffer="+this.buffer+", this.buffer.length="+ this.buffer.length+",this.width="+this.width+",this.height="+this.height);
				}
			}
		}
		if(DEBUG) Log.w("XXXXX display","this.bmp"+this.bmp.isRecycled()+","+this.key+","+this.bmp+","+(this.bmp==null?"null":this.bmp.getWidth()));
    	return this.bmp;
	}
/*	public Bitmap getContentAsBitmap() {
		Bitmap temp=null;
		if (this.buffer != null) {
			temp = BitmapFactory.decodeByteArray(this.buffer, 0,
					this.buffer.length, options);
			this.width = options.outWidth;
			this.height = options.outHeight;
			// Log.w("XXXXX decodeByteArray","this.buffer="+this.buffer+", this.buffer.length="+
			// this.buffer.length+",this.width="+this.width+",this.height="+this.height);
			return temp;
		}
		return null;
		// Log.w("XXXXX display",this.key+","+this.bmp+","+(this.bmp==null?"null":this.bmp.getWidth()));

	}*/
	
	@Override
	public void setContent(byte[] bytes) {
		this.buffer=bytes;
		
	}
	
	public String toString(){
		
		StringBuffer sb=new StringBuffer();
		
		sb.append("id="+id+", \n");
		sb.append("dir="+dir+", \n");
		sb.append("key="+key+", \n");
		sb.append("local_file_name="+local_file_name+", \n");
		sb.append("save_time="+save_time+", \n");
		sb.append("version="+version+", \n");
		sb.append("read_count="+read_count+", \n");
		sb.append("read_time="+read_time+", \n");
		sb.append("read_time="+read_time+", \n");
		
		
		
		//显示文件信息
/*		File f=new File(this.fullFileName);
		if(f.exists()&& f.isFile()){
			
			sb.append("[file information] \n");
			sb.append("[file size: "+f.length()+" bytes] \n");
			
			//如果是图片，显示高宽			
			BitmapFactory.Options options = new BitmapFactory.Options();
	    	options.inJustDecodeBounds = true;
	    	Bitmap pic=BitmapFactory.decodeFile(this.fullFileName,options);
			if (options.outWidth > 0 && options.outHeight > 0) {
				sb.append("[file is a bitmap (W:H)=(" + options.outWidth + ":"
						+ options.outHeight + ") ]\n");
			} else {
				sb.append("[file is not a bitmap] \n");
			}			
		}
		else {
			
			sb.append("[file missing on file system!]");
		
		}
*/
		sb.append("[file information] \n");
		sb.append("[file size: "+this.size()+" bytes] \n");
		
		//如果是图片，显示高宽			
		if (this.width > 0 && this.height > 0) {
			sb.append("[file is a bitmap (W:H)=(" + this.width + ":"
					+ this.height + ") ]\n");
		} else {
			sb.append("[file is not a bitmap] \n");
		}
		
		return sb.toString();
	}
	@Override
	public long size() {
		
		/*File file=new File(fullFileName);
		
		//文件存在
		if(file.exists()&& file.isFile()){
			return file.length();
		}*/
		return this.buffer==null?0:this.buffer.length;
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
	void dump() {
		if(this.bmp!=null){
			this.bmp.recycle();
			//Log.e("dump"+size(),"url="+this.key,new Exception());
		}
		this.bmp=null;
		this.buffer=null;
		
	}


}

package com.fg114.main.weibo;

import java.util.ArrayList;


/**
 * 微博内容构建器，主要是为了封装控制文字长度的逻辑
 * 一些规则：
 * 1) url：的长度固定记为10
 * 2) text：一般性文字，在超出长度后可以被截断，并在后面补“…”
 * 3) importantText: 重要文字超长也不截取
 * 预留未实现：4) 在内容超长的情况下优先截取text，再截取importantText	 
 * 
 * @author xujianjun,2012-06-22
 */
public class WeiboContentBuilder{
	private int maxLength=140; //最大总长度，默认值
	private int realLength=0; //当前实际长度
	private int textLength=0; //记录可变文本的长度
	private ArrayList<WeiboContent> data=new ArrayList<WeiboContent>();
	private static class WeiboContent{
		final static int TYPE_URL=0;
		final static int TYPE_TEXT=1;
		final static int TYPE_IMPORTANT_TEXT=2;
		
		int type;
		String content;
		WeiboContent(int type,String content){
			this.type=type;
			this.content=content;
		}
	}
	public WeiboContentBuilder(){}
	
	public WeiboContentBuilder(int maxLength){
		this.maxLength=maxLength;
	}
	/**
	 * @param url 添加一个url，url可以为null
	 * @return
	 */
	public WeiboContentBuilder appendUrl(String url){
		if(append(WeiboContent.TYPE_URL,url)){
			realLength+=10;
		}			
		return this;
	}
	/**
	 * @param text 添加一个可被截取的text，text可以为null
	 * @return
	 */
	public WeiboContentBuilder appendText(String text){
		if(append(WeiboContent.TYPE_TEXT,text)){
			realLength+=text.length();
			textLength+=text.length();
		}	
		
		return this;
	}
	/**
	 * @param importantText 添加一个不可被截取的importantText，importantText可以为null
	 * @return
	 */
	public WeiboContentBuilder appendImportantText(String importantText){
		if(append(WeiboContent.TYPE_IMPORTANT_TEXT,importantText)){
			realLength+=importantText.length();
		}
		return this;
	}
	private boolean append(int type,String content){
		if(content==null||"".equals(content)||type>2||type<0){
			return false;
		}
		data.add(new WeiboContent(type, content));
		return true;
	}
	/**
	 * @return 返回满足当前字数限制的字符串
	 */
	public String toWeiboString(){
		
		//计算实际允许的“可变文字”的长度
		int allowedTextLength=textLength-(realLength-maxLength);
		int currentAddedTextLength=0; //记录构建时已加入的可变字符数
		boolean isTextDone=false; //可变内容是否已经添加完毕
		
		StringBuilder sb=new StringBuilder();
		
		for(WeiboContent wc : data){
			if(wc.type!=WeiboContent.TYPE_TEXT){
				sb.append(wc.content);
				continue;
			}
			//“可被截取内容”的处理				
			if(isTextDone||allowedTextLength<=0){
				continue;
			}
			//--
			if(currentAddedTextLength + wc.content.length()>allowedTextLength){
				//如果当前加入的内容会超长，则进行截取
				sb.append(wc.content.substring(0,allowedTextLength-currentAddedTextLength-1)+"…");
				isTextDone=true;
			}else if(currentAddedTextLength + wc.content.length()==allowedTextLength){
				//如果当前加入的内容长度刚刚好
				sb.append(wc.content);
				isTextDone=true;
			}else{
				sb.append(wc.content);
				currentAddedTextLength+=wc.content.length();
			}
		}
		return sb.toString();
	}
	/**
	 * @return 返回实际添加到builder的字符串
	 */
	public String toOriginalString(){
		StringBuilder sb=new StringBuilder();
		for(WeiboContent wc : data){
			sb.append(wc.content);
		}
		return sb.toString();
	}
}

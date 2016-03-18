package com.fg114.main.speech.asr;

/**
 * 此类的实例表示语音识别的一条结果
 * @author xu jianjun, 2011-12-12 
 *
 */
public class RecognitionResult {
	
	/**
	 * 识别结果
	 */
	public String text="";
	
	/**
	 * 置信度，0到1
	 */
	public double confidence=0.0d;
	
	public RecognitionResult(String text, double confidence){
		
		this.text=text;
		this.confidence=confidence;
	}
	
}

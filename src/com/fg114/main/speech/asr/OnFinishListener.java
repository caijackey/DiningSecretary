package com.fg114.main.speech.asr;

import java.util.ArrayList;



/**
 * 语音识别引擎完成时的事件处理器 
 * @author xu jianjun, 2011-12-12 
 *
 */

public interface OnFinishListener{
	
	/**
	 * @param result 识别结果集
	 * @param selectedIndex 用户手工选择的结果在结果集中的索引（以0开始计）。
	 * 如果没有经过选择，或者用户放弃选择时，此值为-1
	 */
	void onFinish(ArrayList<RecognitionResult> results, int selectedIndex);
}

package com.fg114.main.speech.asr;

import java.util.ArrayList;

import com.fg114.main.R;
import com.fg114.main.util.ViewUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.*;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * 语音识别引擎抽象类，所有具体的识别引擎必须继承该类
 * 
 * @author xu jianjun, 2011-12-12 * 
 *
 */

public abstract class RecognitionEngine {
	
	
	/**
	 * 讯飞引擎的类名
	 */
	public static final String XUNFEI="com.fg114.main.speech.asr.XunFei";
	
	/**
	 * 当有多个识别结果时，是否自动弹出对话框由用户手工从结果集中选择一个结果
	 */
	public boolean isManualSelectionOn=true;
	
	//默认为讯飞
	public static final String DEFAULT=XUNFEI;
	
	//事件处理器
	//选中的项目索引，以0开头
	protected int selectedIndex=-1;
	protected OnFinishListener finishListener;
	
	/**
	 * 返回系统默认的语音识别引擎
	 * @return 默认语音识别引擎
	 */
	public static RecognitionEngine getEngine(Context context){		
		
		return getEngine(context,DEFAULT);
	}
	
	/**
	 * 返回指定的语音识别引擎
	 * @return 语音识别引擎对象，如果无法创建语音识别引擎则返回null
	 */
	public static RecognitionEngine getEngine(Context context,String engineClassName){
		
		try{
			
			return ((RecognitionEngine)Class.forName(engineClassName).newInstance()).getInstance(context);
				
		}
		catch(Exception ex){
			Log.e("RecognitionEngine", ex.getMessage(), ex);
			showMessage(context,"无法创建语音引擎！");
			return null;
		}
	}
	

	/**
	 * 启动引擎开始语音识别。
	 * @param finishListener 识别完成时的事件监听器
	 * 
	 */
	public void start(OnFinishListener finishListener){
		this.start(null,finishListener);
	}
	
	/**
	 * 启动引擎开始语音识别。使用config指定的参数来控制识别。
	 * @param config 控制识别引擎行为的参数。
	 * 例如：可以传入表示地区，类别的数据，控制引擎进行更精确的识别。具体的参数根据约定来定义。
	 * 目前定义三种提示性参数：area(地域),restaurant(餐厅),food(菜品)
	 * 
	 * @param finishListener 识别完成时的事件监听器
	 * 
	 */
	public abstract void start(Bundle config,OnFinishListener finishListener);
	
	/**
	 * 子类用来实例化自己
	 * @param context
	 */
	public abstract RecognitionEngine getInstance(Context context);
	
	/**
	 * 显示信息的对话框
	 * @param context
	 * @param msg
	 */
	static void showMessage(Context context, String msg){
		
		AlertDialog.Builder bd=new AlertDialog.Builder(context);
		bd.setCancelable(true);
		bd.setMessage(msg);
		bd.setPositiveButton("确定",new OnClickListener() {			

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();				
			}
		});
		bd.create().show();
	}
	/**
	 * 显示“选择结果”对话框
	 * @param context
	 * @param title
	 */
	void showSelectionDialog(Context context, String title, final ArrayList<RecognitionResult> results){
		
		AlertDialog.Builder bd=new AlertDialog.Builder(context);
		bd.setCancelable(true);
		bd.setTitle(title);
		bd.setIcon(R.drawable.voice_recognition_button_small_up);
		//
		String[] items=new String[results.size()];
		int i=0;
		int maxLength=50;//超过50个字符加省略号
		for(RecognitionResult r : results){
			
			items[i]=r.text.substring(0, Math.min(r.text.length(), maxLength));
			if(r.text.length()>maxLength){
				items[i]=items[i]+"...";
			}
			
			
			i++;
		}
		
		bd.setItems(items, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//没有事件处理器，直接返回　
	        	if(RecognitionEngine.this.finishListener==null){
	        		return;
	        	}
	        	//RecognitionEngine.this.selectedIndex=which;
	        	finishListener.onFinish(results, which);
			}
		});
		
		//取消按钮
		bd.setNegativeButton("取消",new OnClickListener() {			

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//没有事件处理器
	        	if(RecognitionEngine.this.finishListener==null){
	        		return;
	        	}
	        	//finishListener.onFinish(results, -1);
				dialog.dismiss();	
				
			}
		});
/*		//确定按钮
		bd.setPositiveButton("确定",new OnClickListener() {			

			@Override
			public void onClick(DialogInterface dialog, int which) {
				//没有事件处理器
	        	if(RecognitionEngine.this.finishListener==null){
	        		return;
	        	}
	        	finishListener.onFinish(results, RecognitionEngine.this.selectedIndex);
				dialog.dismiss();				
			}
		});
*/
		bd.create().show();
	}
	
	
	/**
	 * 设置一个监听器，当识别完成后，监听器的onFinish事件会被调用
	 * @param finishListener
	 */
	public void setOnFinishListener(OnFinishListener finishListener){
		this.finishListener=finishListener;
	}

	/**
	 * 这是一个辅助方法。把一个按钮和一个EditText控件绑定到识别引擎实例上。
	 * 按钮用来触发识别，EditText用来接受识别结果。
	 * 该方法会重新设置引擎的OnFinishListener监听器。
	 * 
	 * 识别结果在EditText控件中的显示模式由mode指定：
	 * 0-"设置"模式，EditText控件中的内容直接设置为识别结果。
	 * 1-"插入"模式，识别结果插入到EditText控件中光标的当前位置，光标移动到插入内容的后面。
	 * 2-"追加"模式，识别结果追加到EditText控件中文本的最后面，光标移动到最后。
	 * 
	 * @param fireButton 触发识别的按钮
	 * @param editText 放置识别结果的文本框
	 * @param mode 识别结果的放置模式
	 * @param config 控制识别引擎行为的参数。见start方法。
	 * @param callback 当识别完成后的回调
	 */
	

	public void bindButtonAndEditText(Button fireButton,
			final EditText editText, final int mode, final Bundle config, final Runnable callback) {

		fireButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				try {
					//保存文本的选择当前状态
					final Editable text=editText.getText();
					final int start=editText.getSelectionStart();
					final int end=editText.getSelectionEnd();
					final boolean isFocused=editText.isFocused();
					final boolean hasSelection=editText.hasSelection();
					
					RecognitionEngine.this.start(config,
							new OnFinishListener() {

								@Override
								public void onFinish(ArrayList<RecognitionResult> results, int selectedIndex) {
									int _mode=mode;
									String result=results.get(selectedIndex).text;
									
									//没有焦点时，获取焦点，并且自动设置为追加模式。
									if(!isFocused){
										editText.requestFocus();
										_mode=2;
									}
									//如果有文本被选择，则切换为替换模式，被选择的文本将被替换成识别结果
									if(hasSelection){
										_mode=3;
									}
									//----									
									switch (_mode) {
									case 1:	// 插入模式									
										int oldLength=editText.getText().length();
										int newLength=oldLength + result.length();
										int realInsertedLength=result.length();
										text.insert(end, result);
										editText.setText(text);	
										//真正插入的字符数
										int newRealLength=editText.getText().length();
										if(newRealLength!=newLength){
											
											realInsertedLength=newRealLength-oldLength;
										}
										Log.e("XXXXX===", newLength+","+newRealLength);
										editText.setSelection(end + realInsertedLength);
										break;
										
									case 2:	// 追加模式

										editText.append(result);
										editText.setSelection(editText.getText().length());
										break;
										
									case 3:	// 替换模式

										text.replace(start>end?end:start, start>end?start:end, result);
										editText.setText(text);
										editText.setSelection((start>end?end:start)+result.length());
										break;
										
									default: // 设置模式
										
										editText.setText(result);
										editText.setSelection(editText.getText().length());
										

									}
									//回调
									if(callback!=null){
										callback.run();
									}

								}
							});
				} catch (Exception ex) {
					Log.e("VoiceRegnition", ex.getMessage(), ex);
				}

			}

		});

	}
	public void bindButtonAndEditText(Button fireButton,
			final EditText editText, final int mode, final Bundle config) {
		bindButtonAndEditText(fireButton, editText, mode,  config, null);
	}
	/**
	 * 这是一个辅助方法。把一个按钮和一个EditText控件不绑定到识别引擎实例上。
	 * 按钮用来触发识别，EditText用来接受识别结果。
	 * 该方法会重新设置引擎的OnFinishListener监听器。
	 * 
	 * @param fireButton 触发识别的按钮
	 * @param editText 放置识别结果的文本框
	 */
	public void bindButtonAndEditText(Button fireButton,EditText editText){
		
		bindButtonAndEditText(fireButton,editText,null);
	}
	/**
	 * 这是一个辅助方法。把一个按钮和一个EditText控件不绑定到识别引擎实例上。
	 * 按钮用来触发识别，EditText用来接受识别结果。
	 * 该方法会重新设置引擎的OnFinishListener监听器。
	 * 
	 * @param fireButton 触发识别的按钮
	 * @param editText 放置识别结果的文本框
	 * @param config 控制识别引擎行为的参数。见start方法。
	 */
	public void bindButtonAndEditText(Button fireButton,EditText editText,Bundle config){
		
		bindButtonAndEditText(fireButton,editText,1,null);
	}
}

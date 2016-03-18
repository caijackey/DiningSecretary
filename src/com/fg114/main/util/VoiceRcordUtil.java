package com.fg114.main.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.view.VoicePopupWindow;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;


public class VoiceRcordUtil
{
	private static final String TAG = VoiceRcordUtil.class.getSimpleName();
	public static final int MAX_RECORD = 10 * 60 * 60 * 1000; // 默认录音最长时间 10小时
	public static final int MIN_RECORD = 2 * 1000; // 最小录音时间控制
	private RecMicToMp3 mRecMicToMp3 = new RecMicToMp3("", 8000);
	private MediaPlayer mPlayer = null;
	private static final int SUCCES = 1;
	private static final int FAIL = 2;
	private File cacheDir;
	private VoicePopupWindow mVoicePopWindow; // 录音弹出辅助窗口
	private static final String VIOCE_CACHE_FILE = Environment.getExternalStorageDirectory() + "/" + Settings.IMAGE_CACHE_DIRECTORY + "/" + Settings.VOICE_CACHE;
	private String mRecordFileName = ""; // 当前正在录制的音频文件保存名 生成当前时间格式
	private String mRecordFilePath = ""; // 当前正在录制的音频文件的路径
	private String mPlayingFilePath = ""; // 当前正在播放音频文件的路径
	private long startTime = 0; // 录音开始时间戳；
	private long endTime = 0; // 录音结束时间戳；
	private Context mCtx;
	private boolean isCancelFocuse = false;
	private onMediaPlayerListener onMediaPlayerListener;
	/** 录音结束回调接口 */
	public interface VoiceRcordListener
	{
		public void onFinish(String filePath);
		public void onCancel();
	}

	public VoiceRcordUtil(Context context)
	{
		// 判断存储卡是否存在
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			// 在sd卡上建立图片存放空间
			cacheDir = new File(VIOCE_CACHE_FILE);
		} else {
			// 如没有存储卡，则在私有存储路径中开辟空间
			cacheDir = ContextUtil.getContext().getCacheDir();
		}
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		mCtx = context;
		mVoicePopWindow = new VoicePopupWindow(context, null);

	}

	/**
	 * 
	 * 
	 * 开始录音
	 * 
	 */
	private void startRecording()
	{

		mRecordFileName = toDateString(Calendar.getInstance().getTimeInMillis(), "yyyyMMddHHmmss");
		mRecordFilePath = cacheDir.getAbsolutePath() + "/" + mRecordFileName + ".mp3";
		mRecMicToMp3.setmFilePath(mRecordFilePath);
		int mFlag = onRecord();
		if (mFlag != SUCCES) {
			DialogUtil.showToast(ContextUtil.getContext(), "失败!取消录音");
			stopRecording();
		}

	}
	private int onRecord()
	{
		try {
			mRecMicToMp3.start();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return FAIL;
		}
		return SUCCES;

	}
	private void stopRecording()
	{
		try {
			mRecMicToMp3.stop();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
	
	private void startPlaying(String pathUrl)
	{
		try {
			if (mPlayer == null) {
				mPlayer = new MediaPlayer();
				mPlayer.setOnCompletionListener(new OnCompletionListener()
				{

					@Override
					public void onCompletion(MediaPlayer mp)
					{
						if (onMediaPlayerListener != null) {
							onMediaPlayerListener.onCompled();
						}

					}
				});
				mPlayer.setOnErrorListener(new OnErrorListener()
				{

					@Override
					public boolean onError(MediaPlayer mp, int what, int extra)
					{
						if (onMediaPlayerListener != null) {
							onMediaPlayerListener.onError();
						}
						return false;
					}
				});
			}
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			mPlayer.reset();
			mPlayer.setDataSource(pathUrl);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IOException e) {
			Log.e(TAG, "prepare() failed" + e.getMessage());
		}
	}
	/**
	 * 播放录音
	 * 
	 * @param pathUrl
	 *            当前需要播放的ID
	 * 
	 */
	public void playRecord(String pathUrl)
	{
		mPlayingFilePath = pathUrl;
		startPlaying(pathUrl);
	}
	public void stopPlaying()
	{
		if (mPlayer != null) {
			mPlayer.stop();

		}

	}
	/**
	 * 判断当前播放器是否在播放
	 */
	public boolean isPlaying()
	{
		try {
			if (mPlayer != null) {
				return mPlayer.isPlaying();
			}
		} catch (Exception e) {
			return false;
		}
		return false;

	}
	public boolean isRecording()
	{
		return mRecMicToMp3.isRecording();

	}
	/**
	 * 外部录音组件绑定
	 * 
	 * @param btVoice
	 *            录音按钮
	 * @param listener
	 *            录音结束回调方法
	 */
	public void bindVoiceButton(Button btVoice, final VoiceRcordListener listener)
	{
		try {
			btVoice.setOnTouchListener(new OnTouchListener()
			{

				@Override
				public boolean onTouch(View v, MotionEvent event)
				{
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						mVoicePopWindow.showPopupWindow();
						startRecording();
						// 记录开始时间戳
						startTime = Calendar.getInstance().getTimeInMillis();

					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						// 记录结束时间戳
						endTime = Calendar.getInstance().getTimeInMillis();
						if (mVoicePopWindow.isPopShowing() && mVoicePopWindow.getmFlag() == Settings.VOICE_CONTENT_STYLE_02) {

							if (mVoicePopWindow.isContains((int) event.getRawX(), (int) event.getRawY())) {
								mVoicePopWindow.disMissPop();
								stopRecording();// 停止录音
								if (listener != null) {
									listener.onCancel();
								}
								deleteFile(mRecordFilePath);
							} else {

								mVoicePopWindow.disMissPop();
								stopRecording();// 停止录音
								checkRecordTime(startTime, endTime,listener);
							}

						} else {
							mVoicePopWindow.disMissPop();
							stopRecording();// 停止录音
							checkRecordTime(startTime, endTime,listener);
							

						}

					} else if (event.getAction() == MotionEvent.ACTION_MOVE) {

						if (event.getY() < 0)// 当移动到按钮以外的时候
						{
							if (mVoicePopWindow.isPopShowing()) {

								mVoicePopWindow.switchContentView(Settings.VOICE_CONTENT_STYLE_02);

							}

						} else {
							if (mVoicePopWindow.isPopShowing()) {
								mVoicePopWindow.switchContentView(Settings.VOICE_CONTENT_STYLE_01);
							}

						}
						if (mVoicePopWindow.isContains((int) event.getRawX(), (int) event.getRawY())) {

							if (!isCancelFocuse) {
								mVoicePopWindow.getmCancel().setBackgroundResource(R.drawable.voice_rcd_cancel_bg_focused);
								mVoicePopWindow.getmCancelTv().setText("取消发送");
								isCancelFocuse = true;
							}

						} else {

							if (isCancelFocuse) {
								mVoicePopWindow.getmCancel().setBackgroundResource(R.drawable.voice_rcd_cancel_bg);
								mVoicePopWindow.getmCancelTv().setText("移到这里取消");
								isCancelFocuse = false;
							}

						}
						return true;
					}
					return false;
				}
			});
		} catch (Exception e) {
			Log.e(TAG, "VoiceRcordUtil onCancel: ");
		}

	}
	/**
	 * 毫秒时间转字符串(默认时区)
	 * 
	 * @param milliseconds
	 * @param pattern
	 */
	public static String toDateString(long milliseconds, String pattern)
	{
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			sdf.setTimeZone(TimeZone.getDefault());
			return sdf.format(milliseconds);
		} catch (Exception e) {
			return "";
		}
	}
	/**
	 * 检查录音的合法时间操作
	 * 
	 * @param start
	 *            开始时间
	 * @param end
	 *            结束时间
	 * @param listener
	 *            返回监听
	 */
	public void checkRecordTime(long start, long end,final VoiceRcordListener listener)
	{
		if (end - start > MIN_RECORD && end - start < MAX_RECORD) {
			mRecMicToMp3.setHandle(new Handler(){

				@Override
				public void handleMessage(Message msg)
				{
					int what=msg.what;
					super.handleMessage(msg);
					switch (what) {
					    //转换MP3格式失败
						case RecMicToMp3.MSG_ERROR_AUDIO_ENCODE :
							showError();
							break;
						//录音失败
						case RecMicToMp3.MSG_ERROR_AUDIO_RECORD:
							showError();
							break;
					    //录音写入文件失败
						case RecMicToMp3.MSG_ERROR_WRITE_FILE:
							showError();
							break;
						//关闭录音文件流失败
						case RecMicToMp3.MSG_ERROR_CLOSE_FILE:
							showError();
							break;
						//正常结束录音,回调接口方法
						case RecMicToMp3.MSG_REC_STOPPED:
							// 在规定范围内，认为一次合格录音
							if(listener!=null)
							{
								listener.onFinish(mRecordFilePath);
							}
							break;
							
					}
					mRecMicToMp3.setHandle(null);
				}
				
				
				
				
			});
			
		} else if (end - start < MIN_RECORD) {
			DialogUtil.showAlert(mCtx, "", "录音时间过短，请重试");
			deleteFile(mRecordFilePath);
		} else {
			DialogUtil.showAlert(mCtx, "", "已超过最大录音时间");
			deleteFile(mRecordFilePath);
		}

	}
	// 删除不成功的音频录制文件
	public void deleteFile(String pathUrl)
	{
		File file = new File(pathUrl);
		if (file.exists()) { // 判断文件是否存在
			if (file.isFile()) { // 判断是否是文件
				file.delete();
			} else if (file.isDirectory()) { // 否则如果它是一个目录
				File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
				for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
					this.deleteFile(files[i].getAbsolutePath()); // 把每个文件
																	// 用这个方法进行迭代
				}
			}
			file.delete();
		}

	}

	public synchronized final onMediaPlayerListener getOnMediaPlayerListener()
	{
		return onMediaPlayerListener;
	}

	public synchronized final void setOnMediaPlayerListener(onMediaPlayerListener onMediaPlayerListener)
	{
		this.onMediaPlayerListener = onMediaPlayerListener;
	}
	/**
	 * 当前正在播放音频文件路径
	 */
	public synchronized final String getmPlayingFilePath()
	{
		return mPlayingFilePath;
	}

	public synchronized final void setmPlayingFilePath(String mPlayingFilePath)
	{
		this.mPlayingFilePath = mPlayingFilePath;
	}

	public interface onMediaPlayerListener
	{

		public void onCompled();
		public void onError();
	}
	/**
	 * 释放当前播放器
	 */
	public void Release()
	{
		try {
			if (mPlayer != null) {
				mPlayer.stop();
				mPlayer.release();
			}
			if (mRecMicToMp3 != null) {
				mRecMicToMp3.stop();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
	public void showError()
	{
		DialogUtil.showToast(mCtx, "录音失败,请重试!");
	}
}

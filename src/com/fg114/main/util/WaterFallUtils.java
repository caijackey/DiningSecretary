package com.fg114.main.util;

import java.util.concurrent.atomic.AtomicInteger;

import com.fg114.main.app.view.WaterFallBaseView;
import com.fg114.main.app.view.WaterFallScrollView;
import com.fg114.main.app.view.WaterFallView;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class WaterFallUtils {
	private WaterFallScrollView fallScrollView;
	private int scrollHeight;
	private AtomicInteger tempT = new AtomicInteger();
	private static int OFFSET = 50;

	public int getScrollHeight(ScrollView scroll) {
		return scroll.getMeasuredHeight();
	}

	public WaterFallUtils(WaterFallScrollView waterFallView) {
		this.fallScrollView = waterFallView;

	}

	public void autoReload(int l, int t, int oldl, int oldt) {

		tempT.set(t);
		scrollHeight = fallScrollView.getMeasuredHeight();
		// Log.e( "bug", "scroll_height:"+scrollHeight);
		// Log.e( "bug", l + "," + t+"-----" + oldl + "," + oldt);
		if (t > oldt) {// 向下滚动
			scrollDown(l, t, oldl, oldt);
		} else if (t < oldt) {// 向上滚动
			scrolldUp(l, t, oldl, oldt);
		}
	}

	private void scrolldUp(int l, int t, int oldl, int oldt) {

		new Thread(new UpdateRun(t)).start();
	}

	//
	private void scrollDown(int l, int t, int oldl, int oldt) {

		new Thread(new UpdateRun(t)).start();
	}

	public class UpdateRun implements Runnable {
		int mT;

		public UpdateRun(int t) {
			mT = t;
		}

		public void run() {
			// SystemClock.sleep(200);
			if (tempT.get() == mT) {
				for (int k = 0; k < fallScrollView.columnCount; k++) {
					for (int i = 0; i < fallScrollView.waterfall_items.get(k).getChildCount(); i++) {
						if (tempT.get() == mT) {
							WaterFallBaseView eachView = (WaterFallBaseView) fallScrollView.waterfall_items.get(k).getChildAt(i);
							if (eachView != null) {
								if (eachView.getTop() >= mT - OFFSET) {
									if (eachView.getBottom() <= mT + scrollHeight + OFFSET) {

										Message m = hand.obtainMessage(0, eachView);
										hand.sendMessage(m);
										// Log.e("bug","top:"+eachView.getTop()+"bottom:"+eachView.getBottom()+"tempT:"+tempT+scrollHeight+"mT:"+mT);
									} else {
										if (eachView.getTop() <= mT + scrollHeight + OFFSET) {
											Message m = hand.obtainMessage(0, eachView);
											hand.sendMessage(m);
											// Log.e("bug","top:"+eachView.getTop()+"bottom:"+eachView.getBottom()+"tempT:"+tempT+scrollHeight+"mT:"+mT);
										} else {
											Message m = hand.obtainMessage(1, eachView);
											hand.sendMessage(m);
										}

									}

								} else {
									if (eachView.getBottom() >= mT) {
										Message m = hand.obtainMessage(0, eachView);
										hand.sendMessage(m);
										// Log.e("bug","top:"+eachView.getTop()+"bottom:"+eachView.getBottom()+"tempT:"+tempT+scrollHeight+"mT:"+mT);
									} else {
										Message m = hand.obtainMessage(1, eachView);
										hand.sendMessage(m);
									}

								}

							}
						} else {
							return;
						}

					}
				}
			}
		}
	};

	private Handler hand = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				((WaterFallBaseView) msg.obj).ReloadViewData();
			} else {
				((WaterFallBaseView) msg.obj).recycle();
			}

			super.handleMessage(msg);
		}

	};

}

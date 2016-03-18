package com.fg114.main.app.view;

import java.util.concurrent.atomic.AtomicBoolean;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.IndexActivity;
import com.fg114.main.app.activity.resandfood.RestaurantPicActivity;
import com.fg114.main.service.dto.RestPicData;
import com.fg114.main.util.ViewUtils;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;

/**
 * 瀑布流 内容控件的基础父类
 * 
 * @author liwenjie
 * 
 */
public abstract class WaterFallBaseView extends LinearLayout {

	public abstract void LoadViewData();

	@SuppressWarnings("hiding")
	public interface onWaterFallClickListener<RestPicData> {
		void onItemClick(RestPicData dto);

	}

	private Context mctx;
	private LayoutInflater mInflater;
	private int columnIndex; // 此View属于第几列
	private int rowIndex; // 此View属于第几行
	private RestPicData dto; // view的数据源
	private int ItemWidth; // 此view必须满足的宽度
	private int ItemHeight;// 子view的高度
	private Handler viewHandler;
	private View contentView;
	private WaterFallScrollView waterFall_Scroll;
	private AtomicBoolean isRemoveContentView = new AtomicBoolean(false);// 判断contentView是否已经移除

	public WaterFallBaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mctx = context;
		init();

	}

	public WaterFallBaseView(Context context) {
		super(context);
		this.mctx = context;
		init();

	}

	public void init() {
		mInflater = LayoutInflater.from(mctx);
		setBackgroundResource(R.drawable.food_style_block);
		setOrientation(LinearLayout.VERTICAL);
	}

	protected int getContentResId() {
		return R.layout.flow_base_view;
	}

	public void initBaseView(int row, RestPicData dto, int ItemWidth, Handler viewHandler, boolean callHandler) {
		this.rowIndex = row;
		this.dto = dto;
		this.ItemWidth = ItemWidth;
		this.viewHandler = viewHandler;
		if (contentView != null) {
			if (contentView.getParent() != null) {
				// Log.e("bug",
				// contentView.getParent()+"  mmmmmmmmmmmmmmmmmmmmmmm, View: " +
				// contentView);
				// contentView=null;
				ReloadViewData();
				//Log.e("--------return------","++++++++++return++++++++++");
				return;
			}

		}
		Log.e("--------------","++++++++++++++++++++");
		if (contentView == null) {
			contentView = mInflater.inflate(getContentResId(), null);
			// Log.e("bug", "initBaseView:" + contentView + ",columnIndex=" +
			// columnIndex + ",row=" + row);
		}
		contentView.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				if (getTag() != null) {
					((onWaterFallClickListener<RestPicData>) getTag()).onItemClick(getDto());

				}

			}
		});

		LoadViewData();
		addView(contentView);
		isRemoveContentView.set(false);
		if (callHandler) {
			Handler hand = getViewHandler();

			Message m = hand.obtainMessage(1, ItemWidth, ItemHeight, this);
			hand.sendMessage(m);
		}
		Log.e("------#####--------","++++++++++++++++++++");
	}

	public synchronized final int getItemHeight() {
		return ItemHeight;
	}

	public synchronized final void setItemHeight(int itemHeight) {
		ItemHeight = itemHeight;
	}

	/**
	 * 重新加载view显示信息
	 */
	public void ReloadViewData() {
		if (isRemoveContentView.get()) {
			if (!waterFall_Scroll.RecyleList.isEmpty()) {
				View view = waterFall_Scroll.RecyleList.get(0);
				setContentView(view);
				waterFall_Scroll.RecyleList.remove(0);
				// Log.e("bug", "ReloadViewData:" + "list size:"
				// +waterFall_Scroll.RecyleList.size() + " ,columnIndex=" +
				// columnIndex + ",row=" + rowIndex + ",view= " + view);
			}
			initBaseView(rowIndex, dto, ItemWidth, viewHandler, false);
		}
	}

	/**
	 * 回收内存
	 */
	public void recycle() {
		if (isRemoveContentView.get()) {
			return;
		}
		isRemoveContentView.set(true);
		waterFall_Scroll.RecyleList.add(contentView);
		// Log.e("bug", "n:" + n + ", removeAllViews:" + contentView +
		// ",columnIndex=" + columnIndex + ",row=" + rowIndex +
		// ", RecyleList.size():"+((RestaurantPicActivity)getContext()).RecyleList.size());
		removeAllViews();
		contentView = null;

	}

	public static class FlowViewHandler extends Handler {
		private WaterFallScrollView fallScrollView;

		public FlowViewHandler(WaterFallScrollView sv) {
			this.fallScrollView = sv;
		}

		private int GetMinValue(int[] array) {
			int m = 0;
			int length = array.length;
			for (int i = 0; i < length; ++i) {

				if (array[i] < array[m]) {
					m = i;
				}
			}
			return m;
		}

		@Override
		public void handleMessage(Message msg) {

			// super.handleMessage(msg);

			switch (msg.what) {
				case 1:

					WaterFallBaseView v = (WaterFallBaseView) msg.obj;
					// WaterFallView.Debug("width->"+msg.arg1);
					int h = msg.arg2;
					int w = msg.arg1;
					// 此处计算列值
					int columnIndex = GetMinValue(fallScrollView.column_height);
					v.setColumnIndex(columnIndex);
					fallScrollView.column_height[columnIndex] += h;
					fallScrollView.pins.put(v.getId(), "1");
					fallScrollView.waterfall_items.get(columnIndex).addView(v);
					fallScrollView.lineIndex[columnIndex]++;

					fallScrollView.pin_mark[columnIndex].put(fallScrollView.lineIndex[columnIndex], fallScrollView.column_height[columnIndex]);
					fallScrollView.bottomIndex[columnIndex] = fallScrollView.lineIndex[columnIndex];
					//Log.e("------add--------","++++++++++++++++++++");
					break;
			}

		}
	}

	public synchronized final WaterFallScrollView getWaterFall_Scroll() {
		return waterFall_Scroll;
	}

	public synchronized final void setWaterFall_Scroll(WaterFallScrollView waterFall_Scroll) {
		this.waterFall_Scroll = waterFall_Scroll;
	}

	public synchronized final int getColumnIndex() {
		return columnIndex;
	}

	public synchronized final void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	public synchronized final int getRowIndex() {
		return rowIndex;
	}

	public synchronized final void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public synchronized final RestPicData getDto() {
		return dto;
	}

	public synchronized final void setDto(RestPicData dto) {
		this.dto = dto;
	}

	public synchronized final int getItemWidth() {
		return ItemWidth;
	}

	public synchronized final void setItemWidth(int itemWidth) {
		ItemWidth = itemWidth;
	}

	public synchronized final Handler getViewHandler() {
		return viewHandler;
	}

	public synchronized final void setViewHandler(Handler viewHandler) {
		this.viewHandler = viewHandler;
	}

	public synchronized final View getContentView() {
		return contentView;
	}

	public synchronized final void setContentView(View contentView) {
		this.contentView = contentView;
	}


}

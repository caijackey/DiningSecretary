package com.fg114.main.app.adapter;

import java.util.*;

import com.fg114.main.util.*;

import android.content.*;

public class ArrayPairWheelAdapter<Value> extends AbstractWheelTextAdapter {

	private List<MyPair<String, Value>> mList = new ArrayList<MyPair<String, Value>>();
	
	private int maxTextLength;
	
	public ArrayPairWheelAdapter(Context context, List<MyPair<String, Value>> list) {
		super(context);
		mList = list;
	}

	@Override
	public CharSequence getItemText(int index) {
		if (index < 0 || index >= mList.size()) {
			return "";
		}
		String text = getText(index);
		if (text == null) {
			return "";
		}
		if (text.length() > maxTextLength) {
			text = ConvertUtil.subString(text, 0, maxTextLength) + "...";
		}
		return text;
	}

	@Override
	public int getItemsCount() {
		return mList.size();
	}
	
	public String getText(int index) {
		if (index < 0 || index >= mList.size()) {
			return "";
		}
		return mList.get(index).first;
	}

	public Value getValue(int index) {
		if (index < 0 || index >= mList.size()) {
			return null;
		}
		return mList.get(index).second;
	}

	public int getMaxTextLength() {
		return maxTextLength;
	}

	public void setMaxTextLength(int maxTextLength) {
		this.maxTextLength = maxTextLength;
	}

	@Override
	public int getIndexByValue(long value) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getValueByIndex(int index) {
		// TODO Auto-generated method stub
		return 0;
	}
}

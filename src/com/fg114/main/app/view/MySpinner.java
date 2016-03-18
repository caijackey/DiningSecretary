package com.fg114.main.app.view;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.AlertDialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

/**
 * 
 * @author zhangyifan
 *
 */
public class MySpinner extends Spinner {
	//是否已选择过
	public boolean selected;

	public MySpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		selected = false;
	}
	
	public MySpinner(Context context) {
		super(context);
		selected = false;
	}
	
	public MySpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		selected = false;
	}

	@Override
	public void setAdapter(SpinnerAdapter adapter) {
        super.setAdapter(adapter);
        //将初始位置 设为-1
        try {
            final Method m = AdapterView.class.getDeclaredMethod("setNextSelectedPositionInt",int.class);
            m.setAccessible(true);
            m.invoke(this, -1);

            final Method n = AdapterView.class.getDeclaredMethod("setSelectedPositionInt",int.class);
            n.setAccessible(true);
            n.invoke(this, -1);
        } catch( Exception e ) {
            e.printStackTrace();
        }
	}

	@Override
	public boolean performClick() {
		boolean handled = true;
		if (selected) {
			//已选择过的场合，显示已有选择的列表弹出
			handled = super.performClick();
		} else {
			try {
		        CharSequence mPrompt = this.getPrompt();
		        Field field = Spinner.class.getDeclaredField("mPopup");
		        field.setAccessible(true);
		        AlertDialog mPopup;
	        	Context context = getContext();
	        	
	        	//
	        	SpinnerAdapter adapter = new DropDownAdapter(getAdapter());
	        	//
	        	Class[] cls=Spinner.class.getDeclaredClasses();
	        	int index=-1;
	        	for(int i=0;cls!=null&&i<cls.length;i++){
	        		if(cls[i].getName().contains("DropDownAdapter")){
	        			index=i;
	        			break;
	        		}
	        	}
	        	if(index!=-1){
	        		Constructor con=cls[index].getConstructor(SpinnerAdapter.class);
	        		con.setAccessible(true);
	        		adapter=(SpinnerAdapter)con.newInstance(getAdapter());
	        	}
	        	
	        	
	        	AlertDialog.Builder builder = new AlertDialog.Builder(context);
	        	if (mPrompt != null) {
	        		builder.setTitle(mPrompt);
	        	}
	        	//创建没有选择项的列表弹出框
	        	mPopup = builder.setSingleChoiceItems((ListAdapter)adapter, -1, this).show();
	        	field.set(this, mPopup);
			} catch (Exception e) {
				handled = false;
			}
		}
        return handled;
	}
	
	private static class DropDownAdapter implements ListAdapter, SpinnerAdapter {

		private SpinnerAdapter mAdapter;
		private ListAdapter mListAdapter;

		public DropDownAdapter(SpinnerAdapter adapter) {
			this.mAdapter = adapter;
			if (adapter instanceof ListAdapter) {
				this.mListAdapter = (ListAdapter) adapter;
			}
		}
		
		public int getCount() {
			return mAdapter == null ? 0 : mAdapter.getCount();
		}

		public Object getItem(int position) {
			return mAdapter == null ? null : mAdapter.getItem(position);
		}
		
		public long getItemId(int position) {
			return mAdapter == null ? -1 : mAdapter.getItemId(position);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			return getDropDownView(position, convertView, parent);
		}

		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return mAdapter == null ? null : mAdapter.getDropDownView(position, convertView, parent);
		}

		public boolean hasStableIds() {
			return mAdapter != null && mAdapter.hasStableIds();
		}

		public void registerDataSetObserver(DataSetObserver observer) {
			if (mAdapter != null) {
				mAdapter.registerDataSetObserver(observer);
			}
		}

		public void unregisterDataSetObserver(DataSetObserver observer) {
			if (mAdapter != null) {
				mAdapter.unregisterDataSetObserver(observer);
			}
		}
		
		public boolean areAllItemsEnabled() {
			final ListAdapter adapter = mListAdapter;
			if (adapter != null) {
				return adapter.areAllItemsEnabled();
			} else {
				return true;
			}
		}

		public boolean isEnabled(int position) {
			final ListAdapter adapter = mListAdapter;
			if (adapter != null) {
				return adapter.isEnabled(position);
			} else {
				return true;
			}
		}

		public int getItemViewType(int position) {
			return 0;
		}

		public int getViewTypeCount() {
			return 1;
		}

		public boolean isEmpty() {
			return getCount() == 0;
		}
	}
}

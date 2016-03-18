package com.fg114.main.app.adapter.common;

import com.fg114.main.app.view.MyImageView;

import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 * 担当ListViewAdapter里面的ViewHolder角色
 * @see com.fg114.main.app.adapter.common.ListViewAdapter
 * @author nieyinyin
 * @since 2013-07-01
 */
public class ViewHolder {
	private SparseArray<View> viewMap = new SparseArray<View>();
	
	public ViewHolder() {
		super();
	}
	
	public ViewHolder(SparseArray<View> viewMap) {
		super();
		this.viewMap = viewMap;
	}

	public SparseArray<View> getViewMap() {
		return viewMap;
	}

	public void setViewMap(SparseArray<View> viewMap) {
		this.viewMap = viewMap;
	}

	/**
	 * 通过资源Id获取相对应的View
	 * 找不到，返回Null
	 */
	public View $(int resourceId){
		return viewMap.get(resourceId);
	}

	/**
	 * 通过资源Id获取相对应的TextView
	 * 找不到，返回Null
	 */
	public TextView $tv(int resourceId) {
		return (TextView) viewMap.get(resourceId);
	}

	/**
	 * 通过资源Id获取相对应的Button
	 * 找不到，返回Null
	 */
	public Button $btn(int resourceId) {
		return (Button) viewMap.get(resourceId);
	}

	/**
	 * 通过资源Id获取相对应的ImageView
	 * 找不到，返回Null
	 */
	public ImageView $iv(int resourceId){
		return (ImageView) viewMap.get(resourceId);
	}

	/**
	 * 通过资源Id获取相对应的EditText
	 * 找不到，返回Null
	 */
	public EditText $et(int resourceId){
		return (EditText) viewMap.get(resourceId);
	}
	
	public MyImageView $myIv(int resourceId){
		return (MyImageView)viewMap.get(resourceId);
	}
	
}

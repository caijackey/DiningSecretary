package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fg114.main.R;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.util.HanziUtil;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class ContactAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private Context mContext;
	private List<CommonTypeDTO> mList = new ArrayList<CommonTypeDTO>();
	private boolean mIsShowPhoto = true;
	private List<CommonTypeDTO> mSelectedList = new ArrayList<CommonTypeDTO>();

	private class ViewHolder {
		TextView tvAlpha;
		TextView tvName;
		TextView tvNumber;
		ImageView ivPhoto;
		CheckBox cbSelect;
	}
	
	public ContactAdapter(Context context, boolean isShowPhoto) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mIsShowPhoto = isShowPhoto;
	}

	public ContactAdapter(Context context, boolean isShowPhoto, List<CommonTypeDTO> list) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mList = list;
		mIsShowPhoto = isShowPhoto;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.contact_list_item, null);
			holder = new ViewHolder();
			holder.tvAlpha = (TextView) convertView.findViewById(R.id.contract_list_item_tvAlpha);
			holder.tvName = (TextView) convertView.findViewById(R.id.contract_list_item_tvName);
			holder.tvNumber = (TextView) convertView.findViewById(R.id.contract_list_item_tvNumber);
			holder.ivPhoto = (ImageView) convertView.findViewById(R.id.contract_list_item_ivPhoto);
			holder.cbSelect = (CheckBox) convertView.findViewById(R.id.contract_list_item_cbSelect);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
			
//		if (convertView == null) {
//			convertView = mInflater.inflate(R.layout.contact_list_item, null);
//			holder = new ViewHolder();
//			holder.tvAlpha = (TextView) convertView.findViewById(R.id.contract_list_item_tvAlpha);
//			holder.tvName = (TextView) convertView.findViewById(R.id.contract_list_item_tvName);
//			holder.tvNumber = (TextView) convertView.findViewById(R.id.contract_list_item_tvNumber);
//			holder.ivPhoto = (ImageView) convertView.findViewById(R.id.contract_list_item_ivPhoto);
//			holder.cbSelect = (CheckBox) convertView.findViewById(R.id.contract_list_item_cbSelect);
//			convertView.setTag(holder);
//		} else {
//			holder = (ViewHolder) convertView.getTag();
//		}
		CommonTypeDTO cv = mList.get(position);
		holder.tvName.setText(cv.getName());
		holder.tvNumber.setText(cv.getPhone());
//		String currentStr = HanziToPinyinUtil.getAlpha(mList.get(position).getName());
		String currentStr = HanziUtil.getFirst(mList.get(position).getUuid());
//		String previewStr = (position - 1) >= 0 ? HanziToPinyinUtil.getAlpha(mList.get(position - 1).getName()) : " ";
		String previewStr = (position - 1) >= 0 ? HanziUtil.getFirst(mList.get(position - 1).getUuid()) : " ";
		if (!previewStr.equals(currentStr)) {
			holder.tvAlpha.setVisibility(View.VISIBLE);
			holder.tvAlpha.setText(currentStr);
		} else {
			holder.tvAlpha.setVisibility(View.GONE);
		}
//		if (mIsShowPhoto) {
//			String id = cv.getAsString(Phones.PERSON_ID);
//			Uri uri = ContentUris.withAppendedId(People.CONTENT_URI, Long.parseLong(id));
//			Bitmap bitmap = People.loadContactPhoto(mContext, uri, R.drawable.icon, null);
//			holder.ivPhoto.setImageBitmap(bitmap);
//			holder.ivPhoto.setVisibility(View.VISIBLE);
//		}
		if (!mSelectedList.isEmpty()) {
			if (mSelectedList.contains(cv)) {
				holder.cbSelect.setChecked(true);
			}
			else {
				holder.cbSelect.setChecked(false);
			}
		}
		return convertView;
	}
	
	public void setList(List<CommonTypeDTO> list) {
		mList = list;
		notifyDataSetChanged();
	}
	
	public void selectItem(int position) {
		if (position > -1 && position < mList.size()) {
			mSelectedList.add(mList.get(position));
		}
	}
	
	public void unselectItem(int position) {
		if (position > -1 && position < mList.size()) {
			CommonTypeDTO cv = mList.get(position);
			if (mSelectedList.contains(cv)) {
				mSelectedList.remove(cv);
			}
		}
	}
	
	public List<CommonTypeDTO> getSelectedList() {
		return mSelectedList;
	}
}

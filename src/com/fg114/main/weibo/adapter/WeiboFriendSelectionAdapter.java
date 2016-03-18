package com.fg114.main.weibo.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.util.HanziUtil;
import com.fg114.main.weibo.dto.User;

/**
 * 新浪微博好友列表适配器
 * @author xujianjun
 *
 */
public class WeiboFriendSelectionAdapter extends BaseAdapter {

	private Context context;
	private List<User> list=new ArrayList<User>();

	private class ViewHolder {
		TextView letterTitle;
		TextView userName;
		MyImageView userPic;
	}
	public WeiboFriendSelectionAdapter(Context context) {
		this.context = context;		
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.w("WWW",position+"");
		ViewHolder holder;
		if (convertView == null) {
			convertView = View.inflate(this.context,R.layout.list_item_at_sina_weibo_select, null);
			holder = new ViewHolder();
			holder.letterTitle = (TextView) convertView.findViewById(R.id.letter_title);
			holder.userName = (TextView) convertView.findViewById(R.id.list_item_at_sina_weibo_user_name);
			holder.userPic = (MyImageView) convertView.findViewById(R.id.user_pic);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		//设置数据
		User user=(User)getItem(position);
		if(user.isRecentPerson()){ //如果是最近联系人，添加特殊的标题
			holder.letterTitle.setText("最近联系人");
			holder.userName.setText(user.getName());
		}else{
			holder.letterTitle.setText(HanziUtil.getFirst(user.getFirstLetter()).toUpperCase());
			holder.userName.setText(user.getName());
			holder.userPic.setImageByUrl(user.getPicUrl(), true, position, ScaleType.FIT_XY);
		}
		
		if (isFirstItemInThisGroup(position)) {
			holder.letterTitle.setVisibility(View.VISIBLE);
		} else {
			holder.letterTitle.setVisibility(View.GONE);
		}

		return convertView;
	}
	
	public void setList(List<User> list) {
		if(list==null){
			list=new ArrayList<User>();
		}
		this.list=list;
		notifyDataSetChanged();
	}
	public List<User> getList() {
		if(this.list==null){
			this.list=new ArrayList<User>();
		}
		return this.list;		
	}
	//判断当前位置是否是分组中的第一个
	private boolean isFirstItemInThisGroup(int position){
		try{
			if(position<=0||position>list.size()){
				return true;
			}
			if(((User)this.getItem(position)).isRecentPerson()){ //如果是最近联系人
				return false;
			}else if(!((User)this.getItem(position-1)).isRecentPerson()){
				return !HanziUtil.getFirst(
						((User)this.getItem(position)).getFirstLetter().toUpperCase()
						).equals(
						HanziUtil.getFirst(((User)this.getItem(position-1)).getFirstLetter().toUpperCase()));
			}
			else{
				return true;
			}
		}
		catch(Exception ex){
			return false;
		}
	}
	
}

/**
 * 
 * 此组件适合非listviev里面添加comment
 * 原因是，餐厅详情页合并成左右滑动的页面后，左边底部点击添加评论，键盘出来后整个餐厅详情页面moveup的速度很慢
 * 
 * 请注意，此组件跟CommentAdapter里面部分内容重复， CommentAdapter适合listview里面添加comment
 */
package com.fg114.main.app.view;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.provider.UserDictionary.Words;
import android.telephony.SmsMessage.SubmitPdu;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.RestaurantCommentDetailActivity;
import com.fg114.main.app.activity.resandfood.RestaurantGalleryActivity;
import com.fg114.main.app.activity.resandfood.RestaurantPicActivity;
import com.fg114.main.service.dto.CommentData;
import com.fg114.main.service.dto.CommentPicData;
import com.fg114.main.service.dto.TakeoutCerData;
import com.fg114.main.service.dto.TakeoutInfoData2;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ConvertUtil;

public class NewTakeawayCerDataItem extends LinearLayout {
	private Context context;

	private String restaurantId; // 餐厅ID，跳转到评论回复时需要传递到下一页
	
	public NewTakeawayCerDataItem (Context context, TakeoutInfoData2 data, String resId) {
		super(context);
		this.context=context;
		restaurantId=resId;
		LinearLayout.inflate(context, R.layout.list_item_new_takeaway_comment, this);
		if (data != null)
			setCommentData(data);
	}

	public void setCommentData(final TakeoutInfoData2 data) {
		LinearLayout infoLayout = (LinearLayout) this.findViewById(R.id.list_item_newtakeaway_infoLayout);
		LinearLayout msgLayout = (LinearLayout) this.findViewById(R.id.list_item_newtakeaway_msgLayout);
		
		ProgressBar pbBar = (ProgressBar) this.findViewById(R.id.list_item_newtakeaway_pBar);
		CerImageHorizontalScrollView imageHorizontalScrollView = (CerImageHorizontalScrollView) this.findViewById(R.id.list_item_newtakeaway_imageScrollView);

		
		// --
		
		imageHorizontalScrollView.setVisibility(View.GONE);			
		
			
		           // 添加底部菜品图片
		          List<TakeoutCerData> picDatas = data.cerList;
					//如果imagelistgroup里面有图片就不需要添加，防止添加多次
					if (picDatas.size() > 0 && imageHorizontalScrollView.getImageCount() == 0) {
						imageHorizontalScrollView.setVisibility(View.VISIBLE);
						if (picDatas.size() > 8) {
							picDatas = picDatas.subList(0, 8);
						}
						imageHorizontalScrollView.setImageData(picDatas);
						
					}
					
			
		}
}



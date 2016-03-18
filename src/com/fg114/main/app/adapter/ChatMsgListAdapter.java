package com.fg114.main.app.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.activity.resandfood.AddOrUpdateResActivity;
import com.fg114.main.app.view.MyImageView;
import com.fg114.main.service.dto.ChatMsgData;
import com.fg114.main.service.dto.ChatMsgSelectRest;
import com.fg114.main.service.dto.ChatMsgSelectTime;
import com.fg114.main.service.dto.ChatMsgText;
import com.fg114.main.service.dto.ChatMsgTitleText;
import com.fg114.main.service.dto.ChatMsgVoice;
import com.fg114.main.service.dto.RealTimeTableRestData;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.ViewUtils;

/**
 * 语音订餐消息列表
 * @author wufucheng
 */
public class ChatMsgListAdapter extends BaseAdapter {

	private static final String TAG = ChatMsgListAdapter.class.getSimpleName();
	private static final boolean DEBUG = Settings.DEBUG;

	private List<ChatMsgData> list = new ArrayList<ChatMsgData>();
	public Set<MyImageView> viewList = new HashSet<MyImageView>();

	private LayoutInflater mInflater = null;

	private MsgButtonListener mListener;

	public interface MsgButtonListener {
		public void onClickOperation1(View view, ChatMsgData chatMsgData);
		public void onClickOperation2(View view, ChatMsgData chatMsgData);
		public void onClickMsg(View view, ChatMsgData chatMsgData);
	}

	public ChatMsgListAdapter(Context c, MsgButtonListener listener) {
		super();
		mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mListener = listener;
	}

	@Override
	public int getCount() {
		if (list != null) {
			return list.size();
		} else {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class ViewHolder {
		ViewGroup vgServer;
		ViewGroup vgClient;
		MyImageView ivHeadServer;
		MyImageView ivHeadClient;
		ViewGroup vgMsgServer;
		ViewGroup vgMsgClient;
		TextView tvTitle;
		TextView tvText;
		TextView tvVoice;
		TextView tvTips;
		Button btOperation1;
		Button btOperation2;
		TextView tvTextClient;
		TextView tvVoiceClient;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (DEBUG)
			Log.d(TAG, "this position is :" + position + " this view is :" + convertView);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_chat_msg, null);
			holder.vgServer = (ViewGroup) convertView.findViewById(R.id.list_item_chat_msg_vgServer);
			holder.vgClient = (ViewGroup) convertView.findViewById(R.id.list_item_chat_msg_vgClient);
			holder.ivHeadServer = (MyImageView) convertView.findViewById(R.id.list_item_chat_msg_ivHeadServer);
			holder.ivHeadClient = (MyImageView) convertView.findViewById(R.id.list_item_chat_msg_ivHeadClient);
			holder.vgMsgServer = (ViewGroup) convertView.findViewById(R.id.list_item_chat_msg_vgMsgServer);
			holder.vgMsgClient = (ViewGroup) convertView.findViewById(R.id.list_item_chat_msg_vgMsgClient);
			holder.tvTitle = (TextView) convertView.findViewById(R.id.list_item_chat_msg_tvTitle);
			holder.tvText = (TextView) convertView.findViewById(R.id.list_item_chat_msg_tvText);
			holder.tvVoice = (TextView) convertView.findViewById(R.id.list_item_chat_msg_tvVoice);
			holder.tvTips = (TextView) convertView.findViewById(R.id.list_item_chat_msg_tvTips);
			holder.btOperation1 = (Button) convertView.findViewById(R.id.list_item_chat_msg_btOperation1);
			holder.btOperation2 = (Button) convertView.findViewById(R.id.list_item_chat_msg_btOperation2);
			holder.tvTextClient = (TextView) convertView.findViewById(R.id.list_item_chat_msg_tvTextClient);
			holder.tvVoiceClient = (TextView) convertView.findViewById(R.id.list_item_chat_msg_tvVoiceClient);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final ChatMsgData data = list.get(position);

		if (data.getSrc() == ChatMsgData.SRC_SERVER) {
			holder.vgServer.setVisibility(View.VISIBLE);
			holder.vgClient.setVisibility(View.GONE);
			
			holder.vgServer.setOnClickListener(null);
			
			holder.ivHeadServer.setImageByUrl(data.getPicUrl(), true, position, ScaleType.CENTER_CROP);
			
			holder.vgMsgServer.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					if (mListener != null) {
						mListener.onClickMsg(v, data);
					}
				}
			});
			
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.vgMsgServer.getLayoutParams();
			if (data.getDataTypeTag() == ChatMsgData.DATA_TYPE_SELECT_TIME
					|| data.getDataTypeTag() == ChatMsgData.DATA_TYPE_SELECT_REST) {
				params.width = RelativeLayout.LayoutParams.FILL_PARENT;
				holder.vgMsgServer.setLayoutParams(params);
			} else {
				params.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
				holder.vgMsgServer.setLayoutParams(params);
			}

			if (data.getDataTypeTag() == ChatMsgData.DATA_TYPE_TEXT) {
				// 简单文本
				holder.tvTitle.setVisibility(View.GONE);
				holder.tvText.setVisibility(View.VISIBLE);
				holder.tvVoice.setVisibility(View.GONE);
				holder.tvTips.setVisibility(View.GONE);
				holder.btOperation1.setVisibility(View.GONE);
				holder.btOperation2.setVisibility(View.GONE);

				try {
					holder.tvText.setText("");
					if (!TextUtils.isEmpty(data.getData())) {
						ChatMsgText text = JsonUtils.fromJson(data.getData(), ChatMsgText.class);
						holder.tvText.setText(text.getDetail());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (data.getDataTypeTag() == ChatMsgData.DATA_TYPE_TITLE_TEXT) {
				// 带标题的文本
				holder.tvTitle.setVisibility(View.VISIBLE);
				holder.tvText.setVisibility(View.VISIBLE);
				holder.tvVoice.setVisibility(View.GONE);
				holder.tvTips.setVisibility(View.GONE);
				holder.btOperation1.setVisibility(View.GONE);
				holder.btOperation2.setVisibility(View.GONE);

				try {
					holder.tvTitle.setText("");
					holder.tvText.setText("");
					if (!TextUtils.isEmpty(data.getData())) {
						ChatMsgTitleText text = JsonUtils.fromJson(data.getData(), ChatMsgTitleText.class);
						if (TextUtils.isEmpty(text.getTitle())) {
							holder.tvTitle.setVisibility(View.GONE);
						}
						holder.tvTitle.setText(text.getTitle());
						holder.tvText.setText(text.getDetail());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (data.getDataTypeTag() == ChatMsgData.DATA_TYPE_VOICE) {
				// 语音
				holder.tvTitle.setVisibility(View.GONE);
				holder.tvText.setVisibility(View.GONE);
				holder.tvVoice.setVisibility(View.VISIBLE);
				holder.tvTips.setVisibility(View.GONE);
				holder.btOperation1.setVisibility(View.GONE);
				holder.btOperation2.setVisibility(View.GONE);

				try {
					holder.tvVoice.setText("");
					if (!TextUtils.isEmpty(data.getData())) {
						ChatMsgVoice voice = JsonUtils.fromJson(data.getData(), ChatMsgVoice.class);
						holder.tvVoice.setText(voice.getText());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (data.getDataTypeTag() == ChatMsgData.DATA_TYPE_SELECT_TIME) {
				// 选择时间
				holder.tvTitle.setVisibility(View.GONE);
				holder.tvText.setVisibility(View.VISIBLE);
				holder.tvVoice.setVisibility(View.GONE);
				holder.tvTips.setVisibility(View.VISIBLE);
				holder.btOperation1.setVisibility(View.VISIBLE);
				holder.btOperation2.setVisibility(View.VISIBLE);

				try {
					holder.tvText.setText("");
					holder.tvTips.setText("");
					holder.btOperation1.setText("");
					holder.btOperation1.setOnClickListener(null);
					holder.btOperation2.setOnClickListener(null);
					if (!TextUtils.isEmpty(data.getData())) {
						ChatMsgSelectTime selectTime = JsonUtils.fromJson(data.getData(), ChatMsgSelectTime.class);
						if (TextUtils.isEmpty(selectTime.getTitle())) {
							holder.tvText.setVisibility(View.GONE);
						}
						holder.tvText.setText(selectTime.getTitle());
						holder.tvTips.setText(selectTime.getDetail());
						holder.btOperation1.setText("确认时间");
						if (selectTime.isHideConfirmButton()) {
							holder.btOperation1.setVisibility(View.GONE);
						}
						holder.btOperation1.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								ViewUtils.preventViewMultipleClick(v, 1000);
								if (mListener != null) {
									mListener.onClickOperation1(v, data);
								}
							}
						});
						holder.btOperation2.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								ViewUtils.preventViewMultipleClick(v, 1000);
								if (mListener != null) {
									mListener.onClickOperation2(v, data);
								}
							}
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (data.getDataTypeTag() == ChatMsgData.DATA_TYPE_SELECT_REST) {
				// 选择分店
				holder.tvTitle.setVisibility(View.GONE);
				holder.tvText.setVisibility(View.VISIBLE);
				holder.tvVoice.setVisibility(View.GONE);
				holder.tvTips.setVisibility(View.VISIBLE);
				holder.btOperation1.setVisibility(View.VISIBLE);
				holder.btOperation2.setVisibility(View.GONE);

				try {
					holder.tvText.setText("");
					holder.tvTips.setText("");
					holder.btOperation1.setText("");
					holder.btOperation1.setOnClickListener(null);
					if (!TextUtils.isEmpty(data.getData())) {
						ChatMsgSelectRest selectRest = JsonUtils.fromJson(data.getData(), ChatMsgSelectRest.class);
						if (TextUtils.isEmpty(selectRest.getTitle())) {
							holder.tvText.setVisibility(View.GONE);
						}
						holder.tvText.setText(selectRest.getTitle());
						holder.tvTips.setText(selectRest.getDetail());
						holder.btOperation1.setText("选择餐厅");
						holder.btOperation1.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								ViewUtils.preventViewMultipleClick(v, 1000);
								if (mListener != null) {
									mListener.onClickOperation1(v, data);
								}
							}
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				holder.tvTitle.setVisibility(View.GONE);
				holder.tvText.setVisibility(View.GONE);
				holder.tvVoice.setVisibility(View.GONE);
				holder.tvTips.setVisibility(View.GONE);
				holder.btOperation1.setVisibility(View.GONE);
				holder.btOperation2.setVisibility(View.GONE);
			}
		} else if (data.getSrc() == ChatMsgData.SRC_CLIENT) {
			holder.vgServer.setVisibility(View.GONE);
			holder.vgClient.setVisibility(View.VISIBLE);
			
			holder.vgClient.setOnClickListener(null);

			holder.ivHeadClient.setImageByUrl(data.getPicUrl(), true, position, ScaleType.CENTER_CROP);
			
			holder.vgMsgClient.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					ViewUtils.preventViewMultipleClick(v, 1000);
					if (mListener != null) {
						mListener.onClickMsg(v, data);
					}
				}
			});

			if (data.getDataTypeTag() == ChatMsgData.DATA_TYPE_TEXT) {
				// 简单文本
				holder.tvTextClient.setVisibility(View.VISIBLE);
				holder.tvVoiceClient.setVisibility(View.GONE);

				try {
					holder.tvTextClient.setText("");
					if (!TextUtils.isEmpty(data.getData())) {
						ChatMsgText text = JsonUtils.fromJson(data.getData(), ChatMsgText.class);
						holder.tvTextClient.setText(ChatMsgData.getInfoFromChatMsgText(text));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (data.getDataTypeTag() == ChatMsgData.DATA_TYPE_VOICE) {
				// 语音
				holder.tvTextClient.setVisibility(View.GONE);
				holder.tvVoiceClient.setVisibility(View.VISIBLE);

				try {
					holder.tvVoiceClient.setText("");
					if (!TextUtils.isEmpty(data.getData())) {
						ChatMsgVoice voice = JsonUtils.fromJson(data.getData(), ChatMsgVoice.class);
						holder.tvVoiceClient.setText(voice.getText());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				holder.tvVoiceClient.setVisibility(View.GONE);
				holder.tvVoiceClient.setVisibility(View.GONE);
			}
		} else {
			holder.vgServer.setVisibility(View.GONE);
			holder.vgClient.setVisibility(View.GONE);
		}

		convertView.setTag(holder);
		return convertView;
	}

	public List<ChatMsgData> getList() {
		return list;
	}

	public void setList(List<ChatMsgData> list) {
		if (this.list == null) {
			this.list = new ArrayList<ChatMsgData>();
		}
		this.list = list;
		notifyDataSetChanged();
	}
}

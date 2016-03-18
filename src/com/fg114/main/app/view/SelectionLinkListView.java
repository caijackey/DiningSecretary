package com.fg114.main.app.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.fg114.main.R;
import com.fg114.main.app.Settings;
import com.fg114.main.app.view.SelectionListView.OnSelectedListener;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.RfTypeDTO;
import com.fg114.main.service.dto.RfTypeListDTO;
import com.fg114.main.util.CheckUtil;
import com.fg114.main.util.DialogUtil;
import com.fg114.main.util.HanziUtil;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.UnitUtil;
import com.google.xiaomishujson.reflect.TypeToken;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 左右相互联动的双列表选择控件
 * 
 * 注意点： 1) 当某个ItemData的uuid为空时，表示该项是“title”项，“title”项不能被选择，
 * 例如字母分组时每组都会在最前面增加一个单个字母的title项 2) 传入的数据将全部合并到右列表，并且被自动按左边列表项目分成组，只能选择子项。 3)
 * 当选择左边列表项目时，右边会定位到相应的分组；当右边项目滑动时，左边项目会跟随左边的分组来选中 4)
 * 使用：调用setData方法设置数据；调用setOnSelectedListener设置选择监听器
 * 
 * @author xujianjun,2013-07-31
 * 
 */
public class SelectionLinkListView extends LinearLayout {
	Context context;
	List<? extends ItemData> mainList;
	List<? extends ItemData> subList;
	ListView mainListView;
	ListView subListView;
	int mainPosition = 0;
	int subPosition = 0;
	private ItemAdapter mainAdapter;
	private ItemAdapter subAdapter;
	private OnSelectedListener selectedListener;

	public SelectionLinkListView(Context context) {
		this(context, null);
	}

	public SelectionLinkListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOrientation(LinearLayout.HORIZONTAL);
		this.context = context;
		this.setBackgroundColor(0xFFf7f7f7);
		initUI();
	}

	private void addAllItem(List<RfTypeListDTO> list, RfTypeListDTO allSubItem, RfTypeListDTO allSubItemTitle) {
		list.add(0, allSubItem);
		list.add(0, allSubItemTitle);
	}

	public void setData(List<? extends ItemData> list) {
		// ---
		if (list == null) {
			this.mainList = new ArrayList<ItemData>();
		}
		this.mainList = list;
		
		//
		// 初始化分组数据
		this.subList = mergeAllSubList((List<RfTypeListDTO>) list, true);
//		RfTypeListDTO allSubItem = new RfTypeListDTO();
//		RfTypeListDTO allSubItemTitle = new RfTypeListDTO();
//		allSubItem.setUuid(String.valueOf(Settings.STATUTE_ALL));
//		allSubItem.setName("-- 全部 --");
//		allSubItem.setParentId(String.valueOf(Settings.STATUTE_ALL));
//
//		allSubItemTitle.setUuid("");
//		allSubItemTitle.setName("#");
//		addAllItem((List<RfTypeListDTO>) this.subList, allSubItem, allSubItemTitle);

		// 判断是否有子列表，没有子列表，则不显示右边列表---------
		int position = 0;
		for (int i = 0; i < list.size(); i++) {
			ItemData data = list.get(i);
			if (data.isSelectTag()) {
				position = i;
			}
		}

		// 初始化选择位置
		mainAdapter.setList(list);
		mainListView.setSelection(position);
		// 子列表初始化选择位置
		position = 0;
		for (int i = 0; i < subList.size(); i++) {
			ItemData data = subList.get(i);
			if (data.isSelectTag()) {
				position = i;
			}
		}
		subAdapter.setList(subList);
		subListView.setSelection(position);
	}

	private static class MyItemData implements ItemData {
		String uuid;
		String name;
		String firstLetter;
		private String parentId;

		public MyItemData(String uuid, String name) {
			this.uuid = uuid;
			this.name = name;
		}

		@Override
		public String getUuid() {
			return uuid;
		}

		@Override
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}

		@Override
		public String getParentId() {
			return this.parentId;
		}

		@Override
		public void setParentId(String parentId) {
			this.parentId = parentId;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public boolean isSelectTag() {
			return false;
		}

		@Override
		public void setSelectTag(boolean selectTag) {
		}

		@Override
		public List<? extends ItemData> getList() {
			return null;
		}

		@Override
		public void setList(List<? extends ItemData> list) {
		}

		@Override
		public void setIsNeedGroupBy(boolean needGroupBy) {
		}

		@Override
		public boolean isNeedGroupBy() {
			return false;
		}

		@Override
		public String getMemo() {
			return null;
		}

		@Override
		public void setMemo(String memo) {
		}

		@Override
		public void setFirstLetter(String firstLetter) {
			this.firstLetter = firstLetter;
		}

		@Override
		public String getFirstLetter() {
			return this.firstLetter;
		}

	}

	public void setOnSelectedListener(OnSelectedListener selectedListener) {
		this.selectedListener = selectedListener;
	}

	private void initUI() {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT, 1);
		mainListView = new ListView(context);
		subListView = new ListView(context);

		// --------
		mainListView.setDivider(new ColorDrawable(0x33000000));
		mainListView.setDividerHeight(1);
		mainListView.setBackgroundColor(0xFFFFFFFF);
		mainListView.setDrawSelectorOnTop(true);
		mainListView.setCacheColorHint(Color.TRANSPARENT);
		mainListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
		mainListView.setVerticalScrollBarEnabled(false);
		mainListView.setScrollingCacheEnabled(false);
		mainListView.setSelector(new ColorDrawable(0x55FFFF00));
		//
		subListView.setDivider(new ColorDrawable(0x33000000));
		subListView.setDividerHeight(1);
		subListView.setBackgroundColor(0xFFeBeBeB);
		subListView.setCacheColorHint(Color.TRANSPARENT);
		subListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
		subListView.setDrawSelectorOnTop(true);
		subListView.setVerticalScrollBarEnabled(false);
		subListView.setScrollingCacheEnabled(false);

		// --------
		mainListView.setLayoutParams(lp);
		subListView.setLayoutParams(lp);
		//
		this.addView(mainListView);
		this.addView(subListView);

		mainAdapter = new ItemAdapter(context, getResources().getDrawable(R.drawable.item_data_selected), 0xFFEE0000);
		mainListView.setAdapter(mainAdapter);

		subAdapter = new ItemAdapter(context, new ColorDrawable(0x11000000), 0xFFEE0000);
		subListView.setAdapter(subAdapter);

		subListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				try {
					int mainFirst = mainListView.getFirstVisiblePosition();
					int mainLast = mainListView.getLastVisiblePosition();
					if (mainList == null) {
						return;
					}

					// 主列表的前一选择
					ItemData preItemData = null;
					int prePos = getSelectedItemDataPosition(mainList);
					if (prePos != -1) {
						preItemData = mainList.get(prePos);
					}
					// -------------
					ItemData currentSubItemData = subList.get(firstVisibleItem);
					// 如果原先没有选择或者选择的不是当前子的父
					if (preItemData == null || !currentSubItemData.getParentId().equals(preItemData.getUuid())) {
						if (preItemData != null) {
							// 取消前一选择
							preItemData.setSelectTag(false);
						}
						int targetMainPosition = getItemDataPositionById(mainList, currentSubItemData.getParentId());
						
						if (targetMainPosition != -1) {
							ItemData currentMainItemData = mainList.get(targetMainPosition);
							currentMainItemData.setSelectTag(true);
							Log.w("currentMainItemData",currentSubItemData.getParentId()+"currentMainItemData="+currentMainItemData.getName());
							mainListView.setSelection(targetMainPosition);
							mainAdapter.notifyDataSetChanged();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// test();

	}

	private void test() {
		String mains = "[{\"uuid\":\"uuid-111\",\"name\":\"全部区域\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-112\",\"name\":\"黄浦区\",\"parentId\":\"\",\"selectTag\":\"true\"},{\"uuid\":\"uuid-113\",\"name\":\"青浦区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-114\",\"name\":\"浦东新区\",\"parentId\":\"\",\"selectTag\":\"false\",\"needGroupBy\":\"false\",\"list\":[{\"uuid\":\"uuid-1\",\"name\":\"第一商区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-2\",\"name\":\"他第三商区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-3\",\"name\":\"我第二商区\",\"parentId\":\"\",\"selectTag\":\"true\"},{\"uuid\":\"uuid-3\",\"name\":\"他可以的商区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-3\",\"name\":\"第二商区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-3\",\"name\":\"第二商区\",\"parentId\":\"\",\"selectTag\":\"false\"}]},{\"uuid\":\"uuid-115\",\"name\":\"普陀区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"}]";
		List<CommonTypeListDTO> mainData = JsonUtils.fromJson(mains, new TypeToken<List<CommonTypeListDTO>>() {
		});
		setData(mainData);
		this.selectedListener = new OnSelectedListener() {

			@Override
			public void onSelected(com.fg114.main.app.view.ItemData mainData, com.fg114.main.app.view.ItemData subData, int mainPosition, int subPosition) {
				DialogUtil.showToast(getContext(), mainData.getName() + "[" + mainPosition + "]," + (subData == null ? null : subData.getName()) + "[" + subPosition + "]");
			}
		};
	}

	public class ItemAdapter extends BaseAdapter {

		private List<? extends ItemData> list = null;
		private Drawable defaultBackground;// =getResources().getDrawable(R.drawable.button_transparent_light_color_effect);
		private Drawable titleBackground = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] { 0xFFAAAADD, 0xFFCCCCEE });// =getResources().getDrawable(R.drawable.button_transparent_light_color_effect);

		private LayoutInflater mInflater = null;
		private Context context;
		private ItemData selectedItemData = null;
		private ViewHolder selectedViewHolder = null;
		private int selectedPosition;
		private Drawable highLight;
		private int highLightFontColor;
		private int defaultFontColor = 0xFF555555;

		public ItemAdapter(Context c, Drawable highLight, int highLightFontColor) {
			super();
			this.context = c;
			this.highLight = highLight;
			this.highLightFontColor = highLightFontColor;
			mInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

		public class ViewHolder {
			LinearLayout layout;
			TextView name;
			ImageView rightArraw;
			ItemData data;
			int position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
				LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(UnitUtil.dip2px(15), UnitUtil.dip2px(15), 0);
				holder.layout = new LinearLayout(context);
				holder.name = new TextView(context);
				holder.name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				holder.name.setTextColor(defaultFontColor);
				holder.name.setGravity(Gravity.LEFT);
				holder.layout.setOnClickListener(clickListener);
				// ---
				holder.rightArraw = new ImageView(context);
				holder.rightArraw.setScaleType(ScaleType.CENTER_INSIDE);
				holder.rightArraw.setImageResource(R.drawable.right_triangle);

				// ---
				holder.layout.setGravity(Gravity.CENTER);
				holder.layout.setPadding(UnitUtil.dip2px(15), UnitUtil.dip2px(10), UnitUtil.dip2px(3), UnitUtil.dip2px(10));
				holder.layout.addView(holder.name, lp);
				holder.layout.addView(holder.rightArraw, lp2);

				convertView = holder.layout;
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.position = position;
			ItemData data = (ItemData) list.get(position);
			// 如果是控制项，文字要特殊显示
			if (data == null || TextUtils.isEmpty(data.getUuid())) {
				holder.rightArraw.setVisibility(View.GONE);
				holder.name.setText(data.getName());
				holder.name.setTextColor(0xFFFFFFFF);
				holder.name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				holder.name.setShadowLayer(2, 2, 2, 0xFF333333);
				holder.layout.setBackgroundDrawable(titleBackground);
				holder.layout.setPadding(UnitUtil.dip2px(15), UnitUtil.dip2px(3), UnitUtil.dip2px(3), UnitUtil.dip2px(3));
			} else {
				holder.layout.setPadding(UnitUtil.dip2px(15), UnitUtil.dip2px(10), UnitUtil.dip2px(3), UnitUtil.dip2px(10));
				holder.name.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
				holder.name.setText(data.getName());
				holder.name.setShadowLayer(0, 0, 0, 0x0);
				if (data.getList() != null && data.getList().size() != 0) {
					holder.rightArraw.setVisibility(View.GONE);
				} else {
					holder.rightArraw.setVisibility(View.GONE);
				}
				// ----
				if (data.isSelectTag()) {
					if (selectedItemData != data) {
						selectedItemData = data;
						selectedViewHolder = holder;
						selectedPosition = position;
					}
					holder.layout.setBackgroundDrawable(highLight);
					holder.layout.setPadding(UnitUtil.dip2px(15), UnitUtil.dip2px(10), UnitUtil.dip2px(3), UnitUtil.dip2px(10));
					holder.name.setTextColor(highLightFontColor);
				} else {
					holder.name.setTextColor(defaultFontColor);
					holder.layout.setBackgroundDrawable(defaultBackground);
				}
			}
			holder.data = data;
			convertView.setTag(holder);
			return convertView;
		}

		public List<? extends ItemData> getList() {
			return list;
		}

		public void setList(List<? extends ItemData> list) {
			this.list = list;
			selectedItemData = null;
			selectedViewHolder = null;
			selectedPosition = -1;
			this.notifyDataSetChanged();
		}

		View.OnClickListener clickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {

				// 如果uuid是null，列表会作为组的title显示，不参与选择事件
				ViewHolder vh = (ViewHolder) v.getTag();
				if (vh.data == null || TextUtils.isEmpty(vh.data.getUuid())) {
					return;
				}
//
//				if (selectedItemData != null) {
//					selectedItemData.setSelectTag(false);
//					mainAdapter.notifyDataSetChanged();
//					Log.d("selectedItemData取消前项了",""+selectedItemData.getName());
//				}else{
					// 主列表的前一选择
					int prePos = getSelectedItemDataPosition(mainList);
					if (prePos != -1) {
						mainList.get(prePos).setSelectTag(false);
						mainAdapter.notifyDataSetChanged();
					}
//				}
				// 先取消前面选择项，再选择当前项
				if (selectedViewHolder != null) {
					selectedViewHolder.layout.setBackgroundDrawable(defaultBackground);
					selectedViewHolder.name.setTextColor(defaultFontColor);
				}
				
				// -----
				selectedViewHolder = (ViewHolder) v.getTag();
				selectedItemData = selectedViewHolder.data;
				selectedItemData.setSelectTag(true);
				selectedPosition = selectedViewHolder.position;
				selectedViewHolder.layout.setBackgroundDrawable(highLight);
				selectedViewHolder.layout.setPadding(UnitUtil.dip2px(15), UnitUtil.dip2px(10), UnitUtil.dip2px(3), UnitUtil.dip2px(10));
				selectedViewHolder.name.setTextColor(highLightFontColor);

				// 如果是主列表
				if (ItemAdapter.this == mainAdapter) {
					// 定位右列表
					for (int i = 0; i < subList.size(); i++) {
						ItemData data = subList.get(i);
						if (!CheckUtil.isEmpty(data.getParentId()) && data.getParentId().equals(selectedItemData.getUuid())) {
							subListView.setSelection(i);
							return;
						}
					}
				} else {
					// 子列表，并触发选择事件
					ItemData mainData = getMainItemData(selectedItemData);
					selectedListener.onSelected(mainData, selectedItemData, mainAdapter.selectedPosition, selectedPosition);
				}
				
			}

			// 根据子选项，返回适当的主选项
			private ItemData getMainItemData(ItemData selectedSubItemData) {
				if (String.valueOf(Settings.STATUTE_ALL).equals(selectedSubItemData.getUuid()) && String.valueOf(Settings.STATUTE_ALL).equals(selectedSubItemData.getParentId())) {
					// 全部榜单，构造一个主的，因为主列表数据里没有全部的项
					return new MyItemData(String.valueOf(Settings.STATUTE_ALL), "全部榜单");
				} else {
					// 从主列表数据里找到子列表parentId指定的那个主项
					for (ItemData mainData : mainList) {
						if (mainData.getUuid().equals(selectedSubItemData.getParentId())) {
							return mainData;
						}
					}
				}
				return null;
			}
		};
	}

	/**
	 * 合并所有的子列表，用于大类“全部XX”中显示，合并后的子类别会把大类id放在parentId字段中
	 * 
	 * @param List
	 * @param needTypeTitle
	 *            是否需要自动添加类型标题，类型标题自动取大类的名称
	 * @return
	 */
	public static List<? extends ItemData> mergeAllSubList(List<RfTypeListDTO> list, boolean needTypeTitle) {
		List<RfTypeDTO> all = new ArrayList<RfTypeDTO>();
		if (list == null || list.size() == 0) {
			return all;
		}
		// --
		for (RfTypeListDTO mainData : list) {
			if (needTypeTitle) {
				RfTypeDTO title = new RfTypeDTO();
				title.setUuid("");
				title.setName(mainData.getName());
				title.setParentId(mainData.getUuid());// 保存主ID
				all.add(title);
			}
			if (mainData.getList() == null) {
				continue;
			}
			// --
			for (RfTypeDTO subData : mainData.getList()) {
				subData.setParentId(mainData.getUuid());// 保存主ID
				all.add(subData);
			}
		}

		return all;
	}

	public static List<? extends ItemData> mergeAllSubList(List<RfTypeListDTO> list) {
		return mergeAllSubList(list, false);
	}

	// 返回列表中被选中的项的position
	private int getSelectedItemDataPosition(List<? extends ItemData> list) {
		for (int i = 0; i < list.size(); i++) {
			ItemData mainData = list.get(i);
			if (mainData.isSelectTag()) {
				return i;
			}
		}
		return -1;
	}

	// 返回列表中uuid为id的项的position
	private int getItemDataPositionById(List<? extends ItemData> list, String uuid) {
		for (int i = 0; i < list.size(); i++) {
			ItemData mainData = list.get(i);
			if (mainData.getUuid().equals(uuid)) {
				return i;
			}
		}
		return -1;
	}
}
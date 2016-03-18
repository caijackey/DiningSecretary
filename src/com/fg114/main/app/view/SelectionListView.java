package com.fg114.main.app.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.fg114.main.R;
import com.fg114.main.service.dto.CommonTypeListDTO;
import com.fg114.main.service.dto.RfTypeDTO;
import com.fg114.main.service.dto.RfTypeListDTO;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 双列表联动选择控件
 * 
 * 注意点：
 * 1) 当某个ItemData的uuid为空时，表示该项是“title”项，“title”项不能被选择，例如字母分组时每组都会在最前面增加一个单个字母的title项
 * 2) 当某个ItemData没有list时（list==null || list.size()==0）,表示这是个“可以被选择”的项，当点击时会触发OnSelectedListener.onSelected
 * 3) 使用：调用setData方法设置数据；调用setOnSelectedListener设置选择监听器
 * 
 * @author xujianjun,2013-07-31
 *
 */
public class SelectionListView extends LinearLayout {
	Context context;
	List<? extends ItemData> list;
	ListView mainListView;
	ListView subListView;
	int mainPosition = 0;
	int subPosition = 0;
	private ItemAdapter mainAdapter;
	private ItemAdapter subAdapter;
	private OnSelectedListener selectedListener;
	
	public interface OnSelectedListener {
		/**
		 * 当某个可选择项被选中的时候触发
		 * @param mainData 当前被选中的主列表项ItemData数据
		 * @param subData 当前被选中的子列表项ItemData数据。如果当前选择的是主列表项，subData为null
		 * @param mainPosition 当前被选择的主列表项在list中的索引，从0开始
		 * @param subPosition 当前被选择的子列表项在list中的索引，从0开始。如果当前选择的是主列表项，subPosition=-1
		 */
		void onSelected(ItemData mainData,ItemData subData, int mainPosition, int subPosition);
	}

	public SelectionListView(Context context) {
		this(context,null);
	}

	public SelectionListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOrientation(LinearLayout.HORIZONTAL);
		this.context = context;
		this.setBackgroundColor(0xFFf7f7f7);
		initUI();
	}

	public void setData(List<? extends ItemData> list) {
		
		//---
		if(list==null){
			this.list=new ArrayList<ItemData>();
		}
		
		//判断是否有子列表，没有子列表，则不显示右边列表---------
		boolean haveSubList=false;
		ItemData selectedData=null;
		int position=0;
		for(int i=0;i<list.size();i++){
			ItemData data=list.get(i);
			if(data.getList()!=null && data.getList().size()>0){
				haveSubList=true;
			}
			if(data.isSelectTag()){
				selectedData=data;
				position=i;
			}
		}
		//是否有子列表，如果为false，则不显示右边列表
		if(haveSubList){
			subListView.setVisibility(View.VISIBLE);
		}else{
			subListView.setVisibility(View.GONE);
		}
		//初始化分组数据
		list=initGroupBy(list,false);
		//初始化选择位置
		if(selectedData!=null){
			mainListView.setSelection(position);
			if(selectedData.getList()!=null && selectedData.getList().size()>0){
				List<? extends ItemData> subList=selectedData.getList();
				subAdapter.setList(subList);
				
				//子列表初始化选择位置
				position=0;
				for(int i=0;i<subList.size();i++){ 
					ItemData data=subList.get(i);
					if(data.isSelectTag()){
						position=i;
					}
				}
				subListView.setSelection(position);
			}
		}
		
		this.list = list;
		//-----------------------
		mainAdapter.setList(list);

	}

	//如果list支持group by，则使用字母分组来组织list
	private  List<? extends ItemData> initGroupBy(List<? extends ItemData> list, boolean needGroupBy) {
		if(list==null||list.size()==0){
			return list;
		}
		List tempList=list;
		//---排序自己
		if(needGroupBy){
			//初始化首字母
			initFirstLetter(list);
			Collections.sort(list, new Comparator<ItemData>() {
				
				@Override
				public int compare(ItemData o1, ItemData o2) {
					if(TextUtils.isEmpty(o1.getName())){
						o1.setName("- -");
						o1.setFirstLetter("#");
					}
					if(TextUtils.isEmpty(o2.getName())){
						o2.setName("- -");
						o2.setFirstLetter("#");
					}
					String letter1=o1.getFirstLetter();
					String letter2=o2.getFirstLetter();
					int c=letter1.compareTo(letter2);
					return c==0?o1.getName().compareTo(o2.getName()):c;
				}
			});
			//去掉“其他”和 重复项
			Iterator<? extends ItemData> it=list.iterator();
			String lastName=null;
			while(it.hasNext()){
				ItemData data=it.next();
				//
				if(lastName!=null && lastName.equals(data.getName())){
					it.remove();
				}else if("其他".equals(data.getName())||"其它".equals(data.getName())){
					it.remove();
				}else{
					lastName=data.getName();
				}
			}
			
			//添加首字母项
			tempList=new ArrayList();
			int i=0;
			for(i=0;i<list.size()-1;i++){
				if(TextUtils.isEmpty(list.get(i).getName()) || TextUtils.isEmpty(list.get(i+1).getName())){
					continue;
				}
				
				final String currentLetter=list.get(i).getFirstLetter();
				final String nextLetter=list.get(i+1).getFirstLetter();
				//直接添加第一项
				if(i==0){
					tempList.add(new MyItemData("",currentLetter));
				}
				tempList.add(list.get(i));
				//前后项不相同，添加字母控制项
				if(!currentLetter.equals(nextLetter)){
					tempList.add(new MyItemData("",nextLetter));
				}
				
			}
			tempList.add(list.get(i));
		}
		//---排序子列表
		for(ItemData data : list){
			data.setList(initGroupBy(data.getList(),data.isNeedGroupBy()));
			if(data.isNeedGroupBy()){
				data.setIsNeedGroupBy(false); //只group by 一次，防止同一数据集多次设置后，数据重复group by
			}
		}
		return tempList;
	}
	private void initFirstLetter(List<? extends ItemData> list) {
		if(list==null||list.size()==0){
			return;
		}
		
		for(ItemData data : list){
			data.setFirstLetter(HanziUtil.getFirst(HanziUtil.getPinyin(data.getName().charAt(0))).toUpperCase());
		}
		
	}
	private static class MyItemData implements ItemData{
		String uuid;
		String name;
		String firstLetter;
		private String parentId;
		
		public MyItemData(String uuid, String name) {
			this.uuid=uuid;
			this.name=name;
		}

		@Override
		public String getUuid() {
			return uuid;
		}

		@Override
		public void setUuid(String uuid) {
			this.uuid=uuid;
		}

		@Override
		public String getParentId() {
			return this.parentId;
		}

		@Override
		public void setParentId(String parentId) {
			this.parentId=parentId;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			this.name=name;
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
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setMemo(String memo) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setFirstLetter(String firstLetter) {
			this.firstLetter=firstLetter;			
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
		
		//test();

	}

	private void test() {
		String mains = "[{\"uuid\":\"uuid-111\",\"name\":\"全部区域\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-112\",\"name\":\"黄浦区\",\"parentId\":\"\",\"selectTag\":\"true\"},{\"uuid\":\"uuid-113\",\"name\":\"青浦区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-114\",\"name\":\"浦东新区\",\"parentId\":\"\",\"selectTag\":\"false\",\"needGroupBy\":\"false\",\"list\":[{\"uuid\":\"uuid-1\",\"name\":\"第一商区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-2\",\"name\":\"他第三商区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-3\",\"name\":\"我第二商区\",\"parentId\":\"\",\"selectTag\":\"true\"},{\"uuid\":\"uuid-3\",\"name\":\"他可以的商区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-3\",\"name\":\"第二商区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-3\",\"name\":\"第二商区\",\"parentId\":\"\",\"selectTag\":\"false\"}]},{\"uuid\":\"uuid-115\",\"name\":\"普陀区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"},{\"uuid\":\"uuid-116\",\"name\":\"虎口区\",\"parentId\":\"\",\"selectTag\":\"false\"}]";
		List<CommonTypeListDTO> mainData = JsonUtils.fromJson(mains, new TypeToken<List<CommonTypeListDTO>>() {
		});
		setData(mainData);
		this.selectedListener = new OnSelectedListener() {

			@Override
			public void onSelected(com.fg114.main.app.view.ItemData mainData, com.fg114.main.app.view.ItemData subData, int mainPosition, int subPosition) {
				DialogUtil.showToast(getContext(), mainData.getName() + "["+mainPosition+"]," + (subData==null?null:subData.getName())+"["+subPosition+"]");
			}
		};
	}

	public class ItemAdapter extends BaseAdapter {

		private List<? extends ItemData> list = null;
		private Drawable defaultBackground;// =getResources().getDrawable(R.drawable.button_transparent_light_color_effect);
		private Drawable titleBackground=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xFFAAAADD,0xFFCCCCEE});// =getResources().getDrawable(R.drawable.button_transparent_light_color_effect);

		private LayoutInflater mInflater = null;
		private Context context;
		private ItemData selectedItemData = null;
		private ViewHolder selectedViewHolder = null;
		private int selectedPosition;
		private Drawable highLight;
		private int highLightFontColor;
		private int defaultFontColor=0xFF555555;

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
				//---
				holder.rightArraw = new ImageView(context);
				holder.rightArraw.setScaleType(ScaleType.CENTER_INSIDE);
				holder.rightArraw.setImageResource(R.drawable.right_triangle);

				//---
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
					holder.rightArraw.setVisibility(View.VISIBLE);
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

				if (selectedItemData != null) {
					selectedItemData.setSelectTag(false);
				}
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

				if (selectedItemData.getList() != null && selectedItemData.getList().size() != 0 && ItemAdapter.this != subAdapter) {
					subAdapter.setList(selectedItemData.getList());
				} else if (selectedListener != null) {
					
					// 如果是主列表，清空一下子列表并触发选择事件
					if (ItemAdapter.this == mainAdapter) {
						subAdapter.setList(new ArrayList<ItemData>());
						selectedListener.onSelected(selectedItemData,null,selectedPosition,-1);
						//DialogUtil.showToast(context, "["+selectedItemData.getUuid()+"]"+selectedItemData.getName()+" | "+"["+null+"]"+null);
					}else{
						//子列表并触发选择事件
						selectedListener.onSelected(mainAdapter.selectedItemData,selectedItemData, mainAdapter.selectedPosition,selectedPosition);
						//DialogUtil.showToast(context, "["+mainAdapter.selectedItemData.getUuid()+"]"+mainAdapter.selectedItemData.getName()+" | "+"["+selectedItemData.getUuid()+"]"+selectedItemData.getName());
					}
				}
			}
		};
	}
	/**
	 * 合并所有的子列表，用于大类“全部XX”中显示，合并后的子类别会把大类id放在parentId字段中
	 * @param List
	 * @param needTypeTitle 是否需要自动添加类型标题，类型标题自动取大类的名称
	 * @return
	 */
	public static List<? extends ItemData> mergeAllSubList(List<RfTypeListDTO> list, boolean needTypeTitle) {
		List<RfTypeDTO> all = new ArrayList<RfTypeDTO>();
		if(list==null||list.size()==0){
			return all;
		}
		//--
		for(RfTypeListDTO mainData : list){
			if(needTypeTitle){
				RfTypeDTO title=new RfTypeDTO();
				title.setUuid("");
				title.setName(mainData.getName());
				all.add(title);
			}
			if(mainData.getList()==null){
				continue;
			}
			//--
			for(RfTypeDTO subData : mainData.getList()){
				subData.setParentId(mainData.getUuid());//保存主ID
				all.add(subData);
			}
		}
		
		return all;
	}	
	public static List<? extends ItemData> mergeAllSubList(List<RfTypeListDTO> list) {
		return mergeAllSubList(list, false);
	}
}
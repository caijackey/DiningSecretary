package com.fg114.main.app.activity;

import java.lang.reflect.*;
import java.util.*;

import android.app.AlertDialog.Builder;
import android.content.*;
import android.database.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.os.Handler.Callback;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.widget.RadioGroup.*;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract;

import com.fg114.main.R;
import com.fg114.main.analytics.OpenPageDataTracer;
import com.fg114.main.app.*;
import com.fg114.main.app.adapter.*;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.service.dto.*;
import com.fg114.main.service.task.*;
import com.fg114.main.util.*;

/**
 * 选取联系人列表
 * @author wufucheng
 */
@SuppressWarnings("deprecation")
public class CityMoreActivity extends MainFrameActivity {

	// 传入参数
	private int fromPage = 0;
	
	private CityListDTO cityListDTO;

	private List<CityData> cityList = new ArrayList<CityData>(); //城市列表
	private CityMoreAdapter cityListAdapter;

	private boolean needHintLetter=true; // 首字母提示是否可见
	private LinearLayout main;
	private TextView searchBar;
	private ListView cityListView;
	private TextView overlay;


	private GetUserFriendListTask getUserFriendListTask;

	//提示字母
	private String hintLetter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//----------------------------
		OpenPageDataTracer.getInstance().enterPage("全部城市", "");
		//----------------------------
		Bundle bundle = this.getIntent().getExtras();
		// 初始化界面
		initComponent();
	}
	@Override
	public void onRestart() {
		super.onRestart();
		//----------------------------
		OpenPageDataTracer.getInstance().enterPage("全部城市", "");
		//----------------------------
	}
	@Override
	public void finish() {
		super.finish();
		
	}
	private List<CityData> getCityList(){
	List<CityData> cityList = new ArrayList<CityData>(); 
	//从缓存中读取城市列表
	CityListDTO cityListDTO = SessionManager.getInstance().getCityListDTO(this);
	if(cityListDTO==null||cityListDTO.getList()==null){
		return cityList;
	}
	//按首字母排序
	/*TreeSet tree=new TreeSet(new Comparator<T>() {
	});*/
	//
	cityList=cityListDTO.getList();
	Collections.sort(cityList, new Comparator<CityData>() {
		public int compare(CityData o1, CityData o2) {
			return HanziUtil.getFirst(o1.getFirstLetter())
			.compareToIgnoreCase(
					HanziUtil.getFirst(o2.getFirstLetter()));
		}
	});
	return cityList;
	
	//测试数据-----------------------------------------------------------
/*	
 	
	for(int i=0;i<9;i++){
		CityData c=new CityData();
		c.setCityId(""+i);
		c.setCityName("北京");
		c.setFirstLetter("bj");
		c.setHotTag(true);
		c.setPinyin("beijing");
		cityList.add(c);
	}

	for(int i=10;i<19;i++){
		CityData c=new CityData();
		c.setCityId(""+i);
		c.setCityName("上海");
		c.setFirstLetter("sh");
		c.setHotTag(true);
		c.setPinyin("shanghai");
		cityList.add(c);
	}
	for(int i=20;i<29;i++){
		CityData c=new CityData();
		c.setCityId(""+i);
		c.setCityName("大连");
		c.setFirstLetter("dl");
		c.setHotTag(false);
		c.setPinyin("dalian");
		cityList.add(c);
	}
	for(int i=30;i<39;i++){
		CityData c=new CityData();
		c.setCityId(""+i);
		c.setCityName("乌鲁木齐");
		c.setFirstLetter("wlmq");
		c.setHotTag(false);
		c.setPinyin("wulumuqi");
		cityList.add(c);
	}
	for(int i=40;i<49;i++){
		CityData c=new CityData();
		c.setCityId(""+i);
		c.setCityName("广州");
		c.setFirstLetter("gz");
		c.setHotTag(true);
		c.setPinyin("guangzhou");
		cityList.add(c);
	}
	return cityList;
	*/
	
	//////////////-------------------------------------------------------
}
	private void initComponent() {
		// 设置标题栏
		this.getTvTitle().setText("更多城市");
		this.getBtnGoBack().setText(R.string.text_button_back);
		this.getBtnOption().setVisibility(View.INVISIBLE);
		this.setFunctionLayoutGone();
		//

		
		// 内容部分
		main=(LinearLayout)View.inflate(this,R.layout.city_more_select_list, null);
		searchBar = (TextView) main.findViewById(R.id.city_more_search_textview);
		cityListView = (ListView) main.findViewById(R.id.city_more_listview);
		overlay=(TextView) main.findViewById(R.id.pop_letter_layer);

	

		searchBar.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ViewUtils.preventViewMultipleClick(v, 1000);
				//DialogUtil.showToast(CityMoreActivity.this, "好吧");
				Bundle bundle = new Bundle();
				ActivityUtil.jump(CityMoreActivity.this, CitySearchActivity.class, 0, bundle);				
			}
		});
		


		cityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//-----
				OpenPageDataTracer.getInstance().addEvent("选择行");	
				//-----
	    		List<CityData> cityList = ((CityMoreAdapter)parent.getAdapter()).getList();
	    		if(cityList != null){
	    			CityData city = cityList.get(position);            	
	            	if (city != null) {
	            		
	            		CityInfo cityInfo = new CityInfo();
	            		cityInfo.setId(city.getCityId());
	            		cityInfo.setName(city.getCityName());
	            		cityInfo.setPhone(city.getPhone());
	            		
	            		//存入缓存
	            	    SessionManager.getInstance().setCityInfo(CityMoreActivity.this, cityInfo);
	            	    Fg114Application.isNeedUpdate=true; //为了广告在首页能及时更新，每城市广告数据是不一样的
	            	    CommonObservable.getInstance().notifyObservers(CommonObserver.CityChangedObserver.class);
	            	    ActivityUtil.jump(CityMoreActivity.this, IndexActivity.class, 0, new Bundle());
						//本页面关闭
						finish();
	            	}
	    		}
						/*if (cbSelect.isChecked()) {
					mLocalAdapter.selectItem(position);
				} else {
					mLocalAdapter.unselectItem(position);
				}*/
			}
		});

		cityListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == ListView.OnScrollListener.SCROLL_STATE_IDLE) {
					hidePopLetterLayer();
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
					int totalItemCount) {
				//Log.i("-----",""+firstVisibleItem+","+visibleItemCount+","+totalItemCount);
				if (needHintLetter&&firstVisibleItem!=0) {
					
					if (CityMoreActivity.this.cityListView.getCount()>0) {
						CityMoreActivity.this.hintLetter=HanziUtil.getFirst(((CityData)view.getAdapter().getItem(firstVisibleItem)).getFirstLetter()).toUpperCase();					
						showPopLetterLayer();
						return;
					}
//					String firstLetter = HanziToPinyinUtil.getAlpha(mFilterLocalList
//							.get(firstVisibleItem).getName());
					/*String firstLetter = HanziToPinyinUtil.getFirst(mFilterLocalList
							.get(firstVisibleItem).getUuid());*/
					
				}
			}
		});
		cityListAdapter=new CityMoreAdapter(this,getCityList());
		cityListView.setAdapter(cityListAdapter);
	
		this.getMainLayout().addView(main, LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
	}

	//显示字母提示框
	private void showPopLetterLayer(){
		if(this.overlay!=null){
			this.overlay.setText(this.hintLetter);
			this.overlay.setVisibility(View.VISIBLE);
		}
		
	}
	

	//隐藏字母提示框
	private void hidePopLetterLayer(){
		
		if(this.overlay!=null){
			this.overlay.setVisibility(View.INVISIBLE);
		}
	}

}

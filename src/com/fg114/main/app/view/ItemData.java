package com.fg114.main.app.view;

import java.util.List;

/**
 * 供双列表选择控件使用的数据接口
 * @author xujianjun,2013-07-31
 *
 */
public interface ItemData {
	/**
	 * 如果uuid是null，列表会作为组的title显示，不参与选择事件
	 * 
	 * @return
	 */
	public String getUuid();

	public void setUuid(String uuid);

	public String getParentId();

	public void setParentId(String parentId);

	public String getName();

	public void setName(String name);

	public boolean isSelectTag();

	public void setSelectTag(boolean selectTag);

	public List<? extends ItemData> getList();

	public void setList(List<? extends ItemData> list);
	
	public String getMemo();
	public void setMemo(String memo);
	
	
	/**
	 * 是否要对列表进行按字母分组
	 * @param needGroupList
	 */
	public void setIsNeedGroupBy(boolean needGroupBy);
	public boolean isNeedGroupBy();
	public void setFirstLetter(String firstLetter);
	public String getFirstLetter();
}

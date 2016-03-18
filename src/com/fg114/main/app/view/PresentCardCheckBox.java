package com.fg114.main.app.view;

import java.util.ArrayList;

import java.util.List;


import com.fg114.main.R;
import com.fg114.main.util.ViewUtils;




import android.content.Context;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;


import android.widget.CheckBox;
import android.widget.LinearLayout;

import android.widget.TextView;

public class PresentCardCheckBox extends LinearLayout
{
	private LayoutInflater mlayoutinf;
	private  List<String> nameList; //数据项
	private List<CheckBox> mCheckBoxList=new ArrayList<CheckBox>();
	private OnItemSelected onItemSelectedListener;
	public PresentCardCheckBox(Context context)
	{
		super(context);
		this.setOrientation(VERTICAL);
		mlayoutinf=LayoutInflater.from(context);
	}
	
	public PresentCardCheckBox(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.setOrientation(VERTICAL);
		mlayoutinf=LayoutInflater.from(context);
	}

	public List<String> getNameList()
	{
		return nameList;
	}

	public void setNameList(List<String> nameList)
	{
		this.nameList = nameList;
		init();
	}
	public void init()
	{
	   if(nameList!=null&&nameList.size()!=0)
	   {
		   for(int i=0;i<nameList.size();i++)
		   {
			  
			  View contentView=initComponent(i);
			  this.addView(contentView);
			   
		   }
	   }
	}
    public interface OnItemSelected {
		
		public void onSelected(int position,boolean isSelected);
	}
	public OnItemSelected getOnItemSelectedListener()
	{
		return onItemSelectedListener;
	}

	public void setOnItemSelectedListener(OnItemSelected onItemSelectedListener)
	{
		this.onItemSelectedListener = onItemSelectedListener;
	}
	
	public View initComponent(final int position)
	{
		 View contentView=mlayoutinf.inflate(R.layout.present_card_item, null);
		 TextView tv=(TextView) contentView.findViewById(R.id.present_card_tv);
		 final CheckBox box=(CheckBox) contentView.findViewById(R.id.present_card_box);
		 mCheckBoxList.add(box);
		 tv.setText(nameList.get(position));
		 
		 box.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				ViewUtils.preventViewMultipleClick(v, 1000);
				if(onItemSelectedListener!=null)
				{
					
					onItemSelectedListener.onSelected(position,box.isChecked());
				}
				for(int i=0;i<mCheckBoxList.size();i++)
				{
					if(i!=position)
					{
						mCheckBoxList.get(i).setChecked(false);
					}
				}
			}
		});
		 return contentView;
		
	}
	
	public void setCanChecked(boolean canCheck)
	{
		if(canCheck)
		{
			for(CheckBox eachBox:mCheckBoxList)
			{
				
				eachBox.setEnabled(true);
			}
			
		}
		else
		{
			for(CheckBox eachBox:mCheckBoxList)
			{
				
				eachBox.setEnabled(false);
			}
		}
	}

}

package com.fg114.main.app.view;

import java.util.ArrayList;
import java.util.List;

import com.fg114.main.util.ViewUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class EllipsizeText extends TextView
{

	private static final String ELLIPSIS = "...";

	public interface EllipsizeListener
	{
		void ellipsizeStateChanged(boolean ellipsized);
	}

	private final List<EllipsizeListener> ellipsizeListeners = new ArrayList<EllipsizeListener>();
    private List<int[]> startEndPoi=new ArrayList<int[]>();
	private boolean isEllipsized;
	private boolean isStale;
	private boolean programmaticChange;
	private String fullText;
	private int maxLines = 2;
	private float lineSpacingMultiplier = 1.0f;
	private float lineAdditionalVerticalPadding = 0.0f;

	public EllipsizeText(Context context)
	{
		super(context);
	}

	public EllipsizeText(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public EllipsizeText(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public void addEllipsizeListener(EllipsizeListener listener)
	{
		if (listener == null) {
			throw new NullPointerException();
		}
		ellipsizeListeners.add(listener);
	}

	public synchronized final List<int[]> getStartEndPoi()
	{
		return startEndPoi;
	}

	public synchronized final void setStartEndPoi(List<int[]> startEndPoi)
	{
		this.startEndPoi = startEndPoi;
	}

	public void removeEllipsizeListener(EllipsizeListener listener)
	{
		ellipsizeListeners.remove(listener);
	}

	public boolean isEllipsized()
	{
		return isEllipsized;
	}

	@Override
	public void setMaxLines(int maxLines)
	{
		super.setMaxLines(maxLines);
		this.maxLines = maxLines;
		isStale = true;
	}

	public int getMaxLines()
	{
		return maxLines;
	}

	@Override
	public void setLineSpacing(float add, float mult)
	{
		this.lineAdditionalVerticalPadding = add;
		this.lineSpacingMultiplier = mult;
		super.setLineSpacing(add, mult);
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int before, int after)
	{
		super.onTextChanged(text, start, before, after);
		if (!programmaticChange) {
			fullText = text.toString();
			isStale = true;
		}
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		if (isStale) {
			super.setEllipsize(null);
			resetText();
		}
		
		super.onDraw(canvas);
	}

	private void resetText()
	{
		int maxLines = getMaxLines();
		String workingText = fullText;
		//System.out.println("fullText =" + fullText);
		boolean ellipsized = false;
		if (maxLines != -1) {
			Layout layout = createWorkingLayout(workingText);
			if (layout.getLineCount() > maxLines) {

				//System.out.println("layout line count =" + layout.getLineCount() + "\t maxLines =" + maxLines);
				workingText = fullText.substring(0, (layout.getLineEnd(maxLines - 1) - 1)).trim();
				Layout layout2 = createWorkingLayout(workingText + ELLIPSIS);
				while (layout2.getLineCount() > maxLines) {
					//System.out.println("layout2 line count =" + layout2.getLineCount() + "\t maxLines =" + maxLines);
					int lastSpace = workingText.lastIndexOf(' ');
					//System.out.println("lastSpace =" + lastSpace);
					if (lastSpace == -1) {
						break;
					}
					workingText = workingText.substring(0, lastSpace);
				}
				workingText = workingText + ELLIPSIS;
				
				ellipsized = true;
			}
		}
		if (!workingText.equals(getText())) {
			programmaticChange = true;
			try {
				
				initHotNumData(workingText);
				setText(workingText);
				ViewUtils.setHighlight(Color.GRAY, this, startEndPoi);
			} finally {
				programmaticChange = false;
			}
		}
		else
		{
			ViewUtils.setHighlight(Color.GRAY, this, startEndPoi);
		}
		isStale = false;
		if (ellipsized != isEllipsized) {
			isEllipsized = ellipsized;
			for (EllipsizeListener listener : ellipsizeListeners) {
				listener.ellipsizeStateChanged(ellipsized);
			}
		}
	}

	private Layout createWorkingLayout(String workingText)
	{
		return new StaticLayout(workingText, getPaint(), getWidth() - getPaddingLeft() - getPaddingRight(), Alignment.ALIGN_NORMAL, lineSpacingMultiplier, lineAdditionalVerticalPadding, false);
	}

	@Override
	public void setEllipsize(TruncateAt where)
	{
		// Ellipsize settings are not respected } }
	}

	//根据截取后的字符串来重新获得需要改变颜色的字体的位置
	public void initHotNumData(String tempString)
	{
		/*for(int i=0;i<startEndPoi.size();i++)
		{
			Log.e("bug", i+"点："+startEndPoi.get(i)[0]+": "+startEndPoi.get(i)[1]);
		}*/
		
		List<int[]> tempList=new ArrayList<int[]>();
		for(int i=0;i<startEndPoi.size();i++)
		{
			if(startEndPoi.get(i)[0]<=tempString.length())
			{
				tempList.add(startEndPoi.get(i));
			}
		}
		startEndPoi=tempList;
		
		
	}
}
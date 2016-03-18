package com.fg114.main.weibo;

import com.fg114.main.weibo.sina.SinaWeiboUtil;
import com.fg114.main.weibo.tencent.TencentWeiboUtil;

/**
 * 根据参数构建具体的某个平台(例如：新浪、腾讯)的WeiboUtil类。
 * 
 * @author xujianjun,2012-08-05
 */
public class WeiboUtilFactory {
	// 标识微博的种类的flag，存储的是具类的类名，方便存储和实例化

	public static final int PLATFORM_SINA_WEIBO = 1;
	public static final int PLATFORM_TENCENT_WEIBO = 2;

	public static WeiboUtil getWeiboUtil(int platform) {
		switch (platform) {
		case PLATFORM_SINA_WEIBO:
			return SinaWeiboUtil.getInstance();
		case PLATFORM_TENCENT_WEIBO:
			return TencentWeiboUtil.getInstance();
		default:
			throw new RuntimeException("Unsupported weibo platform! -> platform=" + platform);
		}
	}
}

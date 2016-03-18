package com.fg114.main.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONObject;

import com.fg114.main.R;
import com.fg114.main.app.data.BaseData;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.service.dto.CommonTypeDTO;
import com.fg114.main.service.dto.JsonPack;

/**
 * @author Sanvi E-mail:sanvibyfish@gmail.com
 * @version 创建时间：2010-8-31 下午01:22:13
 */
public class ConvertUtil {
	
	public static final String DATE_FORMAT_YYYYMMDD_HHMI = "yyyy-MM-dd HH:mm"; 
	public static final String DATE_FORMAT_YYYYMMDD000000 = "yyyyMMddHHmmss"; 
	public static final String DATE_FORMAT_YYYYMMDD = "yyyy-MM-dd"; 
	
	public static final String DATE_FORMAT_YYYYMMDD_HHMM = "yyyy-MM-dd / HH:mm"; // 语音订餐的日期样式
	
	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		StringBuilder sb = new StringBuilder();
		try {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is),32*1024);
		String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
			//网络异常时保存异常信息并调整返回数据
			String msg = "error_net in convertStreamToString ";
			ActivityUtil.saveException(e, msg);
			//--
			sb = new StringBuilder();
			sb.append("{");
			sb.append("re:400;");
			sb.append("msg:\""+ContextUtil.getContext().getString(R.string.text_error_net)+"\";");
			sb.append("obj:{}");
			sb.append("}");
			
			
			return sb.toString();
		} catch (OutOfMemoryError e) {
			ActivityUtil.saveOutOfMemoryError(e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	public static List<BaseData> convertDTOListToDataList(List<CommonTypeDTO> list, int rank) {
		
		List<BaseData> newList = new ArrayList<BaseData>();
		if (list != null) {
			BaseData data = null;
			for (CommonTypeDTO dto : list) {
				data = convertDTOToData(dto, rank);
				newList.add(data);
			}
		}
		return newList;
	}
	
	public static BaseData convertDTOToData(CommonTypeDTO dto, int rank) {
		
		BaseData newData = null;
		if (dto != null) {
			newData = new BaseData();
			newData.setId(dto.getUuid());
			newData.setName(dto.getName());
			newData.setParentId(dto.getParentId());
			newData.setNum(dto.getNum());
			newData.setRank(rank);
			newData.setSelectTag(dto.isSelectTag());
		}
		return newData;
	}
	
	public static List<CityInfo> convertDTOListToCityList(List<CommonTypeDTO> list) {
		
		List<CityInfo> newList = new ArrayList<CityInfo>();
		if (list != null) {
			CityInfo data = null;
			for (CommonTypeDTO dto : list) {
				data = new CityInfo();
				data.setId(dto.getUuid());
				data.setName(dto.getName());
				data.setParentId(dto.getParentId());
				data.setNum(dto.getNum());
				data.setPhone(dto.getPhone());
				newList.add(data);
			}
		}
		return newList;
	}
	
	public static String convertLongToDateString(long time, String pattern) {
		
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		return sdf.format(new Date(time));
	}
	
	public static long convertDateStringToLong(String dateStr, String pattern) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			return sdf.parse(dateStr).getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	/**
	 * inputStream to byte[]
	 * @param iStrm
	 * @return
	 * @throws IOException
	 */
	public static byte[] convertInputStreamToByte(InputStream stream) {
	    ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
	    byte[] imgdata = null;
	    byte[] buffer = new byte[1024];
	    try {
		    while ((stream.read(buffer)) != -1) {
		       bytestream.write(buffer);
		    }
		    imgdata = bytestream.toByteArray();
		    bytestream.close();
	    } catch (IOException e) {
			e.printStackTrace();
		}
	    return imgdata;
	}
	
	public static String cutString(String input, int length) {
		if (input.length() <= length) {
			return input;
		}
		return input.substring(0, length);
	}
	
	public static String subString(String strOrg, int start) {
		return new String(strOrg.substring(start));
	}

	public static String subString(String strOrg, int start, int end) {
		return new String(strOrg.substring(start, end));
	}
	
//	/**
//	 * byte[] to int[]
//	 * @param iStrm
//	 * @return
//	 * @throws IOException
//	 */
//	public static String byteArrayToJsonString(byte [] byteArray) {
//		StringBuffer sbStr = new StringBuffer();
//		sbStr.append("[");
//		for(int i = 0; i < byteArray.length; i++) {
//			sbStr.append(Byte.toString(byteArray[i]));
//			if (i < (byteArray.length - 1)) {
//				sbStr.append(",");
//			}
//		}
//		sbStr.append("]");
//		return sbStr.toString();
//	}
}

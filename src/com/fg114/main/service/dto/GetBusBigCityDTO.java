package com.fg114.main.service.dto;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * 根据经纬度及城市代码查询大城市公交换乘
 * @author wufucheng
 * 
 */
public class GetBusBigCityDTO implements Serializable {

	public int count; // 结果总数
	public List<GetBusBigCityItem> items = new ArrayList<GetBusBigCityItem>();;
	public MapInfo mapinfo;
	public String sort;
	public LonLat orig;

	public GetBusBigCityDTO getFromXml(String strXml) {
		GetBusBigCityDTO dto = new GetBusBigCityDTO();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			XMLReader reader = factory.newSAXParser().getXMLReader();
			GetBusBigCityHandler contentHandler = new GetBusBigCityHandler();
			reader.setContentHandler(contentHandler);
			reader.parse(new InputSource(new StringReader(strXml)));
			dto = contentHandler.getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dto;
	}

	private class GetBusBigCityHandler extends DefaultHandler {

		private StringBuffer sb = new StringBuffer();
		private GetBusBigCityDTO dto;
		private GetBusBigCityItem item;
		private MapInfo mapinfo;
		private LonLat orig;

		public GetBusBigCityHandler() {
			super();
		}

		public GetBusBigCityDTO getResult() {
			return dto;
		}

		public void startDocument() throws SAXException {
			super.startDocument();
		}

		public void endDocument() throws SAXException {
			super.endDocument();
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			sb.delete(0, sb.length());
			if ("result".equals(localName)) {
				dto = new GetBusBigCityDTO();
			} else if ("bus".equals(localName)) {
				dto.count = Integer.parseInt(attributes.getValue("count"));
			} else if ("item".equals(localName)) {
				item = new GetBusBigCityItem();
				item.id = attributes.getValue("id");
			} else if ("mapinfo".equals(localName)) {
				mapinfo = new MapInfo();
			}
			else if ("orig".equals(localName)) {
				orig = new LonLat();
			}
			super.startElement(uri, localName, qName, attributes);
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			if ("result".equals(localName)) {

			} else if ("item".equals(localName)) {
				if (item != null) {
					dto.items.add(item);
				}
				item = null;
			} else if ("mapinfo".equals(localName)) {
				if (mapinfo != null) {
					dto.mapinfo = mapinfo;
				}
				mapinfo = null;
			} else if ("detail".equals(localName)) {
				item.detail = sb.toString();
			} else if ("time".equals(localName)) {
				item.time = sb.toString();
			} else if ("distance".equals(localName)) {
				item.distance = sb.toString();
			} else if ("walk".equals(localName)) {
				String[] strLatlons = sb.toString().split(";");
				if (strLatlons != null && strLatlons.length == 2) {
					String[] strLatlon = strLatlons[0].split(",");
					LonLat latlon = new LonLat(Double.parseDouble(strLatlon[0]), Double.parseDouble(strLatlon[1]));
					latlon.name = "";
					item.walkroutes.add(latlon);
					strLatlon = strLatlons[1].split(",");
					latlon = new LonLat(Double.parseDouble(strLatlon[0]), Double.parseDouble(strLatlon[1]));
					item.walkroutes.add(latlon);
				}
			} else if ("center".equals(localName)) {
				String[] strs = sb.toString().split(",");
				if (strs != null && strs.length == 2) {
					LonLat latlon = new LonLat(Double.parseDouble(strs[0]), Double.parseDouble(strs[1]));
					mapinfo.center = latlon;
				}
			} else if ("scale".equals(localName)) {
				mapinfo.scale = Integer.parseInt(sb.toString());
			} else if ("sort".equals(localName)) {
				dto.sort = sb.toString();
			}
			else if ("orig".equals(localName)) {
				String[] strs = sb.toString().split(",");
				if (strs != null && strs.length == 2) {
					orig.longitude = Double.parseDouble(strs[0]);
					orig.latitude = Double.parseDouble(strs[1]);
					dto.orig = orig;
				}
			}
			super.endElement(uri, localName, qName);
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			sb.append(ch, start, length);
			super.characters(ch, start, length);
		}
	}

	public class GetBusBigCityItem implements Serializable {
		public String id;
		public String detail; // 换乘描述
		public String time; // 耗时
		public String distance; // 全长（单位：公里）
		public List<LonLat> walkroutes = new ArrayList<LonLat>(); // 换乘路线
	}
}

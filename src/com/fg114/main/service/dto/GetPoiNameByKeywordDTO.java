package com.fg114.main.service.dto;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * 根据关键字查询地标点名称列表
 * @author wufucheng
 * 
 */

public class GetPoiNameByKeywordDTO {

	public int count; // 结果总数
	public List<GetPoiNameByKeywordItem> items = new ArrayList<GetPoiNameByKeywordItem>();

	/**
	 * 将xml转为实例
	 * @param strXml
	 * @return
	 */
	public GetPoiNameByKeywordDTO getFromXml(String strXml) {
		GetPoiNameByKeywordDTO dto = new GetPoiNameByKeywordDTO();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			XMLReader reader = factory.newSAXParser().getXMLReader();
			GetPoiNameByKeywordHandler contentHandler = new GetPoiNameByKeywordHandler();
			reader.setContentHandler(contentHandler);
			reader.parse(new InputSource(new StringReader(strXml)));
			dto = contentHandler.getResult();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dto;
	}

	/**
	 * 解读xml
	 * @author wufucheng
	 * 
	 */
	private class GetPoiNameByKeywordHandler extends DefaultHandler {

		private StringBuffer sb = new StringBuffer();
		private GetPoiNameByKeywordDTO dto;
		private GetPoiNameByKeywordItem item;

		public GetPoiNameByKeywordHandler() {
			super();
		}

		public GetPoiNameByKeywordDTO getResult() {
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
				dto = new GetPoiNameByKeywordDTO();
			} else if ("pois".equals(localName)) {
				dto.count = Integer.parseInt(attributes.getValue("count"));
			} else if ("item".equals(localName)) {
				item = new GetPoiNameByKeywordItem();
				item.id = attributes.getValue("id");
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
			} else if ("name".equals(localName)) {
				item.name = sb.toString().trim();
			} else if ("address".equals(localName)) {
				item.address = sb.toString().trim();
			} else if ("strlatlon".equals(localName)) {
				String[] strs = sb.toString().split(",");
				if (strs != null && strs.length == 2) {
					LonLat latlon = new LonLat(Double.parseDouble(strs[0]), Double.parseDouble(strs[1]));
					item.latlon = latlon;
				}
			}
			super.endElement(uri, localName, qName);
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			sb.append(ch, start, length);
			super.characters(ch, start, length);
		}
	}

	/**
	 * 根据关键字查询地标点名称列表的结果
	 * @author wufucheng
	 * 
	 */
	public class GetPoiNameByKeywordItem {
		public String id; // 地点id
		public String name; // 地点名称
		public String address; // 地点地址
		public LonLat latlon; // 地点经纬度
	}
}

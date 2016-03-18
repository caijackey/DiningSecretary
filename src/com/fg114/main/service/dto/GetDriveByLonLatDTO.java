package com.fg114.main.service.dto;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * 根据起点、终点经纬度查询 驾车路线
 * @author wufucheng
 * 
 */

public class GetDriveByLonLatDTO implements Serializable {

	public int count; // 分段总数
	public List<GetDriveByLonLatItem> turns = new ArrayList<GetDriveByLonLatItem>();	// 转向点
	public List<GetDriveByLonLatItem> itemsToDraw = new ArrayList<GetDriveByLonLatItem>();	// 除了起点和终点外还需要绘制的路点
	public MapInfo mapinfo;
	public String style;
	public LonLat orig;
	private HashMap<String, GetDriveByLonLatItem> turnMap = new HashMap<String, GetDriveByLonLatItem>();

	public GetDriveByLonLatDTO getFromXml(String strXml) {
		GetDriveByLonLatDTO dto = new GetDriveByLonLatDTO();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			XMLReader reader = factory.newSAXParser().getXMLReader();
			GetDriveByLatLonHandler contentHandler = new GetDriveByLatLonHandler();
			reader.setContentHandler(contentHandler);
			reader.parse(new InputSource(new StringReader(strXml)));
			dto = contentHandler.getResult();
			//整理转折点和绘制的路点
			GetDriveByLonLatItem itemTmp = null;
			if (dto.turns.size() > 0 && dto.itemsToDraw.size() > 0) {
				for (GetDriveByLonLatItem itemToDraw : dto.itemsToDraw) {
					if (turnMap.containsKey(itemToDraw.lonlat.toPair())) {
						itemTmp = turnMap.get(itemToDraw.lonlat.toPair());
						itemToDraw.id = itemTmp.id;
						itemToDraw.strguide = itemTmp.strguide;;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dto;
	}

	private class GetDriveByLatLonHandler extends DefaultHandler {

		private StringBuffer sb = new StringBuffer();
		private GetDriveByLonLatDTO dto;
		private GetDriveByLonLatItem item;
		private GetDriveByLonLatItem itemToDraw;
		private MapInfo mapinfo;
		private LonLat orig;

		public GetDriveByLatLonHandler() {
			super();
		}

		public GetDriveByLonLatDTO getResult() {
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
				dto = new GetDriveByLonLatDTO();
			} else if ("routes".equals(localName)) {
				dto.count = Integer.parseInt(attributes.getValue("count"));
			} else if ("item".equals(localName)) {
				item = new GetDriveByLonLatItem();
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
					dto.turns.add(item);
					turnMap.put(item.lonlat.toPair(), item);
				}
				item = null;
			} else if ("mapinfo".equals(localName)) {
				if (mapinfo != null) {
					dto.mapinfo = mapinfo;
				}
				mapinfo = null;
			} else if ("strguide".equals(localName)) {
				item.strguide = sb.toString();
			} else if ("turnlatlon".equals(localName)) {
				String[] strs = sb.toString().split(",");
				if (strs != null && strs.length == 2) {
					LonLat latlon = new LonLat(Double.parseDouble(strs[0]), Double.parseDouble(strs[1]));
					item.lonlat = latlon;
				}
			} else if ("center".equals(localName)) {
				String[] strs = sb.toString().split(",");
				if (strs != null && strs.length == 2) {
					LonLat latlon = new LonLat(Double.parseDouble(strs[0]), Double.parseDouble(strs[1]));
					mapinfo.center = latlon;
				}
			} else if ("scale".equals(localName)) {
				mapinfo.scale = Integer.parseInt(sb.toString());
			} else if ("style".equals(localName)) {
				dto.style = sb.toString();
			}
			else if ("orig".equals(localName)) {
				String[] strs = sb.toString().split(",");
				if (strs != null && strs.length == 2) {
					orig.longitude = Double.parseDouble(strs[0]);
					orig.latitude = Double.parseDouble(strs[1]);
					dto.orig = orig;
				}
			}
			else if ("routelatlon".equals(localName)) {
				String[] strs = sb.toString().split(";");
				if (strs != null && strs.length > 0) {
					for (int i=1; i < strs.length - 2; i++) {
						if (strs[i].equals("")) {
							continue;
						}
						String[] strsLatlon = strs[i].split(",");
						if (strsLatlon != null && strsLatlon.length == 2) {
							itemToDraw = new GetDriveByLonLatItem();
							itemToDraw.id = "";
							LonLat lonlat = new LonLat(Double.parseDouble(strsLatlon[0]), Double.parseDouble(strsLatlon[1]));
							itemToDraw.lonlat = lonlat;
							itemToDraw.strguide = "";
							dto.itemsToDraw.add(itemToDraw);
						}
					}
				}
			}
			super.endElement(uri, localName, qName);
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			sb.append(ch, start, length);
			super.characters(ch, start, length);
		}
	}

	public class GetDriveByLonLatItem implements Serializable {
		public String id;
		public String strguide; // 每段线路文字描
		public LonLat lonlat; // 转折点经纬度
	}
}

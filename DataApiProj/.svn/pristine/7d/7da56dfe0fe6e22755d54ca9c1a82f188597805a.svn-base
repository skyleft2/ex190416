/**
 * 중기예보조회
 * 20170512 - Wijy
 */
package api;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IndexMidFcst extends IndexData {
	String idxMf_mf_str = "IDXMF_MF";
	String idxMf_mf_nm = "중기예보 > 중기기상전망";
	String idxMf_lw_str = "IDXMF_LW";
	String idxMf_lw_nm = "중기예보 > 중기육상예보";
	String idxMf_tp_str = "IDXMF_TP";
	String idxMf_tp_nm = "중기예보 > 중기기온예보";
	
	//중기기상전망조회
	String idxMf_mf = "http://newsky2.kma.go.kr/service/MiddleFrcstInfoService/getMiddleForecast";
	//중기육상예보
	String idxMf_lw = "http://newsky2.kma.go.kr/service/MiddleFrcstInfoService/getMiddleLandWeather";
	//중기기온예보
	String idxMf_tp = "http://newsky2.kma.go.kr/service/MiddleFrcstInfoService/getMiddleTemperature";
	
	/*
	String mfAreaCd = "108";			//중기기상전망조회 지점번호(전국) - 서울(109)코드를 쓰는지는 확인 필요. 기존엔 전국코드를 사용함
	String lwAreaCd = "11B00000";		//중기육상예보 구역코드(서울, 인천, 경기도)
	String tpAreaCd = "11B10101";		//중기기온예보 구역코드(서울)
	*/
	
	//중기예보 코드 목록
	String[] aMfAreaCd = {"109"	//서울, 인천, 경기도
	};
	
	String[] aLwAreaCd = {
			  "11B00000" //서울, 인천, 경기도
	};
	
	String[] aTpAreaCd = {
			  "11B10101" 	//서울
	};
	
	//날짜값
	String baseDate = "";
	//중기예보 저장시 stn_id
	String mfAreaCd = "";
	
	@Override
	public void getData() throws Exception {
		// TODO Auto-generated method stub
		String paramStr = "numOfRows=999";
		
		//일 2회 (6:00/18:00) 생성
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String today = df.format(calendar.getTime());
		int thisHour = calendar.get(Calendar.HOUR_OF_DAY);
				
		if(thisHour < 18)
			baseDate = today + "0600";
		else 
			baseDate = today + "1800";
		
		paramStr += "&tmFc=" + baseDate;
		
		//중기기상전망
		for(String cd : aMfAreaCd) {
			mfAreaCd = cd;
			getData(idxMf_mf_str, idxMf_mf, paramStr + "&stnId=" + cd);			
		}
		
		//중기육상예보
		for(String cd : aLwAreaCd) {
			getData(idxMf_lw_str, idxMf_lw, paramStr + "&regId=" + cd);
		}
		
		//중기기온예보
		for(String cd : aTpAreaCd) {
			getData(idxMf_tp_str, idxMf_tp, paramStr + "&regId=" + cd);
		}
		/*
		getData(idxMf_mf_str, idxMf_mf, paramStr + "&stnId=" + mfAreaCd);
		getData(idxMf_lw_str, idxMf_lw, paramStr + "&regId=" + lwAreaCd);
		getData(idxMf_tp_str, idxMf_tp, paramStr + "&regId=" + tpAreaCd);
		*/
		
	}
	
	
	private int getData(String IDX, String url, String param) throws Exception
	{
		int res = (-1);
		File fXml = comm.getXML(IDX, url, param);
		Document doc = comm.parseXML(fXml);
		
		/** LOG **/
		logging.initMap();	//초기화
		logging.setMap("LINK_ID", IDX);
		logging.setMap("SYS_CODE", "ROAD");
		
		if(IDX.equals(idxMf_mf_str)) logging.setMap("LINK_KO_NM", idxMf_mf_nm);
		if(IDX.equals(idxMf_lw_str)) logging.setMap("LINK_KO_NM", idxMf_lw_nm);
		if(IDX.equals(idxMf_tp_str)) logging.setMap("LINK_KO_NM", idxMf_tp_nm);
		
		logging.setMap("REQ_URL", comm.url);
		
		// 결과코드 체크
		if (!checkResult("resultCode", "resultMsg", "0000", doc)) {
			logging.logging();
			return res;
		}
		// Count체크
		if (!checkResultCount("totalCount", doc)) {
			logging.logging();
			return 0;		
		}
		
		res = executeMidFcstData("INS", IDX, doc);
		
		logging.logging();
		return res;
	}


	private int executeMidFcstData(String sqlIdx, String IDX, Document doc) {
		int res = 0;
		
		if(sqlIdx.equals("INS"))
		{
			try {
				NodeList parents = (NodeList)xpath.evaluate("//items/item", doc, XPathConstants.NODESET);
				HashMap<String, String> paramMap = generateParamMap(IDX);
				
				if(IDX.equals(idxMf_mf_str))
				{
					for(int i = 0; i<parents.getLength(); i++)
					{	
						Element el = (Element)parents.item(i);
						
						NodeList value = el.getElementsByTagName("wfSv");
						paramMap.put("WF_SV", value.item(0).getTextContent());
					}
					paramMap.put("STN_ID", mfAreaCd);
					paramMap.put("BASEDATE", baseDate);
										
					String sql = 
						"INSERT INTO TN_MIDFCST_MF(SDATE, WF_SV, STN_ID, ENT_DATE) " 
						+ "SELECT ?, ?, ?, sysdate FROM DUAL "
						+ "WHERE NOT EXISTS ("
						+ "SELECT * FROM TN_MIDFCST_MF WHERE SDATE=? AND STN_ID=?)";
					List<String> paramList = new ArrayList<String>();
					paramList.add(paramMap.get("BASEDATE"));
					paramList.add(paramMap.get("WF_SV"));
					paramList.add(paramMap.get("STN_ID"));
					paramList.add(paramMap.get("BASEDATE"));
					paramList.add(paramMap.get("STN_ID"));
					
					res = iDao.executeIdxSql(sql, paramList);
				} 
				else if (IDX.equals(idxMf_lw_str))
				{
					paramMap.put("SDATE", baseDate);
					
					NodeList els = parents.item(0).getChildNodes();					
					for(int i = 0; i<els.getLength(); i++)
					{	
						Node dataNodes = els.item(i);
						String nodeName = dataNodes.getNodeName().toUpperCase();
						String nodeValue = dataNodes.getTextContent();
						paramMap.put(nodeName, nodeValue);						
					}
					
					List<String> paramList = new ArrayList<String>();
					String insSql = 
							"INSERT INTO TN_MIDWEATHER " +
								"(SDATE, REGID, " + 
								"WF3AM, WF3PM, WF4AM, WF4PM, WF5AM, WF5PM, WF6AM, WF6PM, WF7AM, WF7PM, " +
								"WF8, WF9, WF10, ENT_DATE) " +
							"SELECT " + 
								"?, ?, " +
								"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, sysdate " +
							"FROM DUAL " +
							"WHERE NOT EXISTS (SELECT * FROM TN_MIDWEATHER WHERE SDATE=? AND REGID=?)";
					
					paramList.add(paramMap.get("SDATE"));
					paramList.add(paramMap.get("REGID"));
					paramList.add(paramMap.get("WF3AM"));
					paramList.add(paramMap.get("WF3PM"));
					paramList.add(paramMap.get("WF4AM"));
					paramList.add(paramMap.get("WF4PM"));
					paramList.add(paramMap.get("WF5AM"));
					paramList.add(paramMap.get("WF5PM"));
					paramList.add(paramMap.get("WF6AM"));
					paramList.add(paramMap.get("WF6PM"));
					paramList.add(paramMap.get("WF7AM"));
					paramList.add(paramMap.get("WF7PM"));
					paramList.add(paramMap.get("WF8"));
					paramList.add(paramMap.get("WF9"));
					paramList.add(paramMap.get("WF10"));
					paramList.add(paramMap.get("SDATE"));
					paramList.add(paramMap.get("REGID"));
				
					res = iDao.executeIdxSql(insSql, paramList);
				}
				else if (IDX.equals(idxMf_tp_str))
				{
					paramMap.put("SDATE", baseDate);
					
					NodeList els = parents.item(0).getChildNodes();					
					for(int i = 0; i<els.getLength(); i++)
					{	
						Node dataNodes = els.item(i);
						String nodeName = dataNodes.getNodeName().toUpperCase();
						String nodeValue = dataNodes.getTextContent();
						paramMap.put(nodeName, nodeValue);						
					}
					
					
					List<String> paramList = new ArrayList<String>();
					String insSql = 
							"INSERT INTO TN_MIDTEMPERATURE " +
								"(SDATE, REGID, " + 
								"TAMIN3, TAMAX3, TAMIN4, TAMAX4, TAMIN5, TAMAX5, TAMIN6, TAMAX6, TAMIN7, TAMAX7, " +
								"TAMIN8, TAMAX8, TAMIN9, TAMAX9, TAMIN10, TAMAX10, ENT_DATE) " +
							"SELECT " + 
								"?, ?, " +
								"?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " +
								"?, ?, ?, ?, ?, ?, sysdate " +
							"FROM DUAL " +
							"WHERE NOT EXISTS (SELECT * FROM TN_MIDTEMPERATURE WHERE SDATE=? AND REGID=?)";
					
					paramList.add(paramMap.get("SDATE"));
					paramList.add(paramMap.get("REGID"));
					paramList.add(paramMap.get("TAMIN3"));
					paramList.add(paramMap.get("TAMAX3"));
					paramList.add(paramMap.get("TAMIN4"));
					paramList.add(paramMap.get("TAMAX4"));
					paramList.add(paramMap.get("TAMIN5"));
					paramList.add(paramMap.get("TAMAX5"));
					paramList.add(paramMap.get("TAMIN6"));
					paramList.add(paramMap.get("TAMAX6"));
					paramList.add(paramMap.get("TAMIN7"));
					paramList.add(paramMap.get("TAMAX7"));
					paramList.add(paramMap.get("TAMIN8"));
					paramList.add(paramMap.get("TAMAX8"));
					paramList.add(paramMap.get("TAMIN9"));
					paramList.add(paramMap.get("TAMAX9"));
					paramList.add(paramMap.get("TAMIN10"));
					paramList.add(paramMap.get("TAMAX10"));
					paramList.add(paramMap.get("SDATE"));
					paramList.add(paramMap.get("REGID"));
					
					res = iDao.executeIdxSql(insSql, paramList);
				}				
				
			} catch (Exception e) {
				System.out.println("  [EXCEPTION] : IndexMidFcst.executeMidFcstData(String sqlIdx, String IDX, Document doc)  ");
				e.printStackTrace();
				
				logging.setMap("RES_MSG", "[ERR] " + e.getMessage());
				logging.logging();
				
				return (-1);
			}
		}
		
		logging.setMap("DB_CNT", String.valueOf(res));
		logging.setMap("SUCCESS_YN", "Y");
		
		return res;
	}
	
	
	private HashMap<String, String> generateParamMap(String IDX)
	{
		HashMap<String, String> res = new HashMap<String, String>();
		
		if(IDX.equals(idxMf_lw_str))
		{
			res.put("SDATE", "");
			res.put("REGID", "");			//요청시간
			res.put("WF3AM", "");			//3일후 오전날씨
			res.put("WF3PM", "");			//3일후 오후날씨
			res.put("WF4AM", "");			//4일후 오전날씨
			res.put("WF4PM", "");			//4일후 오후날씨
			res.put("WF5AM", "");			//5일후 오전날씨
			res.put("WF5PM", "");			//5일후 오후날씨
			res.put("WF6AM", "");			//6일후 오전날씨
			res.put("WF6PM", "");			//6일후 오후날씨
			res.put("WF7AM", "");			//7일후 오전날씨
			res.put("WF7PM", "");			//7일후 오후날씨
			res.put("WF8", "");				//8일후 날씨
			res.put("WF9", "");				//9일후 날씨
			res.put("WF10", "");			//10일후 날씨
		}
		else if (IDX.equals(idxMf_tp_str))
		{
			res.put("SDATE", "");
			res.put("REGID", "");			//요청시간
			res.put("TAMIN3", "");			//3일후 최저기온
			res.put("TAMAX3", "");			//3일후 최고기온
			res.put("TAMIN4", "");			//4일후 최저기온
			res.put("TAMAX4", "");			//4일후 최고기온
			res.put("TAMIN5", "");			//5일후 최저기온
			res.put("TAMAX5", "");			//5일후 최고기온
			res.put("TAMIN6", "");			//6일후 최저기온
			res.put("TAMAX6", "");			//6일후 최고기온
			res.put("TAMIN7", "");			//7일후 최저기온
			res.put("TAMAX7", "");			//7일후 최고기온
			res.put("TAMIN8", "");			//8일후 최저기온
			res.put("TAMAX8", "");			//8일후 최고기온
			res.put("TAMIN9", "");			//9일후 최저기온
			res.put("TAMAX9", "");			//9일후 최고기온
			res.put("TAMIN10", "");			//10일후 최저기온
			res.put("TAMAX10", "");			//10일후 최고기온
		}
		
		return res;
	}

}

package api;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  180511 - wijy
 *	지상관측자료 월보 - 일평균데이터 조회 
 */

public class IndexStndays extends IndexData {
	String idx = "AWS_STNDAYS";
	String idxNm = "AWS-월별 일평균데이터";
	
	//url : idxUrl + 연/월 + idxUrlFileNm
	String idxUrl = "https://data.kma.go.kr/OPEN_API/SYNM/";
	String idxUrlFileNm = "/XML/stndays_108.xml";	//108 서울 지점번호
	
	String targetYear = "";
	String targetMonth = "";
	
	@Override
	public void getData() throws Exception {
		//log
		logging.initMap();
		
		//날짜
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		month -= 2;
		if(month < 0) month = 12 + month;
		targetYear = "" + year;
		targetMonth = String.format("%02d", month + 1);
		
		String idxParam = targetYear + "/" + targetMonth;
		
		String serviceUrl = idxUrl + idxParam + idxUrlFileNm;
		
		//log param
		logging.setMap("LINK_ID", idx);
		logging.setMap("LINK_KO_NM", idxNm);
		logging.setMap("REQ_URL", serviceUrl);
		logging.setMap("SYS_CODE", "ROAD");
		
		int resultCnt = getData(idx, serviceUrl);
		logging.setMap("DB_CNT", resultCnt + "");
		
		logging.logging();		
	}
	
	private int getData(String idx, String serviceUrl) throws Exception {
		int result = (-1);
		
		File fXml = comm.getXML(idx, serviceUrl, "");				//File 생성
		if(fXml != null) {
			Document doc = comm.parseXML(fXml);			
			result = executeData(idx, doc);
		}
		return result;
	}
	
	private int executeData(String idx, Document doc) {
		int result = (-1);
		
		try{
			List<Map> paramMapList = new LinkedList<Map>();
			
			NodeList stnDays = (NodeList)xpath.evaluate("//stndays", doc, XPathConstants.NODESET);
			Element el = (Element)stnDays.item(0);
				
			NodeList stnKoNode = el.getElementsByTagName("stn_ko");
			NodeList stnEnNode = el.getElementsByTagName("stn_en");
			String stnKo = stnKoNode.item(0).getTextContent();
			String stnEn = stnEnNode.item(0).getTextContent(); 
			
			NodeList infos = el.getElementsByTagName("info");
			
			logging.setMap("REQ_CNT", infos.getLength() + "");
			if(infos.getLength() > 0) {
				for(int i = 0; i<infos.getLength(); i++) {
					HashMap<String, String> paramMap = generateParamMap();
					//공통내용
					paramMap.put("tm_year", targetYear);
					paramMap.put("tm_month", targetMonth);
					paramMap.put("stn_id", "108");
					paramMap.put("stn_ko", stnKo);
					paramMap.put("stn_en", stnEn);
					
					Element infoItem = (Element)infos.item(i);
					NodeList infoChildren = infoItem.getChildNodes();
					for(int j = 0; j<infoChildren.getLength(); j++) {
						Node infoChild = infoChildren.item(j);
						
						String nodeNm = infoChild.getNodeName().toLowerCase();
						String nodeVal = infoChild.getTextContent();
						
						//월 전체평균데이터의 경우 TM값이 들어오지 않으므로 임의로 - 입력
						if("tm".equals(nodeNm) && ("null".equals(nodeVal) || "".equals(nodeVal))) nodeVal = "-";
						
						if("#text".equals(nodeNm) || "null".equals(nodeVal) || "".equals(nodeVal)) continue;
						
						paramMap.put(nodeNm, nodeVal);	
					}				
					paramMapList.add(paramMap);
				}
				
				//---------------------------------
				//SQL 실행
				String sqlStr = 
						"INSERT INTO TN_AWS_STNDAYS "
						+ "(STN_ID, STN_KO, STN_EN, "
						+ "TM, TM_YEAR, TM_MONTH, "
						+ "PS, TA, TA_MAX, TA_MIN, TD, TG_MIN, "
						+ "HM, HM_MIN, EV_S, WS, WS_MAX, WD_MAX, "
						+ "CA_TOT, SS_DAY, SI_DAY, RN_DAY, SD_NEW, SD_MAX"
						+ ") "
						+ "SELECT "
						+ "?, ?, ?, ?, ?, ?, "
						+ "?, ?, ?, ?, ?, ?, "
						+ "?, ?, ?, ?, ?, ?, "
						+ "?, ?, ?, ?, ?, ? "
						+ "FROM DUAL "
						+ "WHERE NOT EXISTS ("
						+ "SELECT 0 FROM TN_AWS_STNDAYS WHERE STN_ID=? AND TM_YEAR=? AND TM_MONTH=? AND TM=?"
						+ ")";
				List<List<String>> sqlParamList = new LinkedList<List<String>>();
				for(int i = 0; i < paramMapList.size(); i++) {
					Map<String, String> oParam = paramMapList.get(i);				
					List<String> sqlParam = new LinkedList<String>();
					
					sqlParam.add(oParam.get("stn_id"));
					sqlParam.add(oParam.get("stn_ko"));
					sqlParam.add(oParam.get("stn_en"));
					sqlParam.add(oParam.get("tm"));
					sqlParam.add(oParam.get("tm_year"));
					sqlParam.add(oParam.get("tm_month"));
					sqlParam.add(oParam.get("ps"));
					sqlParam.add(oParam.get("ta"));
					sqlParam.add(oParam.get("ta_max"));
					sqlParam.add(oParam.get("ta_min"));
					sqlParam.add(oParam.get("td"));
					sqlParam.add(oParam.get("tg_min"));
					sqlParam.add(oParam.get("hm"));
					sqlParam.add(oParam.get("hm_min"));
					sqlParam.add(oParam.get("ev_s"));
					sqlParam.add(oParam.get("ws"));
					sqlParam.add(oParam.get("ws_max"));
					sqlParam.add(oParam.get("wd_max"));
					sqlParam.add(oParam.get("ca_tot"));
					sqlParam.add(oParam.get("ss_day"));
					sqlParam.add(oParam.get("si_day"));
					sqlParam.add(oParam.get("rn_day"));
					sqlParam.add(oParam.get("sd_new"));
					sqlParam.add(oParam.get("sd_max"));
					sqlParam.add(oParam.get("stn_id"));
					sqlParam.add(oParam.get("tm_year"));
					sqlParam.add(oParam.get("tm_month"));
					sqlParam.add(oParam.get("tm"));
					
					sqlParamList.add(sqlParam);
				}
				
				//sql 실행
				result = iDao.executeListIdxSql(sqlStr, sqlParamList);
				logging.setMap("SUCCESS_YN", "Y");
			} else {
				logging.setMap("RES_MSG", "결과없음");
			}
		} catch(Exception ex) {
			
			ex.printStackTrace();
			
			logging.setMap("RES_MSG", "[ERR] " + ex.getMessage());
			logging.logging();
			
			return (-1);
		}		
		return result;
	}

	
	
	private HashMap<String, String> generateParamMap() {
		HashMap<String, String> param = new HashMap<String, String>();
		
		param.put("tm", "");
		param.put("ps", "");
		param.put("ta", "");
		param.put("ta_max", "");
		param.put("ta_min", "");
		param.put("td", "");
		param.put("tg_min", "");
		param.put("hm", "");
		param.put("hm_min", "");
		param.put("ev_s", "");
		param.put("ws", "");
		param.put("ws_max", "");
		param.put("wd_max", "");
		param.put("ca_tot", "");
		param.put("ss_day", "");
		param.put("si_day", "");
		param.put("rn_day", "");
		param.put("sd_new", "");
		param.put("sd_max", "");
		param.put("tm_month", "");
		param.put("tm_year", "");
		
		return param;
		
	}
}

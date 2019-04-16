/**
 * 기상특보
 * 20170522- Wijy
 */
package api;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IndexWeadWarn extends IndexData {
	String idxWw_warn_str = "IDXWW_WARN";
	String idxWw_warn_nm = "기상특보 > 기상특보";
	String idxWw_info_str = "IDXWW_INFO";
	String idxWw_info_nm = "기상특보 > 기상정보";
	String idxWw_annc_str = "IDXWW_ANNC";
	String idxWw_annc_nm = "기상특보 > 기상속보";
	String idxWw_ppwr_str = "IDXWW_PPWR";
	String idxWw_ppwr_nm = "기상특보 > 기상예비특보";
	String idxWw_spnc_str = "IDXWW_SPNC";
	String idxWw_spnc_nm = "기상특보 > 특보코드";
	
	//기상특보조회
	String idxWw_warn = "http://newsky2.kma.go.kr/service/WetherSpcnwsInfoService/WeatherWarningItem";
	//기상정보조회
	String idxWw_info = "http://newsky2.kma.go.kr/service/WetherSpcnwsInfoService/WeatherInformation";
	//기상속보조회
	String idxWw_annc = "http://newsky2.kma.go.kr/service/WetherSpcnwsInfoService/WeatherAnnouncement";
	//기상예비특보조회
	String idxWw_ppwr = "http://newsky2.kma.go.kr/service/WetherSpcnwsInfoService/WeatherPrepareWarning";
	//특보코드조회
	String idxWw_spnc = "http://newsky2.kma.go.kr/service/WetherSpcnwsInfoService/SpecialNewsCode";
	
	String stn_id = "108";	//전국
	//String stn_id = "109";		//서울
	//특보코드조회 사용 지역코드
	String areaCode = "L1000000";	//전국
	//String areaCode = "L1010100";	//서울
	
	@Override
	public void getData() throws Exception {
		String paramStr = "numOfRows=999";
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String today = df.format(calendar.getTime());
		
		paramStr += "&fromTmFc=" + today + "&toTmFc=" + today;
		
		//areaCode를 넣지 않고 조회해야 결과가 제대로 나옴!
		getData(idxWw_warn_str, idxWw_warn, paramStr + "&stnId=" + stn_id);
		getData(idxWw_info_str, idxWw_info, paramStr + "&stnId=" + stn_id);
		getData(idxWw_annc_str, idxWw_annc, paramStr + "&stnId=" + stn_id);
		getData(idxWw_ppwr_str, idxWw_ppwr, paramStr + "&stnId=" + stn_id);
		getData(idxWw_spnc_str, idxWw_spnc, paramStr);		
	}
	
	
	private int getData(String IDX, String url, String paramStr) throws Exception
	{
		int res = (-1);
		File fXml = comm.getXML(IDX, url, paramStr);
		Document doc = comm.parseXML(fXml);
		
		/** LOG **/
		logging.initMap();	//초기화
		logging.setMap("LINK_ID", IDX);
		logging.setMap("SYS_CODE", "ROAD");
		
		if(IDX.equals(idxWw_warn_str)) logging.setMap("LINK_KO_NM", idxWw_warn_nm);
		if(IDX.equals(idxWw_info_str)) logging.setMap("LINK_KO_NM", idxWw_info_nm);
		if(IDX.equals(idxWw_annc_str)) logging.setMap("LINK_KO_NM", idxWw_annc_nm);
		if(IDX.equals(idxWw_ppwr_str)) logging.setMap("LINK_KO_NM", idxWw_ppwr_nm);
		if(IDX.equals(idxWw_spnc_str)) logging.setMap("LINK_KO_NM", idxWw_spnc_nm);
		
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
		
		res = executeWWData("INS", IDX, doc);
		logging.logging();
		return res;
	}


	private int executeWWData(String sqlIdx, String IDX, Document doc) {
		int res = 0;
		
		if(sqlIdx.equals("INS"))
		{
			try {
				NodeList parents = (NodeList)xpath.evaluate("//items/item", doc, XPathConstants.NODESET);
				
				List<HashMap<String, String>> paramMapList = new ArrayList<HashMap<String, String>>(); 
				
				for(int i = 0; i<parents.getLength(); i++)
				{
					HashMap<String, String> paramMap = generateParameterMap(IDX);
					Element el = (Element)parents.item(i);
					
					NodeList children =  el.getChildNodes();
					for(int j = 0; j<children.getLength(); j++)
					{
						Node thisItem = children.item(j);
						String category = thisItem.getNodeName().toUpperCase();
						String value = thisItem.getTextContent();
						
						paramMap.put(category, value);
					}
					
					paramMapList.add(paramMap);
				}
				
				List<List<String>> paramList = new ArrayList<List<String>>();	
				String sql = "";
				
				if(IDX.equals(idxWw_warn_str))
				{
					sql = "{call P_INSWW_WARNING(" + 
										"?, ?, ?, ?, " +
										"?, ?, ?, ?, ?, ?, ?, ?" +
										")}";				
					
					for(int i = 0; i<paramMapList.size(); i++)
					{
						List<String> tmpList = new ArrayList<String>();
						HashMap<String, String> param = paramMapList.get(i);
						
						tmpList.add(param.get("STNID"));
						tmpList.add(param.get("TMFC"));
						tmpList.add(param.get("TMSEQ"));
						tmpList.add(param.get("WARFC"));
						tmpList.add(param.get("T1"));
						tmpList.add(param.get("T2"));
						tmpList.add(param.get("T3"));
						tmpList.add(param.get("T4"));
						tmpList.add(param.get("T5"));
						tmpList.add(param.get("T6"));
						tmpList.add(param.get("OTHER"));
						
						paramList.add(tmpList);
					}
				} 
				else if (IDX.equals(idxWw_info_str))
				{
					sql = "{call P_INSWW_INFO(" + 
							"?, ?, ?, ?, ?" +
							")}";								
					
					for(int i = 0; i<paramMapList.size(); i++)
					{
						List<String> tmpList = new ArrayList<String>();
						HashMap<String, String> param = paramMapList.get(i);
						
						tmpList.add(param.get("STNID"));
						tmpList.add(param.get("TMFC"));
						tmpList.add(param.get("TMSEQ"));
						tmpList.add(param.get("T1"));
						
						paramList.add(tmpList);
					}
				}
				else if (IDX.equals(idxWw_annc_str))
				{
					sql = "{call P_INSWW_ANNC(" + 
							"?, ?, ?, ?, ?, ?" +
							")}";								
					
					for(int i = 0; i<paramMapList.size(); i++)
					{
						List<String> tmpList = new ArrayList<String>();
						HashMap<String, String> param = paramMapList.get(i);
						
						tmpList.add(param.get("STNID"));
						tmpList.add(param.get("TMFC"));
						tmpList.add(param.get("TMSEQ"));
						tmpList.add(param.get("CNT"));
						tmpList.add(param.get("ANN"));
						
						paramList.add(tmpList);
					}
				}
				else if (IDX.equals(idxWw_ppwr_str))
				{
					sql = "{call P_INSWW_PPWR(" + 
							"?, ?, ?, ?, ?, ?, ?" +
							")}";								
					
					for(int i = 0; i<paramMapList.size(); i++)
					{
						List<String> tmpList = new ArrayList<String>();
						HashMap<String, String> param = paramMapList.get(i);
						
						tmpList.add(param.get("STNID"));
						tmpList.add(param.get("TMFC"));
						tmpList.add(param.get("TMSEQ"));
						tmpList.add(param.get("CNT"));
						tmpList.add(param.get("PWN"));
						tmpList.add(param.get("REM"));
						
						paramList.add(tmpList);
					}
				}
				else if (IDX.equals(idxWw_spnc_str))
				{
					sql = "{call P_INSWW_SNC(" + 
							"?, ?, ?, ?, ?, " +
							"?, ?, ?, ?, ?, " +
							"?, ?, ?" +
							")}";								
					
					for(int i = 0; i<paramMapList.size(); i++)
					{
						List<String> tmpList = new ArrayList<String>();
						HashMap<String, String> param = paramMapList.get(i);
						
						tmpList.add(param.get("STNID"));
						tmpList.add(param.get("TMFC"));
						tmpList.add(param.get("TMSEQ"));
						tmpList.add(param.get("AREACODE"));
						tmpList.add(param.get("AREANAME"));
						tmpList.add(param.get("WARNVAR"));
						tmpList.add(param.get("WARNSTRESS"));
						tmpList.add(param.get("COMMAND"));
						tmpList.add(param.get("STARTTIME"));
						tmpList.add(param.get("ENDTIME"));
						tmpList.add(param.get("ALLENDTIME"));
						tmpList.add(param.get("CANCEL"));
						
						paramList.add(tmpList);
					}
				}
								
				int outParamTypes[] = {java.sql.Types.INTEGER};
				List<List<String>> rs = iDao.executeIdxPlSqlWithList(sql, paramList, outParamTypes);
				
				res = rs.size();
								
				if (IDX.equals(idxWw_spnc_str)) {
					//--해제처리
					String rlsSql = "{call P_UPDWW_RELEASE()}";
					List<String> rlsRs = iDao.executeIdxPlSql(rlsSql, null, null);
					//--해제처리
				}
			} catch (Exception e) {
				System.out.println("  [EXCEPTION] : IndexWeadWarn.executeWWData (String sqlIdx, String IDX, Document doc)  ");
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
	
	
	private HashMap<String, String> generateParameterMap(String IDX)
	{
		HashMap<String, String> map = new HashMap<String, String>();	
		
		if(IDX.equals(idxWw_warn_str))
		{
			map.put("STNID", "");
			map.put("TMFC", "");
			map.put("TMSEQ", "");
			map.put("WARFC", "");
			map.put("T1", "");
			map.put("T2", "");
			map.put("T3", "");
			map.put("T4", "");
			map.put("T5", "");
			map.put("T6", "");
			map.put("OTHER", "");			
		} 
		else if(IDX.equals(idxWw_info_str)) 
		{
			map.put("STNID", "");
			map.put("TMFC", "");
			map.put("TMSEQ", "");
			map.put("T1", "");
		}
		else if(IDX.equals(idxWw_annc_str)) 
		{
			map.put("STNID", "");
			map.put("TMFC", "");
			map.put("TMSEQ", "");
			map.put("CNT", "");
			map.put("ANN", "");
		}
		else if(IDX.equals(idxWw_ppwr_str)) 
		{
			map.put("STNID", "");
			map.put("TMFC", "");
			map.put("TMSEQ", "");
			map.put("CNT", "");
			map.put("PWN", "");
			map.put("REM", "");
		}
		else if(IDX.equals(idxWw_spnc_str))
		{
			map.put("STNID", "");
			map.put("TMFC", "");
			map.put("TMSEQ", "");
			map.put("AREACODE", "");
			map.put("AREANAME", "");
			map.put("WARNVAR", "");
			map.put("WARNSTRESS", "");
			map.put("COMMAND", "");
			map.put("STARTTIME", "");
			map.put("ENDTIME", "");
			map.put("ALLENDTIME", "");
			map.put("CANCEL", "");
		}		
		return map;
	}
}

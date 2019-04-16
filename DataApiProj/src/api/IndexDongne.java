/**
 * 동네예보
 * 20170427- Wijy
 */
package api;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IndexDongne extends IndexData{
	String idxDn_dn_str = "IDXDN_DN";
	String idxDn_dn_nm = "동네예보 > 동네예보";
	String idxDn_fcg_str = "IDXDN_FCG";
	String idxDn_fcg_nm = "동네예보 > 초단기실황";
	String idxDn_fct_str = "IDXDN_FCT";
	String idxDn_fct_nm = "동네예보 > 초단기예보";
	
	//동네예보조회
	String idxDn_dn = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastSpaceData";
	//초단기실황조회
	String idxDn_fcg = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastGrib";
	//초단기예보조회
	String idxDn_fct = "http://newsky2.kma.go.kr/service/SecndSrtpdFrcstInfoService2/ForecastTimeData";
	
	//위치 위경도 세트 (서울 내 격자 목록)
	String xyArr[] = 
		{
		"nx=57&ny=125",
		"nx=57&ny=126",
		"nx=57&ny=127",
		"nx=58&ny=124",
		"nx=58&ny=125",
		"nx=58&ny=126",
		"nx=58&ny=127",
		"nx=59&ny=124",
		"nx=59&ny=125",
		"nx=59&ny=126",
		"nx=59&ny=127",
		"nx=59&ny=128",
		"nx=60&ny=125",
		"nx=60&ny=126",
		"nx=60&ny=127",
		"nx=60&ny=128",
		"nx=61&ny=124",
		"nx=61&ny=125",
		"nx=61&ny=126",
		"nx=61&ny=127",
		"nx=61&ny=128",
		"nx=61&ny=129",
		"nx=62&ny=125",
		"nx=62&ny=126",
		"nx=62&ny=127",
		"nx=62&ny=128",
		"nx=62&ny=129",
		"nx=63&ny=125",
		"nx=63&ny=126",
		"nx=63&ny=127"
		};
	
	@Override
	public void getData() {
		// TODO Auto-generated method stub
		//동네예보는 각 오퍼레이션 별로 실행시간이 다르기 때문에 IDX값을 받는 함수를 씀
	}
	
	/*
	 20170508 참고항목
	 * 동네예보 xml 형식 
	 	-> 예보 일자 asc, 각 항목
	 	
	 * 초단기예보 xml 형식
	 	-> 각 항목, 예보일자 asc
	 
	 */
	
	
	public void getData(String IDX) throws Exception
	{
		//서울특별시 격자 위/경도 -> 지역별로 Request날려야함!
		String paramStr = "numOfRows=999";
		//base_date
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String today = df.format(calendar.getTime());
		
		/** LOG **/
		logging.initMap();	//초기화
		logging.setMap("LINK_ID", IDX);
		logging.setMap("SYS_CODE", "ROAD");
		
		if(IDX.equals(idxDn_dn_str))
		{
			//동네예보는 일 8회 수집
			int tempHour = calendar.get(Calendar.HOUR_OF_DAY);
			int calcBaseHour = (tempHour / 3) * 3 - 1;			
			calcBaseHour = (calcBaseHour == (-1) ? 23 : calcBaseHour);
			
			paramStr += "&base_time=" + String.format("%02d", calcBaseHour) + "00";
			paramStr += "&base_date=" + today;
			
			
			for(int i = 0; i< xyArr.length; i++)
			{
				/** LOG **/
				logging.setMap("LINK_KO_NM", idxDn_dn_nm + "/" + xyArr[i]);
				getData(IDX, idxDn_dn, paramStr + "&" + xyArr[i]);
			}
		}
		else if (IDX.equals(idxDn_fcg_str))
		{
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			
			//paramStr += "&base_time=" + String.format("%02d",(hour == (-1)? 23 : hour)) + "00";
			paramStr += "&base_time=" + String.format("%02d", hour) + "00";
			paramStr += "&base_date=" + today;
						
			for(int i = 0; i< xyArr.length; i++)
			{
				/** LOG **/
				logging.setMap("LINK_KO_NM", idxDn_fcg_nm + "/" + xyArr[i]);
				getData(IDX, idxDn_fcg , paramStr + "&" + xyArr[i]);
			}
		}
		else if (IDX.equals(idxDn_fct_str))
		{
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			
			//paramStr += "&base_time=" + String.format("%02d",(hour == (-1)? 23 : hour)) + "00";
			paramStr += "&base_time=" + String.format("%02d", hour) + "00";
			paramStr += "&base_date=" + today;
			
			for(int i = 0; i< xyArr.length; i++)
			{
				/** LOG **/
				logging.setMap("LINK_KO_NM", idxDn_fct_nm + "/" + xyArr[i]);
				getData(IDX, idxDn_fct, paramStr + "&" + xyArr[i]);
			}
		}		
	}
	
	private int getData(String IDX, String url, String param) throws Exception
	{
		int res = (-1);
		File fXml = comm.getXML(IDX, url, param);				//File 생성
		Document doc = comm.parseXML(fXml);
		
		/** LOG **/
		logging.setMap("REQ_URL", comm.url);
				
		// 결과코드 체크
		if(!checkResult("resultCode", "resultMsg", "0000", doc)) {
			logging.logging();
			return res;
		}
		//Count체크
		if(!checkResultCount("totalCount", doc)) {
			logging.logging();
			return 0;
		}
		
		res = executeDNData("INS", IDX, doc);	//Sql 생성
		
		logging.logging();
		return res;
	}
	
	
	private int executeDNData(String sqlIdx, String IDX, Document doc)
	{
		int res = (-1);
		
		if(sqlIdx.equals("INS"))
		{
			try {
				NodeList parents = (NodeList)xpath.evaluate("//items/item", doc, XPathConstants.NODESET);
				HashMap<String, String> paramMap = getDNParamMap(IDX, doc);
				
				//동네예보 조회
				if(IDX.equals(idxDn_dn_str))
				{
					List<HashMap<String, String>> paramMapList = new ArrayList<HashMap<String, String>>();
					
					for(int i = 0; i<parents.getLength(); i++)
					{
						Element el = (Element)parents.item(i);
						
						NodeList fcstDate = el.getElementsByTagName("fcstDate");
						NodeList fcstTime = el.getElementsByTagName("fcstTime");
						String fcstTimeStr = fcstDate.item(0).getTextContent() + "" + fcstTime.item(0).getTextContent();
												
						if(i == 0)
						{
							//첫번째 데이터 입력
							paramMap.put("FCST_TIME", fcstTimeStr);							//예보일자
						}
						
						//예보날짜 변경시
						if(i != 0 && !(paramMap.get("FCST_TIME").equals(fcstTimeStr)))
						{
							paramMapList.add(paramMap);							
							paramMap = getDNParamMap(IDX, doc);
							
							//예보일자
							paramMap.put("FCST_TIME", fcstTimeStr);
						}
						//공통으로 입력되는 부분
						NodeList baseDate = el.getElementsByTagName("baseDate");
						NodeList baseTime = el.getElementsByTagName("baseTime"); 
						NodeList locX = el.getElementsByTagName("nx");
						NodeList locY = el.getElementsByTagName("ny");
						
						paramMap.put("SDATE", baseDate.item(0).getTextContent());		//요청일자
						paramMap.put("STIME", baseTime.item(0).getTextContent());		//요청시간
						paramMap.put("LOC_X", locX.item(0).getTextContent());			//위경도(X)
						paramMap.put("LOC_Y", locY.item(0).getTextContent());			//위경도(Y)
						
						NodeList categoryNode = el.getElementsByTagName("category");
						NodeList valueNode = el.getElementsByTagName("fcstValue");
						
						String categoryName = categoryNode.item(0).getTextContent().toUpperCase();
						String valueStr = valueNode.item(0).getTextContent();
						
						paramMap.put(categoryName, valueStr);
					}
					paramMapList.add(paramMap);	//마지막
										
					//----------------------------------------------------------------------
					//SQL 생성
					//paramMapList
					String sql = "{call P_INSDN("
							+ "?, ?, ?, ?, ?, "
							+ "?, ?, ?, ?, ?, "
							+ "?, ?, ?, ?, ?, "
							+ "?, ?, ?, ?, ?, ?)}";
					
					
					List<List<String>> paramList = new ArrayList<List<String>>();
					logging.setMap("REQ_CNT", String.valueOf(paramMapList.size()));
					
					for(int i = 0; i < paramMapList.size(); i++)
					{
						HashMap<String, String> temp = paramMapList.get(i);
						List<String> param = new ArrayList<String>();
												
						param.add(temp.get("SDATE"));
						param.add(temp.get("STIME"));
						param.add(temp.get("FCST_TIME"));
						param.add(temp.get("T3H"));
						param.add(temp.get("TMX"));
						param.add(temp.get("TMN"));
						param.add(temp.get("UUU"));
						param.add(temp.get("VVV"));
						param.add(temp.get("SKY"));
						param.add(temp.get("PTY"));
						param.add(temp.get("POP"));
						param.add(temp.get("R06"));
						param.add(temp.get("S06"));
						param.add(temp.get("REH"));
						param.add(temp.get("WAV"));
						param.add(temp.get("VEC"));
						param.add(temp.get("WSD"));
						param.add(temp.get("LOC_X"));
						param.add(temp.get("LOC_Y"));
						
						paramList.add(param);
					}

					int outParamTypes[] =
						{
							java.sql.Types.VARCHAR,
							java.sql.Types.INTEGER
						};
					
					List<List<String>> rs = iDao.executeIdxPlSqlWithList(sql, paramList, outParamTypes);
					res = rs.size();					
				}
				//초단기실황 조회
				else if (IDX.equals(idxDn_fcg_str))
				{
					for(int i = 0; i<parents.getLength(); i++)
					{
						Element el = (Element)parents.item(i);
						
						NodeList baseDate = el.getElementsByTagName("baseDate");
						NodeList baseTime = el.getElementsByTagName("baseTime");
						NodeList locX = el.getElementsByTagName("nx");
						NodeList locY = el.getElementsByTagName("ny");
						
						String fcstTime = baseDate.item(0).getTextContent() + "" + baseTime.item(0).getTextContent();
						
						paramMap.put("SDATE", baseDate.item(0).getTextContent());
						paramMap.put("STIME", baseTime.item(0).getTextContent());
						paramMap.put("LOC_X", locX.item(0).getTextContent());			
						paramMap.put("LOC_Y", locY.item(0).getTextContent());			
						
												
						paramMap.put("FCST_TIME", fcstTime);
						paramMap.put("RCYN", "Y");
						
						NodeList categoryNode = el.getElementsByTagName("category");
						NodeList valueNode = el.getElementsByTagName("obsrValue");
						
						String categoryName = categoryNode.item(0).getTextContent().toUpperCase();
						String valueStr = valueNode.item(0).getTextContent();
						
						paramMap.put(categoryName, valueStr);						
					}
					
					//SQL 생성
					String insSql = "{call P_INSDNFC(" +
									"?, ?, ?, " +
									"?, ?, ?, ?, ?, " +
									"?, ?, ?, ?, ?, " +
									"?, ?, ?, ?, ?)}";
					List<String> paramList = new ArrayList<String>();
					
					paramList.add(paramMap.get("SDATE"));
					paramList.add(paramMap.get("STIME"));
					paramList.add(paramMap.get("FCST_TIME"));
					paramList.add(paramMap.get("T1H"));
					paramList.add(paramMap.get("RN1"));
					paramList.add(paramMap.get("SKY"));
					paramList.add(paramMap.get("UUU"));
					paramList.add(paramMap.get("VVV"));
					paramList.add(paramMap.get("REH"));
					paramList.add(paramMap.get("PTY"));
					paramList.add(paramMap.get("LGT"));
					paramList.add(paramMap.get("VEC"));
					paramList.add(paramMap.get("WSD"));
					paramList.add(paramMap.get("LOC_X"));
					paramList.add(paramMap.get("LOC_Y"));
					paramList.add(paramMap.get("RCYN"));
					
					int outParamTypes[] = 
						{
						java.sql.Types.VARCHAR,
						java.sql.Types.INTEGER							
						};
					
					List<String> result = iDao.executeIdxPlSql(insSql, paramList, outParamTypes);					
					res = result.size();
					
					logging.setMap("REQ_CNT", "1");
				}
				//초단기예보 조회
				else if (IDX.equals(idxDn_fct_str))
				{
					//정렬순서가 category -> 예보일자 asc
					//날짜, 시간을 기준으로 한 Key/Value(MAP)으로 관리
					HashMap<String, HashMap<String, String>> paramMapList = new HashMap<String, HashMap<String, String>>();
					//실황여부
					for(int i = 0; i<parents.getLength(); i++)
					{
						Element el = (Element)parents.item(i);
						
						NodeList baseDate = el.getElementsByTagName("baseDate");
						NodeList baseTime = el.getElementsByTagName("baseTime");
						NodeList locX = el.getElementsByTagName("nx");
						NodeList locY = el.getElementsByTagName("ny");
						
						paramMap.put("SDATE", baseDate.item(0).getTextContent());
						paramMap.put("STIME", baseTime.item(0).getTextContent());
						paramMap.put("LOC_X", locX.item(0).getTextContent());
						paramMap.put("LOC_Y", locY.item(0).getTextContent());
						paramMap.put("RCYN", "N");
						
						NodeList fcstDate = el.getElementsByTagName("fcstDate");
						NodeList fcstTime = el.getElementsByTagName("fcstTime");						
						String fcstStr = fcstDate.item(0).getTextContent() + "" + fcstTime.item(0).getTextContent(); 
						if(i == 0)
						{
							paramMap.put("FCST_TIME", fcstStr);
						}
						
						if(!fcstStr.equals(paramMap.get("FCST_TIME")))
						{
							paramMapList.put(paramMap.get("FCST_TIME"), paramMap);							
							
							if(paramMapList.get(fcstStr) != null)
							{
								paramMap = paramMapList.get(fcstStr);
							} 
							else 
							{
								paramMap = getDNParamMap(IDX, doc);
								paramMap.put("FCST_TIME", fcstStr);
							}
						}
						
						NodeList categoryNode = el.getElementsByTagName("category");
						NodeList valueNode = el.getElementsByTagName("fcstValue");
						
						String categoryName = categoryNode.item(0).getTextContent().toUpperCase();
						String valueStr = valueNode.item(0).getTextContent();
						paramMap.put(categoryName, valueStr);
						
						//last Index 체크....
						if(i == parents.getLength() - 1)
						{
							paramMapList.put(fcstStr, paramMap);
						}
					}
					
					//SQL 생성
					String insSql = "{call P_INSDNFC(" +
							"?, ?, ?, " +
							"?, ?, ?, ?, ?, " +
							"?, ?, ?, ?, ?, " +
							"?, ?, ?, ?, ?)}";
					List<List<String>> paramList = new ArrayList<List<String>>();
					
					logging.setMap("REQ_CNT", String.valueOf(paramMapList.size()));
					
					for(String s : paramMapList.keySet())
					{
						List<String> param = new ArrayList<String>();
						HashMap<String, String> temp = paramMapList.get(s);
						
						param.add(temp.get("SDATE"));
						param.add(temp.get("STIME"));
						param.add(temp.get("FCST_TIME"));
						param.add(temp.get("T1H"));
						param.add(temp.get("RN1"));
						param.add(temp.get("SKY"));
						param.add(temp.get("UUU"));
						param.add(temp.get("VVV"));
						param.add(temp.get("REH"));
						param.add(temp.get("PTY"));
						param.add(temp.get("LGT"));
						param.add(temp.get("VEC"));
						param.add(temp.get("WSD"));
						param.add(temp.get("LOC_X"));
						param.add(temp.get("LOC_Y"));
						param.add(temp.get("RCYN"));
						
						paramList.add(param);
					}
					
					int outParamTypes[] = 
						{
						java.sql.Types.VARCHAR,
						java.sql.Types.INTEGER							
						};
					
					List<List<String>> rs = iDao.executeIdxPlSqlWithList(insSql, paramList, outParamTypes);
					res = rs.size();
				}
			} catch (Exception e) {
				System.out.println("  [EXCEPTION] : indexDongne.executeDNData(String sqlIdx, String IDX, Document doc)  ");
				e.printStackTrace();
				
				logging.setMap("RES_MSG", "[ERR] " + e.getMessage());
				logging.logging();
				
				return (-1);
			}
		}
		else if (sqlIdx.equals("UPD"))
		{
			
		}
		else if (sqlIdx.equals("DEL"))
		{
			
		}
		
		logging.setMap("DB_CNT", String.valueOf(res));
		logging.setMap("SUCCESS_YN", "Y");
		
		return res;
	}


	private HashMap<String, String> getDNParamMap(String IDX, Document doc) {
		HashMap<String, String> paramMap = new HashMap<String, String>();
		
		if(IDX.equals(idxDn_dn_str))
		{
			paramMap.put("SDATE", "");
			paramMap.put("STIME", "");
			paramMap.put("FCST_TIME", "");		//예보일자시간
			paramMap.put("LOC_X", "");			//위경도(X)
			paramMap.put("LOC_Y", "");			//위경도(Y)
			paramMap.put("POP", "");			//강수확률
			paramMap.put("PTY", "");			//강수형태
			paramMap.put("R06", "");			//6시간 강수량
			paramMap.put("REH", "");			//습도
			paramMap.put("S06", "");			//6시간 신적설
			paramMap.put("SKY", "");			//하늘상태
			paramMap.put("T3H", "");			//3시간 기온
			paramMap.put("TMN", "");			//아침 최저기온
			paramMap.put("TMX", "");			//낮 최고기온
			paramMap.put("UUU", "");			//풍속(동서성분)
			paramMap.put("VVV", "");			//풍속(남북성분)
			paramMap.put("WAV", "");			//파고
			paramMap.put("VEC", "");			//풍향
			paramMap.put("WSD", "");			//풍속
			
		}
		else if (IDX.equals(idxDn_fcg_str) || IDX.equals(idxDn_fct_str))
		{
			//초단기실황, 초단기예보 항목 같음						
			paramMap.put("SDATE", "");
			paramMap.put("STIME", "");
			paramMap.put("FCST_TIME", "");		//예보일자시간
			paramMap.put("LOC_X", "");			//위경도(X)
			paramMap.put("LOC_Y", "");			//위경도(Y)
			paramMap.put("T1H", "");			//기온
			paramMap.put("RN1", "");			//1시간 강수량
			paramMap.put("SKY", "");			//하늘상태
			paramMap.put("UUU", "");			//동서바람성분
			paramMap.put("VVV", "");			//남북바람성분
			paramMap.put("REH", "");			//습도
			paramMap.put("PTY", "");			//강수형태
			paramMap.put("LGT", "");			//낙뢰
			paramMap.put("VEC", "");			//풍향
			paramMap.put("WSD", "");			//풍속
			paramMap.put("RCYN", "");			//초단기실황(Y), 초단기예보(N) 구분자
		}
		
		return paramMap;
	}
	
}

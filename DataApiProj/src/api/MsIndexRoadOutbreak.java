package api;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MsIndexRoadOutbreak extends IndexData {

	String idxRoad = "IDX_ROAD_OUTBREAK";
	String idxRoad_nm = "도로돌발";
	String seoulOpenApiUrl = "http://openAPI.seoul.go.kr:8088";
	String tableNam = "TN_ROADACCDNT_INFO";

	String idxRoad_outbreak = "/xml/AccInfo/1/999/";

	String newTblNam = "";

	int totCount = 0;

	public void getData() throws Exception {

		//API이용 데이터 추출 후 INSERT
		File fXml = comm.getXML(idxRoad, seoulOpenApiUrl, idxRoad_outbreak);
		Document doc = comm.parseXML(fXml);

		/** LOG **/
		//logParam = generateLogginMap();	//초기화
//		logParam.put("API_SERVICE_ID", idxRoad);
//		logParam.put("API_SERVICE_NM", idxRoad_nm);
//		logParam.put("API_URL", comm.url);
		logging.initMap();	//초기화
		logging.setMap("API_SERVICE_ID", idxRoad);
		logging.setMap("API_SERVICE_NM", idxRoad_nm);
		logging.setMap("API_URL", comm.url);

		//결과코드체크
		if(!checkResult("RESULT/CODE", "RESULT/MESSAGE", "INFO-000", doc)) {
//			logParam.put("SUCCESS_YN", "N");
//			logParam.put("ERR_MSG", "[ERR1] checkResult....");
//			logging();
			logging.setMap("SUCCESS_YN", "N");
			logging.setMap("ERR_MSG", "[ERR1] checkResult....");
			logging.logging();
			return;
		}
		//count체크
		if(!checkResultCount("list_total_count", doc)) {
//			logParam.put("SUCCESS_YN", "N");
//			logParam.put("ERR_MSG", "[ERR2] checkResultCount....");
			logging.setMap("SUCCESS_YN", "N");
			logging.setMap("ERR_MSG", "[ERR1] checkResult....");
			logging.logging();
			return;
		}


		totCount = getResultCount("totalCount", doc);

		int[] resMsSql = executeRoadOutBreakMsSql("INSLIST", idxRoad, doc);
		if(resMsSql != null) {

			int resultLen = resMsSql.length;

			//테이블 RENAME
			if(resultLen <= 0) {
//				logParam.put("SUCCESS_YN", "N");
//				logParam.put("ERR_MSG", "[ERR3] 구별 정보 INSERT 실패!");
				logging.setMap("SUCCESS_YN", "N");
				logging.setMap("ERR_MSG", "[ERR3] 구별 정보 INSERT 실패!");
				System.out.println("  [EXCEPTION] : IndexRoadOutbreak.executeRoadOutBreak(String sqlIdx, String IDX, Document doc)  ");
			}
			else{

//				logParam.put("SUCCESS_YN", "Y");
//				logParam.put("DB_INS_CNT", String.valueOf(resultLen));
				logging.setMap("SUCCESS_YN", "Y");
				logging.setMap("DB_INS_CNT", String.valueOf(resultLen));
				//System.out.println("  도로돌발상황   [" + resultLen + "]건이 정상 처리되었습니다.");
				System.out.println("  An unexpected road situation  [" + resultLen + "] records successfully processed.");

			}
		}
		logging.logging();
	}

	private List<String> getRoadAcdntListMsSql() {

		boolean flgExist = false;
		String recordCnt = "0";
		List<String> idList = new ArrayList<String>();

		 String selSql = "select ACC_ID from TN_ROADACCDNT_INFO where RELEASE_YN = 'N'";
		 List<HashMap<String, String>> rs = iDaoMsSql.selectIdxSql(selSql, null);

		 if(rs.size() > 0) {
			 for (HashMap<String, String> r : rs) {
		    	String sAccId = r.get("ACC_ID");
		    	idList.add(sAccId);
		     }
		 }

		return idList;
	}

	private boolean existTableMsSql(String tableName) {

		boolean flgExist = false;
		String recordCnt = "0";

		 String selSql = "select count(*) AS CNT from INFORMATION_SCHEMA.TABLES where table_name = '" + tableName + "'";
		 List<HashMap<String, String>> rs = iDaoMsSql.selectIdxSql(selSql, null);

		 if(rs.size() > 0) {
			 for (HashMap<String, String> r : rs) {
		    	 recordCnt = r.get("CNT");
		     }

		     if(!recordCnt.isEmpty() && Integer.parseInt(recordCnt) == 1 ) {
		    	 flgExist = true;
		     }
		 }

		return flgExist;
	}

	public int[] executeRoadOutBreakMsSql(String sqlIdx, String IDX, Document doc) {
		int[] res = null;

		if (sqlIdx.equals("INSLIST"))
		{
			try {
				NodeList parents = (NodeList)xpath.evaluate("//row", doc, XPathConstants.NODESET);
				HashMap<String, String> paramMap = new HashMap<String, String>();

				@SuppressWarnings("unchecked")
				List<String>[] listArray = new List[parents.getLength()];

				//완료처리를 위해 기존 도로돌발 사고 ID 추출
				List<String> idList = getRoadAcdntListMsSql();

				//SQL 생성
				String insSql =
						"INSERT INTO  " + tableNam + " " +
						"(ACC_ID, OCCR_DATE, OCCR_TIME, EXP_CLR_DATE, EXP_CLR_TIME, ACC_TYPE, ACC_DTYPE, LINK_ID, GRS80TM_X, GRS80TM_Y, ACC_INFO, ENT_DATE ) " +
						"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";

				//도로돌발 사고 목록 XML로부터 추출
				int nInsertCount = 0;
				for (int i = 0; i<parents.getLength(); i++)
				{
					Element el = (Element)parents.item(i);
					NodeList children = el.getChildNodes();

					for(int j = 0; j<children.getLength(); j++)
					{
						Node thisItem = children.item(j);
						String category = thisItem.getNodeName().toUpperCase();
						String value = thisItem.getTextContent();

						paramMap.put(category, value);
					}
					List<String> paramList = new ArrayList<String>();

					/*long time = System.currentTimeMillis();
					SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
					String strNow = dayTime.format(new Date(time));*/

					String curAccId = paramMap.get("ACC_ID");
					//DB에 입력된 도로돌발 ID와 동일한 경우
					if(idList.contains(curAccId)) {
						idList.remove(curAccId);
						//System.out.println("완료전 [" + i + "/" + paramMap.get("EXP_CLR_DATE") + "] curAccId : "+curAccId);
						System.out.println("Before completion [" + i + "/" + paramMap.get("EXP_CLR_DATE") + "] curAccId : "+curAccId);
					}
					else { //기존 도로돌발 목록에 없을 경우
						//System.out.println("신규 사고발생 [" + i + "/" + paramMap.get("EXP_CLR_DATE") + "] curAccId : "+curAccId);
						System.out.println("New accident [" + i + "/" + paramMap.get("EXP_CLR_DATE") + "] curAccId : "+curAccId);
						paramList.add(curAccId);
						paramList.add(paramMap.get("OCCR_DATE"));
						paramList.add(paramMap.get("OCCR_TIME"));
						paramList.add(paramMap.get("EXP_CLR_DATE"));
						paramList.add(paramMap.get("EXP_CLR_TIME"));
						paramList.add(paramMap.get("ACC_TYPE"));
						paramList.add(paramMap.get("ACC_DTYPE"));
						paramList.add(paramMap.get("LINK_ID"));
						paramList.add(paramMap.get("GRS80TM_X"));
						paramList.add(paramMap.get("GRS80TM_Y")+100000);
						paramList.add(paramMap.get("ACC_INFO"));

						listArray[nInsertCount] = paramList;
						nInsertCount++;
					}
				}

				String sWhereStr = "";
				for(int q = 0 ; q < idList.size() ; q++ ) {
					sWhereStr += "'" + idList.get(q) + "'";
					if(q != (idList.size()-1)) {
						sWhereStr += ",";
					}
				}

				//상황해제처리가 완료된 적이 있는 (신규 사고발생건으로 최초 입력된 이후 사고목록에 포함되지 안은 경우)사고가 다시 전송된 경우에는 PK무결성 제약조건으로 오류..이후 돌발상황은 입력되지 않는 문제 있음.
				//이러한 상황이 발생하지 않는 다는 보장 필요.?!??

				if(idList.size() > 0) {
					//================상황 해제처리
					 String upSql = "UPDATE TN_ROADACCDNT_INFO SET release_yn='Y' WHERE ACC_ID in ("+ sWhereStr +")";
					 System.out.println("upSql : " + upSql);
					 iDaoMsSql.executeIdxSql(upSql, null);
				}

				res = iDaoMsSql.executeBatchIdxSql(insSql, listArray);

				 if(totCount == res.length){
//						logParam.put("SUCCESS_YN", "Y");
					 	logging.setMap("SUCCESS_YN", "Y");
				 }
				 else if(nInsertCount > res.length) {
//					logParam.put("SUCCESS_YN", "N");
//					logParam.put("ERR_MSG", "[ERR4] "+ nInsertCount + "건 중" + res.length + "만 입력됨.  ACC_ID 가 " + sWhereStr.replace(",", "|") + "인 값이 이미 있는 지 확인 필요");
					logging.setMap("SUCCESS_YN", "N");
					logging.setMap("ERR_MSG", "[ERR4] "+ nInsertCount + "건 중" + res.length + "만 입력됨.  ACC_ID 가 " + sWhereStr.replace(",", "|") + "인 값이 이미 있는 지 확인 필요");
				}

			} catch (Exception e) {
				System.out.println("  [EXCEPTION] : IndexRoadOutbreak.executeRoadOutBreak(String sqlIdx, String IDX, Document doc)  ");
				e.printStackTrace();

//				logParam.put("SUCCESS_YN", "N");
//				logParam.put("ERR_MSG", "[ERR5] 처리중 오류 e.getMessage:" + e.getMessage());
				logging.setMap("SUCCESS_YN", "N");
				logging.setMap("ERR_MSG", "[ERR5] 처리중 오류 e.getMessage:" + e.getMessage());
				logging.logging();

				return new int[]{0};
			}

		}
		else if (sqlIdx.equals("UPD"))
		{

		}

		return res;
	}
}

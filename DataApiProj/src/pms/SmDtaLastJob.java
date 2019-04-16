package pms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import logging.Logging;
import cmmn.Common;
import db.IdxDAO;

public class SmDtaLastJob {
	protected Common comm = Common.getInstance();
	protected IdxDAO iDao = new IdxDAO();
	protected XPath xpath = XPathFactory.newInstance().newXPath();
	protected Logging logging = new Logging();
	
	String idx 		= "PMS_SMDTALAST";
	String idxNm 	= "PMS-예측데이터 생성";
	
	//예측모델 프로시저 실행
	public void startProc(String routeCd) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
		logging.initMap();
		
		//log param
		logging.setMap("LINK_ID", idx);
		logging.setMap("LINK_KO_NM", idxNm);
		logging.setMap("SYS_CODE", "PMS");
		logging.setMap("BEGIN_DT", formatter.format(new Date()));
		
		try{
			String exesql = "{call ROADDATA.PRC_CLAC_PREDCT_LAST(?,?,?,?,?,?)}";
			
			List<String> param = new ArrayList<String>();
			param.add(routeCd);
			param.add(null);
			param.add("3");
			param.add("NONE");
			
			int[] outParam = new int[2];
			outParam[0] = java.sql.Types.VARCHAR;
			outParam[1] = java.sql.Types.VARCHAR;
			
			Map<String,Object> result = iDao.executeMapPlSql(exesql, param, outParam);
			String resultCd = result.get("OUTPUT_4") == null ? "" : result.get("OUTPUT_4").toString();
			String resultMsg = result.get("OUTPUT_5") == null ? "" : result.get("OUTPUT_5").toString();
			
			if(result.get("SUCCESS_AT").toString().equals("Y") && resultCd.equals("true")){
				logging.setMap("SUCCESS_YN", "Y");
				logging.setMap("RES_MSG", "[PMS/예측자료 성공] : \"" + routeCd + "\" 노선의 예측자료 생성에 성공하였습니다.");
			}else{
				logging.setMap("SUCCESS_YN", "N");
				
				String errorMsg =  "[PMS/예측자료 실패] : \"" + routeCd + "\" 노선의 예측자료 생성에 실패하였습니다. " + resultMsg;
				
				if(result.get("FAIL_MSG") != null){
					errorMsg += result.get("FAIL_MSG").toString().substring(0, 400);
				}
				
				logging.setMap("RES_MSG", errorMsg);
			}
		}catch(Exception e){
			logging.setMap("SUCCESS_YN", "N");
			logging.setMap("RES_MSG", "[PMS/예측자료 실패] : \"" + routeCd + "\" 노선의 예측자료 생성에 실패하였습니다. ");
			System.out.println("[EXCEPTION] : SmDtaLastJob.startProc(String routeCd)  ");
		}
		
		logging.setMap("END_DT", formatter.format(new Date()));
		
		logging.logging();
	}
	
}

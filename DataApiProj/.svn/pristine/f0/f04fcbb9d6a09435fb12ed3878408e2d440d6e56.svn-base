package logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import cmmn.Common;
import db.IdxDAO;

public class Logging {
	//로그관련
	private Common comm = Common.getInstance();
	private IdxDAO iDao = new IdxDAO();
	
	private HashMap<String, String> logParam = generateLogginMap();
	private String logFilePath = "";
	
	public Logging() {
		try {
			logFilePath = comm.getProperties("LOG_PATH");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** LOG 관련 **/
	public void initMap() {
		logParam = generateLogginMap();
	}

	//로그 파라미터 생성
	private HashMap<String, String> generateLogginMap() {
		HashMap<String, String> logMap = new HashMap<String, String>();

		//startDate입력
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
		String sBeginDt = formatter.format(new Date());
		
		logMap.put("LINK_ID", "");
		logMap.put("LINK_KO_NM", ""); 
		logMap.put("SYS_CODE", "");
		logMap.put("REQ_URL", comm.url);
		logMap.put("REQ_CNT", "0");
		logMap.put("DB_CNT", "0");
		logMap.put("SUCCESS_YN", "N"); 
		logMap.put("BEGIN_DT", sBeginDt);
		logMap.put("END_DT", "");
		logMap.put("RES_MSG", "");		
		
		return logMap;
	}

	//파라미터 세팅
	public void setMap(String key, String value) {
		if (key != null && !"".equals(key))
			logParam.put(key, value);
	}

	//공간정보 로그 입력
	public int shpLogging(String layerNm) {
		int res = 0;

		logParam.put("LINK_ID", "SHP_BUILD_" + layerNm);
		logParam.put("LINK_KO_NM", "공간정보 구축");
		logParam.put("SYS_CODE", "ROAD");
		logParam.put("REQ_URL", layerNm);	//reqUrl에 layerNm저장
		
		//rowcount체크는 힘듬
		
		//logFile읽기
		//file명 = (layerNm).log
		try {
			RandomAccessFile logFile = new RandomAccessFile(logFilePath + "/" + layerNm + ".log", "r");
			long fileSize = logFile.length();
			long pos = fileSize - 1;
			
			String line = "";
			while(true) {
				logFile.seek(pos);
				if(logFile.readByte() == '\n') {
					if(line == null || "".equals(line)) {
						line = logFile.readLine();
					} else {
						break;
					}
				}
				pos--;
			}
			 
			if(line.indexOf("success") >= 0) {
				//성공
				logParam.put("SUCCESS_YN", "Y");
			} else if(line.indexOf("fail") >= 0) {
				//실패
				//log Message추출
				while(true) {
					logFile.seek(pos);
					if(logFile.readByte() == '\n') 
						break;
					
					pos--;
				}
				line = "";
				line = logFile.readLine();
				logParam.put("RES_MSG", line);
			}
			
			logFile.close();
			logging();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}		
		return res;
	}

	//로그 입력
	public int logging() {
		int res = 0;
		
		//endDate입력되지 않았을 시
		if("".equals(logParam.get("END_DT"))) {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
			String sEndDt = formatter.format(new Date());			
			logParam.put("END_DT", sEndDt);
		}		
		
		String sql = 
				"INSERT INTO TL_LINK_LOG "
				+ "(LOG_ID, LINK_ID, LINK_KO_NM, SYS_CODE, REQ_URL, REQ_CNT, DB_CNT, SUCCESS_YN, RES_MSG, BEGIN_DT, END_DT) "
				+ "VALUES ("
				+ "(SELECT nvl(max(log_id), 0) + 1 FROM tl_link_log), "
				+ "?, ?, ?, ?, ?, ?, ?, ? "
				+ ", TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')"
				+ ", TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')"
				+ ")";
		
		List<String> paramList = new LinkedList<String>();
		paramList.add(logParam.get("LINK_ID"));
		paramList.add(logParam.get("LINK_KO_NM"));
		paramList.add(logParam.get("SYS_CODE"));
		paramList.add(logParam.get("REQ_URL"));
		paramList.add(logParam.get("REQ_CNT"));
		paramList.add(logParam.get("DB_CNT"));
		paramList.add(logParam.get("SUCCESS_YN"));
		paramList.add(logParam.get("RES_MSG"));
		paramList.add(logParam.get("BEGIN_DT"));
		paramList.add(logParam.get("END_DT"));
		
		res = iDao.executeIdxSql(sql, paramList);
		return res;
	}
}

package spmsLink;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import logging.Logging;
import api.IndexData;

/* SPMS에서 속성정보를 LINK해 가져오기 위한 Class */

public class SpmsLink extends IndexData{

	@Override
	//Not Use
	public void getData() throws Exception {
		//각 테이블별로 호출하기때문에 getData()함수는 쓰지 않음
	}

	//속성연계 대상 테이블
	/*

	 [TO]							:	[FROM]

	 PTL_LDAP_DEPT					:	PTL_LDAP_DEPT
	 PTL_LDAP_USER					:	PTL_LDAP_USER

	 TB_MNHL_INS					:	TB_MNHL_INS
	 TB_MNHL_REP					:	TB_MNHL_REP

	 TN_SOF_OCCR_RCEPT				:	TB_SSMCRM
	 TN_SOF_OCCR_RCEPT_EXT			:	TB_SSMCRM_EXT
	 TN_SOF_CNTRWK					:	TB_SSMDBM
	 TN_SOF_CNTRWK_EXT				:	TB_SSMDBM_EXT

	 TN_ATCH_FILE					:	TB_COMMON_FILES

	 TN_CNTRWK						:	TB_GSMIBM
	 TN_CNTRWK_DTL					:	TB_GSMSBH
	 TN_DGG_INTSPT					:	TB_GSMGCJ
	 TN_DGG_RECOVRY					:	TB_GSMDIG
	 TN_FLAW						:	TB_GSMHJM
	 TN_FLAW_CNTRWK					:	TB_GSMHJBSM
	 TN_PAV_RECOVRY_CNTRWK			:	TB_GSMBGGS

	 */


	//spms에서 조회한 속성을 insert한다.
	public int insertDataFromSpms(String _sIdx) {
		int retVal = 0;

		//쿼리
		String sQuery = "";
		int outParamCnt = 0;	//outParam 개수

		//Logging
		Logging logging = new Logging();
		logging.initMap();

		List<String> listKorNm = new LinkedList<String>();

		//실행
		if("LDAP".equals(_sIdx)) {
			sQuery = "{call P_UPDATE_LDAP(?, ?)}";
			outParamCnt = 2;
			listKorNm.add("LDAP_부서");
			listKorNm.add("LDAP_사용자");
		} else if ("EXT_USER".equals(_sIdx)) {
			sQuery = "{call P_UPDATE_USER_EXT(?)}";
			outParamCnt = 1;
			listKorNm.add("외부사용자");
		} else if ("MNHL".equals(_sIdx)) {
			sQuery = "{call P_UPDATE_MNHL(?, ?)}";
			outParamCnt = 2;
			listKorNm.add("맨홀_점검");
			listKorNm.add("맨홀_정비");
		} else if ("SOF".equals(_sIdx)) {
			sQuery = "{call P_UPDATE_SOF(?, ?, ?, ?)}";
			outParamCnt = 4;
			listKorNm.add("소파보수공사");
			listKorNm.add("소파보수공사_외부입력");
			listKorNm.add("소파발생신고접수");
			listKorNm.add("소파발생신고접수_외부입력");
		} else if ("FILES".equals(_sIdx)) {
			sQuery = "{call P_UPDATE_FILE(?)}";
			outParamCnt = 1;
			listKorNm.add("첨부파일");
		} else if ("CNTRWK".equals(_sIdx)) {
			sQuery = "{call P_UPDATE_CNTRWK(?)}";
			outParamCnt = 1;
			listKorNm.add("공사정보");
		}

		int aOutParamType[] = new int[outParamCnt];
		for(; outParamCnt > 0; outParamCnt--)
			aOutParamType[outParamCnt - 1] = java.sql.Types.INTEGER;

		List<List<String>> rs = iDao.executeIdxPlSqlWithList(sQuery, null, aOutParamType);

		logging.setMap("LINK_ID", "ATTR_UPD_" + _sIdx);
		logging.setMap("SYS_CODE", "ROAD");

		if(rs.get(0) != null) {
			List<String> listDatas = rs.get(0);
			for(int i = 0; i<listDatas.size(); i++) {
				logging.setMap("LINK_KO_NM", "속성정보입력-" + listKorNm.get(i));
				logging.setMap("SUCCESS_YN", "Y");
				logging.setMap("DB_CNT", listDatas.get(i));
				logging.logging();
			}
		} else {
			//resCount없음
			logging.setMap("RES_MSG", "[ERR] 결과가 제대로 반환되지 않음!");
			logging.logging();
		}

		return retVal;
	}

}
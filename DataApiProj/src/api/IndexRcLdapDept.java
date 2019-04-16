package api;

import java.util.HashMap;
import java.util.List;

public class IndexRcLdapDept extends IndexData {
	String idx = "WITHDRAWAL OF AUTHORITY ";
	String idxNm = "부서 변경 시 권한회수";
	String tableNm = "TN_USER_BK";
	String delTableNm = "TN_USER_AUTH_BK";
	String pqmsDelTableNm = "TN_USER_MENU_BK";

	int totCount = 0;

	public void getData() throws Exception {

		/** LOG **/
		logging.initMap();	//초기화
		logging.setMap("LINK_ID", idx);
		logging.setMap("LINK_KO_NM", idxNm);
		logging.setMap("SYS_CODE", "ROAD");

		int res[] = executeRcLdapDept();

		if(res != null) {
			int resultLen = 0;

			for (int i = 0; i < res.length; i++) {
				resultLen += res[i];
			}

			if(resultLen == 0) {
				logging.setMap("SUCCESS_YN", "N");
				logging.setMap("RES_MSG", "[EXCEPTION] : IndexRcLdapDept.executeRcLdapDept()");
				System.out.println("  [EXCEPTION] : IndexRcLdapDept.executeRcLdapDept()  ");
			} else{
				logging.setMap("SUCCESS_YN", "Y");
				logging.setMap("DB_CNT", resultLen + "");
				logging.setMap("RES_MSG", "권한회수   [" + resultLen + "]건이 정상 처리되었습니다.");
				System.out.println("권한회수   [" + res[0] + "]건이 정상 처리되었습니다.");
				System.out.println("권한회수   [" + res[1] + "]건이 정상 처리되었습니다.");
				System.out.println("권한회수   [" + res[2] + "]건이 정상 처리되었습니다.");
				System.out.println("권한회수   [" + res[3] + "]건이 정상 처리되었습니다.");
			}
			logging.setMap("SUCCESS_YN", "Y");
			logging.setMap("DB_CNT", resultLen + "");
			logging.setMap("RES_MSG", "권한회수 [" + res[0] + "]건이 정상 처리되었습니다.");

		}

		logging.logging();
	}

	private List<HashMap<String, String>> getRcDeptCompUserNoDeptCodeList() {
		 String selSql = "SELECT t.USER_NO USER_NO, plu.dept_code DEPT_CODE "
		 			   + "  FROM "
		 			   + "		(SELECT tnuser.user_no "
		 			   + "			  , tnuser.pbsvnt_no "
		 			   + " 			  , (SELECT tdi.ldap_dept_code "
		 			   + "	   			   FROM tc_dept_info tdi "
		 			   + "	  			  WHERE tnuser.dept_code = tdi.dept_code "
		 			   + "					AND tdi.USE_AT 		 = 'Y' "
		 			   + "					AND tdi.DELETE_AT 	 = 'N') ldap_dept_code "
		 			   + "	  	   FROM " + tableNm + " tnuser "
		 			   + " 		  WHERE tnuser.use_at     = 'Y' "
		 			   + "	 		AND tnuser.delete_at  = 'N' "
		 			   + "	 		AND tnuser.pbsvnt_no IS NOT NULL) t "
		 			   + " INNER JOIN PTL_LDAP_USER plu "
		 			   + " 	  ON plu.user_no    = t.pbsvnt_no "
		 			   + "	 AND plu.dept_code != t.ldap_dept_code "
		 			   + " WHERE plu.use_at   	= 'Y'";

		 List<HashMap<String, String>> rs = iDao.selectIdxSql(selSql, null);

		return rs;
	}

	//공통 사용 쿼리
	private String getCommonRcDeptCompList() {
		 String selSql = "SELECT t.USER_NO USER_NO"
		 			   + "  FROM "
		 			   + "		(SELECT tnuser.user_no"
		 			   + "			  , tnuser.pbsvnt_no"
		 			   + " 			  , (SELECT tdi.ldap_dept_code"
		 			   + "	   			   FROM tc_dept_info tdi"
		 			   + "	  			  WHERE tnuser.dept_code = tdi.dept_code"
		 			   + "					AND tdi.USE_AT 		 = 'Y'"
		 			   + "					AND tdi.DELETE_AT 	 = 'N') ldap_dept_code "
		 			   + "	  	   FROM " + tableNm + " tnuser "
		 			   + " 		  WHERE tnuser.use_at     = 'Y'"
		 			   + "	 		AND tnuser.delete_at  = 'N'"
		 			   + "	 		AND tnuser.pbsvnt_no IS NOT NULL) t"
		 			   + " INNER JOIN PTL_LDAP_USER plu"
		 			   + " 	  ON plu.user_no    = t.pbsvnt_no"
		 			   + "	 AND plu.dept_code != t.ldap_dept_code"
		 			   + " WHERE plu.use_at   	= 'Y'";

		return selSql;
	}

	private int[] executeRcLdapDept() {
		int res[] = new int[4];
		int cnt = 0;

		try {

			List<HashMap<String, String>> idList = getRcDeptCompUserNoDeptCodeList();

			String selSql = getCommonRcDeptCompList();

			//SQL 생성
			//TN_USER와 PTL_LDAP_USER 사용자 부서코드가 다른 경우 TN_USER테이블의 신청한 권한(REQ_MENUACC_ROLE)과 추가신청한 권한(ADD_REQ_MENUACC_ROLE) 회수
			String upSql = "UPDATE " + tableNm
						 + "   SET REQ_MENUACC_ROLE = NULL, ADD_REQ_MENUACC_ROLE = NULL "
						 + " WHERE USER_NO IN( " + selSql + ") "
						 + "   AND (REQ_MENUACC_ROLE IS NOT NULL OR ADD_REQ_MENUACC_ROLE IS NOT NULL)";

			if(idList.size() > 0) {
				 /*System.out.println("upSql : " + upSql);*/
				 res[0] = iDao.executeIdxSql(upSql, null);

			}

			//TN_USER와 PTL_LDAP_USER 사용자 부서코드가 다른 경우 승인한 권한 회수
			String delSql = "DELETE FROM " + delTableNm + " "
					  	  + " WHERE USER_NO IN( " + selSql + ")";

			if(idList.size() > 0) {
				 /*System.out.println("delSql : " + delSql);*/
				 res[1] = iDao.executeIdxSql(delSql, null);
			}

			//TN_USER와 PTL_LDAP_USER 사용자 부서코드가 다른 경우 TN_USER_MENU(PQMS만 사용) 권한 회수
			String delPqmsSql = "DELETE FROM " + pqmsDelTableNm + " "
					  	  		+ " WHERE USER_NO IN( " + selSql + ")";

			if(idList.size() > 0) {
				 /*System.out.println("delPqmsSql : " + delPqmsSql);*/
				 res[2] = iDao.executeIdxSql(delPqmsSql, null);
			}

			//TN_USER와 PTL_LDAP_USER 사용자 부서코드가 다른 경우 TN_USER 부서 변경
			for (int i = 0; i < idList.size(); i++) {
				String deptUpSql = "UPDATE " + tableNm + " tnuser "
									 + "   SET tnuser.DEPT_CODE = '" + idList.get(i).get("DEPT_CODE")
									 + "' WHERE tnuser.USER_NO = " + idList.get(i).get("USER_NO")
									 + "   AND tnuser.USE_AT = 'Y' AND tnuser.DELETE_AT = 'N'";
				/*System.out.println("deptUpSql : " + deptUpSql);*/
				iDao.executeIdxSql(deptUpSql, null);
				cnt++;
			}
			res[3] = cnt;

			 /*if(totCount == res.length){
				 	logging.setMap("SUCCESS_YN", "Y");
			 }*/

		} catch (Exception e) {
			System.out.println("  [EXCEPTION] : IndexRcLdapDept.executeRcLdapDept()  ");
			e.printStackTrace();

			logging.setMap("SUCCESS_YN", "N");
			logging.setMap("RES_MSG", "[ERR5] 처리중 오류 e.getMessage:" + e.getMessage());
			logging.logging();

			return new int[]{0};
		}

		return res;
	}
}

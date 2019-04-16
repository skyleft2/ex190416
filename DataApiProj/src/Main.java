import pms.PotLocJob;
import pms.SmDtaLastJob;
import spmsLink.SpmsFTPLink;
import spmsLink.SpmsLink;
import logging.Logging;
import api.IndexDongne;
import api.IndexMidFcst;
import api.IndexRcLdapDept;
import api.MsIndexRoadOutbreak;
import api.IndexStndays;
import api.IndexWeadWarn;
import api.IndexRoadOutbreak;
import java.io.*;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
            System.out.println(">> 매개변수 안들어옴 >>");
            return;
        }

		//api모듈 실행
		for (int i = 0; i < args.length; i++) {
			String idx = args[i];

			if(idx.equals("SHPLOG")) {
				//공간데이터 로그
				if(args.length > 1) {
					//0 : SHPLOG, 1: (layer_nm)
					String layerNm = args[i + 1];

					Logging logging = new Logging();
					logging.shpLogging(layerNm);
				}
				break;
			}
			else if(idx.equals("AWS_STNDAYS")) {
				//해당월 일별데이터
				IndexStndays is = new IndexStndays();
				is.getData();
			}
			else if(idx.equals("PMS_PROC")){
				//PMS 예측데이터 배치
				if(args.length > 1 && args.length > i){
					//0 : PMS_PROC, 1 : (ROUTE_CODE)
					String routeCd = args[i+1];
					SmDtaLastJob job = new SmDtaLastJob();

					job.startProc(routeCd);
				}
				break;
			}
			else if (idx.equals("ATTR_UPD")) {
				//기존포장 data link
				if(args.length > 1) {
					String sIdx = args[i + 1];
					SpmsLink s = new SpmsLink();

					s.insertDataFromSpms(sIdx);
				}
				break;
			}
			else if (idx.equals("FTP_DOWN") || idx.equals("FTP_SHAPE")) {
				SpmsFTPLink s = new SpmsFTPLink();

				if("FTP_DOWN".equals(idx)){
					//기존포장 첨부파일 down
					s.init();

				}else{
					//기존 ArcGis Shape file down
					s.init2();
				}
				s.downSpmsFiles();
			}
			else if (idx.equals("IDXDN_DN") || idx.equals("IDXDN_FCG") || idx.equals("IDXDN_FCT")) {
				//동네예보 - 동네예보, 초단기예보, 초단기실황
				IndexDongne d = new IndexDongne();
				d.getData(idx);
			}
			else if (idx.equals("IDXMF")) {
				//중기예보 - 중기기상전망, 중기기온예보, 중기기상예보
				IndexMidFcst m = new IndexMidFcst();
				m.getData();
			}
			else if (idx.equals("IDXWW")) {
				//기상특보 - 기상특보, 기상정보, 기상속보, 기상예비특보, 특보코드
				IndexWeadWarn w = new IndexWeadWarn();
				w.getData();
			}
			else if (idx.equals("POT_LOC")) {
				//소파 신고접수 위치정보
                System.out.println("====================================== [POT_LOC Start] ==================================");
				PotLocJob potloc = new PotLocJob();
				potloc.startProc();
				System.out.println("====================================== [POT_LOC End] ====================================");
			}
			//도로돌발
            else if (idx.equals("IDX_ROAD_OUTBREAK")) {
            	// 10분 단위로 돌아가도록 세팅 필요
                System.out.println("====================================== [IDX_ROAD_OUTBREAK Start] ==================================");
                System.out.println(">>> ORACLE SET");
                IndexRoadOutbreak iRoadoutbreak = new IndexRoadOutbreak();
                iRoadoutbreak.getData();
                System.out.println(">>> MS-SQL SET");
                MsIndexRoadOutbreak iRoadoutbreakMs = new MsIndexRoadOutbreak();
                iRoadoutbreakMs.getData();

                System.out.println("====================================== [IDX_ROAD_OUTBREAK End] ====================================");
            }
            else if (idx.equals("IDX_RC_LDAP_DEPT")) {
                System.out.println("================== [PTL_LDAP_DEPT와 TN_USER 부서 비교  조회] ==================");
                IndexRcLdapDept iRcLdapDept = new IndexRcLdapDept();
                iRcLdapDept.getData();
                System.out.println("========================================================");
            }
		}

	}

}

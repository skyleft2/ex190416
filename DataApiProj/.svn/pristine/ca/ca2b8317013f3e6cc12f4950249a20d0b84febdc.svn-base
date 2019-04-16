/**
 * Parent 
 * 20170427 - Wijy
 */
package api;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import logging.Logging;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import cmmn.Common;
import cmmn.FileControl;
import db.IdxDAO;
import db.MsIdxDAO;

abstract public class IndexData {
	protected Calendar calendar = Calendar.getInstance();
	protected Common comm = Common.getInstance();
	protected IdxDAO iDao = new IdxDAO();
	// ROADDIG - 도로돌발 MSSQL에 사용(iDaoMsSql)
	protected MsIdxDAO iDaoMsSql = new MsIdxDAO();
	protected XPath xpath = XPathFactory.newInstance().newXPath();
	protected FileControl fDown = new FileControl();
	protected Logging logging = new Logging();
	
	
	//main에서 호출하는 함수.
	abstract public void getData() throws Exception;
	
	
	/** 결과코드 체크 **/
	// parameter : 결과코드Node명, 에러메세지Node명, 정상결과값, Document
	protected boolean checkResult(String resultCodeCat, String errMsgCat,
			String resultCodeVal, Document doc) {
		boolean bCk = true;
		
		try 
		{
			// 결과코드 체크
			Node resultCodeNode = (Node) xpath.evaluate("//" + resultCodeCat, doc, XPathConstants.NODE);
			Node errMsgNode = (Node) xpath.evaluate("//" + errMsgCat, doc, XPathConstants.NODE);

			if(resultCodeNode != null && errMsgNode != null) {
				String resCode = resultCodeNode.getTextContent();
				String resMsg = errMsgNode.getTextContent();
				
				if (!resCode.equals(resultCodeVal)) {
					//!resMsg.equals(resultMsgVal)		--> 메세지는 우선 체크 안함
					logging.setMap("RES_MSG", "[" + resCode + "] " + resMsg);
					logging.logging();
					
					bCk = false;
				}
			}
		} 
		catch (Exception e) {
			logging.setMap("RES_MSG", "[ERR-checkResult] " + e.getMessage());
			logging.logging();
			
			bCk = false;
		}
		return bCk;
	}
	
	/** Count 체크 **/
	// parameter : 카운트Node명, Document
	protected boolean checkResultCount(String countNodeCat, Document doc)
	{
		boolean bCk = false;
		
		try
		{
			Node countNode = (Node) xpath.evaluate("//" + countNodeCat, doc, XPathConstants.NODE);
			if(countNode != null) {
				String sTotCount = countNode.getTextContent();
				int iTotCount = Integer.parseInt(sTotCount);			
				logging.setMap("REQ_CNT", sTotCount);
				
				if(sTotCount.equals("0") || iTotCount <= 0)
				{
					/** LOG **/
					logging.setMap("RES_MSG", "결과 없음");
					logging.logging();				
					
					System.out.println("---> 결과없음");
				} else {
					bCk = true;
				}
			}
		}
		catch (Exception e) {
			logging.setMap("RES_MSG", "[ERR-checkResultCount] " + e.getMessage());
			logging.logging();
		}
		
		return bCk;
	}
	

	/** Count 체크 **/
	// parameter : 카운트Node명, Document
	protected int getResultCount(String countNodeCat, Document doc)
	{
		int iTotCount = 0;
		try
		{
			Node countNode = (Node) xpath.evaluate("//" + countNodeCat, doc, XPathConstants.NODE);
			if(countNode != null) {
				String sTotCount = countNode.getTextContent();
				iTotCount = Integer.parseInt(sTotCount);
				
				logging.setMap("REQ_CNT", sTotCount);
				
				if(sTotCount.equals("0") || iTotCount <= 0)
				{
					/** LOG **/
					logging.setMap("RES_MSG", "결과 없음");
					logging.logging();				
					
					System.out.println("---> 결과없음");
				}
			}

		}
		catch (Exception e) {
			logging.setMap("RES_MSG", "[ERR-getResultCount] " + e.getMessage());
			logging.logging();
		}
		
		return iTotCount;
	}
	
    
}

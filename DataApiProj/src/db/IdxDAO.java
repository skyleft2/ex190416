package db;

import db.DBConnectionPoolMgr;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdxDAO {
	//property
	DBConnectionPoolMgr conMgr = null;
	public static Connection conn = null;
	public static PreparedStatement pstmt = null;
	Statement st = null;
	ResultSet rs = null;

	int result = 0;
	int row = 0;

	//기본
	public IdxDAO() {
		this.conMgr = null;
		this.conn = null;
		this.pstmt = null;
		this.st = null;
		this.rs = null;

		this.result = 0;
		this.row = 0;

		this.conMgr = DBConnectionPoolMgr.getInstance();
		try {
			this.conMgr.setDbInfo("");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//DB type을 받는 경우
	public IdxDAO(String type) {
		this.conMgr = null;
		this.conn = null;
		this.pstmt = null;
		this.st = null;
		this.rs = null;

		this.result = 0;
		this.row = 0;

		this.conMgr = DBConnectionPoolMgr.getInstance();
		try {
			this.conMgr.setDbInfo(type);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param sqlStr - 쿼리문자열
	 * @param paramList - 바인딩할 파라미터 목록
	 * @return - 쿼리결과 row수
	 */
	public int executeIdxSql(String sqlStr, List<String> paramList)
	{
		int result = 0;
		this.conn = this.conMgr.getConnection();

		try {
			this.st = this.conn.createStatement();
			this.pstmt = this.conn.prepareStatement(sqlStr);

			if(paramList != null)
			{
				for(int i = 0; i<paramList.size(); i++)
				{
					this.pstmt.setString((i + 1), paramList.get(i));
				}
			}

			result = this.pstmt.executeUpdate();
		}
		catch (SQLException e) {
			System.out.println("  [EXCEPTION] : idxDAO.executeIdxSql(String sqlStr, List<String> paramList)  ");
			e.printStackTrace();
		}
		finally
		{
			close();
		}

		this.conMgr.returnConnection(this.conn);
		return result;
	}


	/**
	 * statement의 batch작업 수행
	 * --> arrayParamList 크기만큼 한꺼번에 수행하기 때문에 너무크지 않은 작업에서만 수행해야함. OutOfMemomryErr 오류 주의
	 * @param sqlStr - 쿼리문자열
	 * @param arrayParamList - 바인딩할 파라미터 배열 목록
	 * @return - 쿼리결과 row수
	 */
	public int[] executeBatchIdxSql(String sqlStr, List<String>[] arrayParamList)
	{
		int result = 0;
		int arrayLen = arrayParamList.length;
		int[] aResult = new int[arrayLen];
		List<String>  paramList = null;
		/*this.conMgr = DBConnectionPoolMgr.getInstance();
		this.conMgr.setDbInfo("");*/

		this.conn = this.conMgr.getConnection();

		try {
			this.st = this.conn.createStatement();
			this.pstmt = this.conn.prepareStatement(sqlStr);

			for(int i = 0 ; i < arrayLen; i++) {
				paramList = arrayParamList[i];
				if(paramList != null)
				{
					int paramLen =  paramList.size();

					for(int j = 0; j < paramLen; j++)
					{
						 this.pstmt.setString((j + 1), paramList.get(j));
					}
					this.pstmt.addBatch();
					this.pstmt.clearParameters();
				}
			}

			this.pstmt.executeBatch();
			this.pstmt.clearBatch();
			this.conn.commit();

		}
		catch (SQLException e) {
			aResult = new int[]{0};
			System.out.println("  [EXCEPTION] : idxDAO.executeBatchIdxSql(String sqlStr, List<String>[] arrayParamList)  ");
			e.printStackTrace();
		}
		finally
		{
			close();
			arrayParamList = null;
		}

		this.conMgr.returnConnection(this.conn);
		return aResult;
	}

	/**
	 *
	 * @param sqlStrs - 쿼리문자열
	 * @return - 쿼리결과row수
	 */
	public int executeIdxSql(List<String> sqlStrs)
	{
		this.result = 0;
		this.row = 0;

		this.conn = this.conMgr.getConnection();

		for(int i = 0; i<sqlStrs.size(); i++)
		{
			try {
				this.st = this.conn.createStatement();
				this.pstmt = this.conn.prepareStatement(sqlStrs.get(i));
				this.result = this.pstmt.executeUpdate();

				if(this.result > 0)
				{
					this.row += this.result;
				}
			}
			catch (SQLException e) {
				System.out.println("  [EXCEPTION] : idxDAO.executeIdxSql(List<String> sqlStrs)  ");
				e.printStackTrace();
			}
			finally
			{
				close();
			}
		}

		this.conMgr.returnConnection(this.conn);
		return this.row;
	}


	/**
	 *
	 * @param sql - 쿼리문자열
	 * @param inParam - 바인딩할 파라미터 목록
	 * @param outParam - output으로 받아올 데이터타입 배열 (java.sql.Types 배열)
	  	 예시 : int[] out = {java.sql.Types.INTEGER, java.sql.Types.VARCHAR};
	 * @return - output결과 List
	 */
	public List<String> executeIdxPlSql(String sql, List<String> inParam, int[] outParam)
	{
		List<String> res = new ArrayList<String>();
		this.conn = this.conMgr.getConnection();
		CallableStatement cs = null;

		try {
			cs = this.conn.prepareCall(sql);

			int paramIdx = 0;
			int inSize = 0;
			int outSize = 0;

			if(inParam != null && inParam.size() > 0)
			{
				inSize = inParam.size();
				for(int i = 0; i <inSize; i++)
				{
					cs.setString(paramIdx+1, inParam.get(i));
					paramIdx ++;
				}
			}
			//cs.setBlob(parameterIndex, inputStream);

			if(outParam != null && outParam.length > 0) {
				outSize = outParam.length;
				for(int i = 0; i<outSize; i++)
				{
					cs.registerOutParameter(paramIdx + 1, outParam[i]);
					paramIdx ++;
				}
			}

			cs.execute();

			for(int i = inSize; i<(inSize + outSize); i++ )
			{
				res.add(cs.getString(i + 1));
			}

		}
		catch (Exception e) {
			System.out.println("  [EXCEPTION] : executeIdxPlSql(String sql, List<String> inParam, List<String> OutParam)  ");
			e.printStackTrace();
		}
		finally
		{
			if(cs != null) try {cs.close();} catch(Exception ex) {}
			close();
		}

		this.conMgr.returnConnection(this.conn);
		return res;
	}

	/**
	 * 같은 sql에 Parameter만 변경되고 중복실행되는경우 *
	 *
	 * @param sql - 쿼리문자열
	 * @param inParam - 바인딩할 파라미터 목록
	 * @param outParam - output으로 받아올 데이터타입 배열 (java.sql.Types 배열)
	  	 예시 : int[] out = {java.sql.Types.INTEGER, java.sql.Types.VARCHAR};
	 * @return - output결과 List
	 */
	public List<List<String>> executeIdxPlSqlWithList(String sql, List<List<String>> inParamList, int[] outParam)
	{
		List<List<String>> res = new ArrayList<List<String>>();
		this.conn = this.conMgr.getConnection();
		CallableStatement cs = null;

		try {
			cs = this.conn.prepareCall(sql);

			if(inParamList != null && inParamList.size() > 0) {
				for(List<String> l  : inParamList)
				{
					int paramIdx = 0;

					for(int i = 0; i <l.size(); i++)
					{
						cs.setString(paramIdx+1, l.get(i));
						paramIdx ++;
					}

					for(int i = 0; i<outParam.length; i++)
					{
						cs.registerOutParameter(paramIdx + 1, outParam[i]);
						paramIdx ++;
					}
					cs.execute();

					List<String> result = new ArrayList<String>();
					for(int i = l.size(); i<(l.size() + outParam.length); i++ )
					{
						result.add(cs.getString(i + 1));
					}
					res.add(result);
				}
			} else if ((inParamList == null || inParamList.size() <= 0) && outParam.length > 0) {
				//OUT PARAM만 있음
				for(int i = 0; i<outParam.length; i++)
				{
					cs.registerOutParameter(i + 1, outParam[i]);
				}
				cs.execute();

				List<String> result = new ArrayList<String>();
				for(int i = 0; i < outParam.length; i++ )
				{
					result.add(cs.getString(i + 1));
				}
				res.add(result);
			}
		}
		catch (Exception e) {
			System.out.println("  [EXCEPTION] : executeIdxPlSqlWithList(String sql, List<List<String>> inParamList, int[] outParam)  ");
			e.printStackTrace();
		}
		finally
		{
			close();
			if(cs != null) try {cs.close();} catch(Exception ex) {}
		}

		this.conMgr.returnConnection(this.conn);
		return res;
	}

	 /**
     * 같은 sql에 Parameter만 변경되고 중복실행되는경우 *
     *
     * @param sql - 쿼리문자열
     * @param inParam - 바인딩할 파라미터 목록
     * @param outParam - output으로 받아올 데이터타입 배열 (java.sql.Types 배열)
         예시 : int[] out = {java.sql.Types.INTEGER, java.sql.Types.VARCHAR};
     * @return - output결과 List
     */
    public List<String> executeIdxPlSqlWithByte(String sql, List<String> inParam, List<String> img_name,int[] outParam)
    {
        List<String> res = new ArrayList<String>();
        this.conn = this.conMgr.getConnection();
        CallableStatement cs = null;

        try {
            cs = this.conn.prepareCall(sql);

            int paramIdx = 0;
            int inSize = 0;
            int imgSize = 0;
            int outSize = 0;

            if(inParam != null && inParam.size() > 0)
            {
                inSize = inParam.size();
                for(int i = 0; i <inSize; i++)
                {
                    cs.setString(paramIdx+1, inParam.get(i));
                    paramIdx ++;
                }
            }
            if(img_name != null && img_name.size() > 0) {
                imgSize = img_name.size();
                for(int i = 0; i < imgSize; i++) {

                    // 이미지저장
                    File f = new File(img_name.get(i));
                    FileInputStream fis = new FileInputStream(f);
                    int fileSize = (int)f.length();
                    cs.setBinaryStream(paramIdx+1, fis, fileSize);
                    paramIdx ++;
                }
            }
            if(outParam != null && outParam.length > 0) {
                outSize = outParam.length;
                for(int i = 0; i<outSize; i++)
                {
                    cs.registerOutParameter(paramIdx + 1, outParam[i]);
                    paramIdx ++;
                }
            }

            cs.execute();

            for(int i = inSize+imgSize; i<(inSize + imgSize+ outSize); i++ )
            {
                res.add(cs.getString(i + 1));
            }

        }
        catch (Exception e) {
            System.out.println("  [EXCEPTION] : executeIdxPlSql(String sql, List<String> inParam, List<String> OutParam)  ");
            e.printStackTrace();
        }
        finally
        {
            if(cs != null) try {cs.close();} catch(Exception ex) {}
            close();
        }

        this.conMgr.returnConnection(this.conn);
        return res;
    }



	/**
	 *
	 * @param sqlStr - 쿼리 문자열
	 * @param paramList - 바인딩할 파라미터 목록
	 * @return 조회쿼리 결과 Map 목록
	 */
	public List<HashMap<String, String>> selectIdxSql(String sqlStr, List<String> paramList)
	{
		List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
		this.conn = this.conMgr.getConnection();

		try {
			this.st = this.conn.createStatement();
			this.pstmt = this.conn.prepareStatement(sqlStr);

			if(paramList != null)
			{
				for(int i = 0; i<paramList.size(); i++)
				{
					this.pstmt.setString((i + 1), paramList.get(i));
				}
			}

			this.rs = pstmt.executeQuery();
			ResultSetMetaData  rsmd = rs.getMetaData();

			while(rs.next())
			{
				HashMap<String, String> resultMap = new HashMap<String, String>();
				for(int i = 1; i <= rsmd.getColumnCount(); i++)
				{
					resultMap.put(rsmd.getColumnName(i), rs.getString(rsmd.getColumnName(i)));
				}
				result.add(resultMap);
			}
		}
		catch (SQLException e) {
			System.out.println("  [EXCEPTION] : idxDAO.selectIdxSql(String sqlStr, List<String> paramList)  ");
			e.printStackTrace();
		}
		finally
		{
			close();
		}

		this.conMgr.returnConnection(this.conn);
		return result;
	}

	 /**
     *
     * @param sql - 쿼리문자열
     * @param paramList - 바인딩할 파라미터 목록
     * @return - 쿼리결과 row수
     */
	public int executeListIdxSql(String sql, List<List<String>> paramList) {

	    int result = 0;
        this.conn = this.conMgr.getConnection();

        try {
            this.st = this.conn.createStatement();
            this.pstmt = this.conn.prepareStatement(sql);

            if(paramList != null)
            {
            	result = 0;
                for(List<String> l  : paramList)
                {
                    int paramIdx = 0;

                    for(int i = 0; i <l.size(); i++)
                    {
                        this.pstmt.setString(paramIdx+1, l.get(i));
                        paramIdx ++;
                    }

                    result += this.pstmt.executeUpdate();
                }
            }
        }
        catch (Exception e) {
            System.out.println("  [EXCEPTION] : executeIdxPlSqlWithList(String sql, List<List<String>> paramList)  ");
            e.printStackTrace();
        }
        finally
        {
            close();
        }

        this.conMgr.returnConnection(this.conn);
        return result;

	}

	/**
	 *
	 * @param sql - 쿼리문자열
	 * @param inParam - 바인딩할 파라미터 목록
	 * @param outParam - output으로 받아올 데이터타입 배열 (java.sql.Types 배열)
	  	 예시 : int[] out = {java.sql.Types.INTEGER, java.sql.Types.VARCHAR};
	 * @return - output결과 List
	 */
	public Map<String, Object> executeMapPlSql(String sql, List<String> inParam, int[] outParam)
	{
		Map<String, Object> res = new HashMap<String, Object>();
		this.conn = this.conMgr.getConnection();
		CallableStatement cs = null;

		try {
			cs = this.conn.prepareCall(sql);

			int paramIdx = 0;
			int inSize = 0;
			int outSize = 0;

			if(inParam != null && inParam.size() > 0)
			{
				inSize = inParam.size();
				for(int i = 0; i <inSize; i++)
				{
					cs.setString(paramIdx+1, inParam.get(i));
					paramIdx ++;
				}
			}
			//cs.setBlob(parameterIndex, inputStream);

			if(outParam != null && outParam.length > 0) {
				outSize = outParam.length;
				for(int i = 0; i<outSize; i++)
				{
					cs.registerOutParameter(paramIdx + 1, outParam[i]);
					paramIdx ++;
				}
			}

			cs.execute();
			res.put("SUCCESS_AT", "Y");

			for(int i = inSize; i<(inSize + outSize); i++ )
			{
				res.put("OUTPUT_" + i , cs.getString(i + 1));
			}

		}
		catch (Exception e) {
			res.put("SUCCESS_AT", "N");
			res.put("FAIL_MSG", e.getMessage());
			System.out.println("  [EXCEPTION] : executeIdxPlSql(String sql, List<String> inParam, List<String> OutParam)  ");
			e.printStackTrace();
		}
		finally
		{
			if(cs != null) try {cs.close();} catch(Exception ex) {}
			close();
		}

		this.conMgr.returnConnection(this.conn);
		return res;
	}

	/**
	 * Close함수
	 */
	public void close()
	{
		if(this.rs != null) try {this.rs.close(); } catch(SQLException sqle) {};
		if(this.st != null) try {this.st.close(); } catch(SQLException sqle) {};
		if(this.pstmt != null) try {this.pstmt.close(); } catch(SQLException sqle) {};
	}
}

/**
 * 파일 다운로드
 * 20170504- Wijy
 */
package cmmn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class FileControl {
	
	public Boolean FileFromURL(String fromUrl, String filePath, String fileName)
	{
		URL url = null;
		File f = null;
		
		InputStream fis = null;
		FileOutputStream fos = null; 
		Boolean rtn = false;
		
		try {
			url = new URL(fromUrl);
			if (url != null) 
			{
				String fullPath = filePath + "/" + fileName;
				
				//디렉토리 생성
				File dir = new File(filePath);					
				dir.mkdirs();
				//-------------
				
				fis = url.openStream();
				fos = new FileOutputStream(fullPath); 
				
				
				byte[] buf = new byte[1024];
				int i = 0;
				
				while((i = fis.read(buf)) != (-1))
				{
					fos.write(buf, 0, i);
				}
				fos.flush();
			}
			rtn = true;
		} 
		catch (Exception e) {
			System.out.println("  [EXCEPTION] : FileDownload.FileFromURL(String fromUrl, String filePath, String fileName)  ");
			e.printStackTrace();
		} 
		finally
		{
			try {
				if(fis != null) fis.close();
				if(fos != null) fos.close();
			}
			catch (Exception ex) { }
			
		}
		return rtn;
	}
	
	
	public int deleteFile(String fileNm)
	{
		int res = 0;
		
		return res;
	}
	
}

/**
 * 공통
 * 20170427 - Wijy
 */
package cmmn;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;

public class Common {
	static Common instance;

	public static Common getInstance() {
		if (instance == null)
			instance = new Common();

		return instance;
	};

	//ServiceKey => 공공데이터 포털용 키
	public String url = ""; //로그 저장용으로 public 선언

	public String getProperties(String keyStr) throws Exception {
		String retVal = "";

		if (keyStr == null || keyStr.equals("")) {
			retVal = "";
		} else {
			System.out.println(">>> common.java getProperties - keyStr: "+keyStr);
/*
			Properties prop = setProperitesFileLoad();
			retVal = prop.getProperty(keyStr);
*/
			Section section = (Section)setIniFileLoad();
			retVal = section.get(keyStr);

		}

		return retVal;
	}
	public String xx_getProperties(String keyStr) throws Exception {
		String retVal = "";

		if (keyStr == null || keyStr.equals("")) {
			retVal = "";
		} else {
			Properties prop = new Properties();
			prop.load(new FileInputStream("./setting.properties"));
			retVal = prop.getProperty(keyStr);
		}

		return retVal;
	}

	/**
	 * XML 파일 생성 20170427 - Wijy Parameter : String IDX(구분자), serviceUrl(데이터
	 * endPoint), paramUrl(GetParameter)
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public File getXML(String IDX, String serviceUrl, String paramUrl) throws Exception {
		String serviceKey = getProperties("serviceKey");
		String roadOutbreakKey = getProperties("roadOutbreakKey");
		String sParamUrl = "";

		String strXml = "";
		String fileName = "XML/" + IDX + "_dataXML.xml";
		File file = null;

		if (IDX.equals("AWS_STNDAYS")) {
			//월보데이터여서 key를 따로 요청하지 않음
			url = serviceUrl;
		} else if(IDX.equals("IDX_ROAD_OUTBREAK")){
			url = serviceUrl + "/" + roadOutbreakKey + paramUrl;
		}
		else {
			//공공데이터포털 key 사용
			url = serviceUrl + "?ServiceKey=" + serviceKey + "&" + paramUrl;
		}
		System.out.println(" **** URL : " + url);

		try {
			URL oUrl = new URL(url);
			InputStreamReader inReader = null;
			HttpURLConnection conn = null;

			//http전송
			if(url.startsWith("https://")) {
				trustAllHosts();

				HttpsURLConnection hsc = (HttpsURLConnection) oUrl.openConnection();
				hsc.setHostnameVerifier(new HostnameVerifier() {
					@Override
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				});

				conn = hsc;
				conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
				conn.setRequestProperty("Accept", "*/*");
				conn.setRequestMethod("POST");
				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setUseCaches(false);

				OutputStream outputStream = conn.getOutputStream();
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
				bw.flush();
				bw.close();
				outputStream.close();

				conn.connect();

				if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					inReader = new InputStreamReader(conn.getInputStream(), "UTF-8");
				} else {
					System.out.println("------------------>> " + conn.getResponseCode());
				}
			}
			//Http 전송
			else {
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(url);

	            HttpResponse httpResponse = httpClient.execute(httpGet);
	            HttpEntity httpEntity = httpResponse.getEntity();

				inReader = new InputStreamReader(httpEntity.getContent());
			}

			BufferedReader br = new BufferedReader(inReader);
			String line = "";
			while((line = br.readLine()) != null) {
				strXml += line;
			}
			br.close();
			inReader.close();

			if(conn != null) conn.disconnect();

			if(!"".equals(strXml)) {
				File dir = new File("XML/");

				if (!dir.exists()) dir.mkdirs();

				char[] in = new char[strXml.length()];
				strXml.getChars(0, strXml.length(), in, 0);

				//encoding문제로 변경 - 180517 wijy
				BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "UTF-8"));
				bufWriter.write(in);
				bufWriter.close();

				file = new File(fileName);
			} else {
				//내용없음
			}
		} catch (Exception e) {
			System.out.println("  [EXCEPTION] : Common.getXML()  ");
			e.printStackTrace();
		} finally {

		}

		return file;
	};

	/**
	 * XML 파일 생성 20170427 - Wijy Parameter : File(xml file)
	 */
	public Document parseXML(File file) {
		Document doc = null;

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(file);

			doc.getDocumentElement().normalize();
		} catch (Exception e) {
			System.out.println("  [EXCEPTION] : Common.parseXML()  ");
			e.printStackTrace();
		}

		return doc;
	};


	//https 요청을 위한 인증정보
	private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                // TODO Auto-generated method stub
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
                // TODO Auto-generated method stub
            }
        }};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/*
	 * 접속한 서버가 운영서버,개발서버,로컬인지 확인한다.
	 */
	public String getServerMode() throws Exception {
		String retVal = "";
		Properties globalsProperties = new Properties();
		String serverMode = "";
		String real = "";
		String dev = "";
		String local = "";
		try {
			globalsProperties.load(new FileInputStream("./setting_common.properties"));

			real = globalsProperties.getProperty("real.ip.address");
			dev = globalsProperties.getProperty("dev.ip.address");
			local = globalsProperties.getProperty("local.ip.address");
		} catch (FileNotFoundException e) {
			throw new Exception(">>>>>>>>>> globals.properties 파일이 없습니다.");
		} catch (Exception e) {
			e.printStackTrace();
		}

		java.net.InetAddress ip = java.net.InetAddress.getLocalHost();
		System.out.println(">>>>>>>>>> ip.getHostAddress(): "+ip.getHostAddress());

		//운영서버/개발서버/로컬인지 체크
		if(real.equals(ip.getHostAddress())){
			serverMode = "REAL";
		}else if(dev.equals(ip.getHostAddress())){

			serverMode = "DEV";
		}else{
			//개발서버인지 로컬인지 체크
			serverMode = "LOCAL";
		}

		System.out.println(">>>>>>>>>> server: "+ serverMode);

		return serverMode;
	}

	/*
	 * 접속한 서버가 운영서버,개발서버,로컬인지 판단하여 properties를 셋팅.
	 */
	public Properties setProperitesFileLoad() throws Exception {
		// 접속한 서버가 운영서버,개발서버,로컬인지 확인한다.
		String serverMode = getServerMode();


		if(null == serverMode || "".equals(serverMode)){
			throw new Exception(">>>>>>>>>> globals.properties 파일에 자료가 없습니다.");
		}

		Properties prop = new Properties();
		if("REAL".equals(serverMode)){
			try {
				//운영서버 setting
				prop.load(new FileInputStream("./setting_real.properties"));
			} catch (FileNotFoundException e) {
				throw new Exception(">>>>>>>>>> setting_real.properties 존재유무확인");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			try {
				//개발서버 or Local setting
				prop.load(new FileInputStream("./setting_dev.properties"));
			} catch (FileNotFoundException e) {
				throw new Exception(">>>>>>>>>> setting_dev.properties 존재유무확인");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if(null == prop || "".equals(prop)){
			throw new Exception(">>>>>>>>>> setting_real.properties or setting_dev.properties 확인");
		}

		//System.out.println(">>>>>>>>>> comm.setProperitesFileLoad() Set SUCCESS!!");

		return prop;
	}


	/*
	 * 접속한 서버가 운영서버,개발서버,로컬인지 판단하여 INI파일을 셋팅.
	 */
	public Section setIniFileLoad() throws Exception {
		String serverMode = "";

		File iniFile = new File("./setting.ini");
		Ini ini = new Ini(iniFile);

		//접속서버의 정보를 저장하여 서버모드 체크
		Section ipAdressSection = ini.get("ipadress");

		String real = ipAdressSection.get("real.ip.address");
		String dev = ipAdressSection.get("dev.ip.address");
		String local = ipAdressSection.get("local.ip.address");

		java.net.InetAddress ip = java.net.InetAddress.getLocalHost();

		//운영서버/개발서버/로컬인지 체크
		if(real.equals(ip.getHostAddress())){
			serverMode = "REAL";
		}else if(dev.equals(ip.getHostAddress())){

			serverMode = "DEV";
		}else{
			//개발서버인지 로컬인지 체크
			serverMode = "LOCAL";
		}

		//운영서버,개발서버 설정정보를 저장
		Section realSection = ini.get("real");
		Section devSection = ini.get("dev");

		//System.out.println(">>>>>>>>>> serverMode: " + serverMode + ", ip.getHostAddress(): "+ip.getHostAddress());

		return ("REAL".equals(serverMode))?realSection:devSection;
	}


}
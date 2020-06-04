package indexdbcom;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class Scraper {

	public static void main(String[] args) throws ClientProtocolException, IOException {
		//https://index.db.com/dbiqweb2/servlet/indexsummary?redirect=benchmarkIndexSummary&indexid=99900423&currencyreturntype=AUD-Local&rebalperiod=3&pricegroup=STD&history=4&reportingfrequency=1&returncategory=ER&indexStartDate=20170603&priceDate=20200603&isnew=true
		String url = args[0];

		HttpClient client = HttpClientBuilder.create()
				.setConnectionTimeToLive(10, TimeUnit.SECONDS)
				.setDefaultRequestConfig(RequestConfig.custom()
			            .setCookieSpec(CookieSpecs.STANDARD).build())
				.build();
		
		System.out.println(" * downloading from "+ url);
		HttpResponse resp = client.execute(new HttpGet(url));
		System.out.println(" * downloaded");
		String data = EntityUtils.toString(resp.getEntity());
		
		//<a id="exceldownload" class="textButton" href="/dbiqweb2/servlet/indexsummary?redirect=exportcsv&charttype=1&cachekey=F9DFDE3D4B46152E444FEEEE7764A43F67EB124A78D4B5E77136452144028EEF">Excel</a>
		int cutFrom = data.indexOf("/dbiqweb2/servlet/indexsummary?redirect=exportcsv");
		int cutTo = data.indexOf("\">Excel</a>", cutFrom);
		System.out.println(" * extracting href: " + cutFrom + " " + cutTo);
		String url2 = "https://index.db.com" + data.substring(cutFrom, cutTo);
		
		HttpResponse respXls = client.execute(new HttpGet(url2));
		String filename = respXls.getHeaders("content-disposition")[0].getValue();
		filename = filename.substring(filename.indexOf("filename =") + "filename =".length());
		System.out.println(" * writing: " + filename);
		byte[] dataxls = EntityUtils.toByteArray(respXls.getEntity());
		FileOutputStream fw = new FileOutputStream(filename);
		fw.write(dataxls);
		fw.flush();
		fw.close();
		
	}
}

package rfx.lambda.util.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import rfx.server.common.StringUtil;
import de.l3s.boilerpipe.BoilerpipeExtractor;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import de.l3s.boilerpipe.sax.HTMLDocument;
import de.l3s.boilerpipe.sax.HTMLFetcher;
import de.l3s.boilerpipe.sax.HTMLHighlighter;

public class ArticleParser {
	


	public static void main(String[] args) throws Exception {
		URL url = new URL("http://news.zing.vn/Neu-Cong-Vinh-tinh-tao-hon-DTVN-da-khong-hoa-post588102.html");	
		String bodyXpath = "div.content";
		String keywordsXpath = "ul.tag-list > li";
		
		HTMLDocument htmlDoc = HTMLFetcher.fetch(url);
		String html = new String(htmlDoc.getData());		
		Document doc = Jsoup.parse(html);
		
		Element titleNode = doc.select("title").first();
		String title = "";
		if(titleNode != null){
			title = titleNode.text();
		}
		System.out.println("title: " + title);
		
		
		String extractedHtml = "";
		Element bodyNode = null;
		if(StringUtil.isNotEmpty(bodyXpath)){
			bodyNode = doc.select(bodyXpath).first();	
		}		
		if(bodyNode == null){
			BoilerpipeExtractor extractor = CommonExtractors.ARTICLE_EXTRACTOR;
			HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance(true, true);
			extractedHtml = hh.process(htmlDoc, extractor);
		} else {
			extractedHtml = bodyNode.html();
		}
		
		
		
		Elements keywordsNodes = null;
		if( StringUtil.isNotEmpty(keywordsXpath) ){
			keywordsNodes = doc.select(keywordsXpath);	
		}
		System.out.println(keywordsNodes.size());
		List<String> keywords = new ArrayList<String>();
		for (Element keywordsNode : keywordsNodes) {
			String s = keywordsNode.text().trim().replace("\u00a0","");			
			if( ! s.isEmpty() ){
				keywords.add(s);
			}
		} 
		System.out.println("keywords: " +keywords);
		System.out.println("extractedHtml: " +extractedHtml);
	}
}

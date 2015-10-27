package rfx.lambda.util.test;

import java.net.URL;

import de.l3s.boilerpipe.BoilerpipeExtractor;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import de.l3s.boilerpipe.sax.HTMLHighlighter;


public class HtmlArticleExtractorDemo {
	public static void main(String[] args) throws Exception {
		URL url = new URL(
				"http://news.zing.vn/5-sao-nam-trieu-view-dang-nguong-mo-cua-Vpop-post587069.html");
		final BoilerpipeExtractor extractor = CommonExtractors.ARTICLE_EXTRACTOR;

		final boolean includeImages = true;
		final boolean bodyOnly = true;
		final HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance(includeImages, bodyOnly);

		String extractedHtml = hh.process(url, extractor);
		

		System.out.println(extractedHtml);

	}
}

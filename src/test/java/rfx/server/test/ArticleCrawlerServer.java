package rfx.server.test;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.IntBinaryOperator;
import java.util.function.ToIntFunction;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.gson.Gson;

import de.l3s.boilerpipe.BoilerpipeExtractor;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import de.l3s.boilerpipe.sax.HTMLDocument;
import de.l3s.boilerpipe.sax.HTMLFetcher;
import de.l3s.boilerpipe.sax.HTMLHighlighter;

import rfx.server.common.ContentTypePool;
import rfx.server.common.SocialAnalyticsUtil;
import rfx.server.common.StringUtil;
import rfx.server.common.configs.ServerConfigs;
import rfx.server.lambda.FunctionPipeline;
import rfx.server.lambda.FunctionsChannelHandler;
import rfx.server.lambda.SimpleHttpResponse;
import rfx.server.lambda.functions.Decorator;
import rfx.server.lambda.functions.Filter;
import rfx.server.lambda.functions.Processor;
import rfx.server.netty.NettyServerUtil;
import scala.collection.mutable.StringBuilder;
import twitter4j.JSONObject;

public class ArticleCrawlerServer {

	final static Map<String, Processor> mapper = new HashMap<String, Processor>();

	static final String getHtmlArticle(URL url) throws Exception {
		BoilerpipeExtractor extractor = CommonExtractors.ARTICLE_EXTRACTOR;
		HTMLHighlighter hh = HTMLHighlighter.newExtractingInstance(true, true);
		String extractedHtml = hh.process(url, extractor);
		return extractedHtml;
	}

	public static class CrawlingRequest {
		URL url;
		String bodyXpath;
		String keywordsXpath;

		public CrawlingRequest(URL url, String bodyXpath, String keywordsXpath) {
			super();
			this.url = url;
			this.bodyXpath = bodyXpath;
			this.keywordsXpath = keywordsXpath;
		}

		public URL getUrl() {
			return url;
		}

		public String getBodyXpath() {
			return bodyXpath;
		}

		public String getKeywordsXpath() {
			return keywordsXpath;
		}

		public int hashCode() {
			return toString().hashCode();
		}

		public boolean equals(Object o) {
			return ((this == o) || ((o != null) && (getClass() == o.getClass()) && url.equals(((CrawlingRequest) o).url)));
		}

		@Override
		public String toString() {
			return (String.valueOf(url) + bodyXpath + keywordsXpath);
		}
	}

	public static class CrawledArticle {
		String title;
		String content = "";
		List<String> keywords = new ArrayList<String>();
		JSONObject facebookStats;

		public CrawledArticle(String title, String content, List<String> keywords, JSONObject facebookStats) {
			super();
			this.title = title;
			this.content = content;
			this.keywords = keywords;
			this.facebookStats = facebookStats;
		}

		public String getContent() {
			return content;
		}

		public List<String> getKeywords() {
			return keywords;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public void setKeywords(List<String> keywords) {
			this.keywords = keywords;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public JSONObject getFacebookStats() {
			return facebookStats;
		}
	}

	static final CrawledArticle parseArticle(URL url, String bodyXpath, String keywordsXpath) throws Exception {
		HTMLDocument htmlDoc = HTMLFetcher.fetch(url);
		String html = new String(htmlDoc.getData());
		Document doc = Jsoup.parse(html);

		Element titleNode = doc.select("title").first();
		String title = "";
		if (titleNode != null) {
			title = titleNode.text();
		}
		// System.out.println("title: " + title);

		String extractedHtml = "";
		Element bodyNode = null;
		if (StringUtil.isNotEmpty(bodyXpath)) {
			bodyNode = doc.select(bodyXpath).first();
		}
		if (bodyNode != null) {
			extractedHtml = bodyNode.html();
		} else {
			extractedHtml = getHtmlArticle(url);
		}

		Elements keywordsNodes = null;
		if (StringUtil.isNotEmpty(keywordsXpath)) {
			keywordsNodes = doc.select(keywordsXpath);
		}
		// System.out.println(keywordsNodes.size());
		List<String> keywords = new ArrayList<String>();
		for (Element keywordsNode : keywordsNodes) {
			String s = keywordsNode.text().trim().replace("\u00a0", "");
			if (!s.isEmpty()) {
				keywords.add(s);
			}
		}

		JSONObject obj = SocialAnalyticsUtil.getFacebookStatsObject(url.toString());
		return new CrawledArticle(title, extractedHtml, keywords, obj);
	}

	static LoadingCache<CrawlingRequest, CrawledArticle> cache = CacheBuilder.newBuilder().maximumSize(100000)
			.expireAfterWrite(5, TimeUnit.SECONDS).build(new CacheLoader<CrawlingRequest, CrawledArticle>() {
				public CrawledArticle load(CrawlingRequest key) {
					try {
						URL url = key.getUrl();
						String bodyXpath = key.getBodyXpath();
						String keywordsXpath = key.getKeywordsXpath();
						System.out.println("miss cache");
						return parseArticle(url, bodyXpath, keywordsXpath);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return null;
				}
			});

	public static void main(String[] args) throws Exception {
		String configPath = null;
		if (args.length >= 1) {
			configPath = args[0];
		}
		ServerConfigs serverConfigs = configPath == null ? ServerConfigs.getInstance()
				: ServerConfigs.getInstance(configPath);

		// the function pipeline for this server
		FunctionPipeline pipe = new FunctionPipeline();

		// filtering not authorized request
		Filter filterAccessAdmin = req -> {
			// req.setNotAuthorized(req.getUri().contains("admin"));
			return req;
		};

		Processor helloFunction = req -> {
			SimpleHttpResponse resp = new SimpleHttpResponse("Hello world !");
			return resp;
		};
		mapper.put("/hello", helloFunction);

		Processor crawlingFunction = req -> {
			try {
				URL url = new URL(req.getParameters().get("url").get(0));
				return new SimpleHttpResponse(getHtmlArticle(url));
			} catch (Exception e) {
				return new SimpleHttpResponse(e.getMessage());
			}
		};
		mapper.put("/article", crawlingFunction);

		Processor parseFunction = req -> {
			try {
				URL url = new URL(req.getFirstParameter("url"));
				String bodyXpath = req.getFirstParameter("body-xpath");
				String keywordsXpath = req.getFirstParameter("keywords-xpath");
				// ()
				CrawledArticle ar = cache.get(new CrawlingRequest(url, bodyXpath, keywordsXpath));
				if (ar != null) {
					return new SimpleHttpResponse(new Gson().toJson(ar));
				}
				return new SimpleHttpResponse("{}");
			} catch (Exception e) {
				e.printStackTrace();
				return new SimpleHttpResponse(e.getMessage());
			}
		};
		mapper.put("/parse", parseFunction);

		// the logic handler
		Processor mainFunction = req -> {
			String uri = req.getUri();
			Processor delegatedF = mapper.get(uri);
			if (delegatedF != null) {
				return delegatedF.apply(req);
			}

			SimpleHttpResponse resp = new SimpleHttpResponse();
			Map<String, List<String>> params = req.getParameters();

			if (uri.contains("/compute") && params.containsKey("x") && params.containsKey("operator")) {
				String operator = params.get("operator").get(0);
				StringBuilder head = new StringBuilder();

				// mapping from String to Integer
				ToIntFunction<String> f = x -> {
					return StringUtil.safeParseInt(x);
				};

				// binary function mapping x1 and x2 with operator
				IntBinaryOperator op = (int x1, int x2) -> {
					int n = 0;
					switch (operator) {
					case "plus":
						n = x1 + x2;
						head.append(x1).append(" + ").append(x2);
						break;
					case "multiply":
						n = x1 * x2;
						head.append(x1).append(" * ").append(x2);
						break;
					default:
						break;
					}
					return n;
				};
				int n = params.get("x").stream().mapToInt(f).reduce(op).getAsInt();
				resp.setHead(head.toString());
				resp.setBody(String.valueOf(n));
			}
			return resp;
		};

		// the decorator of output
		Decorator formatingResult = resp -> {
			if (resp.getBody() != null) {
				resp.setContentType(ContentTypePool.HTML_UTF8);
				StringBuilder s = new StringBuilder();
				s.append(
						"<HTML><HEAD><title>Demo</title><meta http-equiv=\"Content-Type\" content=\"text-html; charset=utf-8\" /></HEAD>");
				s.append("<BODY>").append(resp.getBody()).append("</BODY><HTML>");
				resp.setStatus(200);
				resp.setTime(System.currentTimeMillis());
			} else {
				resp.setStatus(400);
			}
			return resp;
		};
		pipe.apply(filterAccessAdmin).apply(mainFunction).apply(formatingResult);
		pipe.apply(mainFunction);

		NettyServerUtil.newHttpServerBootstrap(serverConfigs.getHost(), serverConfigs.getHttpPort(),
				new FunctionsChannelHandler(pipe));
	}

}

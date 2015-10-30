package rfx.server.test.simple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntBinaryOperator;
import java.util.function.ToIntFunction;

import rfx.server.common.ContentTypePool;
import rfx.server.common.StringUtil;
import rfx.server.lambda.LambdaHttpServer;
import rfx.server.lambda.SimpleHttpRequest;
import rfx.server.lambda.SimpleHttpResponse;
import rfx.server.lambda.functions.Decorator;
import rfx.server.lambda.functions.Filter;
import rfx.server.lambda.functions.Processor;
import scala.collection.mutable.StringBuilder;

public class SimpleServer {
	
	static String htmlHead = "<HTML><HEAD><title>SimpleServer</title><meta http-equiv=\"Content-Type\" content=\"text-html; charset=utf-8\" /></HEAD>";
		
	public static void main(String[] args) {
		String configPath = null;
		if (args.length >= 1) {
			configPath = args[0];
		}
		LambdaHttpServer server = new LambdaHttpServer(configPath);
		final Map<String, Processor> mapper = new HashMap<String, Processor>();
		
		// filtering not authorized request
		Filter filterAccessAdmin = (SimpleHttpRequest req) -> {
			req.setNotAuthorized(req.getUri().contains("admin"));
			return req;
		};
		
		mapper.put("/hello", (SimpleHttpRequest req)  -> {
			SimpleHttpResponse resp = new SimpleHttpResponse("Hello world !");
			return resp;
		});

		// the logic handler
		Processor mainFunction = (SimpleHttpRequest req) -> {
			String uri = req.getUri();
			System.out.println("mainFunction: " + uri);
			
			Processor delegatedF = mapper.get(uri);
			if (delegatedF != null) {
				return delegatedF.apply(req);
			}

			SimpleHttpResponse resp = new SimpleHttpResponse();
			Map<String, List<String>> params = req.getParameters();

			if (uri.contains("/compute") && params.containsKey("x") && params.containsKey("operator")) {
				String operator = params.get("operator").get(0);
				StringBuilder s = new StringBuilder();

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
						s.append(x1).append(" + ").append(x2);
						break;
					case "multiply":
						n = x1 * x2;
						s.append(x1).append(" * ").append(x2);
						break;
					default:
						break;
					}
					return n;
				};
				int n = params.get("x").stream().mapToInt(f).reduce(op).getAsInt();
				s.append("=").append(n);
				resp.setData(s.toString());				
			}
			return resp;
		};

		// the decorator of output
		Decorator formatingResult = resp -> {
			System.out.println("formatingResult: " + resp.getData());
			if ( ! resp.getData().isEmpty() ) {
				resp.setContentType(ContentTypePool.HTML_UTF8);
				StringBuilder s = new StringBuilder();
				s.append(htmlHead);
				s.append("<BODY>").append(resp.getData()).append("</BODY><HTML>");
				resp.setStatus(200);
				resp.setData(s.toString());
				resp.setTime(System.currentTimeMillis());
			} else {
				resp.setStatus(400);
			}
			return resp;
		};
		
		server.apply(filterAccessAdmin).apply(mainFunction).apply(formatingResult).start();
	}
}

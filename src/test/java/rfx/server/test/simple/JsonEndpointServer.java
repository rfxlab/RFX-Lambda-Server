package rfx.server.test.simple;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntBinaryOperator;
import java.util.function.ToIntFunction;

import com.google.gson.JsonObject;

import rfx.server.common.ContentTypePool;
import rfx.server.common.StringUtil;
import rfx.server.lambda.LambdaHttpServer;
import rfx.server.lambda.SimpleHttpRequest;
import rfx.server.lambda.SimpleHttpResponse;
import rfx.server.lambda.functions.Decorator;
import rfx.server.lambda.functions.Filter;
import rfx.server.lambda.functions.Processor;
import scala.collection.mutable.StringBuilder;

public class JsonEndpointServer {

	static String htmlHead = "<HTML><HEAD><title>SimpleServer</title><meta http-equiv=\"Content-Type\" content=\"text-html; charset=utf-8\" /></HEAD>";

	public static void main(String[] args) {
		String configPath = null;
		if (args.length >= 1) {
			configPath = args[0];
		}
		LambdaHttpServer server = new LambdaHttpServer(configPath);
		final Map<String, Processor> mapper = new HashMap<String, Processor>();

		mapper.put("/ping", (SimpleHttpRequest req) -> {
			SimpleHttpResponse resp = new SimpleHttpResponse("PONG");
			return resp;
		});

		mapper.put("/product-collect", (SimpleHttpRequest req) -> {
			System.out.println(req.getUri());
			req.getParamValues("data").stream().forEach(s -> {
				System.out.println(s);
			});
			JsonObject object = new JsonObject();
			object.addProperty("result", "ok");
			SimpleHttpResponse resp = new SimpleHttpResponse(object.toString());
			return resp;
		});

		// the logic handler
		Processor mainFunction = (SimpleHttpRequest req) -> {
			String uri = req.getUri();
			// System.out.println("mainFunction: " + uri);

			Processor delegatedF = mapper.get(uri);
			if (delegatedF != null) {
				return delegatedF.apply(req);
			}

			SimpleHttpResponse resp = new SimpleHttpResponse("{}");
			return resp;
		};

		// the decorator of output
		Decorator formatingResult = resp -> {
			// System.out.println("formatingResult: " + resp.getData());
			if (!resp.getData().isEmpty()) {
				resp.setContentType(ContentTypePool.JSON);
				resp.setStatus(200);
				resp.setData(resp.getData());
				resp.setTime(System.currentTimeMillis());
			} else {
				resp.setStatus(400);
			}
			return resp;
		};

		server.apply(mainFunction).apply(formatingResult).start();
	}
}

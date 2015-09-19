package rfx.server.lambda;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntBinaryOperator;
import java.util.function.ToIntFunction;

import rfx.server.common.StringUtil;
import rfx.server.lambda.functions.Decorator;
import rfx.server.lambda.functions.Filter;
import rfx.server.lambda.functions.Processor;
import rfx.server.netty.NettyServerUtil;


public class LambdaHttpServer {
	
	static String ip = "127.0.0.1";
	static int port = 8080;
	
	public static void main(String[] args) throws Exception {
		//the function pipeline for this server
		FunctionPipeline pipe = new FunctionPipeline();
		
		//filtering not authorized request
		Filter filterAccessAdmin = req -> {
			//req.setNotAuthorized(req.getUri().contains("admin"));
			return req;			
		};		
		Map<String, Processor> mapper = new HashMap<String, Processor>();
		Processor helloFunction = req -> {
			SimpleHttpResponse resp = new SimpleHttpResponse("Hello world !");
			return resp;
			
		};
		mapper.put("/hello", helloFunction);
		
		//the logic handler
		Processor mainFunction = req -> {			
			String uri = req.getUri();
			Processor delegatedF = mapper.get(uri);
			if(delegatedF != null){
				return delegatedF.apply(req);
			}
			
			SimpleHttpResponse resp = new SimpleHttpResponse();
			Map<String,List<String>> params = req.getParameters();
			
			if(uri.contains("/compute") && params.containsKey("x") && params.containsKey("operator") ){
				String operator = params.get("operator").get(0);
				StringBuilder head = new StringBuilder();
				
				//mapping from String to Integer
				ToIntFunction<String> f = x -> {					
					return StringUtil.safeParseInt(x);
				};
				
				//binary function mapping x1 and x2 with operator
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
		
		//the decorator of output
		Decorator formatingResult = resp -> {		
//			if(resp.getBody() != null){
//				resp.setContentType(ContentTypePool.JSON);
//				resp.setStatus(200);
//				resp.setTime(System.currentTimeMillis());
//			} else {
//				resp.setStatus(400);
//			}
			return resp;
		};
		//pipe.apply(filterAccessAdmin).apply(mainFunction).apply(formatingResult);
		pipe.apply(mainFunction);
		
		NettyServerUtil.newHttpServerBootstrap(ip, port, new FunctionsChannelHandler(pipe) );
	}
}

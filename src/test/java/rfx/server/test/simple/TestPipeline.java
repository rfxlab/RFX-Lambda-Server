package rfx.server.test.simple;

import rfx.server.lambda.FunctionPipeline;
import rfx.server.lambda.SimpleHttpRequest;
import rfx.server.lambda.SimpleHttpResponse;
import rfx.server.lambda.functions.Decorator;
import rfx.server.lambda.functions.Filter;
import rfx.server.lambda.functions.FinalProcessor;

public class TestPipeline extends FunctionPipeline {

	
	public static void main(String[] args) throws Exception {

		TestPipeline processor = new TestPipeline();

//		SimpleHttpRequest httpRequest = new SimpleHttpRequest();
//		httpRequest.setUri("/get/123");
//
//		SimpleHttpResponse resp1 = processor.process(httpRequest);
//		System.out.println(resp1.getData());
		//System.out.println(resp.getTime());
		
		Filter filterAccessAdmin = req -> {
			req.setNotAuthorized(req.getUri().contains("admin"));
			return req;			
		};		
		FinalProcessor logic123Function = req -> {
			if(req.getUri().contains("123")){
				return new SimpleHttpResponse("123 data");
			}
			return new SimpleHttpResponse();
		};
		Decorator formatingResult = resp -> {
			resp.setData("(" + resp.getData() + ")");
			return resp;
		};
				
		processor.apply(filterAccessAdmin).apply(logic123Function).apply(formatingResult);		
		System.out.println(processor.apply(new SimpleHttpRequest("admin/edit/123")));
		System.out.println(processor.apply(new SimpleHttpRequest("user/edit/123")));
		System.out.println(processor.apply(new SimpleHttpRequest("user/edit/456")));
				
		
		FinalProcessor logic456Function = req -> {
			if(req.getUri().contains("456")){
				return new SimpleHttpResponse("456 data");
			}
			return null;
		};
		processor.apply(logic456Function);
		
		System.out.println(processor.apply(new SimpleHttpRequest("user/edit/456")));
		
		
	}
}

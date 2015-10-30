package rfx.server.lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import rfx.server.lambda.functions.Decorator;
import rfx.server.lambda.functions.Filter;

public class FunctionPipeline {
	
	protected List<Function<SimpleHttpRequest, SimpleHttpResponse>> processorFunctions = new ArrayList<>();	
	protected List<Filter> filterFunctions = new ArrayList<>();
	protected List<Decorator> decoratorFunctions = new ArrayList<>();
	
	public FunctionPipeline apply(Filter f){
		filterFunctions.add(f);
		return this;
	}	
	
	public FunctionPipeline apply(Function<SimpleHttpRequest , SimpleHttpResponse> f){
		processorFunctions.add(f);
		return this;
	}
	
	
	public FunctionPipeline apply(Decorator f){
		decoratorFunctions.add(f);
		return this;
	}
	
	protected SimpleHttpResponse apply(SimpleHttpRequest req){
		SimpleHttpResponse resp = null;
		int s = 0; 
		
		//apply all filter functions
		s = filterFunctions.size();
		int i = 0;
		while(s > 0){			
			req = filterFunctions.get(i).apply(req);
			if(req.isNotAuthorized()){
				return new SimpleHttpResponse("Not Authorized Request");
			}
			i++;
			if(i >= s){
				break;
			}
		}
		
		//apply all Event Processor functions
		s = processorFunctions.size();
		int j = 0;
		while(s > 0){			
			resp = processorFunctions.get(j).apply(req);
			if(resp == null){
				return new SimpleHttpResponse("empty data");
			}
			j++;
			if(j >= s){
				break;
			}			
		}
		
		//apply all decorator functions (backward to first)
		s = decoratorFunctions.size();
		while(s > 0){
			s--;
			resp = decoratorFunctions.get(s).apply(resp);
			if(resp == null){
				return new SimpleHttpResponse("empty data");
			}
			if(s == 0){
				break;
			}			
		}		
		return resp;
	}
}

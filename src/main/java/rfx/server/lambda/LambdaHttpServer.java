package rfx.server.lambda;

import rfx.server.common.configs.ServerConfigs;
import rfx.server.lambda.functions.Decorator;
import rfx.server.lambda.functions.Filter;
import rfx.server.lambda.functions.Processor;
import rfx.server.netty.NettyServerUtil;


public class LambdaHttpServer {
		
	ServerConfigs serverConfigs;
	
	// the function pipeline for this server
	FunctionPipeline functionPipeline = new FunctionPipeline();
	
	
	public LambdaHttpServer(String configPath) {
		super();
		serverConfigs = configPath == null ? ServerConfigs.getInstance() : ServerConfigs.getInstance(configPath);		
	}
	
	public LambdaHttpServer() {
		super();
		serverConfigs = ServerConfigs.getInstance();		
	}
	
	public LambdaHttpServer apply(Filter f){
		functionPipeline.apply(f);
		return this;
	}
	
	public LambdaHttpServer apply(Processor f){
		functionPipeline.apply(f);
		return this;
	}
	
	public LambdaHttpServer apply(Decorator f){
		functionPipeline.apply(f);
		return this;
	}
	
	public void start(){
		NettyServerUtil.newHttpServerBootstrap(serverConfigs.getHost(), serverConfigs.getHttpPort(),new FunctionsChannelHandler(functionPipeline));
	}
}

package rfx.server.test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;

import rfx.server.common.ContentTypePool;
import rfx.server.common.configs.ServerConfigs;
import rfx.server.lambda.FunctionPipeline;
import rfx.server.lambda.FunctionsChannelHandler;
import rfx.server.lambda.SimpleHttpResponse;
import rfx.server.lambda.functions.Processor;
import rfx.server.netty.NettyServerUtil;

import com.corundumstudio.socketio.listener.*;
import com.corundumstudio.socketio.*;
import com.google.gson.Gson;

public class RfxQueryEngineService {
	
	static Queue<Query> queries = new PriorityQueue<Query>(); 

	static void startWebSocketServer(ServerConfigs serverConfigs) {
		Configuration config = new Configuration();
		config.setHostname(serverConfigs.getHost());
		config.setPort(serverConfigs.getWsPort());
		final SocketIOServer server = new SocketIOServer(config);
		server.addEventListener("queryEvent", Query.class, new DataListener<Query>() {
			@Override
			public void onData(SocketIOClient client, Query q, AckRequest ackRequest) {
				q.setUuid(client.getSessionId());
				queries.add(q);						
			}
		});
		
		new Timer().schedule(new TimerTask() {							
			@Override
			public void run() {
				Query q = queries.poll();
				if(q != null){
					Map<String, Object> map = new HashMap<>();
					map.put("ok", 1);
					map.put("list", Arrays.asList("abc","def"));
					String jsonData = new Gson().toJson(map);
					// broadcast messages to the sender
					server.getClient(q.getUuid()).sendEvent("queryResult",new Result(q.getSenderId(), jsonData ));					
				}
			}
		}, 100, 3000);
		server.start();
	}
	
	static void startHttpServer(ServerConfigs serverConfigs){
		FunctionPipeline pipe = new FunctionPipeline();
		Processor mainFunction = req -> {
			if(req.getUri().equals("/exit")){
				startedServer.set(false);
				new Thread(()->{System.exit(1);}).start();
				return new SimpleHttpResponse("Stopping the server!");
			}			
			String out = "";
			try {
				out = FileUtils.readFileToString(new File("resources/html/index.html"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SimpleHttpResponse resp = new SimpleHttpResponse(out);
			resp.setContentType(ContentTypePool.HTML_UTF8);
			return resp;
		};
		pipe.apply(mainFunction);
		NettyServerUtil.newHttpServerBootstrap(serverConfigs.getHost(),	serverConfigs.getHttpPort(), new FunctionsChannelHandler(pipe));
	}
	
	static AtomicBoolean startedServer = new AtomicBoolean();

	public static void main(String[] args) throws InterruptedException {
		String configPath = null;
		if (args.length >= 1) {
			configPath = args[0];
		}
		final ServerConfigs serverConfigs = configPath == null ? ServerConfigs.getInstance() : ServerConfigs.getInstance(configPath);

		new Thread(() -> {		
			System.out.println("startHttpServer ... ");
			startHttpServer(serverConfigs);
		}).start();
		new Thread(() -> {
			System.out.println("startWebSocketServer ... ");
			startWebSocketServer(serverConfigs);
		}).start();
		startedServer.set(true);
		
		while(startedServer.get()){}
		System.out.println("Stopped !");

	}

}
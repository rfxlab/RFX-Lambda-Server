package rfx.server.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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
					String qs = q.getQuery();
					
					Map<String, Object> map = new HashMap<>();
					map.put("ok", 1);
					
					if(qs.contains("One Direction")){
						map.put("nhacso", "<iframe src='http://nhacso.net/embed/song/XFhRU0NabA==' width='400' height='140' frameborder='0'></iframe>");
					}
					else if(qs.contains("report of AdsPlay")){
						map.put("reportFields", Arrays.asList("date","impressions", "clicks", "trueviews"));
						
						List<Map<String,Object>> reportObj = new ArrayList<Map<String,Object>>();						
						reportObj.add(ImmutableMap.<String, Object>builder()
								.put("date", "2015-10-04").put("impression", 14358).put("click",433).put("trueview",12845)
								.build());						
						reportObj.add(ImmutableMap.<String, Object>builder()
								.put("date", "2015-10-05").put("impression", 13358).put("click",433).put("trueview",12845)
								.build());
						reportObj.add(ImmutableMap.<String, Object>builder()
								.put("date", "2015-10-06").put("impression", 12358).put("click",433).put("trueview",12845)
								.build());
						
						map.put("reportAdsData", reportObj);						
					}
					else if(qs.contains("report of Device Type")){
						List<Map<String,Object>> reportObj = new ArrayList<Map<String,Object>>();
						reportObj.add(ImmutableMap.<String, Object>builder()
								.put("label", "PC").put("value", 323001)
								.build());			
						reportObj.add(ImmutableMap.<String, Object>builder()
								.put("label", "SmartTV").put("value", 144892)
								.build());			
						reportObj.add(ImmutableMap.<String, Object>builder()
								.put("label", "Tablet").put("value", 7034)
								.build());
						reportObj.add(ImmutableMap.<String, Object>builder()
								.put("label", "Mobile").put("value", 17489)
								.build());
						
						map.put("reportDeviceData", reportObj);
					}
					else {
						map.put("msg", Arrays.asList("welcome Mr.Trieu to IRIS System"));	
					}
					
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
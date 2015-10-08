package rfx.server.test;

import java.io.File;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;

import redis.clients.jedis.JedisPubSub;
import rfx.server.common.ContentTypePool;
import rfx.server.common.configs.ServerConfigs;
import rfx.server.lambda.FunctionPipeline;
import rfx.server.lambda.FunctionsChannelHandler;
import rfx.server.lambda.SimpleHttpResponse;
import rfx.server.lambda.functions.Processor;
import rfx.server.netty.NettyServerUtil;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SparkQueryService {

    static RedisPubSub redisPubSub;

    static void startWebSocketServer(ServerConfigs serverConfigs) {
        Configuration config = new Configuration();
        config.setHostname(serverConfigs.getHost());
        config.setPort(serverConfigs.getWsPort());
        final SocketIOServer server = new SocketIOServer(config);
        redisPubSub = new RedisPubSub(server);
        server.addEventListener("queryEvent", Query.class, (client, q, ackRequest) -> {
            q.setUuid(client.getSessionId());
            redisPubSub.pub(q);
        });

        new Thread(() -> {
            System.out.println("Start Thread for subscribing message.");
            redisPubSub.getSubJedis().subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    switch (channel) {
                        case "sql-result":
                            System.out.println("sql-result: " + message);
                            
                            //Type type = new TypeToken<Map<String, String>>(){}.getType();
                            Result rs = new Gson().fromJson(message, Result.class);
                            System.out.println(rs.getReceiverId());
                            System.out.println(rs.getUuid());
                            System.out.println(rs.getJsonData());

                            //Result rs = new Result(map.get("senderId").toString(), map.get("jsonData").toString());
                            server.getClient(rs.getUuid()).sendEvent("queryResult", rs);
                            break;
                    }
                }
            }, "sql-result");
        }).start();

        server.start();
    }

    static void startHttpServer(ServerConfigs serverConfigs) {
        FunctionPipeline pipe = new FunctionPipeline();
        Processor mainFunction = req -> {
            if (req.getUri().equals("/exit")) {
                startedServer.set(false);
                new Thread(() -> {
                    System.exit(1);
                }).start();
                return new SimpleHttpResponse("Stopping the server!");
            }
            String out = "";
            try {
                out = FileUtils.readFileToString(new File("resources/html/spark-query-ui.html"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            SimpleHttpResponse resp = new SimpleHttpResponse(out);
            resp.setContentType(ContentTypePool.HTML_UTF8);
            return resp;
        };
        pipe.apply(mainFunction);
        NettyServerUtil.newHttpServerBootstrap(serverConfigs.getHost(), serverConfigs.getHttpPort(), new FunctionsChannelHandler(pipe));
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

        while (startedServer.get()) {
        }
        System.out.println("Stopped !");

    }

}
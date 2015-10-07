package rfx.server.test;

import com.corundumstudio.socketio.SocketIOServer;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.*;

/**
 * Created by duhc on 07/10/2015.
 */
public class RedisPubSub {

    final SocketIOServer server;
    private Jedis subJedis;
    private Jedis pubJedis;

    public RedisPubSub(SocketIOServer server) {
        this.server = server;
        subJedis = new Jedis("localhost", 6379);
        pubJedis = new Jedis("localhost", 6379);
        init();
    }

    static Queue<Query> queries = new PriorityQueue<Query>();

    void init() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Query q = queries.poll();
                if (q != null) {
                    String qs = q.getQuery();

                    Map<String, Object> map = new HashMap<>();
                    map.put("ok", 1);

                    if (qs.contains("report of Device Type")) {
                        List<Map<String, Object>> reportObj = new ArrayList<Map<String, Object>>();
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

                    String jsonData = new Gson().toJson(map);
                    // broadcast messages to the sender
                    server.getClient(q.getUuid()).sendEvent("queryResult", new Result(q.getSenderId(), jsonData));
                }
            }
        }, 100, 3000);
    }

    public Jedis getSubJedis() {
        return subJedis;
    }

    public void setSubJedis(Jedis subJedis) {
        this.subJedis = subJedis;
    }

    public Jedis getPubJedis() {
        return pubJedis;
    }

    public void setPubJedis(Jedis pubJedis) {
        this.pubJedis = pubJedis;
    }

    public void pub(Query q) {
        queries.add(q);
        pubJedis.publish("sql-query", new Gson().toJson(q));
    }

    public void sub() {
        new Thread(() -> {
            System.out.println("Start Thread for subscribing message.");
            subJedis.subscribe(new JedisSubscriber(), "sql-result");
        }).start();
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        RedisPubSub rps = new RedisPubSub(null);
        rps.sub();
        for (int i = 0; i < 10; i++) {
            rps.pub(new Query(String.valueOf(i), i+ "ohhh"));
        }
        while (true) {
        }
    }

    public static class JedisSubscriber extends JedisPubSub {

        @Override
        public void onMessage(String channel, String message) {
            switch (channel) {
                case "sql-result":
                    System.out.println("sql-result: " + message);
                    break;
            }
            System.out.println("Receive message: " + message);
        }
    }
}

package rfx.server.test.simple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import rfx.server.lambda.query.ActorData;
import rfx.server.lambda.query.FunctionFactory;
import rfx.server.lambda.query.Functor;
import rfx.server.lambda.query.FunctorGroupResultByKey;
import rfx.server.lambda.query.QueryContext;
import rfx.server.lambda.query.QueryResult;
import rfx.server.test.query.QueryWithActor.Person;
import rfx.server.test.query.QueryWithActor.PersonActor;

public class ActorServer {
	static class FunctionGroupByEducation implements FunctionFactory {
		private static final long serialVersionUID = -872087603235171330L;

		@Override
		public Function<ActorData, String> build() {
			return new Function<ActorData, String>() {				
				@Override
				public String apply(ActorData actorData) {
					Person person = (Person)actorData;
					String groupKey = person.getEducation();
					return groupKey;
				}
			};
		}		
	}
	
	class MyUntypedActor extends UntypedActor {
		LoggingAdapter log = Logging.getLogger(getContext().system(), this);

		String msg;

		public MyUntypedActor(String msg) {
			this.msg = msg;
			System.out.println(msg);
		}

		public void onReceive(Object message) throws Exception {
			if (message instanceof String)
				log.info("Received String message: {}", message);
			else
				unhandled(message);
		}
	}
	
	static int MAX_POOL_SIZE = 1000000;
	final static String SAMPLE_DATA_PATH = "/home/trieu/data/user-income.txt";
	static AtomicInteger idCount = new AtomicInteger();	
	static Map<Integer,ActorRef> buildDataActorsFromRawData(ActorSystem system) throws IOException{	
		Map<Integer,ActorRef> dataActors = new ConcurrentHashMap<>(MAX_POOL_SIZE);
		Stream<String> lines = Files.lines(Paths.get(SAMPLE_DATA_PATH));		
		lines.forEach((String row)->{			
			String[] toks = row.split(",");
			if(toks.length == 15){
				int id = idCount.incrementAndGet();
				String education = toks[3].trim();
				String income = toks[14].trim();
				String occupation = toks[6].trim();				
				Person person = new Person(id, education, occupation, income.equals("<=50K"));
				person.setRawData(row);
				
				ActorRef actorRef = system.actorOf(Props.create(PersonActor.class,person));
				dataActors.put(id, actorRef);
				//personMap.put(id, person);
			} else {
				System.out.println(" bad data row "+row);
			}
		});		
		lines.close();
		return dataActors;
	}
	
	public static void main(String[] args) {
		ActorSystem actorSystem = ActorSystem.create("MySystem");
		Class<? extends Functor> functorClass = FunctorGroupResultByKey.class;	
		Map<Integer,ActorRef> dataActors = null;
		try {
			dataActors = buildDataActorsFromRawData(actorSystem);
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		FunctionGroupByEducation functionFactory = new FunctionGroupByEducation();
		QueryResult queryResult = new QueryResult(functionFactory);
		int queryActorPoolSize = 500;
		QueryContext queryContext = new QueryContext(actorSystem, queryResult, dataActors, functorClass, queryActorPoolSize );
	}
}

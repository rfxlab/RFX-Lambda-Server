package rfx.server.lambda.query;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class Query<T> {
	String description;
	PredicateFactory predicateFactory;			
	Map<Integer, ActorRef> queryActorPool;
	QueryContext queryContext;
	int lastIndexActor;
	
	public Query(String description, QueryContext queryContext,PredicateFactory predicateFactory) {
		super();
		this.description = description;
		this.queryContext = queryContext;		
		this.predicateFactory = predicateFactory;
		this.queryActorPool = createQueryActorPool(queryContext);
		lastIndexActor = queryContext.getQueryActorPoolSize() - 1;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}	
	
	public static int randomNumber(int min, int max) {	  
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
	
	public ActorRef getQueryActor() {			
		ActorRef queryActor = queryActorPool.get(randomNumber(0, lastIndexActor));
		return queryActor;
	}
	
	public void execute(){
		 queryContext.getDataActors().keySet().forEach((Integer id)->{			
			Predicate<?> predicate = predicateFactory.build();
			this.getQueryActor().tell(new QueryMessage(predicate , id),null);			
		});
	}
	
	public static Map<Integer, ActorRef> createQueryActorPool( QueryContext queryContext){
		ActorSystem actorSystem = queryContext.getActorSystem();
		int poolSize = queryContext.getQueryActorPoolSize();
		Map<Integer, ActorRef> queryActorPool = new HashMap<>(poolSize);
		for (int i = 0; i < poolSize; i++) {
			ActorRef queryActor = actorSystem.actorOf(Props.create(queryContext.getFunctorClass(), queryContext));
			queryActorPool.put(i, queryActor);
		}
		return queryActorPool;
	}
}

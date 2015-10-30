package rfx.server.lambda.query;

import java.io.Serializable;
import java.util.function.Predicate;

public interface PredicateFactory extends Serializable{
	public Predicate<?> build();
}
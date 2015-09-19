package rfx.server.lambda.functions;

import java.util.function.UnaryOperator;

import rfx.server.lambda.SimpleHttpRequest;



@FunctionalInterface
public interface Filter extends UnaryOperator<SimpleHttpRequest> {
	
	
}

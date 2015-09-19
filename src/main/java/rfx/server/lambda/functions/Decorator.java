package rfx.server.lambda.functions;

import java.util.function.UnaryOperator;

import rfx.server.lambda.SimpleHttpResponse;

@FunctionalInterface
public interface Decorator extends UnaryOperator<SimpleHttpResponse>{

}

package rfx.server.lambda.functions;

import java.util.function.Function;

import rfx.server.lambda.SimpleHttpRequest;
import rfx.server.lambda.SimpleHttpResponse;

@FunctionalInterface
public interface FinalProcessor extends Function<SimpleHttpRequest , SimpleHttpResponse>{

}

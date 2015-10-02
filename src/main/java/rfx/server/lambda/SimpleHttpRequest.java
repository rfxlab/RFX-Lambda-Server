package rfx.server.lambda;


import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cookie.Cookie;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

public class SimpleHttpRequest {
	protected String uri = "";
	protected boolean notAuthorized;
	protected Set<Cookie> cookies;
	protected Map<String, List<String>> parameters;
	protected HttpHeaders headers;
	protected byte[] body;

	public SimpleHttpRequest() {
		// TODO Auto-generated constructor stub
	}

	public SimpleHttpRequest(String uri) {
		super();
		this.uri = uri;
	}

	public SimpleHttpRequest(String uri, Map<String, List<String>> parameters) {
		super();
		this.uri = uri;
		this.parameters = parameters;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}	

	public HttpHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public Map<String, List<String>> getParameters() {
		return parameters;
	}
	
	public String getFirstParameter(String key) {
		List<String> values = parameters.get(key);
		if(values != null){
			if(values.size() > 0){
				return values.get(0);
			}	
		}		
		return "";
	}

	public void setParameters(Map<String, List<String>> parameters) {
		this.parameters = parameters;
	}	

	public Set<Cookie> getCookies() {
		return cookies;
	}

	public void setCookies(Set<Cookie> cookies) {
		this.cookies = cookies;
	}

	public boolean isNotAuthorized() {
		return notAuthorized;
	}

	public void setNotAuthorized(boolean notAuthorized) {
		this.notAuthorized = notAuthorized;
	}

	@Override
	public String toString() {		
		return new Gson().toJson(this);
	}
}
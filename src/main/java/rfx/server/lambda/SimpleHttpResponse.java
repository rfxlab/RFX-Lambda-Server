package rfx.server.lambda;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import rfx.server.common.ContentTypePool;
import rfx.server.common.StringPool;

public class SimpleHttpResponse {
	protected int status = 200;
	protected String contentType = ContentTypePool.TEXT_UTF8;
	protected String data;
	protected long time;
	protected Map<String, Object> headers;
	
	public SimpleHttpResponse() {
		data = StringPool.BLANK;
	}

	public SimpleHttpResponse(String data) {
		super();
		this.data = data;
	}
	
	public SimpleHttpResponse(String data, Map<String, Object> headers) {
		super();
		this.data = data;
		this.headers = headers;
	}
	
	public Map<String, Object> getHeaders() {
		if(headers == null){
			headers = new HashMap<>(0);
		}
		return headers;
	}
	
	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
	public String getData() {
		if(data == null){
			data = StringPool.BLANK;
		}
		return data;
	}
	
	public String toJson() {		
		return new Gson().toJson(this);
	}
	
	@Override
	public String toString() {		
		return getData();
	}

}
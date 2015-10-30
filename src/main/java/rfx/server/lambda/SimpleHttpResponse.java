package rfx.server.lambda;

import com.google.gson.Gson;

import rfx.server.common.ContentTypePool;
import rfx.server.common.StringPool;

public class SimpleHttpResponse {
	protected int status = 200;
	protected String contentType = ContentTypePool.TEXT_UTF8;
	protected String data;
	protected long time;
	
	public SimpleHttpResponse() {
		data = StringPool.BLANK;
	}

	public SimpleHttpResponse(String data) {
		super();
		this.data = data;
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
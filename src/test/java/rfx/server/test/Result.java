package rfx.server.test;

public class Result {

	String receiverId;
	String jsonData;
	
	public Result() {
		// TODO Auto-generated constructor stub
	}
	
	public Result(String receiverId, String jsonData) {
		super();
		this.receiverId = receiverId;
		this.jsonData = jsonData;
	}
	public String getReceiverId() {
		return receiverId;
	}
	public String getJsonData() {
		return jsonData;
	}
	public void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}
	public void setJsonData(String jsonData) {
		this.jsonData = jsonData;
	}
	
}

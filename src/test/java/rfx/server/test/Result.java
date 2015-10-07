package rfx.server.test;

import java.util.UUID;

public class Result {

	String receiverId;
	UUID uuid;
	String jsonData;
	
	public Result() {
		// TODO Auto-generated constructor stub
	}
	
	public Result(String receiverId, String jsonData) {
		super();
		this.receiverId = receiverId;
		this.jsonData = jsonData;
	}

	public Result(String receiverId, UUID uuid, String jsonData) {
		this.receiverId = receiverId;
		this.uuid = uuid;
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

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
}

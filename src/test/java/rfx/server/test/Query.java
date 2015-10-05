package rfx.server.test;

import java.util.UUID;

public class Query {

    private String senderId;
    private String query;    
    private UUID uuid;

    public Query() {
    }
    
	public Query(String senderId, String query) {
		super();
		this.senderId = senderId;
		this.query = query;
	}
	
	public String getSenderId() {
		return senderId;
	}

	public String getQuery() {
		return query;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

  

}
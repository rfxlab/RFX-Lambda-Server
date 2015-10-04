package rfx.server.common.configs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

public class ServerConfigs {
	public static final String COMMON_CONFIGS_PATH = "configs/server.json";

	String host;
	int httpPort;
	int wsPort;

	private static ServerConfigs instance;
	
	public static ServerConfigs getInstance(){
		return getInstance(COMMON_CONFIGS_PATH);
	}

	public static ServerConfigs getInstance(String configPath) {
		if (instance == null) {
			try {
				String json = FileUtils.readFileToString(new File(configPath));
				instance = new Gson().fromJson(json, ServerConfigs.class);;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	

	
	public int getHttpPort() {
		return httpPort;
	}

	public int getWsPort() {
		return wsPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public void setWsPort(int wsPort) {
		this.wsPort = wsPort;
	}

}

package siena.base.test;

import java.util.HashMap;
import java.util.Map;

import com.google.apphosting.api.ApiProxy.Environment;

public class TestEnvironment implements Environment {

	public String getAppId() {
		return "Unit tests";
	}

	public String getAuthDomain() {
		return "gmail.com";
	}

	public String getDefaultNamespace() {
		return "";
	}

	public String getEmail() {
		return "";
	}

	public String getRequestNamespace() {
		return "gmail.com";
	}

	public String getVersionId() {
		return "1.0";
	}

	public boolean isAdmin() {
		return false;
	}

	public boolean isLoggedIn() {
		return false;
	}

	public void setDefaultNamespace(String s) {
	}

	@Override
	public Map<String, Object> getAttributes() {
		return new HashMap<String, Object>();
	}

	@Override
	public long getRemainingMillis() {
		// TODO Auto-generated method stub
		return 0;
	}

}

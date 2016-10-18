package onethread;

/**
 * TODO
 */
public class SignalTemplate {

	private final String type;
	private final String key;
	private final String method;

	public SignalTemplate(String type, String key, String method) {
		this.type = type;
		this.key = key;
		this.method = method;
	}

	public String getType() {
		return type;
	}

	public String getKey() {
		return key;
	}

	public String getMethod() {
		return method;
	}

	@Override
	public String toString() {
		return String.format("%s.%s()", key, method);
	}
}

package net.sf.microlog.midp.bluetooth.server;

/**
 * Retrieves information about the system running the Microlog server.
 * 
 * @author Jarle Hansen (hansjar@gmail.com)
 * 
 */
public enum SystemInfo {
	OS_NAME("os.name"),
	OS_ARCHITECTURE("os.arch"),
	OS_VERSION("os.version"),
	JAVA_VERSION("java.version");

	private final String key;
	private String value = null;

	private SystemInfo(final String key) {
		this.key = key;
	}
	
	public String getValue() {
		if (value == null) {
			value = System.getProperty(key);
		}

		return (value == null) ? "" : value;
	}
}

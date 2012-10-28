package se.purplescout.purplemow.core;

import java.io.Serializable;

public class LogMessage implements Serializable {

	private static final long serialVersionUID = -3967676389632287771L;

	public enum Type {
		BWF_RIGHT, BWF_LEFT, RANGE_RIGHT, RANGE_LEFT, CURRENT_STATE, MISC;
	}
	
	private final Type type;
	private final String message;
	
	public LogMessage(Type type, String message) {
		this.type = type;
		this.message = message;
	}

	public Type getType() {
		return type;
	}

	public String getValue() {
		return message;
	}
	
	public static LogMessage create(Type type, String message) {
		return new LogMessage(type, message);
	}
}

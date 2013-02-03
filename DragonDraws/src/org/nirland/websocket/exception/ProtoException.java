package org.nirland.websocket.exception;

/**
 * Customized exception class.
 *  
 * @author Nirland
 */

public class ProtoException extends Exception {

	private static final long serialVersionUID = 1L;

	public ProtoException() {
		super();	
	}

	public ProtoException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProtoException(String message) {
		super(message);
	}

	public ProtoException(Throwable cause) {
		super(cause);
	}
	
}

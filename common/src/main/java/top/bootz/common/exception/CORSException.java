package top.bootz.common.exception;

public class CORSException extends Exception {

	private static final long serialVersionUID = -2684800360275208140L;

	public static final CORSException ORIGIN_DENIED = new CORSException("CORS origin denied", 403);

	public static final CORSException UNSUPPORTED_METHOD = new CORSException("Unsupported HTTP method", 405);

	public static final CORSException UNSUPPORTED_REQUEST_HEADER = new CORSException("Unsupported HTTP request header",
			403);

	public static final CORSException INVALID_ACTUAL_REQUEST = new CORSException("Invalid simple/actual CORS request",
			400);

	public static final CORSException INVALID_PREFLIGHT_REQUEST = new CORSException("Invalid preflight CORS request",
			400);

	public static final CORSException MISSING_ACCESS_CONTROL_REQUEST_METHOD_HEADER = new CORSException(
			"Invalid preflight CORS request: Missing Access-Control-Request-Method header", 400);

	public static final CORSException INVALID_HEADER_VALUE = new CORSException(
			"Invalid preflight CORS request: Bad request header value", 400);

	public static final CORSException GENERIC_HTTP_NOT_ALLOWED = new CORSException("Generic HTTP requests not allowed",
			403);
	private final int httpStatusCode;

	private CORSException(String message, int httpStatusCode) {
		super(message);
		this.httpStatusCode = httpStatusCode;
	}

	public int getHTTPStatusCode() {
		return this.httpStatusCode;
	}
}

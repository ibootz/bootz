package top.bootz.common.exception;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AppMessage implements Serializable {

	private static final long serialVersionUID = 8936744394607039058L;

	private int httpStatus; // Http状态码

	private String type; // 异常类型(WARNNING, ERROR)

	private String code; // 应用内部自定义错误代码

	private String message; // 错误信息
	
	private String moreInfo;

	@JsonIgnore
	private Throwable throwable;

	public AppMessage() {
	}

	public AppMessage(int httpStatus) {
		this(httpStatus, null, null, null, null);
	}

	public AppMessage(int httpStatus, String type) {
		this(httpStatus, type, null, null, null);
	}

	public AppMessage(int httpStatus, String type, String code) {
		this(httpStatus, type, code, null, null);
	}

	public AppMessage(int httpStatus, String type, String code, String message) {
		this(httpStatus, type, code, message, null);
	}

	public AppMessage(int httpStatus, String type, String code, String message, Throwable throwable) {
		this.setCode(code);
		this.setHttpStatus(httpStatus);
		this.setType(type);
		this.setMessage(message);
		this.throwable = throwable;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	public String getType() {
		return StringUtils.isBlank(type) ? ExceptionType.ERROR.getDesc() : type;
	}

	public int getHttpStatus() {
		return httpStatus == 0 ? HttpStatus.BAD_REQUEST.value() : httpStatus;
	}

	public String getMoreInfo() {
		return moreInfo;
	}

	public void setMoreInfo(String moreInfo) {
		this.moreInfo = moreInfo;
	}

	public void setHttpStatus(int httpStatus) {
		this.httpStatus = httpStatus;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}

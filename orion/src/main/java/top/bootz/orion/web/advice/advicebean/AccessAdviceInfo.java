package top.bootz.orion.web.advice.advicebean;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import top.bootz.common.base.BaseObject;

/**
 * 用来记录访问日志的切面bean
 * 
 */
@Setter
@Getter
public class AccessAdviceInfo extends BaseObject implements Serializable {

	private static final long serialVersionUID = -5877789774624894127L;

	private String visitor; // 请求者身份标识(即 username)
	
	private String token; // 请求者token

	private String visitorIp; // 访问者Ip

	private String requestURL; // 请求的完整路径

	private String className; // 类名

	private String methodName; // 方法名

	private Map<String, String> inputParamMap = new LinkedHashMap<>(); // 入参

	private boolean returned; // 方法是否有返回值

	private String response; // 返回值的序列化结果

	private long tookMillSeconds; // 方法运行总耗时（毫秒）

	private boolean successed; // 方法是否成功结束运行

	private String exceptionTime; // 异常发生的时间

	private String errMsg; // 异常信息

	@JsonIgnore
	private AdviceException adviceException; // 异常对象

	public void putInputParam(String key, String value) {
		if (!inputParamMap.containsKey(key)) {
			inputParamMap.put(key, value);
		}
	}

	public void removeInputParam(String key) {
		if (inputParamMap.containsKey(key)) {
			inputParamMap.remove(key);
		}
	}

}

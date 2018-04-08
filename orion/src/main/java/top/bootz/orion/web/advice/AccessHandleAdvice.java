package top.bootz.orion.web.advice;

import java.lang.reflect.Method;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import top.bootz.common.constant.BaseSecurityConstants;
import top.bootz.common.exception.ApiException;
import top.bootz.common.utils.DateUtil;
import top.bootz.common.utils.ReflectionUtils;
import top.bootz.common.utils.ToStringUtil;
import top.bootz.orion.web.advice.advicebean.AccessAdviceInfo;
import top.bootz.orion.web.advice.advicebean.AdviceException;

/**
 * 记录请求日志信息的切面
 * 
 * @author dong
 *
 */
@Order(1)
@Aspect
@Component
public class AccessHandleAdvice {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccessHandleAdvice.class);

	private static final String IP_UNKNOWN = "Unknown";

	private static final String IP_ZERO = "0:0:0:0:0:0:0:1";

	private static final String IP_LOCALHOST = "127.0.0.1";
	
	private static final String APP_CLASS_PREFIX = "top.bootz";

	private static final String CONTROLLER_EXECUTION = "(execution(public * top.bootz.orion.web.controller..*.*(..))) and (@annotation(org.springframework.web.bind.annotation.RequestMapping))";

	private static final String EXCEPTION_ADVICE_EXECUTION = "(execution(public * top.bootz.orion.web.advice.ExceptionHandleAdvice.*(..)))";

	@Autowired
	private MessageSource messageSource;

	/**
	 * 因为controller和exceptionadvice类之间关系过于密切， 所以放在一个切面中同时处理controller和异常处理类中的信息
	 */
	@Pointcut(CONTROLLER_EXECUTION + " || " + EXCEPTION_ADVICE_EXECUTION)
	private void pointcutInControllerLayer() {
		// do nothing
	}

	/**
	 * 记录方法执行前后的请求路径,请求人信息,正确响应或者错误响应，并使用日志打印出来
	 * 
	 * @throws Throwable
	 */
	@Around(value = "pointcutInControllerLayer()")
	public Object doAroundInControllerLayer(ProceedingJoinPoint joinPoint) throws Throwable {
		HttpServletRequest request = getHttpServletRequest();
		MethodSignature methodSignature = getMethodSignature(joinPoint);
		Method method = methodSignature.getMethod();
		boolean isAccessible = method.isAccessible();
		method.setAccessible(true);
		String[] paramNames = methodSignature.getParameterNames();
		String returnType = methodSignature.getReturnType().getSimpleName();
		AccessAdviceInfo accessAdviceInfo = new AccessAdviceInfo();
		// visitor
		String token = request.getHeader(BaseSecurityConstants.HEADER_AUTH_TOKEN);
		// TODO
//		String username = this.tokenService.getUsernameFromToken(token);
//		accessAdviceInfo.setVisitor(username);
		// token
		accessAdviceInfo.setToken(token);
		// visitorIp
		String remoteHost = getRemoteHost(request);
		accessAdviceInfo.setVisitorIp(remoteHost);
		// requestURL
		accessAdviceInfo.setRequestURL(request.getRequestURL().toString());
		// className
		accessAdviceInfo.setClassName(joinPoint.getTarget().getClass().getName());
		// methodName
		accessAdviceInfo.setMethodName(joinPoint.getSignature().getName());
		// inputParamMap
		Object[] args = joinPoint.getArgs();
		if (!ArrayUtils.isEmpty(args)) {
			putInputParams(paramNames, accessAdviceInfo, args);
		}
		// hasResponse
		accessAdviceInfo.setReturned(!"void".equalsIgnoreCase(returnType));
		long start = System.currentTimeMillis();
		long tookMillSeconds = 0;
		Object object = null;
		boolean successed = true;
		Date exceptionTime = null;
		String errMsg = "";
		AdviceException adviceException = null;
		try {
			object = joinPoint.proceed();
			tookMillSeconds = System.currentTimeMillis() - start;
		} catch (Throwable e) {
			successed = false;
			exceptionTime = DateUtil.now();
			tookMillSeconds = System.currentTimeMillis() - start;
			errMsg = getErrMsg(e);
			adviceException = new AdviceException(errMsg, e);
			LOGGER.error(e.getMessage());
			throw e;
		} finally {
			// response
			if (accessAdviceInfo.isReturned() && object != null) {
				accessAdviceInfo.setResponse(ToStringUtil.toJSON(object));
			}
			// tookMillSeconds
			accessAdviceInfo.setTookMillSeconds(tookMillSeconds);
			// successed
			accessAdviceInfo.setSuccessed(successed);
			if (!accessAdviceInfo.isSuccessed() && exceptionTime != null) {
				accessAdviceInfo.setExceptionTime(DateUtil.date2str(exceptionTime));
			}
			// errMsg
			accessAdviceInfo.setErrMsg(errMsg);
			// adviceException
			accessAdviceInfo.setAdviceException(adviceException);
			method.setAccessible(isAccessible);
			LOGGER.info(accessAdviceInfo.toString());
		}
		return object;
	}

	private void putInputParams(String[] paramNames, AccessAdviceInfo accessAdviceInfo, Object[] args) {
		for (int i = 0; i < args.length; i++) {
			Class<?> clz = args[i].getClass();
			boolean isAppClass = clz.getName().startsWith(APP_CLASS_PREFIX);
			boolean isSimpleClass = ReflectionUtils.isBaseClassOrString(clz) || ReflectionUtils.isCollection(clz)
					|| ReflectionUtils.isMap(clz);
			// 异常处理切面中方法的参数太过于复杂，这里没有必要作为日志打印详细信息，故排除掉
			boolean isNotExceptionClass = !clz.getName().endsWith("Exception");
			if ((isAppClass || isSimpleClass) && isNotExceptionClass) {
				accessAdviceInfo.putInputParam(paramNames[i], ToStringUtil.toJSON(args[i]));
			} else {
				accessAdviceInfo.putInputParam(paramNames[i], args[i].toString());
			}
		}
	}

	private String getErrMsg(Throwable e) {
		String errMsg;
		if (e instanceof ApiException) {
			ApiException apiException = (ApiException) e;
			errMsg = messageSource.getMessage(apiException.getErrorKey(), apiException.getArgs(), e.getMessage(), null);
		} else if (e instanceof NullPointerException) {
			errMsg = "空指针异常";
		} else {
			errMsg = e.getMessage();
		}
		return errMsg;
	}

	private HttpServletRequest getHttpServletRequest() {
		ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return sra.getRequest();
	}

	private MethodSignature getMethodSignature(JoinPoint joinPoint) {
		Signature signature = joinPoint.getSignature();
		return (MethodSignature) signature;
	}

	private String getRemoteHost(javax.servlet.http.HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		// 多级反向代理之后，x-forwarded-for请求头中的ip会变成多个ip拼接串(128.244.32.123,196.128.199.106),取第一个不是unknow的Ip地址，即为真实的客户端ip
		if (ip != null && ip.length() > 15) {
			String[] ips = ip.split(",");
			for (String subIp : ips) {
				if (!IP_UNKNOWN.equalsIgnoreCase(subIp)) {
					ip = subIp;
					break;
				}
			}
		}
		return IP_ZERO.equals(ip) ? IP_LOCALHOST : ip;
	}

}

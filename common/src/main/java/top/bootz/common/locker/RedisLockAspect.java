package top.bootz.common.locker;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class RedisLockAspect {

	private static final Logger LOGGER = LoggerFactory.getLogger(RedisLockAspect.class);

	private static final String PLACEHOLDER_KEY = "PLACEHOLDER_KEY";

	@Autowired
	private RedisGlobalLocker redisGlobalLocker;

	@Pointcut("@annotation(top.bootz.common.locker.RedisLock)")
	public void lockPoint() {
		// do nothing
	}

	@Around("lockPoint()")
	public Object redisLockAround(ProceedingJoinPoint pjp) throws Throwable {
		Method method = getControllerMethod(pjp);
		RedisLock lockInfo = method.getAnnotation(RedisLock.class);
		String redisKey = getRedisKey(method.getParameters(), pjp.getArgs());
		Object obj = null;
		if (redisGlobalLocker.tryLock(redisKey, RedisGlobalLocker.EXPX_PX, lockInfo.keepMills(),
				lockInfo.action().equals(RedisLock.LockFailAction.CONTINUE))) {
			LOGGER.info("得到锁 [{}]", redisKey);
			try {
				obj = pjp.proceed();
			} finally {
				LOGGER.info("释放锁 [{}]", redisKey);
				redisGlobalLocker.unlockByLua();
			}
		}
		return obj;
	}

	/**
	 * logFailEvent
	 *
	 * @param joinPoint
	 *            JoinPoint
	 * @param ex
	 *            Exception
	 */
	@AfterThrowing(value = "lockPoint()", throwing = "ex")
	public void logFailEvent(JoinPoint joinPoint, Exception ex) {
		Method method = getControllerMethod(joinPoint);
		RedisLock lockInfo = method.getAnnotation(RedisLock.class);
		if (lockInfo != null) {
			String key = lockInfo.value(); // 固定key
			if (StringUtils.isBlank(key)) { // 可变key
				key = getRedisKey(method.getParameters(), joinPoint.getArgs());
			}
			LOGGER.info("异常后释放锁 [{}]", key);
			redisGlobalLocker.unlockByLua();
		}

	}

	private Method getControllerMethod(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		return signature.getMethod();
	}

	private String getRedisKey(Parameter[] params, Object[] orgs) {
		String redisKey = PLACEHOLDER_KEY;
		if (params != null && params.length > 0) {
			Parameter param = null;
			for (int i = 0; i < params.length; i++) {
				param = params[i];
				if (param != null && param.getAnnotation(RedisKey.class) != null) {
					redisKey = (String) orgs[i];
				}
			}
		}
		return redisKey;
	}

}

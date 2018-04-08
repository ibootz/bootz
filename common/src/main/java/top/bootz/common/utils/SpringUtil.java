package top.bootz.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import top.bootz.common.exception.AppException;

public class SpringUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringUtil.class);
	

	private ApplicationContext applicationContext;

	private SpringUtil() {
	}

	private static class SingletonHolder {
		private SingletonHolder() {
		}

		private static SpringUtil beanUtil = new SpringUtil();
	}

	public static SpringUtil getInstance() {
		return SingletonHolder.beanUtil;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void close() {
		this.applicationContext = null;
	}

	/**
	 * 容器启动时，会将applicationContext注入到当前类中。如果是Junit测试，需要手动注入才能使用
	 * 
	 * @param beanName
	 * @return
	 */
	public static Object getBean(String beanName) {
		ApplicationContext ac = SpringUtil.getInstance().getApplicationContext();
		if (ac == null) {
			LOGGER.error("failed inject applicationContext into SpringUtil");
			throw new AppException("Failed inject applicationContext into SpringUtil");
		}
		return ac.getBean(beanName);
	}

}

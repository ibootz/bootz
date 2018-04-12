package top.bootz.common.locker;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import redis.clients.jedis.Jedis;

public class RedisGlobalLocker {

	private static final Logger LOGGER = Logger.getLogger(RedisGlobalLocker.class);

	private static final ThreadLocal<Map<String, String>> lockHolder = new ThreadLocal<Map<String, String>>();

	private static final Random random = new Random();

	private static final String SUCCESS_OK = "OK";

	/** 超时单位：秒 */
	public static final String EXPX_EX = "EX";

	/** 超时单位：毫秒 */
	public static final String EXPX_PX = "PX";

	@Autowired
	private StringRedisTemplate redisTemplate;

	/**
	 * 获取锁
	 * 
	 * @param key
	 * @param expx
	 *            锁超时单位 EX：秒，PX：毫秒
	 * @param time
	 *            锁超时时间
	 * @param isRetry
	 *            获取锁失败时，是否循环等待，直到获取到锁, true:是 false:未获取到锁时，直接返回false
	 * @author Zhangq
	 * @date 2018年3月20日 上午10:03:06
	 */
	public boolean tryLock(String key, final String expx, final long time, boolean isRetry) {
		String lockerId = UUID.randomUUID().toString();
		Map<String, String> locker = new HashMap<String, String>(1);
		locker.put(key, lockerId);
		lockHolder.set(locker);
		long timeout = 200L;
		boolean result = false;
		if (isRetry) {
			while (!(result = tryLock(key, lockerId, expx, time))) {
				try {
					// 设置线程等待时间窗，取一个公平的随机数，防止出现线程饥饿
					Thread.sleep(randomLongWithBoundary(timeout));
				} catch (InterruptedException e) {
					LOGGER.error("try lock failure! error msg [" + e.getMessage() + "]");
				}
			}
		} else {
			result = tryLock(key, lockerId, expx, time);
		}
		return result;
	}

	private long randomLongWithBoundary(long max) {
		long min = 1L;
		return min + (long) (random.nextFloat() * (max - min));
	}

	private boolean tryLock(final String key, final String value, final String expx, final long time) {
		String rs = redisTemplate.execute(new RedisCallback<String>() {

			@Override
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				Jedis jedis = (Jedis) connection.getNativeConnection();
				return jedis.set(key, value, "NX", expx.toUpperCase(), time);
			}

		});
		if (SUCCESS_OK.equals(rs)) {
			return true;
		}
		return false;
	}

	/**
	 * 通过redis事务来实现原子性
	 * 
	 * @author Zhangq
	 * @date 2018年3月20日 上午10:27:07
	 */
	public void unlock() {
		Map<String, String> locker = lockHolder.get();
		if (locker != null) {
			for (Map.Entry<String, String> lock : locker.entrySet()) {
				final String key = lock.getKey();
				final String value = lock.getValue();
				List<Object> txResult = redisTemplate.execute(new SessionCallback<List<Object>>() {
					@SuppressWarnings({ "unchecked", "rawtypes" })
					@Override
					public List<Object> execute(RedisOperations operations) throws DataAccessException {
						operations.watch(key);
						operations.multi();
						if (value.equals(operations.opsForValue().get(key))) {
							operations.delete(key);
						}
						return operations.exec();
					}
				});
				LOGGER.info("unlock [" + txResult + "] key [" + key + "] value [" + value + "]");
			}
		}
		lockHolder.remove();
	}

	/**
	 * 通过lua脚本解锁，保证更好的原子性
	 * 
	 * @return
	 * @author Zhangq
	 * @date 2018年3月20日 上午10:26:44
	 */
	public void unlockByLua() {
		final StringBuilder script = new StringBuilder();
		script.append("if redis.call('get', KEYS[1]) == ARGV[1] ");
		script.append("then ");
		script.append("  return redis.call('del', KEYS[1]) ");
		script.append("else ");
		script.append("  return 0 ");
		script.append("end ");
		Map<String, String> locker = lockHolder.get();
		if (locker != null) {
			for (Map.Entry<String, String> lock : locker.entrySet()) {
				final String key = lock.getKey();
				final String lockerId = lock.getValue();
				redisTemplate.execute(new RedisCallback<Long>() {

					@Override
					public Long doInRedis(RedisConnection connection) throws DataAccessException {
						Jedis jedis = (Jedis) connection.getNativeConnection();
						return (Long) jedis.eval(script.toString(), Collections.singletonList(key),
								Collections.singletonList(lockerId));
					}

				});
			}
		}
		lockHolder.remove();
	}

}

package top.bootz.common.utils;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;

import top.bootz.common.constant.BasePatternConstants;
import top.bootz.common.exception.AppException;

public final class CodeGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(CodeGenerator.class);

	private CodeGenerator() {

	}

	private static long getNextSeqFromRedis(String seqKey) {
		// 每天都从1开始获取新的SEQ
		RedisAtomicLong redisAtomicLong = new RedisAtomicLong(seqKey,
				(RedisConnectionFactory) SpringUtil.getBean("jedisConnectionFactory"));
		long seqNum = redisAtomicLong.incrementAndGet();
		if (seqNum == 1) { // 当第一次取出索引的时候，设置过期时间
			redisAtomicLong.expire(1, TimeUnit.HOURS); // 1小时之后过期
		}
		LOGGER.debug("Obtain seqNum from redis. seqKey [{}] seqNum [{}]", seqKey, seqNum);
		return seqNum;
	}

	/**
	 * 生成订单单号
	 * 
	 * @param perfix
	 * @param length
	 * @return
	 */
	public static String generateOrderNo(String perfix, int length) {
		if (length < 18) {
			throw new AppException("必须保证最小字符的长度 >= 18");
		}
		String timestamp = DateUtil.date2str(DateUtil.now(), BasePatternConstants.DATE_FORMAT_PATTERN_7);
		String seqKey = StringUtil.joinWith("_", perfix, timestamp);
		long seqNum = getNextSeqFromRedis(seqKey);
		int zeroLength = length - perfix.length() - timestamp.length() - String.valueOf(seqNum).length();
		StringBuilder buf = new StringBuilder();
		buf.append(perfix);
		buf.append(timestamp);
		for (int i = 0; i < zeroLength; i++) {
			buf.append("0");
		}
		buf.append(seqNum);
		return buf.toString();
	}

}

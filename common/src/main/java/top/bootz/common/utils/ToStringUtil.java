package top.bootz.common.utils;

import java.lang.reflect.Array;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import net.sf.json.JSONObject;
import top.bootz.common.constant.BaseSymbolicConstants;

/**
 * 格式化数组
 * 
 * @author dong
 *
 */
public final class ToStringUtil {

	private ToStringUtil() {

	}

	/**
	 * 输出类toString方法格式的字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String toString(Object obj) {
		Object result = "";
		Class<?> clz = obj.getClass();
		if (ReflectionUtils.isArray(clz)) {
			result = arrayToString(obj, ToStringStyle.SHORT_PREFIX_STYLE);
		} else if (ReflectionUtils.isBaseClassOrString(clz)) {
			result = obj;
		} else {
			result = ToStringBuilder.reflectionToString(obj, ToStringStyle.SHORT_PREFIX_STYLE);
		}
		return result.toString();
	}

	/**
	 * 输出Json风格的字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String toJSON(Object obj) {
		JSONObject jsonObj = new JSONObject();
		return jsonObj.accumulate(obj.getClass().getSimpleName(), JsonUtil.toJSON(obj)).toString();
	}
	
	private static String arrayToString(Object obj, ToStringStyle style) {
		StringBuilder sb = new StringBuilder("");
		int length = Array.getLength(obj);
		sb.append(BaseSymbolicConstants.LEFT_BRACKETS);
		for (int i = 0; i < length; i++) {
			Object subObj = Array.get(obj, i);
			subObj = subObj == null ? "" : subObj;
			Class<?> clazz = subObj.getClass();
			if (ReflectionUtils.isBaseClassOrString(clazz)) { // 基本类型or字符串类型
				sb.append(String.valueOf(subObj));
			} else if (clazz.isArray()) { // 数组
				sb.append(arrayToString(subObj, style));
			} else { // 其他一般引用类型
				sb.append(ToStringBuilder.reflectionToString(subObj, style));
			}
			if (i < length - 1) {
				sb.append(BaseSymbolicConstants.HALF_WIDTH_COMMA).append(BaseSymbolicConstants.HALF_WIDTH_BLANK);
			}
		}
		sb.append(BaseSymbolicConstants.RIGHT_BRACKETS);
		return sb.toString();
	}

}

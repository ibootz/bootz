package top.bootz.common.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * Json与Object之间相互转换的工具方法，使用jackson
 *
 */
public final class JsonUtil {

	public static final String DATE_FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

	private JsonUtil() {

	}

	public static ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT_DATETIME));
		mapper.registerModule(new JaxbAnnotationModule());
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper;
	}

	public static <T> T fromJSON(String json, Class<T> beanClass) {
		return JsonUtil.fromJSON(json, beanClass, new Class<?>[0]);
	}

	/**
	 * 
	 * @param json
	 *            ： json源文件
	 * @param beanClass
	 *            ： 转换目标类
	 * @param elementClasses
	 *            ： 复杂对象的转换
	 * @return
	 * @throws Exception
	 */
	public static <T> T fromJSON(String json, Class<T> beanClass, Class<?>... elementClasses) {
		ObjectMapper mapper = createMapper();
		try {
			return elementClasses == null || elementClasses.length == 0 ? mapper.readValue(json, beanClass)
					: mapper.readValue(json,
							mapper.getTypeFactory().constructParametricType(beanClass, elementClasses));
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static <T> String toJSON(T t) {
		ObjectMapper mapper = createMapper();
		try {
			return mapper.writeValueAsString(t);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static boolean isJsonStr(String content) {
		boolean isJsonStr = true;
		try {
			getJSONObject(content);
		} catch (Exception e) {
			isJsonStr = false;
		}
		return isJsonStr;
	}

	public static JSONObject getJSONObject(Object payload) {
		return JSONObject.fromObject(payload);
	}

	public static JSONArray getJSONArray(Object payload) {
		return JSONArray.fromObject(payload);
	}

}

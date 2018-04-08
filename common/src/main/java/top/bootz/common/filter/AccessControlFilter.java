package top.bootz.common.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;

import top.bootz.common.utils.ArrayUtil;

/**
 * 1.解决Ajax跨域请求问题的过滤器； 2.配置缓存策略
 * 
 * @author John
 *
 */
public class AccessControlFilter implements Filter {

	/**
	 * 跨域访问，服务端要想获取客户端和设置客户端的Cookie，必须要设置具体的域地址，不能使用通配符*
	 */
	public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

	/**
	 * GET,PUT,POST,DELETE,OPTIONS,HEAD
	 */
	public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

	/**
	 * 设置为true，允许前端提交在ACCESS_CONTROL_ALLOW_ORIGIN中设置的域下的cookie
	 */
	public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

	/**
	 * 允许传进来的消息头，如果有自定义的Token之类的请求头，也需要添加进来,不然后台获取不到值
	 * Origin,X-Requested-With,Content-Type,Accept,Cache-Control
	 */
	public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

	/**
	 * 允许暴露给浏览器的消息头，自定义消息相应头需要在此处指明
	 * Origin,X-Requested-With,Content-Type,Accept,Cache-Control
	 */
	public static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

	/**
	 * 该字段可选，用来指定本次预检请求的有效期，单位为秒
	 */
	public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

	private static final String ORIGIN = "Origin";

	private static final String REFERER = "Referer";

	private static final String HTTP = "http";

	private static final String HTTPS = "https";

	private static final String SLASH = "/";

	private static final String SLASH2 = "//";

	private String allowOrigin;

	private String allowMethods;

	private String allowCredentials;

	private String allowHeaders;

	private String exposeHeaders;

	private String accessControlMaxAge;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.allowOrigin = filterConfig.getInitParameter(ACCESS_CONTROL_ALLOW_ORIGIN);
		this.allowMethods = filterConfig.getInitParameter(ACCESS_CONTROL_ALLOW_METHODS);
		this.allowCredentials = filterConfig.getInitParameter(ACCESS_CONTROL_ALLOW_CREDENTIALS);
		this.allowHeaders = filterConfig.getInitParameter(ACCESS_CONTROL_ALLOW_HEADERS);
		this.exposeHeaders = filterConfig.getInitParameter(ACCESS_CONTROL_EXPOSE_HEADERS);
		this.accessControlMaxAge = filterConfig.getInitParameter(ACCESS_CONTROL_MAX_AGE);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		// // cookie test
		// Cookie[] cookies = request.getCookies();
		// if (cookies != null) {
		// for (Cookie cookie : cookies) {
		// LOGGER.debug("cookie [" + cookie.getName() + "] value [" +
		// cookie.getValue() + "]");
		// }
		// }
		// response.addCookie(new Cookie("test-cookie", "test-cookie"));
		//
		// // session test
		// HttpSession session = request.getSession();
		// if (session != null) {
		// LOGGER.debug("session [" + session.getId() + "]");
		// Enumeration<String> anumAttr = session.getAttributeNames();
		// while (anumAttr.hasMoreElements()) {
		// String attrName = anumAttr.nextElement();
		// String value = (String) session.getAttribute(attrName);
		// LOGGER.debug("attrName [" + attrName + "] value [" + value + "]");
		// }
		// session.setAttribute("test-session", "test-session");
		// }

		// Ajax请求必须要包含在PolarisHttpFilter初始化中配置的域列表中，否则不允许进行跨域请求
		if (StringUtils.isNotEmpty(allowOrigin)) {
			String[] allowOrigins = allowOrigin.split(",");
			allowOrigins = ArrayUtil.trimElems(allowOrigins);
			if (ArrayUtils.isNotEmpty(allowOrigins)) {
				String requestOrigin = this.getOrigin(request);
				if (ArrayUtils.contains(allowOrigins, requestOrigin)) {
					response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, requestOrigin);
				}
			}
		}

		if (StringUtils.isNotEmpty(allowMethods)) {
			response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, allowMethods);
		}

		if (StringUtils.isNotEmpty(allowCredentials)) {
			response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, allowCredentials);
		}

		if (StringUtils.isNotEmpty(allowHeaders)) {
			response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
		}

		if (StringUtils.isNotEmpty(exposeHeaders)) {
			response.setHeader(ACCESS_CONTROL_EXPOSE_HEADERS, exposeHeaders);
		}

		if (StringUtils.isNotEmpty(accessControlMaxAge)) {
			response.setHeader(ACCESS_CONTROL_MAX_AGE, accessControlMaxAge);
		}

		// 不要缓存页面（这里先写死，后续如果需要再设置成配置模式）
		response.setHeader(HttpHeaders.EXPIRES, "-1");
		response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
		response.setHeader(HttpHeaders.PRAGMA, "no-cache");

		chain.doFilter(request, resp);
		
	}

	private String getOrigin(HttpServletRequest req) {
		String origin = req.getHeader(ORIGIN);
		if (origin == null) {
			String referer = ("" + req.getHeader(REFERER)).toLowerCase();
			if (referer.startsWith(HTTP) || referer.startsWith(HTTPS)) {
				String[] arrStr = referer.split(SLASH);
				if (arrStr.length > 2) {
					origin = arrStr[0] + SLASH2 + arrStr[2];
				}
			}
		}
		return origin;
	}

	@Override
	public void destroy() {
		// Do Nothing
	}

}

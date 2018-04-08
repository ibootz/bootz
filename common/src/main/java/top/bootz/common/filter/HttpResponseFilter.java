package top.bootz.common.filter;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import top.bootz.common.wrapper.ApplicationHttpResponseWrapper;

/**
 * 响应过滤器，可以在这里对响应数据做出修改
 * @author John
 *
 */
public class HttpResponseFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// do nothing
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		ApplicationHttpResponseWrapper responseWrapper = new ApplicationHttpResponseWrapper(resp);
		chain.doFilter(req, responseWrapper);
		String content = new String(responseWrapper.getContent(), responseWrapper.getCharacterEncoding());
		OutputStream out = response.getOutputStream();
		out.write(content.getBytes());
		out.flush();
		out.close();
	}

	@Override
	public void destroy() {
		// do nothing
	}

}

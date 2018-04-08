package top.bootz.common.security;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import top.bootz.common.constant.BaseAppConstants;
import top.bootz.common.constant.BaseExceptionConstants;
import top.bootz.common.exception.AppMessage;
import top.bootz.common.utils.JsonUtil;

/**
 * 这个入口点其实仅仅是被ExceptionTranslationFilter引用 由此入口决定redirect、forward的操作
 * 
 * @author John
 *
 */
@Component
public class EntryPointUnauthorizedHandler implements AuthenticationEntryPoint {

	private static final Logger LOG = LoggerFactory.getLogger(EntryPointUnauthorizedHandler.class);

	@Autowired
	private MessageSource messageSource;

	@Override
	public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
			AuthenticationException e) throws IOException, ServletException {
		LOG.warn("Auth failure [" + e.getMessage() + "]");
		String message = messageSource.getMessage(BaseExceptionConstants.UNAUTHORIZED_EXCEPTION, null, null);
		AppMessage appMessage = JsonUtil.fromJSON(message, AppMessage.class);
		appMessage.setMoreInfo(e.getMessage());
		httpServletResponse.addHeader("Content-type", MediaType.APPLICATION_JSON_UTF8_VALUE);
		httpServletResponse.setCharacterEncoding(BaseAppConstants.CHARSET_UTF_8);
		httpServletResponse.setStatus(appMessage.getHttpStatus());
		PrintWriter printWriter = httpServletResponse.getWriter();
		printWriter.write(JsonUtil.toJSON(appMessage));
		printWriter.flush();
		printWriter.close();
	}

}

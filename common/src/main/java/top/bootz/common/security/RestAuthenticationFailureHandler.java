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
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import top.bootz.common.constant.BaseExceptionConstants;
import top.bootz.common.exception.AppMessage;
import top.bootz.common.utils.JsonUtil;

@Component
public class RestAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private static final Logger LOG = LoggerFactory.getLogger(RestAuthenticationFailureHandler.class);
	
	
	@Autowired
	private MessageSource messageSource;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		LOG.warn("Auth failure [" + exception.getMessage() + "]");
		String message = messageSource.getMessage(BaseExceptionConstants.UNAUTHORIZED_EXCEPTION, null, null);
		AppMessage appMessage = JsonUtil.fromJSON(message, AppMessage.class);
		appMessage.setMoreInfo(exception.getMessage());
		response.setStatus(appMessage.getHttpStatus());
		response.addHeader("Content-type", MediaType.APPLICATION_JSON_UTF8_VALUE);
		response.setCharacterEncoding("UTF-8");
		PrintWriter printWriter = response.getWriter();
		printWriter.write(JsonUtil.toJSON(appMessage));
		printWriter.flush();
		printWriter.close();
	}
}

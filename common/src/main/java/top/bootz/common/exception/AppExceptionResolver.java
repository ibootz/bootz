package top.bootz.common.exception;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import top.bootz.common.constant.BaseExceptionConstants;
import top.bootz.common.srv.AppMessageService;
import top.bootz.common.utils.JsonUtil;

/**
 * 此处的全局异常处理和ExceptionHandleAdvice异常处理互补，那里没有捕获到的一些次要的异常，在这里封装之后捕获下来
 * 
 * @author John
 *
 */
@Component
public class AppExceptionResolver extends AbstractHandlerExceptionResolver {

	private static final Logger LOG = LoggerFactory.getLogger(AppExceptionResolver.class);

	@Autowired
	private AppMessageService appMessageSource;

	public ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception ex) {
		AppMessage appMessage = generateAppMessage(request, response, ex);
		if (appMessage == null) {
			return null;
		}
		try {
			response.addHeader("Content-type", MediaType.APPLICATION_JSON_UTF8_VALUE);
			response.setCharacterEncoding("UTF-8");
			PrintWriter printWriter;
			printWriter = response.getWriter();
			printWriter.write(JsonUtil.toJSON(appMessage));
			printWriter.flush();
			printWriter.close();
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
		return new ModelAndView();
	}

	// 这里只处理有限的几个异常，其他异常交由ExceptionHandleAdvice处理
	@SuppressWarnings("deprecation")
	private AppMessage generateAppMessage(HttpServletRequest request, HttpServletResponse response, Exception ex) {
		Integer code = null;
		if (ex instanceof org.springframework.web.servlet.mvc.multiaction.NoSuchRequestHandlingMethodException
				|| ex instanceof NoHandlerFoundException) {
			code = HttpServletResponse.SC_NOT_FOUND;
		} else if (ex instanceof HttpRequestMethodNotSupportedException) {
			String[] supportedMethods = ((HttpRequestMethodNotSupportedException) ex).getSupportedMethods();
			if (supportedMethods != null) {
				response.setHeader("Allow", StringUtils.arrayToDelimitedString(supportedMethods, ", "));
			}
			code = HttpServletResponse.SC_METHOD_NOT_ALLOWED;
		} else if (ex instanceof HttpMediaTypeNotSupportedException) {
			List<MediaType> mediaTypes = ((HttpMediaTypeException) ex).getSupportedMediaTypes();
			if (!CollectionUtils.isEmpty(mediaTypes)) {
				response.setHeader("Accept", MediaType.toString(mediaTypes));
			}
			code = HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
		} else if (ex instanceof HttpMediaTypeNotAcceptableException) {
			code = HttpServletResponse.SC_NOT_ACCEPTABLE;
		} else if (ex instanceof MissingPathVariableException) {
			code = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
		} else if (isBadRequest(ex)) {
			code = HttpServletResponse.SC_BAD_REQUEST;
		} else if (ex instanceof AsyncRequestTimeoutException && !response.isCommitted()) {
			code = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
		} else if (ex instanceof AuthenticationException) {
			code = HttpServletResponse.SC_UNAUTHORIZED;
		}
		AppMessage appMessage = null;
		if (code != null) {
			appMessage = this.appMessageSource.getAppMessage(BaseExceptionConstants.RESOLVE_EXCEPTION,
					new Object[] { code, ex.getMessage() });
			appMessage.setMoreInfo(ex.getMessage());
		}
		return appMessage;
	}

	private boolean isBadRequest(Exception ex) {
		return ex instanceof MissingServletRequestParameterException || ex instanceof ServletRequestBindingException
				|| ex instanceof TypeMismatchException || ex instanceof HttpMessageNotReadableException
				|| ex instanceof MethodArgumentNotValidException || ex instanceof MissingServletRequestPartException
				|| ex instanceof BindException;
	}

}

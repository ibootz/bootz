package top.bootz.orion.web.controller.ping;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.RequestContextUtils;

import top.bootz.common.exception.AppMessage;
import top.bootz.common.utils.JsonUtil;
import top.bootz.orion.web.controller.BaseController;
import top.bootz.orion.web.vo.ping.Pong;

@RestController
@RequestMapping("/ping")
public class PingController extends BaseController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PingController.class);

	@Autowired
	private MessageSource messageSource;

	/**
	 * 相应调用发的测试请求
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseStatus(HttpStatus.OK)
	public Pong ping(HttpServletRequest request) {
		Locale locale = RequestContextUtils.getLocaleResolver(request).resolveLocale(request);
		String message = messageSource.getMessage("ping.success.message", null, locale);
		AppMessage appMessage = JsonUtil.fromJSON(message, AppMessage.class);
		Pong pong = new Pong();
		pong.setAck("success");
		pong.setMessage(appMessage.getMessage());
		HttpSession session = request.getSession();
		LOGGER.warn("message [{}], locale language [{}], sessionId [{}]", message, locale.getLanguage(),
				session.getId());
		return pong;
	}

}

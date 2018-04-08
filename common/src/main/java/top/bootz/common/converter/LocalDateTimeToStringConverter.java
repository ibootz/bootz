package top.bootz.common.converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.core.convert.converter.Converter;

import top.bootz.common.constant.BasePatternConstants;

public class LocalDateTimeToStringConverter implements Converter<LocalDateTime, String> {

	@Override
	public String convert(LocalDateTime source) {
		return source.format(DateTimeFormatter.ofPattern(BasePatternConstants.DATE_FORMAT_PATTERN_1));
	}

}

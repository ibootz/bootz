package top.bootz.common.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 权限验证异常
 * @author John
 *
 */
public class AuthException extends AuthenticationException {

	private static final long serialVersionUID = 3262496088033532197L;
	
	public AuthException(String errorKey) {
		super(errorKey);
	}
	
}

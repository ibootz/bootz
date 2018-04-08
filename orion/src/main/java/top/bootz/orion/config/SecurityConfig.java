package top.bootz.orion.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import top.bootz.common.security.EntryPointUnauthorizedHandler;
import top.bootz.orion.constants.AppConstants;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private EntryPointUnauthorizedHandler unauthorizedHandler;
	
	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.exceptionHandling().authenticationEntryPoint(this.unauthorizedHandler)
			.and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
			.and().csrf().disable()
			.authorizeRequests()
				.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.antMatchers(HttpMethod.GET, "/static/**", "/*.html", 
						"/favicon.ico", "/**/*.html", "/**/*.css", "/**/*.js", 
						AppConstants.SECURITY_MAPPING_URL_PATTERN + "/ping").permitAll()
				.antMatchers(AppConstants.SECURITY_MAPPING_URL_PATTERN + "/auth/**").permitAll()
				.anyRequest().authenticated()
			.and().httpBasic();
	}

}

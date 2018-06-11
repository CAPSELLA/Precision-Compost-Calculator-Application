package gr.uoa.di.madgik.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {



	@Override
	public void configure(WebSecurity web) throws Exception {

		web.ignoring()
				// ignoring the "/", "/index.html", "/app/**", "/register",
				// "/favicon.ico"
				.antMatchers("/", "/index.html", "/app/**", "/register","/login","/auth", "/authenticate","/getGroups","/swagger-ui.html", "/webjars/springfox-swagger-ui/css/typography.css",
						"/webjars/springfox-swagger-ui/css/reset.css","/webjars/springfox-swagger-ui/css/screen.css", 
						"/webjars/springfox-swagger-ui/lib/object-assign-pollyfill.js", "/webjars/springfox-swagger-ui/lib/jquery-1.8.0.min.js",
						"/webjars/springfox-swagger-ui/lib/jquery.slideto.min.js", "/webjars/springfox-swagger-ui/lib/jquery.wiggle.min.js",
						"/webjars/springfox-swagger-ui/lib/jquery.ba-bbq.min.js", "/webjars/springfox-swagger-ui/lib/handlebars-4.0.5.js",
						"/webjars/springfox-swagger-ui/lib/lodash.min.js", "/webjars/springfox-swagger-ui/lib/backbone-min.js",
						"/webjars/springfox-swagger-ui/swagger-ui.min.js", "/webjars/springfox-swagger-ui/lib/highlight.9.1.0.pack.js",
						"/webjars/springfox-swagger-ui/lib/highlight.9.1.0.pack_extended.js", "/webjars/springfox-swagger-ui/lib/jsoneditor.min.js", 
						"/webjars/springfox-swagger-ui/lib/marked.js", "/webjars/springfox-swagger-ui/lib/swagger-oauth.js",
						"/webjars/springfox-swagger-ui/springfox.js", "/webjars/springfox-swagger-ui/images/logo_small.png",
						"/webjars/springfox-swagger-ui/css/print.css", "/webjars/springfox-swagger-ui/lib/jquery-1.8.0.min.js",
						"/webjars/springfox-swagger-ui/lib/jquery.slideto.min.js", "/webjars/springfox-swagger-ui/lib/jquery.wiggle.min.js",
						"/webjars/springfox-swagger-ui/lib/jquery.ba-bbq.min.js", "/webjars/springfox-swagger-ui/lib/handlebars-4.0.5.js", 
						"/webjars/springfox-swagger-ui/lib/lodash.min.js", "/webjars/springfox-swagger-ui/lib/backbone-min.js", 
						"/webjars/springfox-swagger-ui/swagger-ui.min.js", "/webjars/springfox-swagger-ui/lib/highlight.9.1.0.pack.js", 
						"/webjars/springfox-swagger-ui/lib/highlight.9.1.0.pack_extended.js", "/webjars/springfox-swagger-ui/lib/jsoneditor.min.js",
						"webjars/springfox-swagger-ui/lib/marked.js", "/webjars/springfox-swagger-ui/lib/swagger-oauth.js", 
						"/webjars/springfox-swagger-ui/springfox.js", "/webjars/springfox-swagger-ui/images/logo_small.png",
						"/webjars/springfox-swagger-ui/images/favicon-16x16.png","/webjars/springfox-swagger-ui/fonts/DroidSans-Bold.ttf",
						"/webjars/springfox-swagger-ui/fonts/DroidSans.ttf", "/swagger-resources/configuration/ui", "/swagger-resources", 
						"/v2/api-docs", "/swagger-resources/configuration/security",  "/reset.css", "/favicon.ico");
	}

	// This method is used for override HttpSecurity of the web Application.
	// We can specify our authorization criteria inside this method.
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			//the session as state less. Which means there is
				// no session in the server
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//				// disabling the CSRF - Cross Site Request Forgery
				.csrf().disable();
	}

}
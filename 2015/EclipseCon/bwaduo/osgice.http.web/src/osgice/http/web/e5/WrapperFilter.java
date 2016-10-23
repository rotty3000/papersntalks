package osgice.http.web.e5;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

@Component(
	enabled = false,
	property = {
		HttpWhiteboardConstants.HTTP_WHITEBOARD_FILTER_PATTERN + "=/*"
	}
)
public class WrapperFilter implements Filter {

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		System.out.println("Filter BEFORE");
		chain.doFilter(request, response);
		System.out.println("Filter AFTER");
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}
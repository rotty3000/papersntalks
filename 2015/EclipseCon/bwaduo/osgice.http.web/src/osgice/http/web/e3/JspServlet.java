package osgice.http.web.e3;

import javax.servlet.Servlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

@Component(
	property = {
		HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "=*.jsp"
	},
	service = Servlet.class
)
@SuppressWarnings("serial")
public class JspServlet extends com.liferay.portal.servlet.jsp.compiler.JspServlet {
}
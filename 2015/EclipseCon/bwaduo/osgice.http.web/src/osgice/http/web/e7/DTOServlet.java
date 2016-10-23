package osgice.http.web.e7;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.runtime.HttpServiceRuntime;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

@Component(
	immediate = true,
	property = {
		HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "=/dto"
	},
	service = Servlet.class
)
@SuppressWarnings("serial")
public class DTOServlet extends HttpServlet {

	protected void service(
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ServletException {

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		PrintWriter writer = response.getWriter();

		writer.write(_httpServiceRuntime.getRuntimeDTO().toString());
		writer.close();
	}

	@Reference
	protected void setHttpServiceRuntime(
		HttpServiceRuntime httpServiceRuntime) {

		_httpServiceRuntime = httpServiceRuntime;
	}

	private HttpServiceRuntime _httpServiceRuntime;

}
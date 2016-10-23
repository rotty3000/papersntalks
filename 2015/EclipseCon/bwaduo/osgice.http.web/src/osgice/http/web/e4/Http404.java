package osgice.http.web.e4;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

@Component(
	enabled = false,
	property = {
		HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_ERROR_PAGE + "=404"
	},
	service = Servlet.class
)
@SuppressWarnings("serial")
public class Http404 extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws IOException {

		PrintWriter writer = response.getWriter();

		writer.write("<h1>Error: 404</h1>");
	}

}
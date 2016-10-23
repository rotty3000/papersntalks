package osgice.http.web.e1;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

@Component(
		property = {HttpWhiteboardConstants.HTTP_WHITEBOARD_SERVLET_PATTERN + "=/osgice"},
		service = Servlet.class
		)
public class HelloServlet extends HttpServlet {

	@Override
	protected void service(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {

		PrintWriter writer = arg1.getWriter();

		writer.write("Hello OSGICE!!!!!");

	}

}
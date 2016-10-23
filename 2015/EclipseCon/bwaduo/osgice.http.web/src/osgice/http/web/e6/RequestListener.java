package osgice.http.web.e6;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.whiteboard.HttpWhiteboardConstants;

@Component(
	enabled = false,
	property = {
		HttpWhiteboardConstants.HTTP_WHITEBOARD_LISTENER + "=true"
	}
)
public class RequestListener implements ServletRequestListener {

	@Override
	public void requestDestroyed(ServletRequestEvent event) {
		System.out.println("Request DESTROYED!");
	}

	@Override
	public void requestInitialized(ServletRequestEvent event) {
		System.out.println("Request INITIALIZED!");
	}

}
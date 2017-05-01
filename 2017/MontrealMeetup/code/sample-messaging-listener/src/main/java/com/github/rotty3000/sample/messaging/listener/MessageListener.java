package com.github.rotty3000.sample.messaging.listener;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageListenerException;

@ObjectClassDefinition
@interface Config {
	String destination_name() default "default";
}

@Component(property = "destination.name=default")
@Designate(ocd = Config.class)
public class MessageListener
	implements com.liferay.portal.kernel.messaging.MessageListener {

	@Override
	public void receive(Message message) throws MessageListenerException {
		_log.info("Recieved Message: {}", message.toString());
	}

	void activate(Config config) {
		if (_log.isDebugEnabled()) {
			_log.debug(
				"Activating Message Listener with {destination.name = {}}", config.destination_name());
		}
	}

	void deactivate() {
		if (_log.isDebugEnabled()) {
			_log.debug("Deactivating Message Listener");
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(MessageListener.class);

}
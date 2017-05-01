package com.github.rotty3000.sample.messaging.generator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.portal.kernel.messaging.Destination;
import com.liferay.portal.kernel.messaging.Message;
import com.liferay.portal.kernel.messaging.MessageBus;
import com.liferay.portal.kernel.messaging.ParallelDestination;

@ObjectClassDefinition
@interface Config {
	String destination_name() default "demo";
	int initial_delay() default 1;
	int period() default 1;
	int threads() default 5;
	String time_unit() default "SECONDS";
}

@Component(property = "destination.name=demo", service = Destination.class)
@Designate(ocd = Config.class)
public class MessageGenerator extends ParallelDestination {

	@Activate
	void activate(final Config config) {
		if (_log.isDebugEnabled()) {
			_log.debug(
				"Activating Message Generator with {destination.name = {}, initial.deplay = {}, " +
					"period = {}, threads = {}, time.unit = {}}", config.destination_name(),
					config.initial_delay(), config.period(), config.threads(), config.time_unit());
		}

		_executor = Executors.newScheduledThreadPool(config.threads());

		final Runnable messanger = new Runnable() {
			public void run() {
				Message message = new Message();

				message.setDestinationName(config.destination_name());
				message.put("initial.delay", config.initial_delay());
				message.put("period", config.period());
				message.put("threads", config.threads());
				message.put("time.unit", config.time_unit());

				_messageBus.sendMessage(config.destination_name(), message);
			}
		};

		_messangerHandler = _executor.scheduleAtFixedRate(
			messanger, config.initial_delay(), config.period(),
			TimeUnit.valueOf(config.time_unit()));
	}

	void deactivate() {
		if (_log.isDebugEnabled()) {
			_log.debug("Deactivating Message Generator");
		}

		_messangerHandler.cancel(false);
		_executor.shutdown();
	}

	private static final Logger _log = LoggerFactory.getLogger(MessageGenerator.class);

	@Reference
	MessageBus _messageBus;

	ScheduledExecutorService _executor;
	ScheduledFuture<?> _messangerHandler;

}
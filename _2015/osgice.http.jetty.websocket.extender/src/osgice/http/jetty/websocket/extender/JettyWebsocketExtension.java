package osgice.http.jetty.websocket.extender;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

@Component
public class JettyWebsocketExtension {

    /**
     * Servlet 3.1 approach.
     * <p>
     * This will use Servlet 3.1 techniques on the {@link ServletContext} to add a filter at the start of the filter chain.
     *
     * @param context the servlet context
     * @param jettyContext the jetty servlet context handler
     * @return the created websocket server container
     * @throws ServletException if unable to create the websocket server container
     */
    public static ServerContainer configureContext(
    		ServletContext context, ServletContextHandler jettyContext)
    	throws ServletException {

        // Create Filter
        WebSocketUpgradeFilter filter = WebSocketUpgradeFilter.configureContext(context);

        // Create the Jetty ServerContainer implementation
        ServerContainer jettyContainer = new ServerContainer(
        	filter, filter.getFactory(), jettyContext.getServer().getThreadPool());

        jettyContext.addBean(jettyContainer);

        // Store a reference to the ServerContainer per javax.websocket spec 1.0 final section 6.4 Programmatic Server Deployment
        context.setAttribute(
        	javax.websocket.server.ServerContainer.class.getName(),jettyContainer);

        return jettyContainer;
    }

    private boolean isEnabled(Set<Class<?>> c, ServletContext context) {
        // if not forced on or off, determine behavior based on annotations.
        if (c.isEmpty()) {
        	logService.log(LogService.LOG_DEBUG, String.format("No JSR-356 annotations or interfaces discovered. JSR-356 support disabled on context %1 - %2", context.getContextPath(), context));
            return false;
        }

        return true;
    }

    public void onStartup(Set<Class<?>> c, ServletContext context) throws ServletException {
        if (!isEnabled(c, context)) {
            return;
        }

        ContextHandler handler = ContextHandler.getContextHandler(context);

        if (handler == null) {
            throw new ServletException("Not running on Jetty, JSR-356 support unavailable");
        }

        if (!(handler instanceof ServletContextHandler)) {
            throw new ServletException("Not running in Jetty ServletContextHandler, JSR-356 support unavailable");
        }

        ServletContextHandler jettyContext = (ServletContextHandler)handler;

        ClassLoader old = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(context.getClassLoader());

            // Create the Jetty ServerContainer implementation
            ServerContainer jettyContainer = configureContext(context,jettyContext);

            // Store a reference to the ServerContainer per javax.websocket spec 1.0 final section 6.4 Programmatic Server Deployment
            context.setAttribute(javax.websocket.server.ServerContainer.class.getName(),jettyContainer);

        	logService.log(LogService.LOG_INFO, String.format("Found %1 classes", c.size()));

            // Now process the incoming classes
            Set<Class<? extends Endpoint>> discoveredExtendedEndpoints = new HashSet<>();
            Set<Class<?>> discoveredAnnotatedEndpoints = new HashSet<>();
            Set<Class<? extends ServerApplicationConfig>> serverAppConfigs = new HashSet<>();

            filterClasses(c, discoveredExtendedEndpoints, discoveredAnnotatedEndpoints, serverAppConfigs);

        	logService.log(LogService.LOG_DEBUG, String.format("Discovered %1 extends Endpoint classes",discoveredExtendedEndpoints.size()));
        	logService.log(LogService.LOG_DEBUG, String.format("Discovered %1 @ServerEndpoint classes",discoveredAnnotatedEndpoints.size()));
        	logService.log(LogService.LOG_DEBUG, String.format("Discovered %1 ServerApplicationConfig classes",serverAppConfigs.size()));

            // Process the server app configs to determine endpoint filtering
            boolean wasFiltered = false;
            Set<ServerEndpointConfig> deployableExtendedEndpointConfigs = new HashSet<>();
            Set<Class<?>> deployableAnnotatedEndpoints = new HashSet<>();

            for (Class<? extends ServerApplicationConfig> clazz : serverAppConfigs) {
            	logService.log(LogService.LOG_DEBUG, String.format("Found ServerApplicationConfig: %1",clazz));

            	try {
                    ServerApplicationConfig config = clazz.newInstance();

                    Set<ServerEndpointConfig> seconfigs = config.getEndpointConfigs(discoveredExtendedEndpoints);

                    if (seconfigs != null) {
                        wasFiltered = true;
                        deployableExtendedEndpointConfigs.addAll(seconfigs);
                    }

                    Set<Class<?>> annotatedClasses = config.getAnnotatedEndpointClasses(discoveredAnnotatedEndpoints);

                    if (annotatedClasses != null) {
                        wasFiltered = true;
                        deployableAnnotatedEndpoints.addAll(annotatedClasses);
                    }
                }
                catch (InstantiationException | IllegalAccessException e) {
                    throw new ServletException("Unable to instantiate: " + clazz.getName(),e);
                }
            }

            // Default behavior if nothing filtered
            if (!wasFiltered) {
                deployableAnnotatedEndpoints.addAll(discoveredAnnotatedEndpoints);
                // Note: it is impossible to determine path of "extends Endpoint" discovered classes
                deployableExtendedEndpointConfigs = new HashSet<>();
            }

        	logService.log(LogService.LOG_DEBUG, String.format("Deploying %1 ServerEndpointConfig(s)", deployableExtendedEndpointConfigs.size()));

        	// Deploy what should be deployed.
            for (ServerEndpointConfig config : deployableExtendedEndpointConfigs) {
                try {
                    jettyContainer.addEndpoint(config);
                }
                catch (DeploymentException e)
                {
                    throw new ServletException(e);
                }
            }

        	logService.log(LogService.LOG_DEBUG, String.format("Deploying %1 @ServerEndpoint(s)",deployableAnnotatedEndpoints.size()));

        	for (Class<?> annotatedClass : deployableAnnotatedEndpoints) {
                try {
                    jettyContainer.addEndpoint(annotatedClass);
                }
                catch (DeploymentException e) {
                    throw new ServletException(e);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    @SuppressWarnings("unchecked")
    private void filterClasses(
    	Set<Class<?>> c, Set<Class<? extends Endpoint>> discoveredExtendedEndpoints,
    	Set<Class<?>> discoveredAnnotatedEndpoints,
    	Set<Class<? extends ServerApplicationConfig>> serverAppConfigs) {

        for (Class<?> clazz : c) {
            if (ServerApplicationConfig.class.isAssignableFrom(clazz)) {
                serverAppConfigs.add((Class<? extends ServerApplicationConfig>)clazz);
            }

            if (Endpoint.class.isAssignableFrom(clazz)) {
                discoveredExtendedEndpoints.add((Class<? extends Endpoint>)clazz);
            }

            ServerEndpoint endpoint = clazz.getAnnotation(ServerEndpoint.class);

            if (endpoint != null) {
                discoveredAnnotatedEndpoints.add(clazz);
            }
        }
    }

    @Reference
    void setLogService(LogService logService) {
    	this.logService = logService;
    }

    private LogService logService;

}
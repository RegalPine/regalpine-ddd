package io.github.regalpine.ddd.messaging;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default {@link MessageRouter} implementation that routes messages based on message type.
 *
 * <p>Routes are explicitly registered (no magic auto-discovery).
 * Supports fallback to a default route for unmatched message types.</p>
 *
 * @author RegalPine
 */
public final class DefaultMessageRouter implements MessageRouter {

    private final Map<String, Route> routes = new ConcurrentHashMap<>();
    private final Route defaultRoute;

    /**
     * Creates a router with no default route.
     */
    public DefaultMessageRouter() {
        this.defaultRoute = null;
    }

    /**
     * Creates a router with a default fallback route.
     *
     * @param defaultRoute the fallback route for unmatched message types
     */
    public DefaultMessageRouter(Route defaultRoute) {
        this.defaultRoute = defaultRoute;
    }

    /**
     * Creates a router pre-configured with the given routes.
     *
     * @param initialRoutes map from message type to route
     */
    public DefaultMessageRouter(Map<String, Route> initialRoutes) {
        this.defaultRoute = null;
        Objects.requireNonNull(initialRoutes, "initialRoutes must not be null");
        routes.putAll(initialRoutes);
    }

    /**
     * Registers a route for a message type.
     *
     * @param messageType the message type
     * @param route       the route
     */
    public void register(String messageType, Route route) {
        Objects.requireNonNull(messageType, "messageType must not be null");
        Objects.requireNonNull(route, "route must not be null");
        routes.put(messageType, route);
    }

    @Override
    public Route route(MessageEnvelope message) {
        Route route = routes.get(message.messageType());
        if (route != null) {
            return route;
        }
        if (defaultRoute != null) {
            return defaultRoute;
        }
        throw new IllegalArgumentException("No route found for message type: " + message.messageType());
    }

    /**
     * Returns all registered routes.
     */
    public Map<String, Route> routes() {
        return Collections.unmodifiableMap(routes);
    }
}

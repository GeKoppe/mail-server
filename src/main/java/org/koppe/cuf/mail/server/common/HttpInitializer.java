package org.koppe.cuf.mail.server.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.koppe.cuf.mail.server.common.exceptions.StartupException;
import org.koppe.cuf.mail.server.http.HttpServer;
import org.koppe.cuf.mail.server.http.entities.Endpoint;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HttpInitializer {
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(HttpInitializer.class);
    /**
     * List of all announced endpoints
     */
    private static final List<Endpoint<?, ?>> endpoints = new ArrayList<>();

    /**
     * Initializes all endpoints on the http server
     * 
     * @throws StartupException Thrown by the http sever in
     *                          {@link HttpServer#registerEndpoint(Endpoint)}
     */
    public static void initializeEndpoints() throws StartupException {
        logger.info("Initializing http endpoints");
        searchEndpoints();
        for (var e : endpoints) {
            logger.info("Registering endpoint {}", e);
            try {
                HttpServer.registerEndpoint(e);
            } catch (StartupException e1) {
                logger.error("Exception occurred while registering endpoint {}", e, e1);
                throw new StartupException("Exception occurred while registering an endpoint", e1);
            }
            logger.info("Endpoint {} successfully registered", e);
        }
        logger.info("Successfully initialized all announced endpoints");
    }

    private static void searchEndpoints() throws StartupException {
        logger.debug("Searching for all endpoints");
        Reflections r = new Reflections("org.koppe.cuf.mail.server.http.endpoints");

        @SuppressWarnings("rawtypes")
        Set<Class<? extends Endpoint>> clazzes = r.getSubTypesOf(Endpoint.class);

        for (var x : clazzes) {
            logger.debug("Trying to instantiate class {} into an endpoint", x);
            try {
                @SuppressWarnings("rawtypes")
                Constructor<? extends Endpoint> c = x.getDeclaredConstructor();

                c.setAccessible(true);
                Endpoint<?, ?> e = c.newInstance();

                logger.info("Instantiated endpoint {}", e);
                endpoints.add(e);
            } catch (NoSuchMethodException e) {
                logger.error("Endpoint {} does not have a no args constructor", x, e);
                throw new StartupException("Missing no args constructor", e);
            } catch (SecurityException e) {
                logger.error("Endpoint {} does not have a public no args constructor", x, e);
                throw new StartupException("Missing public no args constructor", e);
            } catch (InstantiationException e) {
                logger.error("Could not instantiate class {}", x, e);
                throw new StartupException("Could not instantiate endpoint", e);
            } catch (IllegalAccessException e) {
                logger.error("Illegally accessed constructor on class {}", x, e);
                throw new StartupException("Illegally accessed constructor", e);
            } catch (IllegalArgumentException e) {
                logger.error("Illegal arguments in constructor call for class {}", x, e);
                throw new StartupException("Illegal argument in constructor call", e);
            } catch (InvocationTargetException e) {
                logger.error("Could not invoke constructor on class {}", x, e);
                throw new StartupException("Could not invoke constructor", e);
            }
        }
    }

    /**
     * Announce an endpoint to the initializer
     * 
     * @param e Endpoint to announce
     */
    public static void announce(Endpoint<?, ?> e) {
        endpoints.add(e);
    }
}

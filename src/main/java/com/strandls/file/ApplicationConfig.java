package com.strandls.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.spi.Container;
import org.glassfish.jersey.server.spi.ContainerLifecycleListener;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.GenericOpenApiContextBuilder;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.core.Application;

public class ApplicationConfig extends Application {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

	private final SwaggerConfiguration swaggerConfiguration;

	public ApplicationConfig() {
		Properties properties = new Properties();

		try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
			if (in == null) {
				logger.error("config.properties not found in classpath");
			} else {
				properties.load(in);
			}
		} catch (IOException e) {
			logger.error("Error loading config.properties", e);
		}

		// Setup OpenAPI Info from properties
		Info info = new Info().title(properties.getProperty("title")).version(properties.getProperty("version"));

		// Setup server URL from host + basePath + schemes
		String host = properties.getProperty("host");
		String basePath = properties.getProperty("basePath");
		String[] schemes = properties.getProperty("schemes").split(",");
		// Just pick the first scheme (usually http or https)
		String scheme = schemes.length > 0 ? schemes[0] : "http";

		String serverUrl = scheme + "://" + host + basePath;

		Server server = new Server().url(serverUrl);

		Set<String> resourcePackages = Set.of(properties.getProperty("resourcePackage"));

		swaggerConfiguration = new SwaggerConfiguration().openAPI(new OpenAPI().info(info).servers(List.of(server)))
				.resourcePackages(resourcePackages)
				.prettyPrint(Boolean.parseBoolean(properties.getProperty("prettyPrint", "true")))
				.prettyPrint(Boolean.parseBoolean(properties.getProperty("scan", "true")));

		try {
			new GenericOpenApiContextBuilder().openApiConfiguration(swaggerConfiguration).buildContext(true);
		} catch (Exception e) {
			logger.error("Failed to initialize OpenAPI context", e);
		}
	}

	@Override
	public Set<Object> getSingletons() {
		Set<Object> singletons = new HashSet<>();

		singletons.add(new ContainerLifecycleListener() {
			@Override
			public void onStartup(Container container) {
				ServletContainer servletContainer = (ServletContainer) container;
				ServiceLocator serviceLocator = container.getApplicationHandler().getInjectionManager()
						.getInstance(ServiceLocator.class);
				GuiceBridge.getGuiceBridge().initializeGuiceBridge(serviceLocator);
				GuiceIntoHK2Bridge guiceBridge = serviceLocator.getService(GuiceIntoHK2Bridge.class);
				Injector injector = (Injector) servletContainer.getServletContext()
						.getAttribute(Injector.class.getName());
				guiceBridge.bridgeGuiceInjector(injector);
			}

			@Override
			public void onShutdown(Container container) {
			}

			@Override
			public void onReload(Container container) {
			}
		});

		return singletons;
	}

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> resources = new HashSet<>();
		// Add OpenAPI resource that exposes the /openapi endpoint
		resources.add(OpenApiResource.class);

		return resources;
	}
}

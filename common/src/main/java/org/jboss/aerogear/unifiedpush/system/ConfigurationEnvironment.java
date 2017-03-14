package org.jboss.aerogear.unifiedpush.system;

import java.util.Iterator;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;

@Configuration
@PropertySource(value = { "classpath:default.properties", "file://${aerogear.config}" }, ignoreResourceNotFound = true)
public class ConfigurationEnvironment {

	public static final String PROPERTIES_DOCUMENTS_QUERY_DAYS = "aerogear.config.documents.query.period.days";
	public static final String PROP_ENABLE_VERIFICATION = "aerogear.config.verification.enable_verification";
	public static final String PROP_MASTER_VERIFICATION = "aerogear.config.verification.master_code";

	@Autowired
	private Environment env;

	private Properties properties;

	public Boolean isVerificationEnabled() {
		return env.getProperty(PROP_ENABLE_VERIFICATION, Boolean.class, Boolean.FALSE);
	}

	public String getMasterCode() {
		return env.getProperty(PROP_MASTER_VERIFICATION, String.class, null);
	}

	/*
	 * Number of days period to query existing documents.
	 */
	public Integer getQueryDefaultPeriodInDays() {
		return env.getProperty(PROPERTIES_DOCUMENTS_QUERY_DAYS, Integer.class, 14);
	}

	public String getProperty(String key, String defaultValue) {
		return env.getProperty(key, defaultValue);
	}

	public Boolean getProperty(String key, Boolean defaultValue) {
		return env.getProperty(key, Boolean.class, defaultValue);
	}

	/**
	 * Property placeholder configurer needed to process @Value annotations
	 */
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	/**
	 * Cache and return all Environment properties
	 */
	public Properties getProperties() {
		if (properties == null) {
			synchronized (this) {
				if (properties == null)
					properties = new Properties();
			}
		}

		for (Iterator<org.springframework.core.env.PropertySource<?>> it = ((AbstractEnvironment) env)
				.getPropertySources().iterator(); it.hasNext();) {
			org.springframework.core.env.PropertySource<?> propertySource = (org.springframework.core.env.PropertySource<?>) it
					.next();
			if (propertySource instanceof MapPropertySource) {
				properties.putAll(((MapPropertySource) propertySource).getSource());
			}
		}

		return properties;
	}

}

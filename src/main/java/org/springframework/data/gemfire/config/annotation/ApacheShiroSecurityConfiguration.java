/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.data.gemfire.config.annotation;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.geode.cache.GemFireCache;
import org.apache.geode.internal.security.SecurityService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.gemfire.util.CollectionUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * The ApacheShiroSecurityConfiguration class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@Configuration
@Conditional(ApacheShiroSecurityConfiguration.ApacheShiroPresentCondition.class)
@SuppressWarnings("unused")
public class ApacheShiroSecurityConfiguration {

	@Autowired(required = false)
	private List<Realm> realms = Collections.emptyList();

	@Bean
	@Conditional(ShiroRealmsConfigured.class)
	public BeanPostProcessor shiroLifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

	/**
	 * {@link Bean} definition used to configure and declare an Apache Shiro
	 * {@link org.apache.shiro.mgt.SecurityManager} implementation used to secure Apache Geode.
	 *
	 * This {@link Bean} definition adds an arbitrary dependency on the Apache Geode {@link GemFireCache}
	 * in order to ensure the Geode cache instance is created first!
	 *
	 * @return an Apache Shiro {@link org.apache.shiro.mgt.SecurityManager} implementation to secure Apache Geode.
	 * @see org.apache.shiro.mgt.SecurityManager
	 * @see #getRealms()
	 */
	@Bean
	@Conditional(ShiroRealmsConfigured.class)
	public org.apache.shiro.mgt.SecurityManager shiroSecurityManager(GemFireCache gemfireCache) {
		return new DefaultSecurityManager(getRealms());
	}

	/* (non-Javadoc) */
	protected List<Realm> getRealms() {
		return this.realms;
	}

	/* (non-Javadoc) */
	protected boolean isRealmsPresent() {
		return !getRealms().isEmpty();
	}

	/**
	 * Post processes the security configuration by registering the Apache Shiro
	 * {@link org.apache.shiro.mgt.SecurityManager} in the Apache Shiro context, making it accessible
	 * to application components and enables security services in Apache Geode.
	 *
	 * @param securityManager {@link org.apache.shiro.mgt.SecurityManager} to register
	 * and setup for Apache Geode security services.
	 * @see #registerSecurityManager(org.apache.shiro.mgt.SecurityManager)
	 * @see #enableApacheGeodeSecurity()
	 * @see org.apache.shiro.mgt.SecurityManager
	 */
	@PostConstruct
	public void postProcess(org.apache.shiro.mgt.SecurityManager securityManager) {
		if (securityManager != null) {
			registerSecurityManager(securityManager);

			if (!enableApacheGeodeSecurity()) {
				throw new IllegalStateException("Failed to enable security services in Apache Geode");
			}
		}
	}

	/**
	 * Sets the Apache Geode Integrated Security framework {@literal isIntegratedSecurity} property to signify
	 * that Apache Geode Security should be enabled.
	 *
	 * @return a boolean value indicating whether Apache Geode's Integrated Security services were successfully
	 * enabled.
	 */
	protected boolean enableApacheGeodeSecurity() {
		SecurityService securityService = SecurityService.getSecurityService();

		if (securityService != null) {
			Field isIntegratedSecurity = ReflectionUtils.findField(securityService.getClass(),
				"isIntegratedSecurity", Boolean.TYPE);

			if (isIntegratedSecurity != null) {
				ReflectionUtils.setField(isIntegratedSecurity, securityService, true);
			}
		}

		return false;
	}

	/**
	 * Registers the Apache Shiro {@link org.apache.shiro.mgt.SecurityManager} with the Apache Shiro
	 * Security framework, making it accessible in the user's application.
	 *
	 * @param securityManager {@link org.apache.shiro.mgt.SecurityManager} to register with the Apache Shiro framework.
	 * @see org.apache.shiro.SecurityUtils#setSecurityManager(org.apache.shiro.mgt.SecurityManager)
	 * @see org.apache.shiro.mgt.SecurityManager
	 */
	protected void registerSecurityManager(org.apache.shiro.mgt.SecurityManager securityManager) {
		SecurityUtils.setSecurityManager(securityManager);
	}

	/**
	 * A Spring {@link Condition} to determine whether the user has included (declared) the 'shiro-spring' dependency
	 * on their application's classpath, which is necessary to configure Apache Shiro in order to secure Apache Geode
	 * in a Spring context.
	 *
	 * @see org.springframework.context.annotation.Condition
	 */
	public static class ApacheShiroPresentCondition implements Condition {

		protected static final String APACHE_SHIRO_LIFECYCLE_BEAN_POST_PROCESOR_CLASS_NAME =
			"org.apache.shiro.spring.LifecycleBeanPostProcessor";

		/**
		 * @ineritDoc
		 */
		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return ClassUtils.isPresent(APACHE_SHIRO_LIFECYCLE_BEAN_POST_PROCESOR_CLASS_NAME,
				context.getClassLoader());
		}
	}

	/**
	 * A Spring {@link Condition} implementation that determines whether the user configured Apache Shiro
	 * to secure Apache Geode.
	 *
	 * @see org.springframework.context.annotation.Condition
	 */
	public class ShiroRealmsConfigured implements Condition {

		/**
		 * @ineritDoc
		 */
		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return !CollectionUtils.isEmpty(ApacheShiroSecurityConfiguration.this.realms);
		}
	}
}

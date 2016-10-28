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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.geode.security.AuthInitialize;
import org.springframework.context.annotation.Import;

/**
 * The {@link EnableSecurity} annotation marks a Spring {@link org.springframework.context.annotation.Configuration}
 * annotated class to configure and enable Apache Geode's Security features for authentication, authorization
 * and post processing.
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.config.annotation.SecurityConfiguration
 * @see org.apache.geode.security.AuthInitialize
 * @see org.apache.geode.security.SecurityManager
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Import(SecurityConfiguration.class)
@SuppressWarnings("unused")
public @interface EnableSecurity {

	/**
	 * Used for authentication. Static creation method returning an {@link AuthInitialize} object,
	 * which obtains credentials for clients.
	 *
	 * Defaults to unset.
	 */
	String clientAuthenticationInitializer() default "";

	/**
	 * Used with authentication. Static creation method returning an {@link AuthInitialize} object, which obtains
	 * credentials for peers in a distributed system.
	 *
	 * Defaults to unset.
	 */
	String peerAuthenticationInitializer() default "";

	Class<?> securityManagerClass() default Void.class;

	String securityManagerClassName() default "";

	Class<?> securityPostProcessorClass() default Void.class;

	String securityPostProcessorClassName() default "";

	String shiroIniFileLocation() default "";

}

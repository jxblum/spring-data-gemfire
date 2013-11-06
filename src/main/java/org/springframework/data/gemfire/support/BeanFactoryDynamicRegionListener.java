/*
 * Copyright 2010-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.gemfire.support;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.Assert;

import com.gemstone.gemfire.cache.DynamicRegionListener;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionEvent;

/**
 * The BeanFactoryDynamicRegionListener class...
 * <p/>
 * @author John Blum
 * @see com.gemstone.gemfire.cache.DynamicRegionListener
 * @since 1.3.3
 */
public class BeanFactoryDynamicRegionListener implements DynamicRegionListener {

	private final BeanFactory beanFactory;

	public BeanFactoryDynamicRegionListener(final BeanFactory beanFactory) {
		Assert.notNull(beanFactory, "The BeanFactory reference cannot be null!");
		this.beanFactory = beanFactory;
	}

	@Override
	public void beforeRegionCreate(String parentRegionName, String regionName) {
		System.out.printf("Adding dynamic Region (%1$s) to parent Region (%2$s)", regionName, parentRegionName);
		parentRegionName = (parentRegionName.startsWith("/") ? parentRegionName.substring(1) : parentRegionName);
		beanFactory.getBean(parentRegionName, Region.class);
	}

	@Override
	public void afterRegionCreate(final RegionEvent<?, ?> event) {
	}

	@Override
	public void beforeRegionDestroy(final RegionEvent<?, ?> event) {
	}

	@Override
	public void afterRegionDestroy(final RegionEvent<?, ?> event) {
	}

}

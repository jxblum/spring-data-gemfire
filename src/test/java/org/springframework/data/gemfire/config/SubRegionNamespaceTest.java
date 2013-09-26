/*
 * Copyright 2010-2013 the original author or authors.
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
 */

package org.springframework.data.gemfire.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.gemfire.SubRegionFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gemstone.gemfire.cache.AttributesFactory;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheLoader;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionAttributes;

/**
 * @author David Turanski
 */
@ContextConfiguration(locations="subregion-ns.xml")
//@ContextConfiguration(locations="subregion-ns.xml", initializers=GemfireTestApplicationContextInitializer.class)
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressWarnings({ "unused", "deprecation" })
public class SubRegionNamespaceTest {

	@Autowired
	private ApplicationContext context;

	protected static void assertRegionExists(String expectedRegionName, String expectedRegionPath, Region region) {
		assertNotNull(String.format("The Region with the expected name (%1$s) was null!", expectedRegionName), region);

		String regionName = region.getName();
		String regionPath = region.getFullPath();

		assertEquals(String.format("Expected a Region named (%1$s); but was (%2$s)!", expectedRegionName, regionName),
			expectedRegionName, regionName);

		assertEquals(String.format("Expected a Region path of (%1$s); but was (%2$s)!", expectedRegionName, regionPath),
			expectedRegionPath, regionPath);
	}

	protected static void printRegionHierarchy(final Region<?, ?> region) {
		if (region != null) {
			System.out.printf("%1$s%n", region.getFullPath());
			for (Region subRegion : region.subregions(false)) {
				printRegionHierarchy(subRegion);
			}
		}
		else {
			System.out.printf("Region was null!%n");
		}
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void testNestedReplicatedRegions() throws Exception {
		Region parent = context.getBean("parent", Region.class);
		Region child = context.getBean("/parent/child", Region.class);
		Region sibling = context.getBean("/parent/sibling", Region.class);
		Region grandchild = context.getBean("/parent/child/grandchild", Region.class);

		printRegionHierarchy(parent);

		assertRegionExists("parent", "/parent", parent);
		assertRegionExists("child", "/parent/child", child);
		assertSame(child, parent.getSubregion("child"));
		assertRegionExists("sibling", "/parent/sibling", sibling);
		assertSame(sibling, parent.getSubregion("sibling"));
		assertRegionExists("grandchild", "/parent/child/grandchild", grandchild);
		assertSame(grandchild, child.getSubregion("grandchild"));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testMixedNestedRegions() {
		Cache cache = context.getBean(Cache.class);

		Region parent = context.getBean("replicatedParent", Region.class);
		parent.createSubregion("lookupChild", new AttributesFactory().create());

		Region child = context.getBean("/replicatedParent/lookupChild", Region.class);
		Region grandchild = context.getBean("/replicatedParent/lookupChild/partitionedGrandchild", Region.class);
		assertNotNull(child);
		assertEquals("/replicatedParent/lookupChild", child.getFullPath());
		assertSame(child, parent.getSubregion("lookupChild"));

		assertEquals("/replicatedParent/lookupChild/partitionedGrandchild", grandchild.getFullPath());
		assertSame(grandchild, child.getSubregion("partitionedGrandchild"));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testNestedRegionsWithSiblings() {
		Region parent = context.getBean("parentWithSiblings", Region.class);
		Region child1 = context.getBean("/parentWithSiblings/child1", Region.class);
		assertEquals("/parentWithSiblings/child1", child1.getFullPath());
		Region child2 = context.getBean("/parentWithSiblings/child2", Region.class);
		assertEquals("/parentWithSiblings/child2", child2.getFullPath());
		assertSame(child1, parent.getSubregion("child1"));
		assertSame(child2, parent.getSubregion("child2"));

		Region grandchild1 = context.getBean("/parentWithSiblings/child1/grandChild11", Region.class);
		assertEquals("/parentWithSiblings/child1/grandChild11", grandchild1.getFullPath());
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void testComplexNestedRegions() throws Exception {
		Region parent = context.getBean("complexNested", Region.class);
		Region child1 = context.getBean("/complexNested/child1", Region.class);
		Region child2 = context.getBean("/complexNested/child2", Region.class);
		Region grandchild1 = context.getBean("/complexNested/child1/grandChild11", Region.class);

		SubRegionFactoryBean grandchild1fb = context.getBean("&/complexNested/child1/grandChild11",
				SubRegionFactoryBean.class);
		assertNotNull(grandchild1fb);
		RegionAttributes attr = grandchild1fb.create();
		assertNotNull(attr);
		CacheLoader cl = attr.getCacheLoader();
		assertNotNull(cl);
	}

}

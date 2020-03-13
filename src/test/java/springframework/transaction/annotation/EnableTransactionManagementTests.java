/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package springframework.transaction.annotation;

import org.junit.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.event.TransactionalEventListenerFactory;
import springframework.tests.transaction.AnnotationTransactionNamespaceHandlerTests;
import springframework.tests.transaction.CallCountingTransactionManager;

import javax.annotation.PostConstruct;
import java.util.Map;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests demonstrating use of @EnableTransactionManagement @Configuration classes.
 *
 * @author Chris Beams
 * @author Stephane Nicoll
 * @author Sam Brannen
 * @since 3.1
 */
public class EnableTransactionManagementTests {

	/**
	 * 测试开启事务管理注解目标类能否被代理
	 */
	@Test
	public void transactionProxyIsCreated() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
				EnableTxConfig.class, TxManagerConfig.class);
		AnnotationTransactionNamespaceHandlerTests.TransactionalTestBean bean = ctx.getBean(AnnotationTransactionNamespaceHandlerTests.TransactionalTestBean.class);
		assertTrue("testBean is not a proxy", AopUtils.isAopProxy(bean));
		Map<?,?> services = ctx.getBeansWithAnnotation(Service.class);
		assertTrue("Stereotype annotation not visible", services.containsKey("testBean"));
		ctx.close();
	}

	/**
	 * 测试父类开启事务管理注解目标类能否被代理
	 */
	@Test
	public void transactionProxyIsCreatedWithEnableOnSuperclass() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
				InheritedEnableTxConfig.class, TxManagerConfig.class);
		AnnotationTransactionNamespaceHandlerTests.TransactionalTestBean bean = ctx.getBean(AnnotationTransactionNamespaceHandlerTests.TransactionalTestBean.class);
		assertTrue("testBean is not a proxy", AopUtils.isAopProxy(bean));
		Map<?,?> services = ctx.getBeansWithAnnotation(Service.class);
		assertTrue("Stereotype annotation not visible", services.containsKey("testBean"));
		ctx.close();
	}

	/**
	 * 多事务配置默认事务
	 */
	@Test
	public void txManagerIsResolvedCorrectlyWhenMultipleManagersArePresent() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
				EnableTxConfig.class, MultiTxManagerConfig.class);
		AnnotationTransactionNamespaceHandlerTests.TransactionalTestBean bean = ctx.getBean(AnnotationTransactionNamespaceHandlerTests.TransactionalTestBean.class);

		// invoke a transactional method, causing the PlatformTransactionManager bean to be resolved.
		bean.findAllFoos();
		ctx.close();
	}

	/**
	 *  ASPECTJ mode
	 */
	@Test
	public void proxyTypeAspectJCausesRegistrationOfAnnotationTransactionAspect() {
		try {
			new AnnotationConfigApplicationContext(EnableAspectjTxConfig.class, TxManagerConfig.class);
			fail("should have thrown CNFE when trying to load AnnotationTransactionAspect. " +
					"Do you actually have org.springframework.aspects on the classpath?");
		}
		catch (Exception ex) {
			assertThat(ex.getMessage(), containsString("AspectJTransactionManagementConfiguration"));
		}
	}

	/**
	 * 测试开启事务管理后事务事件监听是否注册
	 */
	@Test
	public void transactionalEventListenerRegisteredProperly() {
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(EnableTxConfig.class);
		assertTrue(ctx.containsBean(TransactionManagementConfigUtils.TRANSACTIONAL_EVENT_LISTENER_FACTORY_BEAN_NAME));
		assertEquals(1, ctx.getBeansOfType(TransactionalEventListenerFactory.class).size());
		ctx.close();
	}

	@Configuration
	@EnableTransactionManagement
	static class EnableTxConfig {
	}


	@Configuration
	static class InheritedEnableTxConfig extends EnableTxConfig {
	}


	@Configuration
	@EnableTransactionManagement
	@Conditional(NeverCondition.class)
	static class ParentEnableTxConfig {

		@Bean
		Object someBean() {
			return new Object();
		}
	}


	@Configuration
	static class ChildEnableTxConfig extends ParentEnableTxConfig {

		@Override
		Object someBean() {
			return "X";
		}
	}


	private static class NeverCondition implements ConfigurationCondition {

		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			return false;
		}

		@Override
		public ConfigurationPhase getConfigurationPhase() {
			return ConfigurationPhase.REGISTER_BEAN;
		}
	}


	@Configuration
	@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
	static class EnableAspectjTxConfig {
	}


	@Configuration
	@EnableTransactionManagement
	static class Spr11915Config {

		@Autowired
		private ConfigurableApplicationContext applicationContext;

		@PostConstruct
		public void initializeApp() {
			applicationContext.getBeanFactory().registerSingleton(
					"qualifiedTransactionManager", new CallCountingTransactionManager());
		}

		@Bean
		public AnnotationTransactionNamespaceHandlerTests.TransactionalTestBean testBean() {
			return new AnnotationTransactionNamespaceHandlerTests.TransactionalTestBean();
		}
	}


	@Configuration
	static class TxManagerConfig {

		@Bean
		public AnnotationTransactionNamespaceHandlerTests.TransactionalTestBean testBean() {
			return new AnnotationTransactionNamespaceHandlerTests.TransactionalTestBean();
		}

		@Bean
		public PlatformTransactionManager txManager() {
			return new CallCountingTransactionManager();
		}
	}


	@Configuration
	static class MultiTxManagerConfig extends TxManagerConfig implements TransactionManagementConfigurer {

		@Bean
		public PlatformTransactionManager txManager2() {
			return new CallCountingTransactionManager();
		}

		@Override
		public PlatformTransactionManager annotationDrivenTransactionManager() {
			return txManager2();
		}
	}

}

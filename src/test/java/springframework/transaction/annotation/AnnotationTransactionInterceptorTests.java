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
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import springframework.tests.transaction.CallCountingTransactionManager;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Rob Harrop
 * @author Juergen Hoeller
 */
public class AnnotationTransactionInterceptorTests {

	private final CallCountingTransactionManager ptm = new CallCountingTransactionManager();

	private final AnnotationTransactionAttributeSource source = new AnnotationTransactionAttributeSource();

	private final TransactionInterceptor ti = new TransactionInterceptor(this.ptm, this.source);


	@Test
	public void classLevelOnly() {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(new TestClassLevelOnly());
		proxyFactory.addAdvice(this.ti);

		TestClassLevelOnly proxy = (TestClassLevelOnly) proxyFactory.getProxy();

		proxy.doSomething();
		assertGetTransactionAndCommitCount(1);

		proxy.doSomethingElse();
		assertGetTransactionAndCommitCount(2);

		proxy.doSomething();
		assertGetTransactionAndCommitCount(3);

		proxy.doSomethingElse();
		assertGetTransactionAndCommitCount(4);
	}

	@Test
	public void withSingleMethodOverride() {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(new TestWithSingleMethodOverride());
		proxyFactory.addAdvice(this.ti);

		TestWithSingleMethodOverride proxy = (TestWithSingleMethodOverride) proxyFactory.getProxy();

		proxy.doSomething();
		assertGetTransactionAndCommitCount(1);

		proxy.doSomethingElse();
		assertGetTransactionAndCommitCount(2);

		proxy.doSomethingCompletelyElse();
		assertGetTransactionAndCommitCount(3);

		proxy.doSomething();
		assertGetTransactionAndCommitCount(4);
	}

	@Test
	public void withSingleMethodOverrideInverted() {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(new TestWithSingleMethodOverrideInverted());
		proxyFactory.addAdvice(this.ti);

		TestWithSingleMethodOverrideInverted proxy = (TestWithSingleMethodOverrideInverted) proxyFactory.getProxy();

		proxy.doSomething();
		assertGetTransactionAndCommitCount(1);

		proxy.doSomethingElse();
		assertGetTransactionAndCommitCount(2);

		proxy.doSomethingCompletelyElse();
		assertGetTransactionAndCommitCount(3);

		proxy.doSomething();
		assertGetTransactionAndCommitCount(4);
	}

	@Test
	public void withMultiMethodOverride() {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(new TestWithMultiMethodOverride());
		proxyFactory.addAdvice(this.ti);

		TestWithMultiMethodOverride proxy = (TestWithMultiMethodOverride) proxyFactory.getProxy();

		proxy.doSomething();
		assertGetTransactionAndCommitCount(1);

		proxy.doSomethingElse();
		assertGetTransactionAndCommitCount(2);

		proxy.doSomethingCompletelyElse();
		assertGetTransactionAndCommitCount(3);

		proxy.doSomething();
		assertGetTransactionAndCommitCount(4);
	}

	@Test
	public void withRollback() {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(new TestWithRollback());
		proxyFactory.addAdvice(this.ti);

		TestWithRollback proxy = (TestWithRollback) proxyFactory.getProxy();

		try {
			proxy.doSomethingErroneous();
			fail("Should throw IllegalStateException");
		}
		catch (IllegalStateException ex) {
			assertGetTransactionAndRollbackCount(1);
		}

		try {
			proxy.doSomethingElseErroneous();
			fail("Should throw IllegalArgumentException");
		}
		catch (IllegalArgumentException ex) {
			assertGetTransactionAndRollbackCount(2);
		}
	}

	@Test
	public void withInterface() {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(new TestWithInterfaceImpl());
		proxyFactory.addInterface(TestWithInterface.class);
		proxyFactory.addAdvice(this.ti);

		TestWithInterface proxy = (TestWithInterface) proxyFactory.getProxy();

		proxy.doSomething();
		assertGetTransactionAndCommitCount(1);

		proxy.doSomethingElse();
		assertGetTransactionAndCommitCount(2);

		proxy.doSomethingElse();
		assertGetTransactionAndCommitCount(3);

		proxy.doSomething();
		assertGetTransactionAndCommitCount(4);
	}

	@Test
	public void crossClassInterfaceMethodLevelOnJdkProxy() throws Exception {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(new SomeServiceImpl());
		proxyFactory.addInterface(SomeService.class);
		proxyFactory.addAdvice(this.ti);

		SomeService someService = (SomeService) proxyFactory.getProxy();

		someService.bar();
		assertGetTransactionAndCommitCount(1);

		someService.foo();
		assertGetTransactionAndCommitCount(2);

		someService.fooBar();
		assertGetTransactionAndCommitCount(3);
	}

	@Test
	public void crossClassInterfaceOnJdkProxy() throws Exception {
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setTarget(new OtherServiceImpl());
		proxyFactory.addInterface(OtherService.class);
		proxyFactory.addAdvice(this.ti);

		OtherService otherService = (OtherService) proxyFactory.getProxy();

		otherService.foo();
		assertGetTransactionAndCommitCount(1);
	}

	private void assertGetTransactionAndCommitCount(int expectedCount) {
		assertEquals(expectedCount, this.ptm.begun);
		assertEquals(expectedCount, this.ptm.commits);
	}

	private void assertGetTransactionAndRollbackCount(int expectedCount) {
		assertEquals(expectedCount, this.ptm.begun);
		assertEquals(expectedCount, this.ptm.rollbacks);
	}


	@Transactional
	public static class TestClassLevelOnly {

		public void doSomething() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		}

		public void doSomethingElse() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		}
	}


	@Transactional
	public static class TestWithSingleMethodOverride {

		public void doSomething() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		}

		@Transactional(readOnly = true)
		public void doSomethingElse() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		}

		public void doSomethingCompletelyElse() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		}
	}


	@Transactional(readOnly = true)
	public static class TestWithSingleMethodOverrideInverted {

		@Transactional
		public void doSomething() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		}

		public void doSomethingElse() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		}

		public void doSomethingCompletelyElse() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		}
	}


	@Transactional
	public static class TestWithMultiMethodOverride {

		@Transactional(readOnly = true)
		public void doSomething() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		}

		@Transactional(readOnly = true)
		public void doSomethingElse() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		}

		public void doSomethingCompletelyElse() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		}
	}


	@Transactional(rollbackFor = IllegalStateException.class)
	public static class TestWithRollback {

		public void doSomethingErroneous() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
			throw new IllegalStateException();
		}

		@Transactional(rollbackFor = IllegalArgumentException.class)
		public void doSomethingElseErroneous() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
			throw new IllegalArgumentException();
		}
	}


	@Transactional
	public static interface TestWithInterface {

		public void doSomething();

		@Transactional(readOnly = true)
		public void doSomethingElse();
	}


	public static class TestWithInterfaceImpl implements TestWithInterface {

		@Override
		public void doSomething() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertFalse(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		}

		@Override
		public void doSomethingElse() {
			assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
			assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
		}
	}


	public static interface SomeService {

		void foo();

		@Transactional
		void bar();

		@Transactional(readOnly = true)
		void fooBar();
	}


	public static class SomeServiceImpl implements SomeService {

		@Override
		public void bar() {
		}

		@Override
		@Transactional
		public void foo() {
		}

		@Override
		@Transactional(readOnly = false)
		public void fooBar() {
		}
	}


	public interface OtherService {

		void foo();
	}


	@Transactional
	public static class OtherServiceImpl implements OtherService {

		@Override
		public void foo() {
		}
	}

}

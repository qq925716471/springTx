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
import org.springframework.transaction.annotation.Propagation;
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

    /**
     * 测试类级别事务
     */
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

    /**
     * 测试单个方法重写
     */
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

    /**
     * 单个方法重写还原
     */
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
    }

    /**
     * 回滚测试
     */
    @Test
    public void withRollback() {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(new TestWithRollback());
        proxyFactory.addAdvice(this.ti);

        TestWithRollback proxy = (TestWithRollback) proxyFactory.getProxy();

        try {
            proxy.doSomethingErroneous();
            fail("Should throw IllegalStateException");
        } catch (IllegalStateException ex) {
            assertGetTransactionAndRollbackCount(1);
        }

        try {
            proxy.doSomethingElseErroneous();
            fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertGetTransactionAndRollbackCount(2);
        }
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

        @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
        public void doSomethingElse() {
            assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
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
}

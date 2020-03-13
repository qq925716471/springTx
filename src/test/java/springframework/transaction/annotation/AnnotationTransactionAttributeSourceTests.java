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
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import springframework.tests.transaction.CallCountingTransactionManager;
import springframework.tests.transaction.SerializationTestUtils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Colin Sampaleanu
 * @author Juergen Hoeller
 * @author Sam Brannen
 */
public class AnnotationTransactionAttributeSourceTests {

    @Test
    public void nullOrEmpty() throws Exception {
        Method method = ITestBean.class.getMethod("getAge");
        AnnotationTransactionAttributeSource atas = new AnnotationTransactionAttributeSource();
        assertNull(atas.getTransactionAttribute(method, null));
    }

    /**
     * 测试调用接口方法,但事务属性定义在实现类上。
     */
    @Test
    public void transactionAttributeDeclaredOnClassMethod() throws Exception {
        Method classMethod = ITestBean.class.getMethod("getAge");
        AnnotationTransactionAttributeSource atas = new AnnotationTransactionAttributeSource();
        TransactionAttribute actual = atas.getTransactionAttribute(classMethod, TestBean1.class);
        assertNotNull(actual);
        RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();
        rbta.getRollbackRules().add(new RollbackRuleAttribute(Exception.class));
        assertEquals(rbta.getRollbackRules(), ((RuleBasedTransactionAttribute) actual).getRollbackRules());
    }

    /**
     * 测试cglib代理类
     */
    @Test
    public void transactionAttributeDeclaredOnCglibClassMethod() throws Exception {
        Method classMethod = ITestBean.class.getMethod("getAge");
        TestBean1 tb = new TestBean1();
        ProxyFactory pf = new ProxyFactory(tb);
        pf.setProxyTargetClass(true);
        Object proxy = pf.getProxy();

        AnnotationTransactionAttributeSource atas = new AnnotationTransactionAttributeSource();
        TransactionAttribute actual = atas.getTransactionAttribute(classMethod, proxy.getClass());

        RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();
        rbta.getRollbackRules().add(new RollbackRuleAttribute(Exception.class));
        assertEquals(rbta.getRollbackRules(), ((RuleBasedTransactionAttribute) actual).getRollbackRules());
    }

    /**
     * 事务属性定义在接口
     */
    @Test
    public void transactionAttributeDeclaredOnInterfaceMethodOnly() throws Exception {
        Method interfaceMethod = ITestBean2.class.getMethod("getAge");

        AnnotationTransactionAttributeSource atas = new AnnotationTransactionAttributeSource();
        TransactionAttribute actual = atas.getTransactionAttribute(interfaceMethod, TestBean2.class);

        RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();
        assertEquals(rbta.getRollbackRules(), ((RuleBasedTransactionAttribute) actual).getRollbackRules());
    }

    /**
     * 事务属性定义在目标类并重写了接口定义
     */
    @Test
    public void transactionAttributeOnTargetClassMethodOverridesAttributeOnInterfaceMethod() throws Exception {
        Method interfaceMethod = ITestBean3.class.getMethod("getAge");
        Method interfaceMethod2 = ITestBean3.class.getMethod("getName");

        AnnotationTransactionAttributeSource atas = new AnnotationTransactionAttributeSource();
        TransactionAttribute actual = atas.getTransactionAttribute(interfaceMethod, TestBean3.class);
        assertEquals(TransactionAttribute.PROPAGATION_REQUIRES_NEW, actual.getPropagationBehavior());
        assertEquals(TransactionAttribute.ISOLATION_REPEATABLE_READ, actual.getIsolationLevel());
        assertEquals(5, actual.getTimeout());
        assertTrue(actual.isReadOnly());

        RuleBasedTransactionAttribute rbta = new RuleBasedTransactionAttribute();
        rbta.getRollbackRules().add(new RollbackRuleAttribute(Exception.class));
        rbta.getRollbackRules().add(new NoRollbackRuleAttribute(IOException.class));
        assertEquals(rbta.getRollbackRules(), ((RuleBasedTransactionAttribute) actual).getRollbackRules());

        TransactionAttribute actual2 = atas.getTransactionAttribute(interfaceMethod2, TestBean3.class);
        assertEquals(TransactionAttribute.PROPAGATION_REQUIRED, actual2.getPropagationBehavior());
    }

    /**
     * 事务回滚配置
     *
     * @throws Exception
     */
    @Test
    public void rollbackRulesAreApplied() throws Exception {
        Method method = TestBean3.class.getMethod("getAge");
        AnnotationTransactionAttributeSource atas = new AnnotationTransactionAttributeSource();
        TransactionAttribute actual = atas.getTransactionAttribute(method, TestBean3.class);
        assertTrue(actual.rollbackOn(new Exception()));
        assertFalse(actual.rollbackOn(new IOException()));
    }


    /**
     * 自定义注解
     *
     * @throws Exception
     */
    @Test
    public void customClassAttributeDetected() throws Exception {
        Method method = TestBean5.class.getMethod("getAge");
        AnnotationTransactionAttributeSource atas = new AnnotationTransactionAttributeSource();
        TransactionAttribute actual = atas.getTransactionAttribute(method, TestBean5.class);
        assertTrue(actual.rollbackOn(new Exception()));
        assertFalse(actual.rollbackOn(new IOException()));
    }


    interface ITestBean {

        int getAge();

        void setAge(int age);

        String getName();

        void setName(String name);
    }


    interface ITestBean2 {

        @Transactional
        int getAge();

        void setAge(int age);

        String getName();

        void setName(String name);
    }


    @Transactional
    interface ITestBean3 {

        int getAge();

        void setAge(int age);

        String getName();

        void setName(String name);
    }

    @SuppressWarnings("serial")
    static class TestBean1 implements ITestBean, Serializable {

        private String name;

        private int age;

        public TestBean1() {
        }

        public TestBean1(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public int getAge() {
            return age;
        }

        @Override
        public void setAge(int age) {
            this.age = age;
        }
    }


    static class TestBean2 implements ITestBean2 {

        private String name;

        private int age;

        public TestBean2() {
        }

        public TestBean2(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int getAge() {
            return age;
        }

        @Override
        public void setAge(int age) {
            this.age = age;
        }
    }


    static class TestBean3 implements ITestBean3 {

        private String name;

        private int age;

        public TestBean3() {
        }

        public TestBean3(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ, timeout = 5,
            readOnly = true, rollbackFor = Exception.class, noRollbackFor = {IOException.class})
        public int getAge() {
            return age;
        }

        @Override
        public void setAge(int age) {
            this.age = age;
        }
    }


    @Transactional(rollbackFor = Exception.class, noRollbackFor = {IOException.class})
    static class TestBean4 implements ITestBean3 {

        private String name;

        private int age;

        public TestBean4() {
        }

        public TestBean4(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int getAge() {
            return age;
        }

        @Override
        public void setAge(int age) {
            this.age = age;
        }
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Transactional(rollbackFor = Exception.class, noRollbackFor = {IOException.class})
    @interface Tx {
    }


    @Tx
    static class TestBean5 {

        public int getAge() {
            return 10;
        }
    }
}

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

package springframework.transaction;

import org.junit.After;
import org.junit.Test;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Juergen Hoeller
 * @since 29.04.2003
 */
public class TransactionSupportTests {

	@Test
	public void noExistingTransaction() {
		PlatformTransactionManager tm = new TestTransactionManager(false, true);
		DefaultTransactionStatus status1 = (DefaultTransactionStatus)
				tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_SUPPORTS));
		assertTrue("Must not have transaction", status1.getTransaction() == null);

		DefaultTransactionStatus status2 = (DefaultTransactionStatus)
				tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED));
		assertTrue("Must have transaction", status2.getTransaction() != null);
		assertTrue("Must be new transaction", status2.isNewTransaction());

		try {
			tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_MANDATORY));
			fail("Should not have thrown NoTransactionException");
		}
		catch (IllegalTransactionStateException ex) {
			// expected
		}
	}

	@Test
	public void existingTransaction() {
		PlatformTransactionManager tm = new TestTransactionManager(true, true);
		DefaultTransactionStatus status1 = (DefaultTransactionStatus)
				tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_SUPPORTS));
		assertTrue("Must have transaction", status1.getTransaction() != null);
		assertTrue("Must not be new transaction", !status1.isNewTransaction());

		DefaultTransactionStatus status2 = (DefaultTransactionStatus)
				tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED));
		assertTrue("Must have transaction", status2.getTransaction() != null);
		assertTrue("Must not be new transaction", !status2.isNewTransaction());

		try {
			DefaultTransactionStatus status3 = (DefaultTransactionStatus)
					tm.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_MANDATORY));
			assertTrue("Must have transaction", status3.getTransaction() != null);
			assertTrue("Must not be new transaction", !status3.isNewTransaction());
		}
		catch (NoTransactionException ex) {
			fail("Should not have thrown NoTransactionException");
		}
	}

	@Test
	public void commitWithoutExistingTransaction() {
		TestTransactionManager tm = new TestTransactionManager(false, true);
		TransactionStatus status = tm.getTransaction(null);
		tm.commit(status);
		assertTrue("triggered begin", tm.begin);
		assertTrue("triggered commit", tm.commit);
		assertTrue("no rollback", !tm.rollback);
		assertTrue("no rollbackOnly", !tm.rollbackOnly);
	}

	@Test
	public void rollbackWithoutExistingTransaction() {
		TestTransactionManager tm = new TestTransactionManager(false, true);
		TransactionStatus status = tm.getTransaction(null);
		tm.rollback(status);
		assertTrue("triggered begin", tm.begin);
		assertTrue("no commit", !tm.commit);
		assertTrue("triggered rollback", tm.rollback);
		assertTrue("no rollbackOnly", !tm.rollbackOnly);
	}

	@Test
	public void rollbackOnlyWithoutExistingTransaction() {
		TestTransactionManager tm = new TestTransactionManager(false, true);
		TransactionStatus status = tm.getTransaction(null);
		status.setRollbackOnly();
		tm.commit(status);
		assertTrue("triggered begin", tm.begin);
		assertTrue("no commit", !tm.commit);
		assertTrue("triggered rollback", tm.rollback);
		assertTrue("no rollbackOnly", !tm.rollbackOnly);
	}

	@Test
	public void commitWithExistingTransaction() {
		TestTransactionManager tm = new TestTransactionManager(true, true);
		TransactionStatus status = tm.getTransaction(null);
		tm.commit(status);
		assertTrue("no begin", !tm.begin);
		assertTrue("no commit", !tm.commit);
		assertTrue("no rollback", !tm.rollback);
		assertTrue("no rollbackOnly", !tm.rollbackOnly);
	}

	@Test
	public void rollbackWithExistingTransaction() {
		TestTransactionManager tm = new TestTransactionManager(true, true);
		TransactionStatus status = tm.getTransaction(null);
		tm.rollback(status);
		assertTrue("no begin", !tm.begin);
		assertTrue("no commit", !tm.commit);
		assertTrue("no rollback", !tm.rollback);
		assertTrue("triggered rollbackOnly", tm.rollbackOnly);
	}

	@Test
	public void rollbackOnlyWithExistingTransaction() {
		TestTransactionManager tm = new TestTransactionManager(true, true);
		TransactionStatus status = tm.getTransaction(null);
		status.setRollbackOnly();
		tm.commit(status);
		assertTrue("no begin", !tm.begin);
		assertTrue("no commit", !tm.commit);
		assertTrue("no rollback", !tm.rollback);
		assertTrue("triggered rollbackOnly", tm.rollbackOnly);
	}


	@Test
	public void transactionTemplate() {
		TestTransactionManager tm = new TestTransactionManager(false, true);
		TransactionTemplate template = new TransactionTemplate(tm);
		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
			}
		});
		assertTrue("triggered begin", tm.begin);
		assertTrue("triggered commit", tm.commit);
		assertTrue("no rollback", !tm.rollback);
		assertTrue("no rollbackOnly", !tm.rollbackOnly);
	}

	@Test
	public void transactionTemplateWithCallbackPreference() {
		MockCallbackPreferringTransactionManager ptm = new MockCallbackPreferringTransactionManager();
		TransactionTemplate template = new TransactionTemplate(ptm);
		template.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
			}
		});
		assertSame(template, ptm.getDefinition());
		assertFalse(ptm.getStatus().isRollbackOnly());
	}



	@After
	public void tearDown() {
		assertTrue(TransactionSynchronizationManager.getResourceMap().isEmpty());
		assertFalse(TransactionSynchronizationManager.isSynchronizationActive());
	}

}

package com.zlj.support;

public class TransactionStatus {

    private Object transaction;

    private final boolean newTransaction;

    private boolean rollbackOnly = false;

    private final Object suspendedResources;


    public TransactionStatus(
        Object transaction, boolean newTransaction, Object suspendedResources) {
        this.transaction = transaction;
        this.newTransaction = newTransaction;
        this.suspendedResources = suspendedResources;
    }

    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }

    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    public Object getTransaction() {
        return this.transaction;
    }

    public boolean hasTransaction() {
        return (this.transaction != null);
    }

    public boolean isNewTransaction() {
        return (hasTransaction() && this.newTransaction);
    }

    public Object getSuspendedResources() {
        return this.suspendedResources;
    }

}

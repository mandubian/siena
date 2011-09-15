package siena.base.test;

import java.sql.Connection;

import siena.SienaException;
import siena.SienaRestrictedApiException;
import siena.base.test.model.TransactionAccountFrom;
import siena.base.test.model.TransactionAccountTo;

public abstract class BaseTestNoAutoInc_10_TRANSACTION extends BaseTestNoAutoInc_BASE {
	
	public void testTransactionUpdate() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(1000L);
			pm.insert(accFrom, accTo);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				accFrom.amount-=100L;
				pm.update(accFrom);
				accTo.amount+=100L;
				pm.update(accTo);
				pm.commitTransaction();
			}catch(SienaException e){
				pm.rollbackTransaction();
				fail();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertTrue(900L == accFromAfter.amount);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertTrue(1100L == accToAfter.amount);
		}
	}
	
	public void testTransactionUpdateFailure() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(1000L);
			pm.insert(accFrom, accTo);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				accFrom.amount-=100L;
				pm.update(accFrom);
				accTo.amount+=100L;
				pm.update(accTo);
				throw new SienaException("test");
			}catch(SienaException e){
				pm.rollbackTransaction();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertTrue(1000L == accFromAfter.amount);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertTrue(1000L == accToAfter.amount);
		}
	}
	
	public void testTransactionInsert() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(1000L);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				accFrom.amount=1000L;
				accTo.amount=100L;
				pm.insert(accFrom);
				pm.insert(accTo);
				pm.commitTransaction();
			}catch(SienaException e){
				pm.rollbackTransaction();
				fail();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertTrue(1000L == accFromAfter.amount);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertTrue(100L == accToAfter.amount);
		}
	}
	
	public void testTransactionInsertFailure() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(1000L);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				accFrom.amount=1000L;
				accTo.amount=100L;
				pm.insert(accFrom, accTo);
				throw new SienaException("test");
			}catch(SienaException e){
				pm.rollbackTransaction();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertNull(accFromAfter);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertNull(accToAfter);
		}
	}
	
	public void testTransactionSave() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(1000L);
			pm.insert(accFrom, accTo);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				accFrom.amount-=100L;
				pm.save(accFrom);
				accTo.amount+=100L;
				pm.save(accTo);
				pm.commitTransaction();
			}catch(SienaException e){
				pm.rollbackTransaction();
				fail();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertTrue(900L == accFromAfter.amount);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertTrue(1100L == accToAfter.amount);
		}
	}
	
	public void testTransactionSaveFailure() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(1000L);
			pm.insert(accFrom, accTo);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				accFrom.amount-=100L;
				pm.save(accFrom);
				accTo.amount+=100L;
				pm.save(accTo);
				throw new SienaException("test");
			}catch(SienaException e){
				pm.rollbackTransaction();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertTrue(1000L == accFromAfter.amount);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertTrue(1000L == accToAfter.amount);
		}
	}
	
	public void testTransactionDelete() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(1000L);
			pm.insert(accFrom, accTo);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				pm.delete(accFrom);
				pm.delete(accTo);
				pm.commitTransaction();
			}catch(SienaException e){
				pm.rollbackTransaction();
				fail();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertNull(accFromAfter);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertNull(accToAfter);
		}
	}
	
	public void testTransactionDeleteFailure() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(100L);
			pm.insert(accFrom, accTo);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				pm.delete(accFrom);
				pm.delete(accTo);
				throw new SienaException("test");
			}catch(SienaException e){
				pm.rollbackTransaction();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertTrue(1000L == accFromAfter.amount);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertTrue(100L == accToAfter.amount);
		}
	}
	
	public void testTransactionInsertBatch() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(1000L);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				accFrom.amount=1000L;
				accTo.amount=100L;
				pm.insert(accFrom, accTo);
				pm.commitTransaction();
			}catch(SienaException e){
				pm.rollbackTransaction();
				fail();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertTrue(1000L == accFromAfter.amount);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertTrue(100L == accToAfter.amount);
		}
	}
	
	public void testTransactionInsertBatchFailure() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(1000L);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				accFrom.amount=1000L;
				accTo.amount=100L;
				pm.insert(accFrom, accTo);
				throw new SienaException("test");
			}catch(SienaException e){
				pm.rollbackTransaction();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertNull(accFromAfter);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertNull(accToAfter);
		}
	}
	
	public void testTransactionDeleteBatch() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(1000L);
			pm.insert(accFrom, accTo);
	
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				pm.delete(accFrom, accTo);
				pm.commitTransaction();
			}catch(SienaException e){
				pm.rollbackTransaction();
				fail();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertNull(accFromAfter);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertNull(accToAfter);
		}
	}
	
	public void testTransactionDeleteBatchFailure() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(100L);
			pm.insert(accFrom, accTo);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				pm.delete(accFrom, accTo);
				throw new SienaException("test");
			}catch(SienaException e){
				pm.rollbackTransaction();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertTrue(1000L == accFromAfter.amount);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertTrue(100L == accToAfter.amount);
		}
	}
	
	public void testTransactionUpdateBatch() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(1000L);
			pm.insert(accFrom, accTo);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				accFrom.amount-=100L;
				accTo.amount+=100L;
				pm.update(accFrom, accTo);
				pm.commitTransaction();
			}catch(SienaException e){
				pm.rollbackTransaction();
				fail();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertTrue(900L == accFromAfter.amount);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertTrue(1100L == accToAfter.amount);
		}
	}
	
	public void testTransactionUpdateBatchFailure() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(1000L);
			pm.insert(accFrom, accTo);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				accFrom.amount-=100L;
				accTo.amount+=100L;
				pm.update(accFrom, accTo);
				throw new SienaException("test");
			}catch(SienaException e){
				pm.rollbackTransaction();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertTrue(1000L == accFromAfter.amount);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertTrue(1000L == accToAfter.amount);
		}
	}
	
	public void testTransactionSaveBatch() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(1000L);
			pm.insert(accFrom, accTo);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				accFrom.amount-=100L;
				accTo.amount+=100L;
				pm.save(accFrom, accTo);
				pm.commitTransaction();
			}catch(SienaException e){
				pm.rollbackTransaction();
				fail();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertTrue(900L == accFromAfter.amount);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertTrue(1100L == accToAfter.amount);
		}
	}
	
	public void testTransactionSaveBatchFailure() {
		if(supportsTransaction()){
			TransactionAccountFrom accFrom = new TransactionAccountFrom(1000L);
			TransactionAccountTo accTo = new TransactionAccountTo(1000L);
			pm.insert(accFrom, accTo);
		
			try {
				pm.beginTransaction(Connection.TRANSACTION_READ_COMMITTED);
				accFrom.amount-=100L;
				accTo.amount+=100L;
				pm.save(accFrom, accTo);
				throw new SienaException("test");
			}catch(SienaException e){
				pm.rollbackTransaction();
			}finally{
				pm.closeConnection();
			}
			
			TransactionAccountFrom accFromAfter = pm.getByKey(TransactionAccountFrom.class, accFrom.id);
			assertTrue(1000L == accFromAfter.amount);
			TransactionAccountTo accToAfter = pm.getByKey(TransactionAccountTo.class, accTo.id);
			assertTrue(1000L == accToAfter.amount);
		}
	}
}

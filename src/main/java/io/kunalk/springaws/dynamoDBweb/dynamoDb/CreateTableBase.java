/** \file
 * 
 * Mar 16, 2015
 *
 * Copyright Ian Kaplan 2015
 *
 * @author Ian Kaplan, www.bearcave.com, iank@bearcave.com
 */
package io.kunalk.springaws.dynamoDBweb.dynamoDb;

import java.util.logging.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.util.TableUtils;


/**
 * <h4>
 * CreateTableBase
 * </h4>
 * 
 * <p>
 * A base class for subclasses that create DynamoDB tables.
 * </p>

 */
public abstract class CreateTableBase {
	private String mTableName = null;
	private Logger log = null;
	
	protected CreateTableBase(String tableName) {
		mTableName = tableName;
		log = Logger.getLogger( getClass().getName() );
	}
	
	public String getTableName() {
		return mTableName;
	}
	
	/**
	 * @param startMsec
	 * @param endMsec
	 * @return a String for the elapsed time in seconds.
	 */
    protected String toMinuteSecString(long startMsec, long endMsec) {
        StringBuilder builder = new StringBuilder();
        int seconds = (int)(endMsec - startMsec)/1000;
        int minutes = 0;
        if (seconds > 60) {
            minutes = seconds / 60;
            seconds = seconds % 60;
            builder.append( Integer.toString( minutes ) );
            builder.append(" minutes, ");
        }
        builder.append( Integer.toString(seconds) );
        builder.append(" seconds");
        return builder.toString();
    }
    
    public boolean tableExists(AmazonDynamoDB dynamoDBClient) {
    	boolean exists = true;
        final int timeout = 60 * 1000; // 60 seconds x 1000 msec/second
        final int pollInterval = 250; // polling interval in milliseconds (e.g., 1/4 second)
        String tableName = getTableName();
        Long startMsec = System.currentTimeMillis();
        try {
            TableUtils.waitUntilExists(dynamoDBClient, tableName, timeout, pollInterval);
        } catch (AmazonClientException e) {
        	exists = false;
            Long endMsec = System.currentTimeMillis();
            String elapsedTime = (endMsec - startMsec) + " msec";
            log.info("Did not find " + tableName + " (" + elapsedTime + ")");
        }
        catch (InterruptedException e) {
        	exists = false;
        	log.severe("InterruptedExcepion in CreateTableBase::tableExits: " + e.getLocalizedMessage());
        }
        if (exists) {
            Long endMsec = System.currentTimeMillis();
            String elapsedTime = (endMsec - startMsec) + " msec";
            log.info("Found table " + tableName + " (" + elapsedTime + ")");
        }
        return exists;
    }

    public abstract void createTable(AmazonDynamoDB client, long readThroughput, long writeThroughput);

}

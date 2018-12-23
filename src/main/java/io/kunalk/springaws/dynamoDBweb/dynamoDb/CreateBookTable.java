/** \file
 * 
 * Mar 16, 2015
 *
 * Copyright Ian Kaplan 2015
 *
 * @author Ian Kaplan, www.bearcave.com, iank@bearcave.com
 */
package io.kunalk.springaws.dynamoDBweb.dynamoDb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.ArrayList;
import java.util.logging.Logger;


/**
 *
 * <h4>
 * CreateBookTable
 * </h4>
 * 
 * <p>
 * Create a a DynamoDB table for books information. 
 * </p>
 * <p>
 * The book table can be searched in two ways:
 * </p>
 * <ol>
 * <li>By title and author (which will result in a single book being returned)</li>
 * <li>By author</li>
 * </ol>
 * <p>
 * The book title title is the primary hash index for the table. The author is the range key for this hash index.
 * </p>
 * <p>
 * There is also a global secondary index on the author, which allows the table to be searched by author. This query
 * may return multiple books.
 * </p>
 * <p>

 */
public class CreateBookTable extends CreateTableBase {
    /** The name of the DynamoDB global secondary index for the author column */
	private Logger log = null;
    public final static String AUTHOR_INDEX_NAME = "author_index";
    public final static String AUTHOR_HASH_NAME = "author";

	public CreateBookTable(String bookTableName) {
		super(bookTableName);
		log = Logger.getLogger( this.getClass().getName() );
	}

	@Override
	public void createTable(AmazonDynamoDB client, long readThroughput, long writeThroughput) {
		boolean tableOK = false;
		DynamoDB dynamoDB = new DynamoDB( client );
		log.info("Creating " + getTableName() + " table");

		ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
		ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();

		// title : the hash key
		keySchema.add(new KeySchemaElement().withAttributeName("title").withKeyType(KeyType.HASH));
		attributeDefinitions.add(new AttributeDefinition().withAttributeName("title").withAttributeType(ScalarAttributeType.S));

		// author : the range key
		keySchema.add(new KeySchemaElement().withAttributeName(AUTHOR_HASH_NAME).withKeyType(KeyType.RANGE));
        attributeDefinitions.add(new AttributeDefinition().withAttributeName(AUTHOR_HASH_NAME).withAttributeType(ScalarAttributeType.S));
		
		ProvisionedThroughput throughPut = new ProvisionedThroughput();
		throughPut.withReadCapacityUnits(readThroughput);
		throughPut.withWriteCapacityUnits(writeThroughput);
		
		// Now create a global secondary index for the book author
        // ProjectType.ALL is used so that a query will return all of the table attributes, not just the key attributes
        GlobalSecondaryIndex authorGlobalIndex = new GlobalSecondaryIndex();
        authorGlobalIndex.withIndexName(AUTHOR_INDEX_NAME)
                                        .withProvisionedThroughput(throughPut)
                                        .withProjection( new Projection().withProjectionType(ProjectionType.ALL) );
        
        ArrayList<KeySchemaElement> indexKeySchema = new ArrayList<KeySchemaElement>();
        indexKeySchema.add(new KeySchemaElement().withAttributeName(AUTHOR_HASH_NAME).withKeyType(KeyType.HASH));
        
        authorGlobalIndex.setKeySchema(indexKeySchema);

		CreateTableRequest request = new CreateTableRequest()
		                                 .withTableName( getTableName() )
		                                 .withKeySchema(keySchema)
		                                 .withGlobalSecondaryIndexes(authorGlobalIndex)
				                         .withProvisionedThroughput( throughPut );
        request.setAttributeDefinitions(attributeDefinitions);

		Long startMsec = System.currentTimeMillis();
		Table table = dynamoDB.createTable(request);
		log.info("Waiting for '" + getTableName() + "' table to be created...");
		try {
			table.waitForActive();
			tableOK = true;
		} catch (InterruptedException e) {
			log.warning("Creation of '" + getTableName() + "' table interrupted");
		}
		Long endMsec = System.currentTimeMillis();
		if (tableOK) {
			log.info("Elapsed time: " + toMinuteSecString(startMsec, endMsec) );
		}
	} // createTable

}

/** \file
 * 
 * Mar 29, 2018
 *
 * Copyright Ian Kaplan 2018
 *
 * @author Ian Kaplan, www.bearcave.com, iank@bearcave.com
 */
package io.kunalk.springaws.dynamoDBweb.service;

import io.kunalk.springaws.dynamoDBweb.dynamoDb.IDynamoDBKeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;



/**
 * <h4>
 * BookTableService
 * </h4>
 * <p>
 * This service provides read, write and search functions that access a book table that is stored in a database. In this case
 * the table is stored in DynamoDB.
 * </p>

 */
public class BookTableService implements IDynamoDBKeys {
    /** DynamoDB read throughput */
    private final static long READ_THROUGHPUT = 4;
    /** DynamoDB write throughput */
    private final static long WRITE_THROUGHPUT = 2;
    /** The name of the DynamoDB table used to store the book information */
    private final String tableName;
    private static DynamoDBService dynamoDBService = null;
    final private Logger log;
    
    private String getTableName() {
        return tableName;
    }
    
    /**
     * 
     * @param bookTableName the name of the DynamoDB table that stores the book information.
     */
    public BookTableService(final String bookTableName ) {
        tableName = bookTableName;
        log = Logger.getLogger( this.getClass().getName() );
        if (dynamoDBService == null) {
            dynamoDBService = new DynamoDBService( region, full_dynamodb_access_ID, IDynamoDBKeys.full_dynamodb_access_KEY);
        }
        // check to see if the book table exists. If it doesn't, create it.
        checkBookTable();
    }


    /** 
     * Check to see if the DynamoDB book table exists. If it doesn't exist, create it.
     * 
     */
    protected void checkBookTable() {
        AmazonDynamoDB client = dynamoDBService.getClient();
        CreateBookTable createBookTable = new CreateBookTable( getTableName() );
        if (! createBookTable.tableExists(client)) {
            createBookTable.createTable(client, READ_THROUGHPUT, WRITE_THROUGHPUT);
        }
    }
    
    /**
     * A query on title and author (note the withHashKeyValues clause).
     * @param bookInfo
     * @return
     */
    protected DynamoDBQueryExpression<BookInfo> buildBookInfoAuthorEQQuery(BookInfo bookInfo) {
        Condition rangeCondition = new Condition();
        rangeCondition.setComparisonOperator("EQ");
        AttributeValue attribute = new AttributeValue( bookInfo.getAuthor() );
        ArrayList<AttributeValue> attrs = new ArrayList<AttributeValue>();
        attrs.add( attribute );
        rangeCondition.setAttributeValueList( attrs );
        DynamoDBQueryExpression<BookInfo> queryExpression = new DynamoDBQueryExpression<BookInfo>()
                .withHashKeyValues(bookInfo)
                .withRangeKeyCondition("author", rangeCondition);
        return queryExpression;
    }
    
    
    /**
     * Write a BookInfo object to a book information table, but override the table
     * name. This is used for testing, so that test data can be written to a temporary table.
     * 
     * @param info the book table information
     * @param tableName the name of the DynamoDbTable
     */
    public void writeToBookTable(BookInfo info, String tableName ) {
        if (info != null) {
            DynamoDBMapper mapper = dynamoDBService.getMapper();
            mapper.save( info, new TableNameOverride( tableName ).config());
        }
    }


    /**
     * <p>
     * Write a BookInfo object to DynamoDB. Note that if the object with the title and author already
     * exists, it will be overwritten.
     * </p>
     * 
     * @param info
     */
    public void writeToBookTable(BookInfo info) {
        if (info != null) {
            DynamoDBMapper mapper = dynamoDBService.getMapper();
            mapper.save( info );
        }
    }
    
    public boolean hasBookEntry(BookInfo bookInfo, String tableName ) {
        boolean foundBook = false;
        DynamoDBQueryExpression<BookInfo> queryExpression = buildBookInfoAuthorEQQuery( bookInfo );
        List<BookInfo> itemList = dynamoDBService.getMapper(tableName).query(BookInfo.class, queryExpression);
        if (itemList != null && itemList.size() > 0) {
            foundBook = true;
        }
        return foundBook;
    }
    
    /**
     * Return true if a book with that title and author alreadys exists in the database.
     * 
     * @param title
     * @param author
     * @return
     */
    public boolean hasBookEntry(BookInfo bookInfo ) {
        boolean foundBook = hasBookEntry(bookInfo, getTableName() );
        return foundBook;
    }
    
   

    
    /**
     * <p>
     * Find one or more DynamoDB table BookInfo entries by searching for the author.
     * </p>
     * <p>
     * The author attribute is a global secondary index. This query is designed to return values
     * on the basis of this secondary index (the main index is on the {title, author} pair).
     * </p>
     * @param author
     * @param tableName
     * @return
     */
    public List<BookInfo> findBookByAuthor( String author, String tableName ) {
        List<BookInfo> bookList = new ArrayList<BookInfo>();
        AmazonDynamoDB client = dynamoDBService.getClient();

        Condition hashKeyCondition = new Condition();
        Condition authorCondition = new Condition();
        authorCondition.setComparisonOperator("EQ");
        hashKeyCondition.withComparisonOperator( ComparisonOperator.EQ )
                        .withAttributeValueList(new AttributeValue().withS(author));

        Map<String, Condition> keyConditions = new HashMap<String, Condition>();
        keyConditions.put(CreateBookTable.AUTHOR_HASH_NAME, hashKeyCondition);

        QueryRequest queryRequest = new QueryRequest();
        queryRequest.withTableName( tableName );
        queryRequest.withIndexName(CreateBookTable.AUTHOR_INDEX_NAME);
        queryRequest.withKeyConditions(keyConditions);

        QueryResult result = client.query(queryRequest);
        if (result.getCount() > 0) {
            // The itemList is a set of one or more DynamoDB row values stored in a attribute name/value map.
            List<Map<String, AttributeValue>> itemList = result.getItems();
            try {
                for (Map<String, AttributeValue> item : itemList) {
                    BookInfo info = new BookInfo();
                    DynamoDBUtil.attributesToObject(info, item);
                    bookList.add(info);
                }
            }
            catch (ReflectiveOperationException e) {
                log.severe("findBookByAuthor: " + e.getLocalizedMessage());
            }
        }
        return bookList;
    }
    
    
    /**
     * Find all the books by a particular author. This query uses the global secondary author index.
     * @param author the name of the author to be searched for.
     * @return A list of zero or more books.
     */
    public List<BookInfo> findBookByAuthor( String author ) {
        List<BookInfo> bookList = findBookByAuthor(author, getTableName() );
        return bookList;
    }
    
    
    public List<BookInfo> findBookByTitleAuthor(String author, String title) {
        BookInfo searchObj = new BookInfo();
        searchObj.setAuthor(author);
        searchObj.setTitle(title);
        DynamoDBQueryExpression<BookInfo> queryExp = buildBookInfoAuthorEQQuery( searchObj );
        List<BookInfo> info = dynamoDBService.getMapper(tableName).query(BookInfo.class, queryExp );
        return info;
    }
    
    /**
     * <p>
     * Find one or more books by title or partial title. For example, if there is a book titled "The Peripheral",
     * searching for "Peripheral" will return the book. Similarly, if there are two books, "Bangkok 8" and "Bangkok Haunts"
     * searching for "Bangkok" will return both books.
     * </p>
     * <p>
     * This query performs a table scan.
     * </p>
     * 
     * @param titleWords a string to search for in the book titles.
     * @return
     */
    public List<BookInfo> findBookByTitle(String titleWords ) {
        List<BookInfo> bookList = new ArrayList<BookInfo>();
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<String, AttributeValue>();
        Condition containsCondition = new Condition()
                                      .withComparisonOperator(ComparisonOperator.CONTAINS.toString())
                                      .withAttributeValueList(new AttributeValue().withS( titleWords ));
        Map<String, Condition> keyConditions = new HashMap<String, Condition>();
        keyConditions.put("title", containsCondition);
        
        ScanRequest scanRequest = new ScanRequest()
                                      .withTableName( getTableName() )
                                      .withScanFilter(keyConditions);

        AmazonDynamoDB client = dynamoDBService.getClient();
        ScanResult result = client.scan(scanRequest);
        if (result.getCount() > 0) {
            // The itemList is a set of one or more DynamoDB row values stored in a attribute name/value map.
            List<Map<String, AttributeValue>> itemList = result.getItems();
            try {
                for (Map<String, AttributeValue> item : itemList) {
                    BookInfo info = new BookInfo();
                    DynamoDBUtil.attributesToObject(info, item);
                    bookList.add(info);
                }
            }
            catch (ReflectiveOperationException e) {
                log.severe("findBookByTitle: " + e.getLocalizedMessage());
            }
        }
        return bookList;
    }
    
    /**
     * Read the entire book database. This function does a scan, which can be expensive on DynamoDB. However,
     * the assumption here is that the size of the book database is in the thousands, not millions (e.g., it's
     * not the Library of Congress).
     * 
     * @return
     */
    public List<BookInfo> getBooks() {
        AmazonDynamoDB client = dynamoDBService.getClient();
        ScanRequest scanRequest = new ScanRequest().withTableName( getTableName() );
        ScanResult result = client.scan(scanRequest);
        List<BookInfo> bookList = new ArrayList<BookInfo>();
        if (result.getCount() > 0) {
            // The itemList is a set of one or more DynamoDB row values stored in a attribute name/value map.
            List<Map<String, AttributeValue>> itemList = result.getItems();
            try {
                for (Map<String, AttributeValue> item : itemList) {
                    BookInfo info = new BookInfo();
                    DynamoDBUtil.attributesToObject(info, item);
                    bookList.add(info);
                }
                if (bookList.size() > 1) {
                    BookInfo[] infoArray = bookList.toArray( new BookInfo[1] );
                    Arrays.sort(infoArray, new BookInfoComparator() );
                    bookList.clear();
                    bookList.addAll(Arrays.asList( infoArray ) );
                }
            }
            catch (ReflectiveOperationException e) {
                log.severe("getBooks: " + e.getLocalizedMessage());
            }
        }
        return bookList;
    }
    
}

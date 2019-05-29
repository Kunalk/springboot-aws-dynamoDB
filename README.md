# springboot-aws-dynamoDB
Springboot application that connects to DynamicDB

This application uses AWS-SDK to connect to Dynamo DB

<!-- https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk -->
		<dependency>
			<groupId>com.amazonaws</groupId>
			<artifactId>aws-java-sdk</artifactId>
			<version>1.11.194</version>
		</dependency>
 
Creates connection with DynamoDB explicitly
```
 AWSCredentials credentials = getCredentials();
            ClientConfiguration config = new ClientConfiguration();
            config.setProtocol(Protocol.HTTP);
            mClient = AmazonDynamoDBClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                      .withClientConfiguration(config)
                      .withRegion(getRegion())
                      .build();
```
                      
POJO (repository) anotated fro com.amazonaws.services.dynamodbv2.datamodeling.
```
@DynamoDBTable(tableName="book_table")
public class BookInfo {

...
 @DynamoDBHashKey(attributeName="title")
    public String getTitle() {
        return title;
    }
    
 ...
 @DynamoDBAttribute(attributeName="genre")
    public String getGenre() {
        return genre;
    }
    ...
     @DynamoDBIgnore
    private String trimString(String input) {
        String rslt = "";
        if (input != null && input.length() > 0) {
            rslt = input.trim();
        }
        return rslt;
    }
 }
```
Now we build the dynamoDB service to create connection and mapper object for each table to interact with...
```
public class DynamoDBService {
 public AmazonDynamoDB getClient() {
        if (mClient == null) {
            AWSCredentials credentials = getCredentials();
            ClientConfiguration config = new ClientConfiguration();
            config.setProtocol(Protocol.HTTP);
            mClient = AmazonDynamoDBClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials))
                      .withClientConfiguration(config)
                      .withRegion(getRegion())
                      .build();
        }
        return mClient;
    }
    public DynamoDBMapper getMapper( String tableName ) {
        AmazonDynamoDB client = getClient();
        DynamoDBMapper mapper = new DynamoDBMapper( client,  new DynamoDBMapperConfig.TableNameOverride( tableName ).config() );
        return mapper;
    }
 }
```
The next changes includes specific service
```
public class BookTableService implements IDynamoDBKeys {
	...
	private static DynamoDBService dynamoDBService = null;
	...
	public BookTableService(final String bookTableName ) {
		tableName = bookTableName;
		log = Logger.getLogger( this.getClass().getName() );
		if (dynamoDBService == null) {
		    dynamoDBService = new DynamoDBService( region, full_dynamodb_access_ID, IDynamoDBKeys.full_dynamodb_access_KEY);
		}
		// check to see if the book table exists. If it doesn't, create it.
		checkBookTable();
	    }
	
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
	    
	    public void writeToBookTable(BookInfo info, String tableName ) {
		if (info != null) {
		    DynamoDBMapper mapper = dynamoDBService.getMapper();
		    mapper.save( info, new DynamoDBMapperConfig.TableNameOverride( tableName ).config());
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
	    
	    public List<BookInfo> findBookByTitleAuthor(String author, String title) {
		BookInfo searchObj = new BookInfo();
		searchObj.setAuthor(author);
		searchObj.setTitle(title);
		DynamoDBQueryExpression<BookInfo> queryExp = buildBookInfoAuthorEQQuery( searchObj );
		List<BookInfo> info = dynamoDBService.getMapper(tableName).query(BookInfo.class, queryExp );
		return info;
	    }
	    
	    // scan based implementation
	    
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
```

            

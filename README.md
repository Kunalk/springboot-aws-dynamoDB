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

To operate over data, use mapper which allows to integrate with service
 DynamoDBMapper mapper = dynamoDBService.getMapper();
E.g.
mapper.save( info, new DynamoDBMapperConfig.TableNameOverride( tableName ).config());
List<BookInfo> itemList = dynamoDBService.getMapper(tableName).query(BookInfo.class, queryExpression);       
```
            

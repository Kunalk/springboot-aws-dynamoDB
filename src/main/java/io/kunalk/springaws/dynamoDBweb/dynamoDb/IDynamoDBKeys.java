/** \file
 * 
 * Apr 11, 2018
 *
 * Copyright Ian Kaplan 2018
 *
 * @author Ian Kaplan, www.bearcave.com, iank@bearcave.com
 */
package io.kunalk.springaws.dynamoDBweb.dynamoDb;

import com.amazonaws.regions.Regions;

/**
 * <h4>
 * IDynamoDBKeys
 * </h4>
 * 
 * <p>
 * This interface holds the DynamoDB keys. This allows the key values to be removed in one place
 * before checkin to the public GitHub repository.
 * </p>
 * <p>
 * The keys in this interface should be created with the IAM Amazon Web Service and should have the
 * minimal permissions needed. The "full_dynamodb_access" should have full table create, read and
 * write to DynamoDB, but should not have access to any other service. This minimizes the potential
 * damage if the key is compromised.
 * </p>
 * <p>
 * The region is included here was well, since region generally goes along with the keys.
 * </p>
 * Apr 11, 2018
 * 
 * @author Ian Kaplan, iank@bearcave.com
 */
public interface IDynamoDBKeys {
    final static String full_dynamodb_access_ID = "Your AWS ID goes here";
    final static String full_dynamodb_access_KEY = "Your AWS secret key goes here";
    
    // The AWS region for the DynamoDB instance.
    // Frankfurt Germany
    final static Regions region = Regions.EU_CENTRAL_1;
}

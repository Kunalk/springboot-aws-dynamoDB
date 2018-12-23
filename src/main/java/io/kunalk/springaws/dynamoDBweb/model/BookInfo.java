/** \file
 * 
 * Mar 28, 2018
 *
 * Copyright Ian Kaplan 2018
 *
 * @author Ian Kaplan, www.bearcave.com, iank@bearcave.com
 */
package io.kunalk.springaws.dynamoDBweb.model;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * <p>
 * BookInfo
 * </p>
 * 
 * <p>
 * Information about a book. The information in this class comes from the book information form
 * filled out by the user.
 * </p>
 * <p>
 * This class is annotated so that it can be persisted to DynamoDB.
 * </p>

 */
@DynamoDBTable(tableName="book_table")
public class BookInfo {
    @NotBlank(message="A book title is required")
    private String title;
    @NotBlank(message="An author name is required")
    private String author;
    @NotBlank(message="Please include a genre for the book")
    private String genre;
    private String publisher;
    @Digits(integer=4, fraction=0, message="Please enter a year")
    private String year;
    @Pattern(regexp="^\\d{0,8}(\\.\\d{1,4})?$", message="Please enter a price i.e., 16, 16.00, 15.95")
    private String price;
    
    @DynamoDBHashKey(attributeName="title")
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = trimString( title );
    }
    
    @DynamoDBRangeKey(attributeName="author")
    @DynamoDBIndexHashKey(attributeName="author", globalSecondaryIndexName="author_index")
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = trimString( author );
    }    
    
    @DynamoDBAttribute(attributeName="genre")
    public String getGenre() {
        return genre;
    }
    public void setGenre(String genre) {
        this.genre = trimString( genre );
    }
    
    @DynamoDBAttribute(attributeName="publisher")
    public String getPublisher() {
        return publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = trimString( publisher );
    }
    
    @DynamoDBAttribute(attributeName="year")
    public void setYear(String year) {
        this.year = trimString( year );
    }
    public String getYear() {
        return this.year;
    }
    
    @DynamoDBAttribute(attributeName="price")
    public String getPrice() {
        return price;
    }
    public void setPrice(String price) {
        this.price = trimString( price );
    }
    
    @DynamoDBIgnore
    private String trimString(String input) {
        String rslt = "";
        if (input != null && input.length() > 0) {
            rslt = input.trim();
        }
        return rslt;
    }
    
    @Override
    @DynamoDBIgnore
    public String toString() {
        return "BookForm [title=" + title + ", author=" + author + ", genre=" + genre + ", publisher=" + publisher + ", year = " + year + ", price=" + price
                + "]";
    }
    
    
    @Override
    @DynamoDBIgnore
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BookInfo)) {
            return false;
        }
        BookInfo other = (BookInfo) obj;
        if (author == null) {
            if (other.author != null) {
                return false;
            }
        } else if (!author.equals(other.author)) {
            return false;
        }
        if (genre == null) {
            if (other.genre != null) {
                return false;
            }
        } else if (!genre.equals(other.genre)) {
            return false;
        }
        if (price == null) {
            if (other.price != null) {
                return false;
            }
        } else if (!price.equals(other.price)) {
            return false;
        }
        if (publisher == null) {
            if (other.publisher != null) {
                return false;
            }
        } else if (!publisher.equals(other.publisher)) {
            return false;
        }
        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            return false;
        }
        if (year == null) {
            if (other.year != null) {
                return false;
            }
        } else if (!year.equals(other.year)) {
            return false;
        }
        return true;
    }
    
    
    
}

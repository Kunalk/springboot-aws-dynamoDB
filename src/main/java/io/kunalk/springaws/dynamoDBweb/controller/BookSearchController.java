/** \file
 * 
 * May 14, 2018
 *
 * Copyright Ian Kaplan 2018
 *
 * @author Ian Kaplan, www.bearcave.com, iank@bearcave.com
 */
package io.kunalk.springaws.dynamoDBweb.controller;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



/**
 * <h4>
 * BookSearchController
 * </h4>
 * <p>
 * This controller handles database search for books.
 * </p>
 * <p>
 * Note that the List<BookInfo> object is passed in the redirect via a flash attribute. Flash attributes are not limited
 * to strings, as are attributes.  See https://stackoverflow.com/a/24302616
 * </p>
 * <p>
 * For a reference on Spring controllers, see http://www.codejava.net/frameworks/spring/14-tips-for-writing-spring-mvc-controller
 * </p>

 *
 */
@Controller
public class BookSearchController extends BookControllerBase {

    private static final Log logger = LogFactory.getLog(BookSearchController.class);
    
    /**
     * <p>
     * Search by the book title and author.
     * </p>
     * @param title Book title
     * @param author Book author
     * @param redirect Spring redirect attribute used to return the search result (as a flash attribute).
     * @return the redirect page.
     */
    @RequestMapping(value = "/title-author-search", method = RequestMethod.POST)
    public String searchByTitleAuthor(@RequestParam("title")  String title,
                                      @RequestParam("author") String author,
                                      RedirectAttributes redirect) {
        if (title != null && title.length() > 0) {
            if (author != null && author.length() > 0) {
                logger.info("searchByTitleAuthor: title = " + title + ", author = " + author);
                List<BookInfo> bookInfoList = getBookTableService().findBookByTitleAuthor(author, title);
                if (bookInfoList != null && bookInfoList.size() > 0) {
                    redirect.addFlashAttribute(BOOK_LIST, bookInfoList);
                }
            } else {
                redirect.addFlashAttribute("title_author_author_error", "Author must be specified");
            }
        } else {
            redirect.addFlashAttribute("title_author_title_error", "Title must be specified");
        }
        
        return "redirect:/";
    }
    
    /**
     * <p>
     * Search by the book author.
     * </p>
     * @param author book author
     * @param redirect Spring redirect object used to return the search result.
     * @return the redirect page.
     */
    @RequestMapping(value = "/author-search", method = RequestMethod.POST)
    public String searchByAuthor(@RequestParam("author") String author, RedirectAttributes redirect) {
        if (author != null && author.length() > 0) {
            List<BookInfo> bookInfoList = getBookTableService().findBookByAuthor(author);
            if (bookInfoList != null && bookInfoList.size() > 0) {
                redirect.addFlashAttribute(BOOK_LIST, bookInfoList);
            }
        }
        return "redirect:/";
    }
    
    /**
     * <p>
     * Search the book database by title (or title substring).
     * </p>
     * 
     * @param title The title substring to search book titles for 
     * @param redirect the RedirectAttributes object used to return the search result
     * @return the redirect page
     */
    @RequestMapping(value = "/title-search", method = RequestMethod.POST)
    public String searchByTitle(@RequestParam("title") String title, RedirectAttributes redirect) {
        if (title != null && title.length() > 0) {
            List<BookInfo> bookInfoList = getBookTableService().findBookByTitle(title);
            if (bookInfoList != null && bookInfoList.size() > 0) {
                redirect.addFlashAttribute(BOOK_LIST, bookInfoList);
            }
        }
        return "redirect:/";
    }
    
    /**
     * <p>
     * Return all books in the database
     * </p>
     * 
     * @param redirect
     * @return
     */
    @RequestMapping(value = "/list-all-books", method = RequestMethod.POST)
    public String getAllBooks( RedirectAttributes redirect ) {
        List<BookInfo> bookInfoList = getBookTableService().getBooks();
        if (bookInfoList != null && bookInfoList.size() > 0) {
            redirect.addFlashAttribute(BOOK_LIST, bookInfoList);
        }
        return "redirect:/";
    }
}

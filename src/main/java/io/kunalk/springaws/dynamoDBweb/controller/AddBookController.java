/** \file
 * 
 * Mar 28, 2018
 *
 * Copyright Ian Kaplan 2018
 *
 * @author Ian Kaplan, www.bearcave.com, iank@bearcave.com
 */
package io.kunalk.springaws.dynamoDBweb.controller;

import javax.validation.Valid;

import io.kunalk.springaws.dynamoDBweb.model.BookInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



/**
 * * <h4>
 * AddBookController
 * </h4>
 * <p>
 * Add a book to the database
 * </p>
 */
@Controller
public class AddBookController extends BookControllerBase {
    
    private static final Log logger = LogFactory.getLog(AddBookController.class);

    @GetMapping( value="/addbook" )
    public String addbook( Model model ) {
        return "addbook";
    } // addBookForm
    

    @RequestMapping( value="/save-book", method = RequestMethod.POST)
    public String saveBook(@Valid BookInfo bookForm, Errors errors, RedirectAttributes redirectAttributes) {
        if (! errors.hasErrors()) {
            if (bookForm != null) {
                getBookTableService().writeToBookTable(bookForm);
                redirectAttributes.addFlashAttribute("book_saved", "Saved the information for " + bookForm.getTitle());
            } else {
                logger.info("bookForm argument is null");
            }
        } else {
            redirectAttributes.addFlashAttribute("errors", errors);
        }
        return "redirect:addbook";
    }

}

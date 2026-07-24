package com.revature.util;

import java.util.List;
import java.util.Objects;

/**
 * The Page class represents a paginated collection of items, along with 
 * metadata that facilitates a fluid paging experience for users. This 
 * class encapsulates the items on the current page as well as 
 * information about the total number of pages and elements. It supports 
 * generics to allow flexibility in the type of items stored in the 
 * page. Additionally, the class implements overridden equals and 
 * hashCode methods, which are essential for comparing Page objects 
 * and storing them in collections.
 *
 * You do not need to edit this class.
 * 
 * @param <E> the type of elements in the page
 */

public class Page<E> {

    // fields

    /** The current page number. */
    private int pageNumber;
    /** The size of the page, or the number of items per page. */
    private int pageSize;
    /** The current page number. */
    private int totalPages;
    /** The total number of pages available. */
    private int totalElements;
    /** The total number of elements across all pages. */
    private List<E> items;

    // constructors
    public Page() {
      
    }

    public Page(int pageNumber, int pageSize, int totalPages, int totalElements, List<E> items) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.items = items;
    }

    // getters and setters
    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    public List<E> getItems() {
        return items;
    }

    public void setItems(List<E> items) {
        this.items = items;
    }

    /**
     * Returns a hash code value for the Page object.
     *
     * @return the hash code value for this Page
     */
    @Override
    public int hashCode() {
        return Objects.hash(pageNumber, pageSize, totalPages, totalElements, items);
    }

    /**
     * Compares this Page object to another object for equality.
     *
	 * (FOR REFERENCE) This method is part of the backend logic.
     * No modifications or implementations are required.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj){
            return true;
        }
        if (obj == null || getClass() != obj.getClass()){
            return false;
        }
        Page<?> page = (Page<?>) obj;
        return pageNumber == page.pageNumber &&
               pageSize == page.pageSize &&
               totalPages == page.totalPages &&
               totalElements == page.totalElements &&
               Objects.equals(items, page.items);
    }
}

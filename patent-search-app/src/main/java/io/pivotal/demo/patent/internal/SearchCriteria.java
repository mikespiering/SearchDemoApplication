package io.pivotal.demo.patent.internal;

import java.io.Serializable;

/**
 * 
 */
public class SearchCriteria implements Serializable {
    private static final long serialVersionUID = 1L;
    private String searchString;
    private int pageSize;
    private int page;

    public String getSearchString() {
	return searchString;
    }

    public void setSearchString(String searchString) {
	this.searchString = searchString;
    }

    public int getPageSize() {
	return pageSize;
    }

    public void setPageSize(int pageSize) {
	this.pageSize = pageSize;
    }

    public int getPage() {
	return page;
    }

    public void setPage(int page) {
	this.page = page;
    }
    
    public String toString() {
    	return "Query=[" + searchString + "], PageSize=["+ pageSize +"], Page=["+ page+"]";
    }
}
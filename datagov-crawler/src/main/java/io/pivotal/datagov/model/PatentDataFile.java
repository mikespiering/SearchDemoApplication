package io.pivotal.datagov.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
public class PatentDataFile {
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	@Column(unique=true)
	private String fileName;
	@Basic
    @Temporal(TemporalType.DATE)
	private Date postedDate;
	@Basic
    @Temporal(TemporalType.DATE)
	private Date foundDate;
	private Boolean fileDownloaded = Boolean.FALSE;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Date getFoundDate() {
		return foundDate;
	}
	public void setFoundDate(Date foundDate) {
		this.foundDate = foundDate;
	}
	public Boolean getFileDownloaded() {
		return fileDownloaded;
	}
	public void setFileDownloaded(Boolean fileDownloaded) {
		if (fileDownloaded == null) {
			this.fileDownloaded = Boolean.FALSE;
		} else {
			this.fileDownloaded = fileDownloaded;			
		}
	}
	public Date getPostedDate() {
		return postedDate;
	}
	public void setPostedDate(Date postedDate) {
		this.postedDate = postedDate;
	}
}

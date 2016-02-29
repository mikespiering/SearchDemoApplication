package io.pivotal.datagov.services;

import java.util.List;

import io.pivotal.datagov.model.PatentDataFile;

import org.springframework.data.repository.CrudRepository;

public interface PatentDataFileRepository extends CrudRepository<PatentDataFile, Long> {

	PatentDataFile findByFileName(String fileName);
	
	List<PatentDataFile> findByfileDownloaded(Boolean fileDownloaded);
}

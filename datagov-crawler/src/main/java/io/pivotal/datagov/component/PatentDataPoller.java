package io.pivotal.datagov.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PatentDataPoller {
	private static final Logger logger = LoggerFactory.getLogger(PatentDataPoller.class);
	@Autowired private PatentDataFilePoller dataFilePoller;
	@Autowired private PatentDataUpdater dataUpdater;
	
	/**
	 * Checks for patents files posted in the remote site. Files containing patent data are posted 
	 * once a week (randomly, selected). 
	 * <p>This process runs every 2 hours and checks for the latest file detected.
	 */
	@Scheduled(fixedRate = 7200000)
    public void checkForLatestPatentFile() {
		logger.info("Polling remote site for files...");
		dataFilePoller.checkForLatestPatentFile();
		
		logger.info("Process any new detected files...");
		dataUpdater.processPatentFile();
	}
}

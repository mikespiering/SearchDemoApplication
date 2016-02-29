package io.pivotal.datagov.component;

import io.pivotal.datagov.model.PatentDataFile;
import io.pivotal.datagov.services.PatentDataFileRepository;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Component responsible for polling for data files available at the remote site. These files are
 * expected to be located as described at the "data.gov". 
 * <p>In the case of "patent" data sets, these are managed by reedtech.com
 * 
 * @see
 * https://catalog.data.gov/dataset/patent-grant-full-text-1976-present-doc-33862
 * http://patents.reedtech.com/pgrbft.php
 * http://patents.reedtech.com/downloads/GrantRedBookText/2014/ipg140916.zip (example file)
 */
@Component
public class PatentDataFilePoller {
	private static final Logger logger = LoggerFactory.getLogger(PatentDataFilePoller.class);
	@Autowired private PatentDataFileRepository patentDataFileRepository;
	private static final int MAX_ATTEMPTS = 30;
	private static final String REMOTE_SITE_LOCATION = "http://patents.reedtech.com/downloads/GrantRedBookText/";
	
    public void checkForLatestPatentFile() {
		Calendar instance = Calendar.getInstance();	
		int attempts = 0;
		boolean remoteFileFound = false;
		
		// check for more only if not already found latest
		while (!remoteFileFound && attempts < MAX_ATTEMPTS) {
			String remoteFile = buildRemoteURI("ipg", instance.getTime());
			remoteFileFound = isRemoteFileAvailable(remoteFile);
			
			// Sync with our database if a remote file is available
			if (remoteFileFound) {
				PatentDataFile patentDataFile = patentDataFileRepository.findByFileName(remoteFile);
				if (patentDataFile == null) {
					patentDataFile = new PatentDataFile();
					patentDataFile.setFileName(remoteFile);
					patentDataFile.setPostedDate(instance.getTime());
					patentDataFile.setFoundDate(new Date());
					patentDataFileRepository.save(patentDataFile);
					logger.info("Saving new remote file metadata {}", remoteFile);
				} else {
					logger.info("Remote file {} already detected at data.gov site. Ignoring!", remoteFile);
				}
			}
			
			// update counters
			attempts++;
			instance.add(Calendar.DAY_OF_MONTH, -1);
		}
		
		// file not found in the last MAX_ATTEMPTS days
		if (!remoteFileFound) {
			logger.warn("No new Patent file found in the last {} days.", MAX_ATTEMPTS);
		}
	}
	
	/**
	 * Checks if a remote file is available for the given date
	 * @param the fully qualified name of the file at the remote site. This conforms with 
	 * the format of the data set management site.
	 * @return true if there is a file available in the remote server, false otherwise. 
	 */
	public boolean isRemoteFileAvailable(String remoteFile) {
		logger.info("Checking for remote file at {}", remoteFile);
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = restTemplate.headForHeaders(remoteFile);
		boolean found = headers.getContentLength() > 0;
		if (found) {
			logger.info("File found at {}", remoteFile);
		}
		return found;
	}
    
	/**
	 * Returns the fully qualified location of the file at the remote site
	 * as described by data set management office (reedtech.com).
	 * @param prefix the prefix used by reedtech.com for data files
	 * @param date the date used in the remote file name pattern.
	 */
    public String buildRemoteURI(String prefix, Date date) {
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
    	
    	StringBuilder buffer = new StringBuilder();
    	buffer.append(REMOTE_SITE_LOCATION);
    	buffer.append(formatter.format(date)).append("/");
    	buffer.append(prefix);
    	
    	formatter = new SimpleDateFormat("yyMMdd");
    	buffer.append(formatter.format(date));
    	buffer.append(".zip");
    	return buffer.toString();
    }
}

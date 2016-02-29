package io.pivotal.datagov.component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import io.pivotal.datagov.model.Patent;
import io.pivotal.datagov.model.PatentDataFile;
import io.pivotal.datagov.services.PatentDataFileRepository;
import io.pivotal.datagov.services.PatentRepository;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Component
public class PatentDataUpdater {
	private static final Logger logger = LoggerFactory.getLogger(PatentDataUpdater.class);
	@Autowired private PatentDataFileRepository patentDataFileRepository;
	@Autowired private PatentRepository patentRepository;
	private XPath xPath =  XPathFactory.newInstance().newXPath();
	private String root = "us-patent-grant";
	private String documentIdRoot = root + "/us-bibliographic-data-grant";
	private String documentPath = documentIdRoot + "/publication-reference/document-id";
	private String docNumberQuery = documentPath + "/doc-number";
	private String kindQuery = documentPath + "/kind";
	private String dateQuery = documentPath + "/date";
	private String inventionName = documentIdRoot + "/invention-title";
	private String abstractSummary = root + "/abstract/p";
	private DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
    public static int batchSize = 100;
    
    public void processPatentFile() {
		List<PatentDataFile> files = patentDataFileRepository.findByfileDownloaded(Boolean.FALSE);
		
		if (CollectionUtils.isEmpty(files)) {
			logger.info("No files pending processing. Will check again later!");
		} else {
			logger.info("Processing {} files", files.size());
			for (PatentDataFile file : files) {
				try {
					// get stream of remote zip file
					URL website = new URL(file.getFileName());
					ZipInputStream zis = new ZipInputStream(website.openStream());
					ZipEntry ze = null; 

					List<Patent> patents = new LinkedList<Patent>();
					int counter = 0;
					while ((ze = zis.getNextEntry()) != null) { 
						InputStreamReader is = new InputStreamReader(zis);  
						LineIterator it = IOUtils.lineIterator(is);
						String fileName = ze.getName();  
						logger.info("Reading file {}", fileName);

						String prefix = "<?xml "; // denotes a new xml section within the compress file
						StringBuilder xmlString = null;
						while(it.hasNext()) {  
							String line = it.nextLine();  
							if (line.startsWith(prefix)) {
								// if we are here, then a new xml section is about to start, so let's 
								// process what we have so far
								if (xmlString != null) {
									Patent patent = convertToPatent(xmlString.toString());
									if (patent != null) {
										counter++;
										if (counter % batchSize == 0) {
											processPatents(patents);
										} else {
											patents.add(patent);
										}
									} 
								}
								xmlString = new StringBuilder();
							}
							xmlString.append(line);
						}
						// processing last xml section
						if (xmlString != null) {
							logger.info("Processing last XML buffer!");
							Patent patent = convertToPatent(xmlString.toString());
							if (patent != null) {
								counter++;
								patents.add(patent);
							}
						}
						// cleaning patents batch
						if (!patents.isEmpty()) {
							processPatents(patents);
						}
					}
				} catch (Exception e) {
					logger.error("Detected error during processing of file", e);
				}
				// update file as processed
				file.setFileDownloaded(Boolean.TRUE);
				patentDataFileRepository.save(file);
			}
		}
	}
	
	
	public String querySingleValue(XPath xPath, Document xmlDocument, String expression) throws Exception {
		return xPath.compile(expression).evaluate(xmlDocument);
	}
	
	public void processPatents(List<Patent> patents) {
		logger.info("Saving batch of patents...");
		patentRepository.save(patents);
		patents.clear();
	}
	
	private Patent convertToPatent(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;  
        try 
        {  
            builder = factory.newDocumentBuilder();  
            // this is a hack so that we don't resolve DTDs
            builder.setEntityResolver(new EntityResolver() {
	            public InputSource resolveEntity(String publicId, String systemId)
	                    throws SAXException, IOException {
	                return new InputSource(new StringReader(""));
	            }
	        });
            Document document = builder.parse( new InputSource( new StringReader( xmlStr ) ) ); 
            Patent patent = new Patent();
			patent.setDocNumber(querySingleValue(xPath, document, docNumberQuery));
			patent.setKind(querySingleValue(xPath, document, kindQuery));
			
			// handle date transformation
			String publishedDate = querySingleValue(xPath, document, dateQuery);
			Date date = null;
			try {
				date = formatter.parse(publishedDate);
			} catch (ParseException e) {
				// not much we can do, so ignoring 
			}
			patent.setPublishedDate(date);
			patent.setTitle(querySingleValue(xPath, document, inventionName));
			
			String abstractInfo = querySingleValue(xPath, document, abstractSummary);
			if (StringUtils.isNotEmpty(abstractInfo)) {
				patent.setSummary(abstractInfo);
			}
            return patent;
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
        return null;
    }
}

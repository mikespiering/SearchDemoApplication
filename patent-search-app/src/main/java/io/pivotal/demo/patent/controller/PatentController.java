package io.pivotal.demo.patent.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.pivotal.demo.patent.internal.Patent;
import io.pivotal.demo.patent.internal.SearchCriteria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/")
public class PatentController {
	private static final Logger logger = LoggerFactory.getLogger(PatentController.class);
	@Autowired private io.pivotal.demo.patent.internal.PatentRepository patentRepository;
	
	@RequestMapping
	public String main(Model model) throws Exception {
		updateModel(model);
		return "noaa/main";
	}
	
	@RequestMapping(value = "noaa/search", method = RequestMethod.GET)
	public String list(SearchCriteria criteria, Model model) {
		logger.info("Searching for {}", criteria.toString());
		Page<Patent> pageOfResults = patentRepository.findByTitleContainingOrSummaryContaining(criteria.getSearchString(), criteria.getSearchString(),
													new PageRequest(criteria.getPage(), criteria.getPageSize()));
		model.addAttribute("patentList", pageOfResults.getContent());
		return "noaa/list";
	} 
	
	@RequestMapping(value = "noaa/{id}", method = RequestMethod.GET)
    public String show(@PathVariable Long id, Model model) {
		logger.info("Showing patent {}", id);
		model.addAttribute("patent", patentRepository.findOne(id));
		return "noaa/show";
    }

	@RequestMapping(value = "noaa", method = RequestMethod.GET)
	public String createForm(SearchCriteria searchCriteria) {
		logger.info("Setting up search form...");
		return "noaa/search";
	}
	
	/**
	 * Action to initiate shutdown of the system.  In CF, the application 
	 * <em>should</em>f restart.  In other environments, the application
	 * runtime will be shut down.
	 */
	@RequestMapping(value = "kill", method = RequestMethod.GET)
	public void kill() {
		logger.warn("*** The system is shutting down. ***");
		System.exit(-1);
	}
	
	private void updateModel(Model model) throws Exception {
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		String serverTime = dateFormat.format(date);
		model.addAttribute("serverTime", serverTime);
		
		String port = System.getenv("PORT");
		model.addAttribute("port", port);

		String vcapApplication = System.getenv("VCAP_APPLICATION");
		
		if (vcapApplication != null) {
			ObjectMapper mapper = new ObjectMapper();
			Map vcapMap = mapper.readValue(vcapApplication, Map.class);
			model.addAttribute("vcapApplication", vcapMap);
		} else {
			// VCAP NOT SET (not a CF deployment)
			Map vcapMap = new HashMap<String, String>();
			vcapMap.put("started_at", new Date()); // so that UI layer doesn't blow
			model.addAttribute("vcapApplication", vcapMap);
		}
		
		String vcapServices = System.getenv("VCAP_SERVICES");
		if (vcapServices != null) {
			ObjectMapper mapper = new ObjectMapper();
			Map vcapMap = mapper.readValue(vcapServices, Map.class);
			model.addAttribute("vcapServices", vcapMap.keySet());
		} else {
			// VCAP NOT SET (not a CF deployment)
			model.addAttribute("vcapServices", new HashMap<String, String>());
		}
		
		logger.info("Current date and time = [{}], port = [{}].", serverTime, port);
	}
}

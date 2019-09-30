package uk.ac.ebi.ddi.task.ddicitationcount;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.ddi.task.ddicitationcount.service.CitationTaskService;

@SpringBootApplication
public class DdiCitationCountApplication implements CommandLineRunner {

	@Autowired
	private CitationTaskService citationTaskService;

	public static void main(String[] args) {
		SpringApplication.run(DdiCitationCountApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		citationTaskService.addAllCitations();
	}
}

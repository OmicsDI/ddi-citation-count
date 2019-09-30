package uk.ac.ebi.ddi.task.ddicitationcount;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ddi.task.ddicitationcount.configuration.CitationTaskProperties;
import uk.ac.ebi.ddi.task.ddicitationcount.service.CitationTaskService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DdiCitationCountApplicationTests {

	@Autowired
	private DdiCitationCountApplication citationCountApplication;

	@Test
	public void contextLoads() throws Exception {
		citationCountApplication.run();
	}

}

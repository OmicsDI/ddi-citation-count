package uk.ac.ebi.ddi.task.ddicitationcount.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import uk.ac.ebi.ddi.task.ddicitationcount.service.CitationTaskService;

@EnableTask
@Configuration
@EnableConfigurationProperties({CitationTaskProperties.class})
@EnableMongoRepositories(value = "uk.ac.ebi.ddi.service.db.repo")
@ComponentScan({"uk.ac.ebi.ddi.service.db","uk.ac.ebi.ddi.ebe.ws.dao.client.europmc"})
public class TaskConfiguration {

}

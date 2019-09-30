package uk.ac.ebi.ddi.task.ddicitationcount.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ddi.ddidomaindb.dataset.DSField;
import uk.ac.ebi.ddi.ebe.ws.dao.client.europmc.CitationClient;
import uk.ac.ebi.ddi.ebe.ws.dao.config.AbstractEbeyeWsConfig;
import uk.ac.ebi.ddi.ebe.ws.dao.config.EbeyeWsConfigDev;
import uk.ac.ebi.ddi.ebe.ws.dao.config.EbeyeWsConfigProd;
import uk.ac.ebi.ddi.ebe.ws.dao.model.europmc.CitationResponse;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.service.db.model.dataset.Scores;
import uk.ac.ebi.ddi.service.db.model.similarity.Citations;
import uk.ac.ebi.ddi.service.db.service.dataset.DatasetService;
import uk.ac.ebi.ddi.service.db.service.similarity.CitationService;
import uk.ac.ebi.ddi.task.ddicitationcount.utils.SimilarityConstants;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CitationTaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CitationTaskService.class);

    private Integer startDataset = 0;

    private Integer numberOfDataset = 2000;

    private Integer numberOfCitations = 500;

    @Autowired
    private DatasetService datasetService;


    private CitationClient citationClient;

    @Autowired
    private CitationService citationService;

    public void CitationService(){
        citationClient = new CitationClient(new EbeyeWsConfigProd());
    }

    public void CitationService(AbstractEbeyeWsConfig configType){
            citationClient = new CitationClient(configType);
    }


    public void addAllCitations() {
        try {
            for (int i = startDataset; i < datasetService.getDatasetCount() / numberOfDataset; i = i + 1) {
                LOGGER.info("value of i is" + i);
                datasetService.readAll(i, numberOfDataset).getContent()
                        .forEach(dt -> getCitationCount(
                                dt.getDatabase(), dt.getAccession(),
                                dt.getAdditional().containsKey(DSField.Additional.SECONDARY_ACCESSION.key()) ?
                                        new ArrayList<String>(
                                                dt.getAdditional().get(DSField.Additional.SECONDARY_ACCESSION.key())
                                        ) :
                                        new ArrayList<String>()));
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occurred, ", ex);
        }
    }

    public void getCitationCount(String database, String accession, List<String> secondaryAccession) {
        try {
            final Dataset dataset = datasetService.read(accession, database);
            Set<String> primaryCitationIds = getCitationsSet(accession, dataset);
            if (!secondaryAccession.isEmpty()) {
                secondaryAccession.forEach(acc -> {
                    Set<String> secondaryCitationIds = getCitationsSet(acc, dataset);
                    primaryCitationIds.addAll(secondaryCitationIds);
                });
            }
            addCitationData(dataset, primaryCitationIds);
        } catch (Exception ex) {
            LOGGER.error("Exception occurred when getting dataset {},  ", accession, ex);
        }
    }

    public Set<String> getCitationsSet(String accession, Dataset dataset) {
        List<CitationResponse> citations = new ArrayList<>();
        Set<String> primaryCit = new HashSet<String>();
        int numberOfPages = 0;
        CitationResponse primaryCitation = citationClient.getCitations(accession, numberOfCitations, "*");
        primaryCit.addAll(Arrays.stream(primaryCitation.citations.get(SimilarityConstants.RESULT))
                .filter(data -> (dataset.getCrossReferences() != null
                        && dataset.getCrossReferences().get(DSField.CrossRef.PUBMED.key()) != null
                        && !dataset.getCrossReferences().get(DSField.CrossRef.PUBMED.key()).contains(data.pubmedId)))
                .map(dt -> dt.pubmedId).collect(Collectors.toSet()));

        if (primaryCitation.count > numberOfCitations) {
            while (primaryCitation.count / numberOfCitations - numberOfPages > 0) {
                primaryCitation = citationClient.getCitations(accession, numberOfCitations, primaryCitation.cursorMark);
                primaryCit.addAll(Arrays.stream(primaryCitation.citations.get(SimilarityConstants.RESULT))
                        .filter(data -> (dataset.getCrossReferences() != null
                                && dataset.getCrossReferences().get(DSField.CrossRef.PUBMED.key()) != null
                                && !dataset.getCrossReferences().get(
                                DSField.CrossRef.PUBMED.key()).contains(data.pubmedId)))
                        .map(dt -> dt.pubmedId).collect(Collectors.toSet()));
                numberOfPages++;
            }
        }
        return primaryCit;

    }

    public Dataset addCitationData(Dataset dataset, Set<String> allCitationIds) {

        Dataset updateDataset = datasetService.read(dataset.getAccession(), dataset.getDatabase());
        Citations citations = new Citations();
        citations.setAccession(dataset.getAccession());
        citations.setDatabase(dataset.getDatabase());
        citations.setPubmedId(allCitationIds);
        citations.setPubmedCount(allCitationIds.size());
        citationService.saveCitation(citations);

        if (dataset.getScores() != null) {
            updateDataset.getScores().setCitationCount(allCitationIds.size());
        } else {
            Scores scores = new Scores();
            scores.setCitationCount(allCitationIds.size());
            updateDataset.setScores(scores);

        }
        HashSet<String> count = new HashSet<>();
        count.add(String.valueOf(allCitationIds.size()));
        updateDataset.getAdditional().put(DSField.Additional.CITATION_COUNT.key(), count);
        datasetService.update(updateDataset.getId(), updateDataset);
        return dataset;
    }

}

package org.cbioportal.genome_nexus.annotation.domain.internal;

import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.cbioportal.genome_nexus.annotation.domain.PfamDomain;
import org.cbioportal.genome_nexus.annotation.domain.PfamDomainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author Selcuk Onur Sumer
 */
@Component("defaultPfamDomainRepository")
public class PfamDomainRepositoryImpl implements PfamDomainRepository
{
    private String resourceURI;

    private List<PfamDomain> pfamDomainList;
    private Map<String, List<PfamDomain>> indexByTranscript;
    private Map<String, List<PfamDomain>> indexByProtein;
    private Map<String, List<PfamDomain>> indexByGene;
    private Map<String, List<PfamDomain>> indexByPfam;

    @Autowired
    public PfamDomainRepositoryImpl(@Value("${ensembl.biomart.pfam}") String resourceURI)
    {
        this.resourceURI = resourceURI;

        // populate the list and build the indices
        this.populateData();
    }

    public List<PfamDomain> findAll()
    {
        return this.pfamDomainList;
    }

    @Override
    public List<PfamDomain> findByTranscriptId(String transcriptId) {
        return this.find(this.indexByTranscript, transcriptId);
    }

    @Override
    public List<PfamDomain> findByProteinId(String proteinId) {
        return this.find(this.indexByProtein, proteinId);
    }

    @Override
    public List<PfamDomain> findByGeneId(String geneId) {
        return this.find(this.indexByGene, geneId);
    }

    @Override
    public List<PfamDomain> findByPfamDomainId(String pfamDomainId) {
        return this.find(this.indexByPfam, pfamDomainId);
    }

    private List<PfamDomain> parseTSV()
    {
        BeanListProcessor<PfamDomain> rowProcessor = new BeanListProcessor<>(PfamDomain.class);

        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.setHeaderExtractionEnabled(true);
        parserSettings.getFormat().setDelimiter('\t');
        parserSettings.setRowProcessor(rowProcessor);

        CsvParser parser = new CsvParser(parserSettings);
        parser.parse(getReader(this.resourceURI));

        return rowProcessor.getBeans();
    }

    /**
     * Creates a reader for a resource in the relative path
     *
     * @param resourceURI path of the resource to be read
     * @return a reader of the resource
     */
    private Reader getReader(String resourceURI)
    {
        Resource resource = new ClassPathResource(resourceURI);

        try {
            return new InputStreamReader(resource.getInputStream(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Unable to read input", e);
        } catch (IOException e) {
            throw new IllegalStateException("Input not found", e);
        }
    }

    private List<PfamDomain> find(Map<String, List<PfamDomain>> index, String key)
    {
        List<PfamDomain> domains = index.get(key);

        if (domains == null)
        {
            domains = Collections.emptyList();
        }

        return domains;
    }

    private void populateData()
    {
        if (this.pfamDomainList == null)
        {
            this.pfamDomainList = new ArrayList<>();
            this.pfamDomainList.addAll(this.parseTSV());

            this.indexByTranscript = this.index(pfamDomainList, new TranscriptIndexKeyGetter());
            this.indexByProtein = this.index(pfamDomainList, new ProteinIndexKeyGetter());
            this.indexByGene = this.index(pfamDomainList, new GeneIndexKeyGetter());
            this.indexByPfam = this.index(pfamDomainList, new PfamIndexKeyGetter());
        }
    }

    private Map<String, List<PfamDomain>> index(List<PfamDomain> pfamDomainList, IndexKeyGetter getter)
    {
        Map<String, List<PfamDomain>> map = new HashMap<>();

        for (PfamDomain domain : pfamDomainList)
        {
            String id = getter.getIndexKey(domain);

            // skip invalid/empty id values
            if (id == null || id.trim().length() == 0) {
                continue;
            }

            List<PfamDomain> list = map.get(id);

            if (list == null)
            {
                list = new ArrayList<>();
                map.put(id, list);
            }

            list.add(domain);
        }

        return map;
    }

    private interface IndexKeyGetter
    {
        String getIndexKey(PfamDomain domain);
    }

    private class TranscriptIndexKeyGetter implements IndexKeyGetter
    {
        public String getIndexKey(PfamDomain domain)
        {
            return domain.getTranscriptId();
        }
    }

    private class ProteinIndexKeyGetter implements IndexKeyGetter
    {
        public String getIndexKey(PfamDomain domain)
        {
            return domain.getProteinId();
        }
    }

    private class GeneIndexKeyGetter implements IndexKeyGetter
    {
        public String getIndexKey(PfamDomain domain)
        {
            return domain.getGeneId();
        }
    }

    private class PfamIndexKeyGetter implements IndexKeyGetter
    {
        public String getIndexKey(PfamDomain domain)
        {
            return domain.getPfamDomainId();
        }
    }
}

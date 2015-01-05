package net.berthereau.exlibris.rta.test;

import java.util.List;
import java.util.Map;

import com.exlibris.primo.api.common.IMappingTablesFetcher;

/**
 * Returns examples used in the recommendation and in Koha without fetching from
 * Primo.
 *
 * This class is made for testing purpose only.
 */
public class MappingTablesFetcherTestUse implements IMappingTablesFetcher {

    private Map<String, List<Map<String, String>>> codes;

    public void setUp(Map<String, List<Map<String, String>>> codes) {
        this.codes = codes;
    }

    @Override
    public List<Map<String, String>> getTableRows(String tableName) {
        return codes.get(tableName);
    }
}

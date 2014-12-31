package com.exlibris.primo.api.common;

import java.util.List;
import java.util.Map;

public interface IMappingTablesFetcher {

    public List<Map<String, String>> getTableRows(String tableName);
}

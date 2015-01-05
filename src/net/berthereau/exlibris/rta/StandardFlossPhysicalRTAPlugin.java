package net.berthereau.exlibris.rta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.diglib.ilsdi.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.exlibris.primo.api.common.IMappingTablesFetcher;
import com.exlibris.primo.api.common.IPrimoLogger;
import com.exlibris.primo.api.plugins.rta.HoldingStatus;
import com.exlibris.primo.api.plugins.rta.Library;
import com.exlibris.primo.api.plugins.rta.PhysicalRTAPlugin;
import com.exlibris.primo.api.plugins.rta.RTARequest;

/**
 * Check the availability of an item on a remote system that implements the
 * ILS-DI standard of DLF.
 *
 * <p>
 * To be used, some parameters should be set. First, the base url to request rta
 * of a record on the remote system should be set in the Institution params, for
 * example for Koha: <code>https://www.example.com/cgi-bin/koha/ilsdi.pl</code>.
 * Second, in the Plugins Parameters Mapping Table, the first two values below
 * are required (but are not used in this plugin). Next ones are optional.
 * <ul>
 * <li>SourceSystem: The standard Floss ILS-DI registered in each record, for
 * example "ILS-DI". This value should be the same than in PNX records, at
 * "record/control/sourcesystem".</li>
 * <li>IdentifierXpath: The path in the PNX record to the id of this record on
 * the remote system, for example "record/control/sourcerecordid".</li>
 * <li>Debug: If true, more log will be written in Primo logs.</li>
 * <li>ConnectionTimeout: Default is 1000 milliseconds.</li>
 * <li>ReadTimeout: Default is 1000 milliseconds.</li>
 * <li>IdType: Define if the record id passed to the remote system is a "bib"
 * (default) or an "item" one. It depends on the IdentifierXpath.</li>
 * <li>ReturnType: Define the level of the response of the remote system. It can
 * be "bib" (default) or "item". It can't be "bib" if IdType is "item".</li>
 * <li>ReturnFmt: Empty is the default, for Simple Availability. Any supported
 * format can be used. This parameter is currently unmanaged.</li>
 * </ul>
 * </p>
 *
 * @internal Because the public documentation of the closed and proprietary API
 *           of Primo is incomplete (even the version of Java used by Primo is
 *           unknown, not publicly available and forbidden to be sought), some
 *           checks, loops, structures, imports, etc. may be useless or not
 *           exactly or optimally integrated.
 *
 * @see https://developers.exlibrisgroup.com/primo/integrations/frontend/rta
 * @author Daniel Berthereau <Daniel.java@Berthereau.net>
 */
public class StandardFlossPhysicalRTAPlugin implements PhysicalRTAPlugin {

    private IPrimoLogger logger;
    // private HashMap<String, String> institutionCodes = new HashMap<>();
    private HashMap<String, String> libraryCodes = new HashMap<>();

    // TODO Check if these params are already defined in the interface.
    // Plugin parameters.
    private int connectionTimeout = 1000;
    private int readTimeout = 1000;
    private boolean debug = false;

    // Plugins params that are specific to ILS-DI.
    private String idType = "bib";
    private String returnType = "bib";
    // Only the default format is implemented.
    private String returnFmt = "";

    // Prepare XPath during init so xml tools will be available quickly to
    // process each response.
    private XPathExpression exprRecordsList;
    private XPathExpression exprRecordId;
    private XPathExpression exprAvailability;
    private XPathExpression exprCountItems;
    private XPathExpression exprCountItemsAvailable;
    private XPathExpression exprCountItemsUnavailable;
    private XPathExpression exprItemsList;
    private XPathExpression exprItemId;
    // private XPathExpression exprLocation;

    /**
     * Primo invokes this empty constructor to creates this RTA plugin.
     */
    public StandardFlossPhysicalRTAPlugin() {
        // TODO Check if this super() is needed.
        super();
    }

    /**
     * Initializes the plugin and provides access to utilities and parameters
     * defined in the Plugins Parameters mapping table.
     *
     * TODO Throws exception in case of init failure? The example and the
     * presentation don't talk about that.
     */
    @Override
    public void init(
            IPrimoLogger logger,
            IMappingTablesFetcher mtFetcher,
            Map<String, Object> params) {

        this.logger = logger;

        List<Map<String, String>> rows;

        // Params set in the tables.

        // Institution code to get from Primo code.
        // TODO Check if institution code is needed (normally no, because check
        // is done for one institution only and there is the rta base url).
        // rows = mtFetcher.getTableRows("ILS Institution Codes");
        // for (Map<String, String> row : rows) {
        //    String ilsInstitution = row.get("ILS Institution");
        //    String primoInstitution = row.get("Primo Institution");
        //    institutionCodes.put(ilsInstitution, primoInstitution);
        // }

        // List of library codes to get from Primo codes.
        // TODO Library codes are currently not used, because IlS-DI doesn't
        // require to reply with it.
        rows = mtFetcher.getTableRows("ILS Library Codes");
        for (Map<String, String> row : rows) {
            String libraryCode = row.get("Library Code");
            String primoCode = row.get("Primo Code");
            libraryCodes.put(libraryCode, primoCode);
        }

        // Params of the plugin.
        initPluginParams(params);

        // Prepare xml tools used to process each response.
        try {
            initXPathProcessor();
        } catch (XPathExpressionException e) {
            logger.error("Cannot initialize " + getClass() + ": " + e.getMessage(), e);
            return;
        }

        logger.info("Plugin " + getClass() + " is initialized.");
    }

    /**
     * Check and set plugins params.
     *
     * @param params
     *            List of the plugin parameters set in the admin interface.
     * @return void
     */
    private void initPluginParams(Map<String, Object> params) {
        String param = "";

        param = (String) params.get("ConnectionTimeout");
        if (param != null && !param.isEmpty()) {
            try {
                connectionTimeout = Integer.valueOf(param);
            } catch (NumberFormatException e) {
                logger.warn("ConnectionTimeout param should be a number of milliseconds.");
            }
        }

        param = (String) params.get("ReadTimeout");
        if (param != null && !param.isEmpty()) {
            try {
                readTimeout = Integer.valueOf(param);
            } catch (NumberFormatException e) {
                logger.warn("ReadTimeout param should be a number of milliseconds.");
            }
        }

        // Only two possible values for id_type: "bib" or "item".
        param = (String) params.get("IdType");
        if (param == null || !param.equals("item")) {
            idType = "bib";
            if (!(param == null || param.isEmpty() || param.equals("bib"))) {
                logger.warn("idType is malformed. The plugin will use the default.");
            }
        }
        else {
            idType = "item";
        }

        // Only two possible values for id_type: "bib" or "item".
        param = (String) params.get("ReturnType");
        if (param == null || !param.equals("item")) {
            returnType = "bib";
            if (!(param == null || param.isEmpty() || param.equals("bib"))) {
                logger.warn("ReturnType is malformed. The plugin will use the default.");
            }
            if (idType == "item") {
                logger.warn("To use [item] as IdType and [bib] as ReturnType is currently unsupported.");
            }
        }
        else {
            returnType = "item";
        }

        // Any value is possible, so it should be url encoded if not pure ascii.
        param = (String) params.get("ReturnFmt");
        if (param == null || param.equals("")) {
            returnFmt = "";
        }
        else {
            // returnType = param;
            logger.warn("ReturnFmt is currently unmanaged. The plugin will use the default.");
            returnFmt = "";
        }

        debug = Boolean.parseBoolean((String) params.get("Debug"));
    }

    /**
     * Prepare XPath processor to speed up process..
     *
     * @return void
     * @throws XPathExpressionException
     */
    private void initXPathProcessor() throws XPathExpressionException {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        SimpleNamespaceContext ctx = new SimpleNamespaceContext();
        ctx.addNamespace("dlf", "http://diglib.org/ilsdi/1.1");
        // ctx.addNamespace("ncip", "http://ncip.envisionware.com/documentation/ncip_v1_0.xsd");
        // ctx.addNamespace("holdings", "http://www.loc.gov/standards/iso20775/");
        xpath.setNamespaceContext(ctx);

        // Currently, only accept DLF Simple Availability xml format.
        exprRecordsList = xpath.compile("/dlf:collection/dlf:record");
        exprRecordId = xpath.compile("dlf:bibliographic/@id");
        exprAvailability = xpath.compile("normalize-space(dlf:simpleavailability/dlf:availabilitystatus)");
        exprCountItems = xpath.compile("count(dlf:items/dlf:item)");
        exprCountItemsAvailable =
                xpath.compile("count(dlf:items/dlf:item/dlf:simpleavailability/dlf:availabilitystatus[normalize-space(text())='available'])");
        exprCountItemsUnavailable =
                xpath.compile("count(dlf:items/dlf:item/dlf:simpleavailability/dlf:availabilitystatus[normalize-space(text())='not available'])");
        exprItemsList = xpath.compile("dlf:items/dlf:item");
        exprItemId = xpath.compile("normalize-space(dlf:simpleavailability/dlf:identifier)");
        // exprLocation = xpath.compile("normalize-space(dlf:simpleavailability/dlf:location)");
    }

    /**
     * Get the up-to-date availability status of a list or records in a list of
     * libraries that are set in a list of RTA requests.
     *
     * If there is no result, for whatever reason (bad request, time out, etc.),
     * no update is done.
     *
     * TODO This process can be simplified if rta requests for different
     * institutions are send to multiple instances of this plugin, but this is
     * not clear in the documentation of Primo API, and its source is closed.
     */
    @Override
    public void updateAvailability(List<RTARequest> rtaRequests) {
        // TODO Check if a check has been done inside the closed Primo API.
        if (rtaRequests == null || rtaRequests.isEmpty()) {
            logger.warn("An empty rta request has been sent.");
            return;
        }

        // The remote API can manage multiple records by request, so
        // the requests to same institution, identified by the rta base url, are
        // merged.
        // TODO Institution is not used, because the rta base url is enough.
        Map<String, Map<String, List<Library>>> recordsByInstitution = sortRecordsByInstitution(rtaRequests);
        // Check if there is at least one library to request.
        if (recordsByInstitution.isEmpty()) {
            logger.error("No RTA request can be processed.");
            return;
        }

        // TODO Use java.nio2 to make parallel threads.
        for (Entry<String, Map<String, List<Library>>> entry : recordsByInstitution.entrySet()) {
            String rtaBaseUrl = entry.getKey();
            Map<String, List<Library>> recordsToRequest = entry.getValue();
            if (recordsToRequest.isEmpty()) {
                logger.warn("No records for this institution [rta base url: " + rtaBaseUrl + "].");
                continue;
            }

            // Before update of status by library for each record, we should get
            // availability...
            Set<String> recordIds = recordsToRequest.keySet();
            Map<String, HoldingStatus> results = checkAvailabilityForRecords(recordIds, rtaBaseUrl);
            if (results == null) {
                logger.warn("No results for record identifiers: " + recordIds + " with rta base url: [" + rtaBaseUrl
                        + "].");
                continue;
            }
            if (debug) {
                logger.info("Updated holding status of records: " + results);
            }

            // Set status for each record in each library.
            for (Entry<String, List<Library>> recordsToUpdate : recordsToRequest.entrySet()) {
                String recordIdentifier = recordsToUpdate.getKey();
                HoldingStatus holdingStatus = results.get(recordIdentifier);
                // No result for this record.
                if (holdingStatus == null) {
                    logger.warn("No response for record id [" + recordIdentifier + "].");
                }
                // Update all libraries for this record.
                else {
                    for (Library library : recordsToUpdate.getValue()) {
                        // TODO To be removed.
                        if (debug) {
                            logger.info("Update of record [" + recordIdentifier + "] for library ["
                                    + library.getLibraryCode() + "]: " + library.getHoldingStatus() + " => "
                                    + holdingStatus + ".");
                        }
                        // Update only if needed.
                        if (library.getHoldingStatus() != holdingStatus) {
                            library.setHoldingStatus(holdingStatus);
                        }
                    }
                }
            }
        }
    }

    /**
     * Re-map rta requests by institution, remote record ids and libraries.
     *
     * The institution is identified by its rta base url, because we don't know
     * if there is an Institution class that can be used.
     *
     * This helper is useful to manage update of multiple records for multiple
     * libraries. The Primo record id is not needed for that.
     *
     * @param rtaRequests
     * @return
     */
    Map<String, Map<String, List<Library>>> sortRecordsByInstitution(List<RTARequest> rtaRequests) {
        Map<String, Map<String, List<Library>>> recordsByInstitution = new HashMap<>();
        Map<String, List<Library>> records;
        // TODO Check if a Set of libraries can be used instead of a List.
        List<Library> recordsLibraries;

        for (RTARequest request : rtaRequests) {
            List<Library> libraries = request.getLibraries();
            // Quick check of libraries because we don't know if Primo do it.
            if (libraries == null) {
                logger.warn("No library is set in the rta request for primoRecordId ["
                        + request.getPrimoRecordId() + "].");
                continue;
            }
            // Quick check of record ids because we don't know if Primo do it.
            String recordIdentifier = request.getRecordIdentifier();
            if (recordIdentifier == null || recordIdentifier.isEmpty()) {
                logger.warn("The record primoRecordId [" + request.getPrimoRecordId()
                        + "] has no record identifier.");
                continue;
            }

            for (Library library : libraries) {
                String rtaBaseUrl = library.getRtaBaseURL();
                // Quick check of base url because we don't know if Primo do it.
                if (rtaBaseUrl == null || rtaBaseUrl.isEmpty()) {
                    logger.warn("RTA base url is not defined in the request for library ["
                            + library.getLibraryCode() + "] and primoRecordId ["
                            + request.getPrimoRecordId() + "].");
                    continue;
                }

                // Store the record for the library for the institution.
                if (!recordsByInstitution.containsKey(rtaBaseUrl)) {
                    records = new HashMap<String, List<Library>>();
                    recordsByInstitution.put(rtaBaseUrl, records);
                }
                else {
                    records = recordsByInstitution.get(rtaBaseUrl);
                }

                if (!records.containsKey(recordIdentifier)) {
                    recordsLibraries = new ArrayList<Library>();
                    records.put(recordIdentifier, recordsLibraries);
                }
                else {
                    recordsLibraries = records.get(recordIdentifier);
                }
                recordsLibraries.add(library);
            }
        }

        return recordsByInstitution;
    }

    /* Specific methods for the xml standard ILS-DI */

    /**
     * Calls ILS-DI service "GetAvailability", passing it the record ids and
     * receiving back information for each library of the institution.
     *
     * The standard ILS-DI allows to pass multiple record ids by query.
     *
     * @param recordIds
     *            The list of unique identifiers of the records to check on the
     *            remote system.
     * @return Availability of each record (record id -> holding status).
     */
    private Map<String, HoldingStatus> checkAvailabilityForRecords(Set<String> recordIds, String rtaBaseUrl) {
        // Build the query.
        // TODO Use a query builder.
        // URLEncoder is currently useless because args are controlled during
        // init and simple alphanumeric Ascii. The full url is re-checked below
        // too.
        String pUrl = rtaBaseUrl
                + "?service=" + Service.GET_AVAILABILITY
                + "&id=" + joinList(recordIds, "+")
                + "&id_type=" + idType;

        if (!returnType.equals("bib")) {
            pUrl += "&return_type=" + returnType;
        }

        // Currently, only accept DLF SimpleAvailability xml format.
        if (!returnFmt.isEmpty()) {
            pUrl += "&return_fmt=" + returnFmt;
        }

        if (debug) {
            logger.info("Request for [" + idType + "] records " + recordIds + " for response at [" + returnType
                    + "] level via [" + pUrl + "].");
        }

        // Quick way to secure the url because it is created with a builder.
        pUrl = checkUrl(pUrl);
        if (pUrl == null) {
            return null;
        }

        String output = httpGet(pUrl);

        if (debug) {
            logger.info("Received response: " + output);
        }

        // Convert the result and return it.
        // Currently, only accept DLF SimpleAvailability xml format.
        return extractSimpleAvailability(output);
    }

    /**
     * Helper to convert returned string as xml standard of DLF for ILS-DI into
     * a java map, that can manage bib and item level.
     *
     * @internal Currently, process is done via a simple XPath and not via a
     *           StAX model, because ILS-DI responses with the default format
     *           (SimpleAvailability) are light.
     *
     * @see http://diglib.org/ilsdi/1.1
     *
     * @param xmlString
     * @return Extracted availability.
     */
    private Map<String, HoldingStatus> extractSimpleAvailability(String output) {

        Map<String, HoldingStatus> results = new HashMap<>();

        // TODO Institution is not used, because the query is done for one
        // institution only.

        Document doc = convertStringToXml(output);
        if (doc == null) {
            logger.warn("Cannot process response as XML: " + output);
            return null;
        }

        NodeList records;
        Node record;
        String recordId;
        String status;
        HoldingStatus statusPrimo;
        int countItems, countAvailable, countUnavailable;
        try {
            // Prepare process for each record.
            records = (NodeList) exprRecordsList.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0, num = records.getLength(); i < num; i++) {
                record = records.item(i);

                switch (idType) {
                    case "bib":
                        switch (returnType) {
                            case "bib":
                                recordId = (String) exprRecordId.evaluate(record, XPathConstants.STRING);
                                status = (String) exprAvailability.evaluate(record, XPathConstants.STRING);
                                statusPrimo = AvailabilityILSDI2Primo.convert(status);
                                if (statusPrimo == null) {
                                    throw new Exception("Response contains an unknown status for bib: " + status + ".");
                                }
                                results.put(recordId, statusPrimo);
                                break;

                            case "item":
                                recordId = (String) exprRecordId.evaluate(record, XPathConstants.STRING);
                                // If one is available, returns available. If
                                // all are not available, returns unavailable;
                                // else returns check holdings.
                                countItems = ((Double) exprCountItems.evaluate(record, XPathConstants.NUMBER))
                                        .intValue();
                                countAvailable =
                                        ((Double) exprCountItemsAvailable.evaluate(record, XPathConstants.NUMBER))
                                                .intValue();
                                if (countAvailable > 0) {
                                    statusPrimo = HoldingStatus.AVAILABLE;
                                }
                                // None available.
                                // TODO Add a response check as for bib?
                                else {
                                    countUnavailable =
                                            ((Double) exprCountItemsUnavailable.evaluate(record, XPathConstants.NUMBER))
                                                    .intValue();
                                    statusPrimo = (countUnavailable == countItems)
                                            ? HoldingStatus.UNAVAILABLE
                                            : HoldingStatus.CHECK_HOLDINGS;
                                }
                                results.put(recordId, statusPrimo);
                                break;
                        }
                        break;

                    case "item":
                        switch (returnType) {
                            case "bib":
                                throw new Exception(
                                        "To use [item] as IdType and [bib] as ReturnType is currently unsupported.");

                            case "item":
                                NodeList items;
                                Node item;
                                String itemId;
                                // In ILS-DI, the location may not be the
                                // library name, so mapping may be difficult.
                                // Furthermore, the location is not a required
                                // element. So we can't check it against Primo
                                // list.
                                items = (NodeList) exprItemsList.evaluate(record, XPathConstants.NODESET);
                                countItems = items.getLength();
                                for (int j = 0; j < countItems; j++) {
                                    item = items.item(j);
                                    itemId = (String) exprItemId.evaluate(item, XPathConstants.STRING);
                                    status = (String) exprAvailability.evaluate(item, XPathConstants.STRING);
                                    statusPrimo = AvailabilityILSDI2Primo.convert(status.trim());
                                    if (statusPrimo == null) {
                                        throw new Exception("Response contains an unknown status for item: " + status
                                                + ".");
                                    }
                                    results.put(itemId, statusPrimo);
                                }
                                break;
                        }
                        break;
                }
            }
        } catch (XPathExpressionException xpe) {
            logger.warn("Cannot process records of the response: " + xpe.getMessage(), xpe);
            return null;
        } catch (Exception e) {
            logger.warn("Cannot process response: " + e.getMessage(), e);
            return null;
        }

        return results;
    }

    /** Some tools and helpers needed because there is no access to Primo Api */

    /**
     * Join elements of a collection, set or list of strings with a delimiter.
     *
     * TODO Use Apache commons?
     *
     * @param str
     * @param delimiter
     * @return
     */
    private String joinList(Collection<String> str, String delimiter) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        // Add the delimiter before the string, except for the first.
        String delim = "";
        for (String s : str) {
            builder.append(delim).append(s);
            delim = delimiter;
        }
        return builder.toString();
    }

    /**
     * Provides a quick way to secure the format of a url.
     *
     * @param pUrl
     * @return Checked and cleaned url.
     */
    private String checkUrl(String pUrl) {
        URL url;
        try {
            url = new URL(pUrl);
        } catch (MalformedURLException e) {
            logger.error("Request url [" + pUrl + "] is malformed: " + e.getMessage(), e);
            return null;
        }

        URI uri;
        try {
            uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
                    url.getPort(), url.getPath(), url.getQuery(), url.getRef());
        } catch (URISyntaxException e) {
            logger.error("Request URI is malformed: " + e.getMessage(), e);
            return null;
        }

        return uri.toASCIIString();
    }

    /**
     * Get content from a remote system via http.
     *
     * @param pUrl
     *            The url to fetch.
     * @return Content get from the remote system.
     */
    private String httpGet(String pUrl) {
        String output;

        // TODO Because the Primo API is closed and proprietary, we don't know
        // if we should throw an error or catch it.
        // TODO For the same reason, we don't know which process to use to get
        // remote content, so a very basic REST client is build.
        // TODO Use or reset http.keepAlive? During init?
        // TODO Use java.nio2

        URL url;
        try {
            url = new URL(pUrl);
        } catch (MalformedURLException e) {
            logger.error("Request url [" + pUrl + "] is malformed: " + e.getMessage(), e);
            return null;
        }

        // Secured https url can be used, because we don't use specific method
        // of HttpsURLConnection.
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(connectionTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestMethod("GET");
            // Currently, only accept DLF SimpleAvailability xml format.
            switch (returnFmt) {
                default:
                    connection.setRequestProperty("Accept", "text/xml");
            }
        } catch (IOException e) {
            logger.error("Cannot connect to remote system: " + e.getMessage(), e);
            if (connection != null) {
                connection.disconnect();
            }
            return null;
        }

        try {
            if (connection.getResponseCode() != 200) {
                logger.warn("Failed to receive correct response. HTTP error code: " + connection.getResponseCode());
                // throw new RuntimeException("Failed : HTTP error code: "
                // + conn.getResponseCode());
                return null;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            output = sb.toString();
        } catch (IOException e) {
            logger.warn("Failed when fetching response: " + e.getMessage(), e);
            return null;
        } finally {
            connection.disconnect();
        }

        return output;
    }

    /**
     * Check and convert a string into an xml document.
     *
     * @param xmlString
     * @return XML Document.
     */
    private Document convertStringToXml(String xmlString) {
        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document;
        try {
            xmlFactory.setNamespaceAware(true);
            builder = xmlFactory.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (ParserConfigurationException pce) {
            if (debug) {
                logger.error("Error in Parser Configuration: " + pce.getMessage(), pce);
            }
            return null;
        } catch (SAXException se) {
            if (debug) {
                logger.error("Error in SAX: " + se.getMessage(), se);
            }
            return null;
        } catch (IOException ioe) {
            if (debug) {
                logger.error("Error In/Out: " + ioe.getMessage(), ioe);
            }
            return null;
        }
        return document;
    }
}

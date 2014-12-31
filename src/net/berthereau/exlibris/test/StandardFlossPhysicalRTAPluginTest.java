package net.berthereau.exlibris.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.berthereau.exlibris.StandardFlossPhysicalRTAPlugin;

import org.junit.Before;
import org.junit.Test;

import com.exlibris.primo.api.common.IPrimoLogger;
import com.exlibris.primo.api.plugins.rta.HoldingStatus;
import com.exlibris.primo.api.plugins.rta.Library;
import com.exlibris.primo.api.plugins.rta.RTARequest;

/**
 * Allows to test the plugin without the proprietary API nor access to Primo.
 * 
 * Currently, tests use files located at "http://localhost/exlibris/test/". This
 * can be changed below.
 */
public class StandardFlossPhysicalRTAPluginTest {
    private String localServer = "http://localhost/exlibris/test/";

    private StandardFlossPhysicalRTAPlugin rta = new StandardFlossPhysicalRTAPlugin();

    // Set simplified classes used for testing purpose without access to Primo.
    private IPrimoLogger logger = new PrimoLoggerTestUse();
    private MappingTablesFetcherTestUse mtFetcher = new MappingTablesFetcherTestUse();
    private Map<String, Object> params = new Hashtable<>();

    @Before
    public void setUp() throws Exception {
        // TODO Load this config from a file.
        // Set some tables parameters. These data are not used by the plugin.
        Map<String, List<Map<String, String>>> codes = new HashMap<>();
        List<Map<String, String>> results = new ArrayList<>();
        Map<String, String> m = new HashMap<>();
        // Institution codes.
        m.put("ILS Institution", "North Carolina State University");
        m.put("Primo Institution", "NCSU");
        results.add(m);
        codes.put("ILS Institution Codes", results);
        // Libraries codes.
        results = new ArrayList<>();
        m = new HashMap<>();
        m.put("Library Code", ">D. H. Hill Library");
        m.put("Primo Code", "DHHL");
        results.add(m);
        m = new HashMap<>();
        m.put("Library Code", "Natural Resources Library");
        m.put("Primo Code", "NRL");
        results.add(m);

        // Setup the test MappingTableFetcher.
        codes.put("ILS Library Codes", results);
        mtFetcher.setUp(codes);

        // Set plugins parameters (as string, as set in the admin interface).
        params.put("SourceSystem", "ILS-DI");
        params.put("IdentifierXpath", "record/control/sourcerecordid");
        params.put("ConnectionTimeout", "500");
        params.put("ReadTimeout", "500");
        params.put("Debug", "true");
    }

    @Test
    public void init() {
        // This allows to check init (this is reset just after the test).
        PrintStream stdout = System.out;
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        rta.init(logger, mtFetcher, params);

        // Reset to standard output.
        System.setOut(stdout);
        System.out.print(outContent);

        assertEquals("Logger Info: Plugin " + rta.getClass() + " is initialized.\n", outContent.toString());
    }

    @Test
    public void updateAvailabilityBibBib() {
        params.put("IdType", "bib");
        params.put("ReturnType", "bib");
        rta.init(logger, mtFetcher, params);

        List<RTARequest> rtaRequests = new ArrayList<>();
        RTARequest rtaRequest = new RTARequest();
        prepareRtaRequest(rtaRequest, "92005291", "bib");
        rtaRequests.add(rtaRequest);

        rta.updateAvailability(rtaRequests);
        assertEquals(HoldingStatus.AVAILABLE, rtaRequest.getLibraries().get(0).getHoldingStatus());
    }

    @Test
    public void updateAvailabilityItemItem() {
        params.put("IdType", "item");
        params.put("ReturnType", "item");
        rta.init(logger, mtFetcher, params);

        List<RTARequest> rtaRequests = new ArrayList<>();
        RTARequest rtaRequest = new RTARequest();
        prepareRtaRequest(rtaRequest, "S01149512N", "item");
        rtaRequests.add(rtaRequest);

        rta.updateAvailability(rtaRequests);
        assertEquals(HoldingStatus.AVAILABLE, rtaRequest.getLibraries().get(0).getHoldingStatus());
    }

    @Test
    public void updateAvailabilityBibItem() {
        params.put("IdType", "bib");
        params.put("ReturnType", "item");
        rta.init(logger, mtFetcher, params);

        List<RTARequest> rtaRequests = new ArrayList<>();
        RTARequest rtaRequest = new RTARequest();
        prepareRtaRequest(rtaRequest, "92005291", "item");
        rtaRequests.add(rtaRequest);

        rta.updateAvailability(rtaRequests);
        assertEquals(HoldingStatus.AVAILABLE, rtaRequest.getLibraries().get(0).getHoldingStatus());
    }

    // To use item / bib is currently unsupported.
    // @Test
    public void updateAvailabilityItemBib() {
        params.put("IdType", "item");
        params.put("ReturnType", "bib");
        rta.init(logger, mtFetcher, params);

        List<RTARequest> rtaRequests = new ArrayList<>();
        RTARequest rtaRequest = new RTARequest();
        prepareRtaRequest(rtaRequest, "S01149512N", "bib");
        rtaRequests.add(rtaRequest);

        rta.updateAvailability(rtaRequests);
        assertEquals(HoldingStatus.AVAILABLE, rtaRequest.getLibraries().get(0).getHoldingStatus());
    }

    @Test
    public void updateAvailabilityItemMultiple() {
        params.put("ReturnType", "bib");
        params.put("ReturnType", "item");
        rta.init(logger, mtFetcher, params);

        List<RTARequest> rtaRequests = new ArrayList<>();
        RTARequest rtaRequest_1 = new RTARequest();
        prepareRtaRequest(rtaRequest_1, "1", "item");
        rtaRequests.add(rtaRequest_1);
        RTARequest rtaRequest_2 = new RTARequest();
        prepareRtaRequest(rtaRequest_2, "2", "item");
        rtaRequests.add(rtaRequest_2);
        RTARequest rtaRequest_99999 = new RTARequest();
        prepareRtaRequest(rtaRequest_99999, "99999", "item");
        rtaRequests.add(rtaRequest_99999);

        rta.updateAvailability(rtaRequests);
        assertEquals(HoldingStatus.AVAILABLE, rtaRequest_1.getLibraries().get(0).getHoldingStatus());
        assertEquals(HoldingStatus.AVAILABLE, rtaRequest_2.getLibraries().get(0).getHoldingStatus());
        assertEquals(HoldingStatus.CHECK_HOLDINGS, rtaRequest_99999.getLibraries().get(0).getHoldingStatus());
    }

    @Test
    public void updateAvailabilityMultipleLibraries() {
        params.put("ReturnType", "bib");
        params.put("ReturnType", "item");
        rta.init(logger, mtFetcher, params);

        List<RTARequest> rtaRequests = new ArrayList<>();
        RTARequest rtaRequest = new RTARequest();
        prepareRtaRequest(rtaRequest, "92005291", "item");
        rtaRequests.add(rtaRequest);
        RTARequest rtaRequest_1 = new RTARequest();
        prepareRtaRequest(rtaRequest_1, "1", "item");
        rtaRequests.add(rtaRequest_1);
        RTARequest rtaRequest_2 = new RTARequest();
        prepareRtaRequest(rtaRequest_2, "2", "item");
        rtaRequests.add(rtaRequest_2);
        RTARequest rtaRequest_99999 = new RTARequest();
        prepareRtaRequest(rtaRequest_99999, "99999", "item");
        rtaRequests.add(rtaRequest_99999);

        rta.updateAvailability(rtaRequests);
        assertEquals(HoldingStatus.AVAILABLE, rtaRequest.getLibraries().get(0).getHoldingStatus());
        assertEquals(HoldingStatus.AVAILABLE, rtaRequest_1.getLibraries().get(0).getHoldingStatus());
        assertEquals(HoldingStatus.AVAILABLE, rtaRequest_2.getLibraries().get(0).getHoldingStatus());
        assertEquals(HoldingStatus.CHECK_HOLDINGS, rtaRequest_99999.getLibraries().get(0).getHoldingStatus());
    }

    /**
     * Helper to get a rta request.
     * 
     * @param rtaRequest
     *            The request to update.
     * @param recordIdentifier
     *            The identifier of the record to check.
     * @param returnType
     *            "bib" or "item" (used only for some records).
     */
    private void prepareRtaRequest(RTARequest rtaRequest, String recordIdentifier, String returnType) {
        List<Library> libraries = new ArrayList<>();

        Library library = new Library();
        // These data are not used by the plugin.
        library.setInstitution("NCSU");
        library.setLibraryCode("DHHL");
        library.setCollection("Heritage");
        library.setCallNumber("QA241 .S53 2007");
        library.setHoldingStatus(HoldingStatus.CHECK_HOLDINGS);

        switch (recordIdentifier) {
        // Sample from DLF_ILS_Discovery_1.1-Dec8, Appendix 4.
            case "92005291":
                if (returnType.equals("bib"))
                    library.setRtaBaseURL(localServer + "record_92005291.bib.ilsdi.xml");
                else
                    library.setRtaBaseURL(localServer + "record_92005291.item.ilsdi.xml");
                rtaRequest.setPrimoRecordId("92005291primo");
                rtaRequest.setRecordIdentifier("92005291");
                break;

            // Response is part of a multiple.
            case "S01149512N":
                if (returnType.equals("bib"))
                    library.setRtaBaseURL(localServer + "record_92005291.bib.ilsdi.xml");
                else
                    library.setRtaBaseURL(localServer + "record_92005291.item.ilsdi.xml");
                rtaRequest.setPrimoRecordId("S01149512Nprimo");
                rtaRequest.setRecordIdentifier("S01149512N");
                break;

            // For 1, 2 and 99999, sample from
            // http://rocks.mines-paristech.fr/cgi-bin/koha/ilsdi.pl?service=Describe&verb=GetAvailability
            // Response is part of a multiple.
            case "1":
                library.setRtaBaseURL(localServer + "records_1-2-99999.item.ilsdi.xml");
                rtaRequest.setPrimoRecordId("1primo");
                rtaRequest.setRecordIdentifier("1");
                break;

            // Response is part of a multiple.
            case "2":
                library.setRtaBaseURL(localServer + "records_1-2-99999.item.ilsdi.xml");
                rtaRequest.setPrimoRecordId("2primo");
                rtaRequest.setRecordIdentifier("2");
                break;

            // Response is part of a multiple.
            case "99999":
                library.setHoldingStatus(HoldingStatus.UNAVAILABLE);
                library.setRtaBaseURL(localServer + "records_1-2-99999.item.ilsdi.xml");
                rtaRequest.setPrimoRecordId("99999primo");
                rtaRequest.setRecordIdentifier("99999");
                break;
        }

        libraries.add(library);
        rtaRequest.setLibraries(libraries);
    }
}

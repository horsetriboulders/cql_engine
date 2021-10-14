package org.opencds.cqf.cql.engine.fhir.retrieve;

import static ca.uhn.fhir.util.UrlUtil.escapeUrlParam;
import static org.testng.Assert.assertEquals;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import org.hl7.fhir.r4.model.Patient;
import org.opencds.cqf.cql.engine.fhir.R4FhirTest;
import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterMap;
import org.opencds.cqf.cql.engine.fhir.searchparam.SearchParameterResolver;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.cql.engine.runtime.DateTime;
import org.opencds.cqf.cql.engine.runtime.Interval;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;

public class TestRestFhirRetrieveProvider extends R4FhirTest {

    static SearchParameterResolver RESOLVER;
    static IGenericClient CLIENT;

    RestFhirRetrieveProvider provider;

    @BeforeClass
    public void setUpBeforeClass() {
        CLIENT= newClient();
        RESOLVER = new SearchParameterResolver(CLIENT.getFhirContext());
    }

    @BeforeMethod
    public void setUp() {
        this.provider = new RestFhirRetrieveProvider(RESOLVER, CLIENT);
    }


    @Test
    public void noUserSpecifiedPageSizeUsesDefault() {
        SearchParameterMap map = provider.getFhirQueryGenerator().getBaseMap(null, null, null);
        assertEquals( map.getCount(), null );
    }

    @Test
    public void userSpecifiedPageSizeIsUsed() {
        Integer expected = 100;
        provider.getFhirQueryGenerator().setPageSize(expected);
        SearchParameterMap map = provider.getFhirQueryGenerator().getBaseMap(null, null, null);
        assertEquals( map.getCount(), expected );
    }

    @Test
    public void userSpecifiedPageSizeIsUsedWhenCodeBasedQuery() {
        Code code = new Code().withSystem("http://mysystem.com").withCode("mycode");
        List<Code> codes = Arrays.asList( code );

        mockFhirSearch("/Condition?code=" + escapeUrlParam(code.getSystem() + "|" + code.getCode()) + "&subject=" + escapeUrlParam("Patient/123") + "&_count=500");

        provider.getFhirQueryGenerator().setPageSize(500);
        provider.retrieve("Patient", "subject", "123", "Condition", null, "code", codes, null, null, null, null, null);
    }

    @Test
    public void userSpecifiedPageSizeIsUsedWhenValueSetQuery() {

        String valueSetUrl = "http://myterm.com/fhir/ValueSet/MyValueSet";

        mockFhirSearch("/Condition?code" + escapeUrlParam(":") + "in=" + escapeUrlParam(valueSetUrl) + "&subject=" + escapeUrlParam("Patient/123") + "&_count=500");

        provider.getFhirQueryGenerator().setPageSize(500);
        provider.retrieve("Patient", "subject", "123", "Condition", "http://hl7.org/fhir/StructureDefinition/Condition", "code", null, valueSetUrl, null, null, null, null);
    }

    @Test
    public void userSpecifiedPageSizeIsUsedWhenDateQuery() {
       /**
        * As best as I can tell, the date range optimized queries are
        * broken right now. See https://github.com/DBCG/cql_engine/issues/467.
        */

        OffsetDateTime start = OffsetDateTime.of(2020, 11, 12, 1, 2, 3, 0, ZoneOffset.UTC);
        OffsetDateTime end = start.plusYears(1);

        Interval interval = new Interval(new DateTime(start), true, new DateTime(end), false);

        // The dates will be rendered in the URL in the build machine's local
        // time zone, so the URL value might fluctuate from one environment to another.
        // We could try to match that formatting logic to get an exact URL, but it isn't
        // necessary for what we are trying to achieve here, so we just accept any date
        // string.
        mockFhirInteraction(get(urlMatching("/Condition\\?subject=" + escapeUrlParam("Patient/123") +
                "&onset-date=ge2020[^&]+&onset-date=le[^&]+" +
                "&_count=500")), makeBundle());

        provider.getFhirQueryGenerator().setPageSize(500);
        provider.retrieve("Patient", "subject", "123", "Condition", null, "code", null, null, "onset", null, null, interval);
    }

    @Test
    public void userSpecifiedPageSizeNotUsedWhenIDQuery() {
        mockFhirRead("/Patient/123", new Patient());

        provider.getFhirQueryGenerator().setPageSize(500);
        provider.retrieve("Patient", "id", "123", "Patient", "http://hl7.org/fhir/StructureDefinition/Patient", null, null, null, null, null, null, null);
    }

    @Test
    public void noUserSpecifiedPageSizeSpecifiedNoCountInURL() {
        Code code = new Code().withSystem("http://mysystem.com").withCode("mycode");
        List<Code> codes = Arrays.asList( code );

        mockFhirSearch("/Condition?code=" + escapeUrlParam(code.getSystem() + "|" + code.getCode()) + "&subject=" + escapeUrlParam("Patient/123"));

        provider.retrieve("Patient", "subject", "123", "Condition", null, "code", codes, null, null, null, null, null);
    }
}

package components;

import controllers.routes;
import models.CompanySummary;
import models.CompanySummaryWithAddress;

import java.io.IOException;

public class MockCompaniesHouseCommunicator implements CompaniesHouseCommunicator {
    private CompaniesHouseCommunicator inner = new ApiCompaniesHouseCommunicator(new HttpWrapper());

    @Override
    public String getAuthorizationUri(String callbackUri, String companiesHouseIdentifier) {
        return routes.CompaniesHouseMock.getPage1(companiesHouseIdentifier).url();
    }

    @Override
    public PagedList<CompanySummaryWithAddress> searchCompanies(String search, int page, int itemsPerPage) throws IOException {
        return inner.searchCompanies(search,page,itemsPerPage);
    }

    @Override
    public String verifyAuthCode(String authCode, String redirectUri, String companiesHouseIdentifier) throws IOException {
        return "ok";
    }

    @Override
    public RefreshTokenAndValue<Boolean> isInScope(String companiesHouseIdentifier, String oAuthToken) throws IOException {
        return new RefreshTokenAndValue<>(oAuthToken, true);
    }

    @Override
    public RefreshTokenAndValue<String> getEmailAddress(String token) throws IOException {
        return new RefreshTokenAndValue<>(token, "daniel.rothig@digital.beis.gov.uk");
    }

    @Override
    public CompanySummary getCompany(String s) throws IOException {
        return inner.getCompany(s);
    }
}

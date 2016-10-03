package components;

import models.CompanyModel;
import models.CompanySummary;
import models.ReportModel;
import models.ReportSummary;
import play.db.Database;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel.rothig on 03/10/2016.
 *
 * Database-backed repository. Currently the constructor injects some fake data
 */
final class JdbcReportsRepository implements ReportsRepository {

    private Database db;

    @Inject
    public JdbcReportsRepository(Database db) {
        this.db = db;
        InitialiseSchema();

    }

    private Connection InitialiseSchema() {
        Connection conn = null;
        try {
            conn = db.getConnection();
            conn.createStatement().execute("DROP TABLE Company; DROP TABLE Report;");
        }
        catch(Exception e)
        {
            play.Logger.info("Didn't drop tables.");
        }

        try {
            conn = db.getConnection();
            play.Logger.debug("Creating schema...");
            conn.createStatement().execute(
                    "CREATE TABLE Company(CompaniesHouseIdentifier nvarchar(30), Name nvarchar(256));" +
                    "CREATE TABLE Report(Identifier INTEGER, CompaniesHouseIdentifier nvarchar(30), FilingDate date);" +

                    "INSERT INTO Company(Name, CompaniesHouseIdentifier) VALUES ('Tesco', '120');" +
                    "INSERT INTO Company(Name, CompaniesHouseIdentifier) VALUES ('Costa', '121');" +
                    "INSERT INTO Company(Name, CompaniesHouseIdentifier) VALUES ('Eigencode Ltd.', '122');" +

                    "INSERT INTO Report(Identifier, CompaniesHouseIdentifier, FilingDate) VALUES (1, '120', '2015-02-01');" +
                    "INSERT INTO Report(Identifier, CompaniesHouseIdentifier, FilingDate) VALUES (2, '120', '2015-08-01');" +
                    "INSERT INTO Report(Identifier, CompaniesHouseIdentifier, FilingDate) VALUES (3, '120', '2016-02-01');" +
                    "INSERT INTO Report(Identifier, CompaniesHouseIdentifier, FilingDate) VALUES (1, '121', '2015-07-01');" +
                    "INSERT INTO Report(Identifier, CompaniesHouseIdentifier, FilingDate) VALUES (2, '121', '2016-01-01');" +
                    "INSERT INTO Report(Identifier, CompaniesHouseIdentifier, FilingDate) VALUES (1, '122', '2016-05-01');"
            );

            play.Logger.debug("Schema created, rowcount of reports:");
            ResultSet resultSet = conn.createStatement().executeQuery("SELECT * FROM Company WHERE CompaniesHouseIdentifier = '120'");
            while (resultSet.next()) {
                play.Logger.debug("" + resultSet.getString(1) + " " + resultSet.getString(2));
            }
        }
        catch(Exception e) {
            play.Logger.error("Cant build schema", e);
        }
        return conn;
    }

    @Override
    public List<CompanySummary> searchCompanies(String company) {
        return ExecuteQuery(
                "SELECT Name, CompaniesHouseIdentifier FROM Company WHERE LOWER(Name) LIKE LOWER(?)",
                new String[] { "%"+company+"%"},
                _CompanySummaryMapper);
    }

    @Override
    public CompanyModel getCompanyByCompaniesHouseIdentifier(String identifier) {
        CompanySummary summary = GetCompanySummaryByIdentifier(identifier);

        List<ReportSummary> reports = ExecuteQuery(
                "SELECT Identifier, FilingDate FROM Report WHERE CompaniesHouseIdentifier = ?",
                new String[]{identifier},
                _ReportSummaryMapper);

        return new CompanyModel(summary, reports);
    }

    @Override
    public ReportModel getReport(String company, int reportId) {
        CompanySummary companySummary = GetCompanySummaryByIdentifier(company);

        ReportSummary report = ExecuteQuery(
                "SELECT TOP 1 Identifier, FilingDate FROM Report WHERE CompaniesHouseIdentifier = ? AND Identifier = ?",
                new Object[]{company, reportId},
                _ReportSummaryMapper).get(0);

        return new ReportModel(report, companySummary);
    }

    private CompanySummary GetCompanySummaryByIdentifier(String identifier) {
        return ExecuteQuery(
                "SELECT TOP 1 Name, CompaniesHouseIdentifier FROM Company WHERE CompaniesHouseIdentifier = ?",
                new String[]{identifier},
                _CompanySummaryMapper).get(0);
    }

    private <T> List<T> ExecuteQuery(String sql, Object[] parameters, Mapper<T> mapper) {
        Connection conn;
        try {
            conn = db.getConnection();
            PreparedStatement statement = conn.prepareStatement(sql);
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            ResultSet results = statement.executeQuery();
            List<T> rtn = new ArrayList<>();
            while (results.next()) {
                rtn.add(mapper.map(results));
            }

            conn.close();
            return rtn;
        }
        catch(Exception e) {
            play.Logger.error("Can't execute query", e);
            return new ArrayList<>();
        }
    }

    private interface Mapper<T> {
        T map(ResultSet results) throws SQLException;
    }

    private Mapper<CompanySummary> _CompanySummaryMapper =
            results -> new CompanySummary(results.getString(1), results.getString(2));

    private Mapper<ReportSummary> _ReportSummaryMapper =
            results -> new ReportSummary(results.getInt(1), results.getDate(2));
}
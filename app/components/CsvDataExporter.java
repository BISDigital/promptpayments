package components;

import com.google.inject.Inject;
import models.CompanySummary;
import models.ReportModel;
import play.libs.F;
import utils.TimeProvider;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by daniel.rothig on 04/10/2016.
 *
 * Creates a CSV string based on a data export
 */
public class CsvDataExporter {

    private final TimeProvider timeProvider;
    private final ReportsRepository reportsRepository;

    @SuppressWarnings("FieldCanBeLocal")
    private static String header="Filing date,Company,Company number, Number one, Number two, Number three\n";

    private static final int cacheMinutes = 30;

    private String cachedCsv;
    private Calendar lastCached;

    @Inject
    CsvDataExporter(ReportsRepository reportsRepository, TimeProvider timeProvider) {
        this.reportsRepository = reportsRepository;
        this.timeProvider = timeProvider;
        cachedCsv = null;
        lastCached = new GregorianCalendar();
        lastCached.setTimeInMillis(0);
    }

    public String GenerateCsv() {
        if (timeProvider.Now().getTimeInMillis() - lastCached.getTimeInMillis() < cacheMinutes * 60000 && cachedCsv != null) {
            return cachedCsv;
        }

        List<F.Tuple<CompanySummary, ReportModel>> data = reportsRepository.ExportData(24);

        Stream<String> stringStream = data.stream().map(x -> String.join(",",
                escape(x._2.Info.UiDateString()),
                escape(x._1.Name),
                escape(x._1.CompaniesHouseIdentifier),
                escape(x._2.NumberOne),
                escape(x._2.NumberTwo),
                escape(x._2.NumberThree)));

        lastCached = timeProvider.Now();
        cachedCsv = header + String.join("\n", stringStream.collect(Collectors.toList()));

        return cachedCsv;
    }

    private String escape(Object obj) {
        String raw = obj.toString();
        if (raw.contains(",")) {
            if (raw.contains("\"")) {
                raw = raw.replace("\"", "\"\"");
            }
            raw = "\"" + raw + "\"";
        }
        return raw;

    }
}

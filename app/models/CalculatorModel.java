package models;

import org.assertj.core.util.Lists;
import utils.UtcConverter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Takes a start and end date of a financial year and determines the correct reporting periods for that year.
 *
 * If a year prior to 6 April 2017 is specified, the reporting periods for the first year that starts on or after 6 April 2017 is provided instead. In that case, showsFuture() returns true.
 */
public class CalculatorModel{
    public final String startYear;
    public final String startMonth;
    public final String startDay;
    public final String endYear;
    public final String endMonth;
    public final String endDay;

    private static final Calendar cutoff = UtcConverter.tryMakeUtcDate("2017", "4", "6");

    public CalculatorModel() {
        this.startYear = null;
        this.startMonth = null;
        this.startDay = null;
        this.endYear = null;
        this.endMonth = null;
        this.endDay = null;
    }

    public CalculatorModel(String startYear, String startMonth, String startDay, String endYear, String endMonth, String endDay)  {
        this.startYear = startYear;
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.endYear = endYear;
        this.endMonth = endMonth;
        this.endDay = endDay;
    }

    public boolean isValid() {
        if (isEmpty()) return true;
        Calendar startDate = UtcConverter.tryMakeUtcDate(startYear, startMonth, startDay);
        Calendar endDate = UtcConverter.tryMakeUtcDate(endYear, endMonth, endDay);

        return startDate != null && endDate != null && startDate.getTime().getTime() < endDate.getTime().getTime();
    }

    public String getStartDate() {return new UiDate(UtcConverter.tryMakeUtcDate(startYear, startMonth, startDay)).ToDateString(); }
    public String getEndDate() {return new UiDate(UtcConverter.tryMakeUtcDate(endYear, endMonth, endDay)).ToDateString(); }

    public boolean showsFuture() {
        if (!isValid()) return false;

        Calendar startDate = UtcConverter.tryMakeUtcDate(startYear,startMonth,startDay);
        return startDate.getTime().getTime() - cutoff.getTime().getTime() < -100;
    }

    public boolean isEmpty() {
        return nullOrEmpty(startYear) && nullOrEmpty(startMonth) && nullOrEmpty(startDay)
                && nullOrEmpty(endYear) && nullOrEmpty(endMonth) && nullOrEmpty(endDay);
    }
    private boolean nullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public List<ReportingPeriod> getReportingPeriods() {
        Calendar startDate = UtcConverter.tryMakeUtcDate(startYear, startMonth, startDay);
        Calendar endDate = UtcConverter.tryMakeUtcDate(endYear, endMonth, endDay);

        if (!isValid()) {
            return Lists.emptyList();
        }

        if (showsFuture()) {
            int days = Math.round((endDate.getTimeInMillis() - startDate.getTimeInMillis())/ (1000 * 3600 * 24));
            do {
                startDate = (Calendar) endDate.clone();
                startDate.add(Calendar.DATE, 1);
                endDate.add(Calendar.YEAR, 1);
            } while(startDate.getTime().getTime() - cutoff.getTime().getTime() < -100);
        }

        int months = countMonths(startDate, endDate);
        if (months == -1) {
            return Lists.emptyList();
        }

        List<ReportingPeriod> res = new ArrayList<>();

        int startDateOffset = 0;
        while (months > 9 && res.size() < 2) {
            Calendar intermediaryStartDate = (Calendar) startDate.clone();
            intermediaryStartDate.add(Calendar.MONTH, startDateOffset);

            Calendar intermediaryEndDate = (Calendar) startDate.clone();
            intermediaryEndDate.add(Calendar.MONTH, startDateOffset + 6);
            intermediaryEndDate.add(Calendar.DATE, -1);

            res.add(new ReportingPeriod(intermediaryStartDate, intermediaryEndDate));

            startDateOffset += 6;
            months -=6;
        }

        Calendar finalStartDate = (Calendar) startDate.clone();
        finalStartDate.add(Calendar.MONTH, startDateOffset);

        Calendar finalEndDate = (Calendar) endDate.clone();

        res.add(new ReportingPeriod(finalStartDate, finalEndDate));
        return res;
    }

    private int countMonths(Calendar startDate, Calendar endDate) {
        for (int res = 1; res < 1000; res++) {
            Calendar c = (Calendar) startDate.clone();
            c.add(Calendar.DATE, -1);
            c.add(Calendar.MONTH, res);
            if (endDate.getTime().getTime() - c.getTime().getTime() < 100) return res;
        }
        return -1;
    }

    public class ReportingPeriod {
        public final UiDate StartDate;
        public final UiDate EndDate;
        public final UiDate FilingDeadline;

        public ReportingPeriod(Calendar startDate, Calendar endDate) {
            StartDate = new UiDate(startDate);
            EndDate = new UiDate(endDate);

            Calendar c = (Calendar) endDate.clone();
            c.add(Calendar.DATE, 30);
            FilingDeadline = new UiDate(c);
        }
    }
}

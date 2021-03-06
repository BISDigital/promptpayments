package models;

import java.security.InvalidParameterException;
import java.util.Calendar;
import java.util.TimeZone;



@SuppressWarnings({"WeakerAccess", "FieldCanBeLocal"})
public class ReportFilingModelValidationImpl implements ReportFilingModelValidation {
    private final ReportFilingModel model;
    private final Calendar utcNow;

    private final String message_required = "Please answer this question";
    private final String message_nonnegative = "This should be a non-negative number";
    private final String message_integer = "Please round up or down to the nearest whole number";
    private final String message_percentagebounds = "This should be a number between 0 and 100";
    private final String message_upperpercentagebounds = "This cannot exceed 100%";
    private final String message_sumto100 = "Figures A, B and C do not add up to 100";
    private final String message_invaliddate = "This date is invalid";
    private final String message_future = "Reporting period cannot cover the future";
    private final String message_startbeforeend = "The end date cannot be before the start date";

    public ReportFilingModelValidationImpl(ReportFilingModel model, Calendar utcNow) {
        this.model = model;
        if (!utcNow.getTimeZone().equals(TimeZone.getTimeZone("UTC"))) {
            throw new InvalidParameterException("utcNow is not UTC");
        }
        this.utcNow = utcNow;
    }

    @Override
    public boolean isValid() {
        return
                validateAverageTimeToPay().isOk() &&
                validatePercentInvoicesPaidBeyondAgreedTerms().isOk() &&
                validatePercentInvoicesWithin30Days().isOk() &&
                validatePercentInvoicesWithin60Days().isOk() &&
                validatePercentInvoicesBeyond60Days().isOk() &&
                validateTimePercentages().isOk() &&
                validateStartDate().isOk() &&
                validateEndDate().isOk() &&
                validatePaymentTerms().isOk() &&
                validateMaximumContractPeriod().isOk() &&
                validatePaymentTermsChanged().isOk() &&
                validatePaymentTermsChangedComment().isOk() &&
                validatePaymentTermsChangedNotified().isOk() &&
                validatePaymentTermsChangedNotifiedComment().isOk() &&
                validatePaymentTermsComment().isOk() &&
                validateDisputeResolution().isOk() &&
                validateHasPaymentCodes().isOk() &&
                validatePaymentCodes().isOk() &&
                validateOfferEInvoicing().isOk() &&
                validateOfferSupplyChainFinance().isOk() &&
                validateRetentionChargesInPast().isOk() &&
                validateRetentionChargesInPolicy().isOk();
    }

    @Override
    public FieldValidation validateAverageTimeToPay() {
        if (model.getAverageTimeToPay() == null || model.getAverageTimeToPay().equals("")) return FieldValidation.fail(message_required);
        if (model.getAverageTimeToPayAsDecimal() == null) return FieldValidation.fail(message_nonnegative);
        if (model.getAverageTimeToPayAsDecimal().doubleValue() < 0) return FieldValidation.fail(message_nonnegative);
        if (isNotWholeNumber(model.getAverageTimeToPay())) return FieldValidation.fail(message_integer);
        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validatePercentInvoicesPaidBeyondAgreedTerms() {
        if (model.getPercentInvoicesPaidBeyondAgreedTerms() == null || model.getPercentInvoicesPaidBeyondAgreedTerms().equals("")) return FieldValidation.fail(message_required);
        if (model.getPercentInvoicesPaidBeyondAgreedTermsAsDecimal() == null) return FieldValidation.fail(message_percentagebounds);
        if (model.getPercentInvoicesPaidBeyondAgreedTermsAsDecimal().doubleValue() < 0) return FieldValidation.fail(message_percentagebounds);
        if (model.getPercentInvoicesPaidBeyondAgreedTermsAsDecimal().doubleValue() > 100) return FieldValidation.fail(message_upperpercentagebounds);
        if (isNotWholeNumber(model.getPercentInvoicesPaidBeyondAgreedTerms())) return FieldValidation.fail(message_integer);
        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validatePercentInvoicesWithin30Days() {
        if (model.getPercentInvoicesWithin30Days() == null || model.getPercentInvoicesWithin30Days().equals("")) return FieldValidation.fail(message_required);
        if (model.getPercentInvoicesWithin30DaysAsDecimal() == null) return FieldValidation.fail(message_percentagebounds);
        if (model.getPercentInvoicesWithin30DaysAsDecimal().doubleValue() < 0) return FieldValidation.fail(message_percentagebounds);
        if (model.getPercentInvoicesWithin30DaysAsDecimal().doubleValue() > 100) return FieldValidation.fail(message_upperpercentagebounds);
        if (isNotWholeNumber(model.getPercentInvoicesWithin30Days())) return FieldValidation.fail(message_integer);
        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validatePercentInvoicesWithin60Days() {
        if (model.getPercentInvoicesWithin60Days() == null || model.getPercentInvoicesWithin60Days().equals("")) return FieldValidation.fail(message_required);
        if (model.getPercentInvoicesWithin60DaysAsDecimal() == null) return FieldValidation.fail(message_percentagebounds);
        if (model.getPercentInvoicesWithin60DaysAsDecimal().doubleValue() < 0) return FieldValidation.fail(message_percentagebounds);
        if (model.getPercentInvoicesWithin60DaysAsDecimal().doubleValue() > 100) return FieldValidation.fail(message_upperpercentagebounds);
        if (isNotWholeNumber(model.getPercentInvoicesWithin60Days())) return FieldValidation.fail(message_integer);
        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validatePercentInvoicesBeyond60Days() {
        if (model.getPercentInvoicesBeyond60Days() == null || model.getPercentInvoicesBeyond60Days().equals("")) return FieldValidation.fail(message_required);
        if (model.getPercentInvoicesBeyond60DaysAsDecimal() == null) return FieldValidation.fail(message_percentagebounds);
        if (model.getPercentInvoicesBeyond60DaysAsDecimal().doubleValue() < 0) return FieldValidation.fail(message_percentagebounds);
        if (model.getPercentInvoicesBeyond60DaysAsDecimal().doubleValue() > 100) return FieldValidation.fail(message_upperpercentagebounds);
        if (isNotWholeNumber(model.getPercentInvoicesBeyond60Days())) return FieldValidation.fail(message_integer);
        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validateTimePercentages() {
        // only apply if the inner fields are valid
        if (!validatePercentInvoicesWithin30Days().isOk() || !validatePercentInvoicesWithin60Days().isOk() || !validatePercentInvoicesBeyond60Days().isOk()) {
            return FieldValidation.ok();
        }
        Double totalAbs = Math.abs(100.0
                - model.getPercentInvoicesWithin30DaysAsDecimal().doubleValue()
                - model.getPercentInvoicesWithin60DaysAsDecimal().doubleValue()
                - model.getPercentInvoicesBeyond60DaysAsDecimal().doubleValue());
        if (totalAbs > 2.001) return FieldValidation.fail(message_sumto100);
        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validateStartDate() {
        if (model.getStartDate() == null) return FieldValidation.fail(message_invaliddate);
        if (model.getStartDate().getTime().getTime() - utcNow.getTime().getTime() > 86400000)
            return FieldValidation.fail(message_future);
        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validateEndDate() {
        if (model.getEndDate() == null) return FieldValidation.fail(message_invaliddate);
        if (model.getEndDate().getTime().getTime() - utcNow.getTime().getTime() > 86400000)
            return FieldValidation.fail(message_future);
        if (validateStartDate().isOk() && model.getStartDate().getTime().getTime() - model.getEndDate().getTime().getTime() > 100)
            return FieldValidation.fail(message_startbeforeend);
        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validatePaymentTerms() {
        if (model.getPaymentTerms() == null || model.getPaymentTerms().equals("")) return FieldValidation.fail(message_required);
        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validateMaximumContractPeriod() {
        if (model.getMaximumContractPeriod() == null || model.getMaximumContractPeriod().isEmpty()) {
            return FieldValidation.fail(message_required);
        }
        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validatePaymentTermsChanged() {
        return model.isPaymentTermsChanged() == null
                ? FieldValidation.fail(message_required)
                : FieldValidation.ok();
    }

    @Override
    public FieldValidation validatePaymentTermsChangedComment() {
        if (model.isPaymentTermsChanged() == null || model.isPaymentTermsChanged().equals(false)) {
            return FieldValidation.ok();
        }

        if (model.getPaymentTermsChangedComment() == null || model.getPaymentTermsChangedComment().isEmpty()) {
            return FieldValidation.fail(message_required);
        }
        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validatePaymentTermsChangedNotified() {
        if (model.isPaymentTermsChanged() == null || model.isPaymentTermsChanged().equals(false)) {
            return FieldValidation.ok();
        }

        return model.isPaymentTermsChangedNotified() == null
                ? FieldValidation.fail(message_required)
                : FieldValidation.ok();
    }

    @Override
    public FieldValidation validatePaymentTermsChangedNotifiedComment() {
        if (model.isPaymentTermsChanged() == null || model.isPaymentTermsChanged().equals(false)) {
            return FieldValidation.ok();
        }

        if (model.isPaymentTermsChangedNotified() == null || model.isPaymentTermsChangedNotified().equals(false)) {
            return FieldValidation.ok();
        }

        if (model.getPaymentTermsChangedNotifiedComment() == null || model.getPaymentTermsChangedNotifiedComment().isEmpty()) {
            return FieldValidation.fail(message_required);
        }
        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validatePaymentTermsComment() {
        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validateDisputeResolution() {
        if (model.getDisputeResolution() == null || model.getDisputeResolution().equals(""))
            return FieldValidation.fail(message_required);
        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validateHasPaymentCodes() {
        return model.isHasPaymentCodes() == null
                ? FieldValidation.fail(message_required)
                : FieldValidation.ok();
    }

    @Override
    public FieldValidation validatePaymentCodes() {
        if (model.isHasPaymentCodes() == null || model.isHasPaymentCodes().equals(false)) {
            return FieldValidation.ok();
        }

        if (model.getPaymentCodes() == null || model.getPaymentCodes().equals(""))
            return FieldValidation.fail(message_required);

        return FieldValidation.ok();
    }

    @Override
    public FieldValidation validateOfferEInvoicing() {
        return model.isOfferEInvoicing() == null ? FieldValidation.fail(message_required) : FieldValidation.ok();
    }

    @Override
    public FieldValidation validateOfferSupplyChainFinance() {
        return model.isOfferSupplyChainFinance() == null ? FieldValidation.fail(message_required) : FieldValidation.ok();
    }

    @Override
    public FieldValidation validateRetentionChargesInPolicy() {
        return model.isRetentionChargesInPolicy() == null ? FieldValidation.fail(message_required) : FieldValidation.ok();
    }

    @Override
    public FieldValidation validateRetentionChargesInPast() {
        return model.isRetentionChargesInPast() == null ? FieldValidation.fail(message_required) : FieldValidation.ok();
    }

    private boolean isNotWholeNumber(String definitelyNumeric) {
        double v = Double.parseDouble(definitelyNumeric);
        return Math.abs(Math.abs((v+0.5)%1) - 0.5) > 0.0001;
    }
}

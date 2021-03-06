package com.ozguryazilim.tekir.contact.reports;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

import com.google.common.base.Strings;
import com.ozguryazilim.tekir.account.AccountTxnRepository;
import com.ozguryazilim.tekir.contact.ContactListModel;
import com.ozguryazilim.tekir.contact.ContactRepository;
import com.ozguryazilim.tekir.contact.ContactRoleRegistery;
import com.ozguryazilim.tekir.contact.config.ContactPages;
import com.ozguryazilim.tekir.core.query.filter.TagSuggestionService;
import com.ozguryazilim.tekir.core.reports.TekirDynamicReportUtils;
import com.ozguryazilim.tekir.entities.AccountTxn;
import com.ozguryazilim.telve.entities.FeaturePointer;
import com.ozguryazilim.telve.messages.Messages;
import com.ozguryazilim.telve.query.filters.DateValueType;
import com.ozguryazilim.telve.reports.DynamicReportBase;
import com.ozguryazilim.telve.reports.Report;
import com.ozguryazilim.telve.reports.ReportDate;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import javax.inject.Inject;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.builder.style.TemplateStylesBuilder;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Report(filterPage = ContactPages.ContactListDynaReport.class, permission = "contactListReport", path = "/contact", template = "contactListDynaReport")
public class ContactListDynaReport extends DynamicReportBase<ContactListFilter> {

    private static final Logger LOG = LoggerFactory.getLogger(ContactListDynaReport.class);

    @Inject
    private ContactRepository repository;

    @Inject
    private AccountTxnRepository txnRepository;

    private TagSuggestionService suggestionProvider;

    @Override
    protected ContactListFilter buildFilter() {
        ContactListFilter result = new ContactListFilter();

        result.setDate(new ReportDate(DateValueType.Today));

        return result;
    }

    @Override
    protected void buildReport(JasperReportBuilder report, Boolean forExport) {

        TextColumnBuilder<String> contactCode = col.column("contactCode", type.stringType())
            .setTitle(msg("ContactListReport.Code")).setFixedWidth(cm(3));
        TextColumnBuilder<String> contactName = col.column("contactName", type.stringType())
            .setTitle(msg("ContactListReport.Name")).setFixedWidth(cm(6));
        TextColumnBuilder<String> contactInfo = col.column("contactInfo", type.stringType())
            .setTitle(msg("ContactListReport.Info"));

        if (getFilter().getDetail()) {
            SubreportBuilder sub = cmp.subreport(new SubreportExpression())
                .setDataSource(exp.subDatasourceBeanCollection("txnList"));

            report
                .detailFooter(cmp.horizontalList(
                cmp.horizontalGap(cm(1)), sub));
        }

        report
            .columns(contactCode, contactName, contactInfo)
            .highlightDetailEvenRows();
    }

    @Override
    protected JRDataSource getReportDataSource() {
        List<ContactListModel> rows = repository.findByListFilter(getFilter());

        if (getFilter().getDetail()) {
            for (ContactListModel c : rows) {
                List<AccountTxn> txnRows = txnRepository
                    .findOpenTxnByContactId(c.getContactId(), getFilter());
                c.setTxnList(txnRows);
            }
        }

        return new JRBeanCollectionDataSource(rows);
    }

    @Override
    protected String getReportSubTitle() {
        String pattern = Messages.getMessage("general.format.Date");
        DateFormat df = new SimpleDateFormat(pattern);
        Date dt = getFilter().getDate().getCalculatedValue();
        String date = df.format(dt);

        StringBuilder sb = new StringBuilder();
        sb.append(date).append(" ").append(Messages.getMessage("general.label.AsOf")).append('\n');
        if (!Strings.isNullOrEmpty(getFilter().getCode())) {
            sb.append(Messages.getMessage("general.label.Code")).append(" : ")
                .append(getFilter().getCode()).append('\n');
        }
        if (!Strings.isNullOrEmpty(getFilter().getName())) {
            sb.append(Messages.getMessage("general.label.Name")).append(" : ")
                .append(getFilter().getName()).append('\n');
        }
        if (getFilter().getContactCategory() != null) {
            sb.append(Messages.getMessage("general.label.Category")).append(" : ")
                .append(getFilter().getContactCategory().getName())
                .append('\n');
        }
        if (getFilter().getCorporationType() != null) {
            sb.append(Messages.getMessage("contact.label.CorporationType")).append(" : ")
                .append(getFilter().getCorporationType().getName())
                .append('\n');
        }
        if (getFilter().getIndustry() != null) {
            sb.append(Messages.getMessage("general.label.Industry")).append(" : ")
                .append(getFilter().getIndustry().getName()).append('\n');
        }
        if (getFilter().getTerritory() != null) {
            sb.append(Messages.getMessage("general.label.Territory")).append(" : ")
                .append(getFilter().getTerritory().getName()).append('\n');
        }
        if (getFilter().getOwner() != null) {
            sb.append(Messages.getMessage("general.label.Owner")).append(" : ")
                .append(getFilter().getOwner()).append('\n');
        }
        if (getFilter().getRoles() != null) {
            ListIterator<String> iterator = getFilter().getRoles().listIterator();

            if (iterator.hasNext()) {
                sb.append(Messages.getMessage("contact.label.ContactRoles")).append(" : ");
            }

            while (iterator.hasNext()) {
                sb.append(Messages.getMessage("contact.role." + iterator.next()));
                if (!iterator.hasNext()) {
                    sb.append(".\n");
                } else {
                    sb.append(", ");
                }
            }
        }

        if (getFilter().getTag() != null && getFilter().getDetail()) {
            ListIterator<String> iterator = getFilter().getTag().listIterator();

            if (iterator.hasNext()) {
                sb.append(Messages.getMessage("general.label.Tag")).append(" : ");
            }

            while (iterator.hasNext()) {
                sb.append(iterator.next());
                if (!iterator.hasNext()) {
                    sb.append(".");
                } else {
                    sb.append(", ");
                }
            }
        }

        return sb.toString();
    }

    @Override
    protected Boolean isReportPotrait() {
        if (getFilter().getDetail()){
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    public List<String> getSuggestions() {
        suggestionProvider = BeanProvider.getContextualReference(TagSuggestionService.class);
        return suggestionProvider.getSuggestions("*");
    }

    public List<String> getRoles() {
        return ContactRoleRegistery.getFilterableContactRoles();
    }

    private class SubreportExpression extends AbstractSimpleExpression<JasperReportBuilder> {

        @Override
        public JasperReportBuilder evaluate(ReportParameters reportParameters) {
            JasperReportBuilder report = report();

            TextColumnBuilder<Date> txnDate = col
                .column(msg("general.label.Date"), "date", type.dateType()).setFixedWidth(cm(3))
                .setPattern(msg("general.format.Date"))
                .setHorizontalTextAlignment(HorizontalTextAlignment.LEFT);
            TextColumnBuilder<String> status = TekirDynamicReportUtils.buildStatusColumn("status");
            TextColumnBuilder<FeaturePointer> feature = TekirDynamicReportUtils
                .buildFeatureColumn("feature");
            TextColumnBuilder<BigDecimal> amount = col
                .column(msg("general.label.Amount"), "amount", type.bigDecimalType())
                .setFixedWidth(cm(4))
                .setValueFormatter(new AbstractValueFormatter<String, BigDecimal>() {
                    @Override
                    public String format(BigDecimal value, ReportParameters reportParameters) {
                        Currency ccy = reportParameters.getValue("currency");
                        return
                            type.bigDecimalType().valueToString(value, reportParameters.getLocale())
                                + " " + ccy.getCurrencyCode();
                    }
                });
            TextColumnBuilder<BigDecimal> localAmount = col
                .column(msg("general.label.LocalAmount"), "localAmount", type.bigDecimalType())
                .setFixedWidth(cm(3));
            TextColumnBuilder<String> topic = col.column("topic", type.stringType())
                .setTitle(msg("AccountTxnReport.Topic"))
                .setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT);

            InputStream iss = ContactListDynaReport.class.getResourceAsStream("/" + ConfigResolver
                .getPropertyValue("dynamicreports.styles", "telve.jrtx"));

            TemplateStylesBuilder tsb = stl.loadStyles(iss);

            report
                .templateStyles(tsb)
                .setColumnStyle(stl.templateStyle("Base"))
                .setColumnTitleStyle(stl.templateStyle("ColumnTitle"))
                .columns(txnDate, status, feature, amount, localAmount, topic)
                .fields(
                    DynamicReports.field("currency", Currency.class),
                    DynamicReports.field("feature", FeaturePointer.class))
                .highlightDetailEvenRows();

            return report;
        }
    }
}
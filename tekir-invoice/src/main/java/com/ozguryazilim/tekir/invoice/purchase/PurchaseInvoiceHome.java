/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.tekir.invoice.purchase;

import com.ozguryazilim.tekir.entities.Contact;
import com.ozguryazilim.tekir.entities.Corporation;
import com.ozguryazilim.tekir.entities.InvoiceItem;
import com.ozguryazilim.tekir.entities.InvoiceSummary;
import com.ozguryazilim.tekir.entities.Person;
import com.ozguryazilim.tekir.entities.ProcessType;
import com.ozguryazilim.tekir.entities.PurchaseInvoice;
import com.ozguryazilim.tekir.entities.VoucherState;
import com.ozguryazilim.tekir.entities.VoucherStateEffect;
import com.ozguryazilim.tekir.entities.VoucherStateType;
import com.ozguryazilim.tekir.voucher.VoucherCommodityItemEditor;
import com.ozguryazilim.tekir.voucher.VoucherCommodityItemEditorListener;
import com.ozguryazilim.tekir.voucher.VoucherFormBase;
import com.ozguryazilim.tekir.voucher.VoucherStateAction;
import com.ozguryazilim.tekir.voucher.VoucherStateChange;
import com.ozguryazilim.tekir.voucher.VoucherStateConfig;
import com.ozguryazilim.tekir.voucher.process.ProcessService;
import com.ozguryazilim.tekir.voucher.utils.SummaryCalculator;
import com.ozguryazilim.telve.data.RepositoryBase;
import com.ozguryazilim.telve.forms.FormEdit;
import com.ozguryazilim.telve.messages.FacesMessages;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 *
 * @author oyas
 */
@FormEdit(feature = PurchaseInvoiceFeature.class)
public class PurchaseInvoiceHome extends VoucherFormBase<PurchaseInvoice> implements VoucherCommodityItemEditorListener<InvoiceItem> {

    @Inject
    private PurchaseInvoiceRepository repository;

    @Inject
    private VoucherCommodityItemEditor commodityItemEditor;

    @Inject
    private ProcessService processService;
    
    @Override
    public boolean onAfterLoad() {
        if (!getEntity().getAccount().getContactRoles().contains("ACCOUNT")) {
            FacesMessages.error("Seçtiğiniz bağlantı bir Cari değil!", "Bağlantıyı cariye dönüştürmelisiniz?");
        }
        if (!getEntity().getAccount().getContactRoles().contains("VENDOR")) {
            FacesMessages.warn("Seçtiğiniz bağlantı bir Tedarikci değil.", "Bağlantıyı tedarikci olarak işaretlemek ister misiniz?");
        }
        return super.onAfterLoad(); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean onBeforeSave() {
        if (getEntity().getProcess() == null) {
            getEntity().setProcess(processService.createProcess(getEntity().getAccount(), getEntity().getTopic(), ProcessType.PURCHASE));
        }

        return super.onBeforeSave();
    }

    @Override
    protected boolean onBeforeTrigger(VoucherStateChange e) {
        if ("publish".equals(e.getAction().getName())) {
            if (!getEntity().getAccount().getContactRoles().contains("ACCOUNT")) {
                FacesMessages.error("Seçtiğiniz bağlantı bir Cari değil!", "Bağlantıyı cariye dönüştürmelisiniz?");
                return false;
            }
        }
        return super.onBeforeTrigger(e);
    }

    @Override
    protected VoucherStateConfig buildStateConfig() {
        VoucherStateConfig config = new VoucherStateConfig();
        config.addTranstion(VoucherState.DRAFT, new VoucherStateAction("publish", "fa fa-check"), VoucherState.OPEN);
        config.addTranstion(VoucherState.OPEN, new VoucherStateAction("won", "fa fa-check", false), new VoucherState("WON", VoucherStateType.CLOSE, VoucherStateEffect.POSIVITE));
        config.addTranstion(VoucherState.OPEN, new VoucherStateAction("loss", "fa fa-close", true), new VoucherState("WON", VoucherStateType.CLOSE, VoucherStateEffect.NEGATIVE));
        config.addTranstion(VoucherState.OPEN, new VoucherStateAction("cancel", "fa fa-ban", true), VoucherState.CLOSE);
        config.addTranstion(VoucherState.OPEN, new VoucherStateAction("revise", "fa fa-unlock", true), VoucherState.DRAFT);
        //config.addTranstion(VoucherState.CLOSE, new VoucherStateAction("unlock", "fa fa-unlock", true ), VoucherState.DRAFT);
        return config;
    }

    @Override
    protected RepositoryBase<PurchaseInvoice, ?> getRepository() {
        return repository;
    }

    @Override
    public void addItem() {
        InvoiceItem item = new InvoiceItem();
        item.setMaster(getEntity());
        commodityItemEditor.openDialog(item, getEntity().getCurrency(), this);
    }

    @Override
    public void saveItem(InvoiceItem item) {
        if (!getEntity().getItems().contains(item)) {
            getEntity().getItems().add(item);
        }
        calculateSummaries();
    }

    ///////// Buradan aşağıdakiler Quote üzerinden doğrudan kopya bir üst sınıfa alınabilirler sanki.
    public com.ozguryazilim.tekir.entities.Process getProcess() {
        return getEntity().getProcess();
    }

    public void setProcess(com.ozguryazilim.tekir.entities.Process process) {
        getEntity().setProcess(process);
        if (process != null) {
            getEntity().setAccount(process.getAccount());
            getEntity().setTopic(process.getTopic());
        }
    }

    public Contact getAccount() {
        return getEntity().getAccount();
    }

    public void setAccount(Contact account) {
        getEntity().setAccount(account);
        getEntity().setProcess(null);
        if (!account.getContactRoles().contains("ACCOUNT")) {
            FacesMessages.error("Seçtiğiniz bağlantı bir Cari değil!", "Bağlantıyı cariye dönüştürmelisiniz?");
        }
        if (!getEntity().getAccount().getContactRoles().contains("VENDOR")) {
            FacesMessages.warn("Seçtiğiniz bağlantı bir Tedarikci değil.", "Bağlantıyı tedarikci olarak işaretlemek ister misiniz?");
        }
    }

    public Person getPerson() {
        if (getAccount() instanceof Person) {
            return (Person) getAccount();
        } else {
            return ((Corporation) getAccount()).getPrimaryContact();
        }
    }

    public Corporation getCorporation() {
        if (getAccount() instanceof Corporation) {
            return (Corporation) getAccount();
        } else {
            return ((Person) getAccount()).getCorporation();
        }
    }

    @Override
    public List<InvoiceSummary> getTaxes() {
        List<InvoiceSummary> result = new ArrayList<>();

        getEntity().getSummaries().entrySet().stream()
                .filter(e -> e.getKey().startsWith("Tax."))
                .forEach(e -> {
                    result.add(e.getValue());
                });

        return result;
    }

    @Override
    public void calculateSummaries() {
        SummaryCalculator<PurchaseInvoice, InvoiceItem, InvoiceSummary> sc = new SummaryCalculator();
        sc.calcSummaries(this::getEntity, getEntity()::getItems, getEntity()::getSummaries, () -> new InvoiceSummary(), getEntity()::setTotal);
    }

    @Override
    public void editItem(InvoiceItem item) {
        commodityItemEditor.openDialog(item, getEntity().getCurrency(), this);
    }

    @Override
    public void removeItem(InvoiceItem item) {
        getEntity().getItems().remove(item);
        calculateSummaries();
    }

}

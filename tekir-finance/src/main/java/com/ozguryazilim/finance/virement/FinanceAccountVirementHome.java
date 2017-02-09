/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.finance.virement;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.inject.Inject;

import org.primefaces.context.RequestContext;

import com.ozguryazilim.tekir.core.currency.CurrencyService;
import com.ozguryazilim.tekir.entities.FinanceAccount;
import com.ozguryazilim.tekir.entities.FinanceAccountVirement;
import com.ozguryazilim.tekir.entities.VoucherState;
import com.ozguryazilim.tekir.voucher.VoucherFormBase;
import com.ozguryazilim.tekir.voucher.VoucherStateAction;
import com.ozguryazilim.tekir.voucher.VoucherStateConfig;
import com.ozguryazilim.tekir.voucher.process.ProcessService;
import com.ozguryazilim.telve.data.RepositoryBase;
import com.ozguryazilim.telve.forms.FormEdit;
import com.ozguryazilim.telve.messages.FacesMessages;
import com.ozguryazilim.telve.sequence.SequenceManager;

/**
 *
 * @author oyas
 */
@FormEdit( feature = FinanceAccountVirementFeature.class)
public class FinanceAccountVirementHome extends VoucherFormBase<FinanceAccountVirement>{

    @Inject
    private FinanceAccountVirementRepository repository;
    
    @Inject
    private CurrencyService currencyService;

    @Inject
    private SequenceManager sequenceManager;
    
    @Inject
    private ProcessService processService;
    
    private boolean fromCurrencyEditable = true;
    
    private boolean toCurrencyEditable = true;
    
    private boolean toAmountRendered = true;
    
    private boolean cont;
       
    @Override
    public void createNew() {
        super.createNew(); 
        getEntity().setFromCurrency(currencyService.getDefaultCurrency());
        getEntity().setToCurrency(currencyService.getDefaultCurrency());
        initCurrencyFeatures();
        cont = false;

    }
    
    @Override   
    public boolean onAfterLoad() {
    	// TODO Auto-generated method stub
    	enableCurrencyFeatures();
    	cont = false;
    	return super.onAfterLoad();
    }

    @Override
    public boolean onBeforeSave() {
    	
    	if(!isFromCurrencyEditable()){
    		getEntity().setFromCurrency(getEntity().getFromAccount().getCurrency());
    	}
    	if(!isToCurrencyEditable()){
    		getEntity().setToCurrency(getEntity().getToAccount().getCurrency());
    	}
    	if(!isToAmountRendered()){
    		getEntity().setToAmount(getEntity().getFromAmount());
    	}
    	
    	
    	if(getEntity().getFromCurrency() == currencyService.getReportCurrency()){
    		getEntity().setLocalAmount(getEntity().getFromAmount());
    	}
    	else if(getEntity().getToCurrency() == currencyService.getReportCurrency()){
    		getEntity().setLocalAmount(getEntity().getToAmount());
    	}
    	else{
        getEntity().setLocalAmount(currencyService.convert(getEntity().getToCurrency(), getEntity().getToAmount(), getEntity().getDate()));
    	}

    	if(isExchangeRateTooBig() && !cont){
    		RequestContext context = RequestContext.getCurrentInstance();
    		context.execute("PF('rateWarnDialog').show();");
    		return false;
    	}
    	
        return super.onBeforeSave(); 
    }

    
    @Override
    protected VoucherStateConfig buildStateConfig() {
        VoucherStateConfig config = new VoucherStateConfig();
        config.addTranstion(VoucherState.DRAFT, new VoucherStateAction("publish", "fa fa-check" ), VoucherState.CLOSE);
        config.addTranstion(VoucherState.CLOSE, new VoucherStateAction("reopen", "fa fa-unlock", true ), VoucherState.DRAFT);
        return config;
    }

    @Override
    protected RepositoryBase<FinanceAccountVirement, ?> getRepository() {
        return repository;
    }
    
    private void initCurrencyFeatures(){
    	setToAmountRendered(true);
    	setFromCurrencyEditable(true);
    	setToCurrencyEditable(true);
    }
    
    public void enableCurrencyFeatures(){
    	try{
    	List<String> fromAccountRoles = getEntity().getFromAccount().getAccountRoles();
    	List<String> toAccountRoles = getEntity().getToAccount().getAccountRoles();
    	
    	getEntity().setFromCurrency(getEntity().getFromAccount().getCurrency());
    	getEntity().setToCurrency(getEntity().getToAccount().getCurrency());
    	initCurrencyFeatures();
    	
    	if(getEntity().getFromAccount().getCurrency() == getEntity().getToAccount().getCurrency()){
    		setToAmountRendered(false);
    	}
    	
    	if(!fromAccountRoles.contains("MULTI_CURRENCY")){    		
    		setFromCurrencyEditable(false);
    	}
    	else{    		
    		setToAmountRendered(true);
    	}
    	
    	if(!toAccountRoles.contains("MULTI_CURRENCY")){    		
    		setToCurrencyEditable(false);
    	}
    	else{
    		setToAmountRendered(true);    		
    	}
    }
    	catch (Exception e) {
    		initCurrencyFeatures();
			// TODO: handle exception
		}
    }

	public boolean isFromCurrencyEditable() {
		return fromCurrencyEditable;
	}

	public void setFromCurrencyEditable(boolean fromCurrencyEditable) {
		this.fromCurrencyEditable = fromCurrencyEditable;
	}

	public boolean isToCurrencyEditable() {
		return toCurrencyEditable;
	}

	public void setToCurrencyEditable(boolean toCurrencyEditable) {
		this.toCurrencyEditable = toCurrencyEditable;
	}

	public boolean isToAmountRendered() {
		return toAmountRendered;
	}

	public void setToAmountRendered(boolean toAmountEnabled) {
		this.toAmountRendered = toAmountEnabled;
	}
	
	public FinanceAccount getFromAccount() {
		return getEntity().getFromAccount();
	}

	public FinanceAccount getToAccount() {
		return getEntity().getToAccount();
	}

	public void setToAccount(FinanceAccount toAccount) {		
		getEntity().setToAccount(toAccount);
		enableCurrencyFeatures();
	}

	public void setFromAccount(FinanceAccount fromAccount) {
		getEntity().setFromAccount(fromAccount);
		enableCurrencyFeatures();
	}
	
	
	private boolean isExchangeRateTooBig(){
		BigDecimal result = currencyService.convert(getEntity().getFromCurrency().getCurrencyCode(), 
													getEntity().getFromAmount(), 
													getEntity().getToCurrency().getCurrencyCode());
		
		BigDecimal param = new BigDecimal(2);
		
		if(getEntity().getToAmount().compareTo(result.multiply(param)) == 1){
			return true;
		}
		
		if(getEntity().getToAmount().compareTo(result.divide(param)) == -1){
			return true;
		}

		return false;
					
	}

	public boolean isCont() {
		return cont;
	}

	public void setCont(String cont) {
		if(cont.equalsIgnoreCase("false")){                    
			this.cont = false;
                        
		}
		else{
			this.cont = true;           
		}
		
	}
	
	
}
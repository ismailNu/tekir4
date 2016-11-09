/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.tekir.account.debit;

import com.ozguryazilim.tekir.account.AccountTxnService;
import com.ozguryazilim.tekir.entities.AccountDebitNote;
import com.ozguryazilim.telve.entities.FeaturePointer;
import com.ozguryazilim.telve.forms.EntityChangeAction;
import com.ozguryazilim.telve.forms.EntityChangeEvent;
import com.ozguryazilim.telve.qualifiers.After;
import com.ozguryazilim.telve.qualifiers.EntityQualifier;
import java.io.Serializable;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Alacak dekontu'nu cari hareket'e işler.
 * @author Hakan Uygun
 */
@Dependent
public class AccountDebitNoteAccountTxnFeeder implements Serializable{
    
    @Inject
    private AccountTxnService accountTxnService;
    
    
    public void feed(@Observes @EntityQualifier(entity = AccountDebitNote.class) @After EntityChangeEvent event) {
        
        if( event.getAction() != EntityChangeAction.DELETE   ) {
            AccountDebitNote entity = (AccountDebitNote) event.getEntity();
            
            FeaturePointer voucherPointer = new FeaturePointer();
            voucherPointer.setBusinessKey(entity.getVoucherNo());
            voucherPointer.setPrimaryKey(entity.getId());
            voucherPointer.setFeature(entity.getClass().getSimpleName());

            
            accountTxnService.saveFeature(voucherPointer, entity.getAccount(), entity.getCode(), entity.getInfo(), Boolean.TRUE, Boolean.TRUE, entity.getCurrency(), entity.getAmount(), entity.getDate(), entity.getOwner(), "", entity.getState().toString(), entity.getStateReason());
        }
        
        //TODO: Delete edildiğinde de gidip txn'den silme yapılmalı.
            
    }
}
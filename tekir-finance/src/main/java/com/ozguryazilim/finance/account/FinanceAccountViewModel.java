/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.finance.account;

import com.ozguryazilim.telve.entities.ViewModel;
import java.io.Serializable;
import java.util.Currency;

/**
 *
 * @author oyas
 */
public class FinanceAccountViewModel implements ViewModel, Serializable {
    
    private Long id;
    private String code;
    private String name;
    private Currency currency;
    private String iban;

    public FinanceAccountViewModel(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public FinanceAccountViewModel(Long id, String code, String name, Currency currency, String iban) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.currency = currency;
        this.iban = iban;
    }

    
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    
}

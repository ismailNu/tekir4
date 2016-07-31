/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.tekir.entities;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 *
 * @author oyas
 */
@Embeddable
public class Quantity implements Serializable{
    
    @Column(name = "AMOUNT")
    private BigDecimal amount;
    
    @Column(name = "UNIT")
    private String unit;

    public Quantity() {
    }

    public Quantity(BigDecimal amount, String unit) {
        this.amount = amount;
        this.unit = unit;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    
}
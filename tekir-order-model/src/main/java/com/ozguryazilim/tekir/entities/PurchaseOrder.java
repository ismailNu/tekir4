/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.tekir.entities;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Alış Siparişi
 * 
 * @author Hakan Uygun
 */
@Entity
@DiscriminatorValue("PURCHASE")
public class PurchaseOrder extends Order{
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.tekir.entities.converters;

import com.ozguryazilim.tekir.entities.VoucherState;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * JPA converter for VoucherState class.
 * 
 * @author Hakan Uygun
 */
@Converter(autoApply = true)
public class VoucherStateConverter implements AttributeConverter<VoucherState, String>{

    @Override
    public String convertToDatabaseColumn(VoucherState x) {
        return x.toString();
    }

    @Override
    public VoucherState convertToEntityAttribute(String y) {
        return VoucherState.valueOf(y);
    }

}

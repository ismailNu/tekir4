/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ozguryazilim.tekir.entities;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Gerçek adress bilgisi
 * 
 * @author Hakan Uygun 
 */
@Entity
@DiscriminatorValue(value = "ADR")
public class ContactAddress extends ContactInformation{


    @Column( name = "ZIP")
    private String zipCode;
    
    //Gerçek adresler için lazım alanlar : alt seviye bir location alınır. Gerisi onun üzerinden çözümlenir.
    @ManyToOne
    @JoinColumn(name = "LOCATION_ID", foreignKey = @ForeignKey(name = "FK_CONADDR_LOC"))
    private Location location;
    
        //Lat-Lon biçminde? Bu sayede harita gösterilebilir?
    @Column( name = "LAT")
    private Double latitude;
    
    @Column( name = "LON")
    private Double longitude;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    @Override
    public List<String> getAcceptedRoles() {
        List<String> ls = super.getAcceptedRoles(); 
        
        ls.add("INVOICE");
        ls.add("SHIPMENT");
        
        return ls;
    }

    @Override
    public String getCaption(){
        //FIXME: location'dan geri nasıl bir şey dönecek?
        return getAddress() + location.getName() ;
    }

    @Override
    public String getIcon() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
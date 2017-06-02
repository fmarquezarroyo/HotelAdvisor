package es.pfm.hoteladvisor.model;

import java.util.ArrayList;

public class Hotel {

    private String name;
    private String address;
    private String zip;
    private String city;
    private String country;
    private String continent;
    private Integer ufi;
    private String currencyCode;
    private String city_unique;
    private String city_preferred;
    private Integer id;
    private String photo_url;
    private String hotel_url;
    private Double review_score;
    private Integer review_nr;
    private Double clazz;
    private Double min_rate_EUR;
    private Double max_rate_EUR;
    private Integer nr_rooms;
    private String desc_es;
    private String desc_en;
    private ArrayList <Double> coord;
    private Double distance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public Integer getUfi() {
        return ufi;
    }

    public void setUfi(Integer ufi) {
        this.ufi = ufi;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCity_unique() {
        return city_unique;
    }

    public void setCity_unique(String city_unique) {
        this.city_unique = city_unique;
    }

    public String getCity_preferred() {
        return city_preferred;
    }

    public void setCity_preferred(String city_preferred) {
        this.city_preferred = city_preferred;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public String getHotel_url() {
        return hotel_url;
    }

    public void setHotel_url(String hotel_url) {
        this.hotel_url = hotel_url;
    }

    public Double getReview_score() {
        return review_score;
    }

    public void setReview_score(Double review_score) {
        this.review_score = review_score;
    }

    public Integer getReview_nr() {
        return review_nr;
    }

    public void setReview_nr(Integer review_nr) {
        this.review_nr = review_nr;
    }

    public Double getClazz() {
        return clazz;
    }

    public void setClazz(Double clazz) {
        this.clazz = clazz;
    }

    public Double getMin_rate_EUR() {
        return min_rate_EUR;
    }

    public void setMin_rate_EUR(Double min_rate_EUR) {
        this.min_rate_EUR = min_rate_EUR;
    }

    public Double getMax_rate_EUR() {
        return max_rate_EUR;
    }

    public void setMax_rate_EUR(Double max_rate_EUR) {
        this.max_rate_EUR = max_rate_EUR;
    }

    public Integer getNr_rooms() {
        return nr_rooms;
    }

    public void setNr_rooms(Integer nr_rooms) {
        this.nr_rooms = nr_rooms;
    }

    public String getDesc_es() {
        return desc_es;
    }

    public void setDesc_es(String desc_es) {
        this.desc_es = desc_es;
    }

    public String getDesc_en() {
        return desc_en;
    }

    public void setDesc_en(String desc_en) {
        this.desc_en = desc_en;
    }

    public ArrayList<Double> getCoord() {
        return coord;
    }

    public void setCoord(ArrayList<Double> coord) {
        this.coord = coord;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }
}

package es.pfm.hoteladvisor.model;

import java.util.ArrayList;

public class HotelQuery {

    private String city;
    private String country;
    private String continent;
    private String review_score;
    private String clazz;
    private String min_rate_EUR;


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

    public String getReview_score() {
        return review_score;
    }

    public void setReview_score(String review_score) {
        this.review_score = review_score;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getMin_rate_EUR() {
        return min_rate_EUR;
    }

    public void setMin_rate_EUR(String min_rate_EUR) {
        this.min_rate_EUR = min_rate_EUR;
    }
}

package es.pfm.hoteladvisor.model;


import java.util.ArrayList;

public class Poi {

    private String poi_name;
    private String poi_type;
    private ArrayList<Double> coord;
    private String pois_osm_id;


    public String getPoi_name() {
        return poi_name;
    }

    public void setPoi_name(String poi_name) {
        this.poi_name = poi_name;
    }

    public String getPoi_type() {
        return poi_type;
    }

    public void setPoi_type(String poi_type) {
        this.poi_type = poi_type;
    }

    public ArrayList<Double> getCoord() {
        return coord;
    }

    public void setCoord(ArrayList<Double> coord) {
        this.coord = coord;
    }

    public String getPois_osm_id() {
        return pois_osm_id;
    }

    public void setPois_osm_id(String pois_osm_id) {
        this.pois_osm_id = pois_osm_id;
    }
}

package gr.uoa.di.madgik.model;

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import com.vividsolutions.jts.geom.Geometry;

final public class GeoFeaturesSingleton {

    static private GeoFeaturesSingleton _instance = null;

    private DataStore store;
    private FeatureIterator<SimpleFeature> features;
    private FeatureCollection<SimpleFeatureType, SimpleFeature> collection;
    private List<Geometry> geometries = new ArrayList<>();
    private List<Integer> zoneAreas = new ArrayList<>();
    private List<LayerFeature> layerFeatures = new ArrayList<>();
    private Float totalArea;
    private String GeoJsonFeatures;

    static private GeoFeaturesSingleton makeInstance() {
        return new GeoFeaturesSingleton();
    }

    /**
     * This is the accessor for the Singleton.
     */
    static public synchronized GeoFeaturesSingleton instance() {
        if (null == _instance) {
            _instance = makeInstance();
        }
        return _instance;
    }


    static public synchronized void setInstance(GeoFeaturesSingleton instance) {
        _instance = instance;
    }

    public static GeoFeaturesSingleton get_instance() {
        return _instance;
    }

    public static void set_instance(GeoFeaturesSingleton _instance) {
        GeoFeaturesSingleton._instance = _instance;
    }

    public FeatureIterator<SimpleFeature> getFeatures() {
        return features;
    }

    public void setFeatures(FeatureIterator<SimpleFeature> features) {
        this.features = features;
    }

    public FeatureCollection<SimpleFeatureType, SimpleFeature> getCollection() {
        return collection;
    }

    public void setCollection(FeatureCollection<SimpleFeatureType, SimpleFeature> collection) {
        this.collection = collection;
    }

    public List<Geometry> getGeometries() {
        return geometries;
    }

    public void setGeometries(List<Geometry> geometries) {
        this.geometries = geometries;
    }

    public List<Integer> getZoneAreas() {
        return zoneAreas;
    }

    public void setZoneAreas(List<Integer> zoneAreas) {
        this.zoneAreas = zoneAreas;
    }

    public List<LayerFeature> getLayerFeatures() {
        return layerFeatures;
    }

    public void setLayerFeatures(List<LayerFeature> layerFeatures) {
        this.layerFeatures = layerFeatures;
    }

    public Float getTotalArea() {
        return totalArea;
    }

    public void setTotalArea(Float totalArea) {
        this.totalArea = totalArea;
    }

    public String getGeoJsonFeatures() {
        return GeoJsonFeatures;
    }

    public void setGeoJsonFeatures(String geoJsonFeatures) {
        GeoJsonFeatures = geoJsonFeatures;
    }

    public DataStore getStore() {
        return store;
    }

    public void setStore(DataStore store) {
        this.store = store;
    }


}

package gr.uoa.di.madgik.model;

import java.io.Serializable;
import java.util.Comparator;

public class LayerFeature implements Serializable, Comparable<LayerFeature>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	String id;
	Double area;
	Float ecValue;
	Double tonsPerHectare;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Double getArea() {
		return area;
	}
	public void setArea(Double area) {
		this.area = area;
	}
	public Float getEcValue() {
		return ecValue;
	}
	public void setEcValue(Float ecValue) {
		this.ecValue = ecValue;
	}
	
	
	public Double getTonsPerHectare() {
		return tonsPerHectare;
	}
	public void setTonsPerHectare(Double tonsPerHectare) {
		this.tonsPerHectare = tonsPerHectare;
	}
	public LayerFeature(String id, Double area, Float ecValue) {
		super();
		this.id = id;
		this.area = area;
		this.ecValue = ecValue;
	}
	public LayerFeature() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public int compareTo(LayerFeature o) {
		  if(this.getEcValue() == o.getEcValue())
	             return 0;
	         return this.getEcValue() < o.getEcValue() ? -1 : 1;
	}
	
	public class AreaSorter implements Comparator<LayerFeature>{

	    public int compare(LayerFeature one, LayerFeature another){
	        int returnVal = 0;

	    if(one.getArea() < another.getArea()){
	        returnVal =  -1;
	    }else if(one.getArea() < another.getArea()){
	        returnVal =  1;
	    }else if(one.getArea() < another.getArea()){
	        returnVal =  0;
	    }
	    return returnVal;

	    }
	}
}

angular.module('PrecisionCompostCalculatorApp')
// Creating the Angular Service for storing logged user details
.service('GeoService', function($rootScope,$window, $http, SharedProperties) {

	var service = this;

    service.initializeFeatures =  function(zones){
        var geofeatures = SharedProperties.getGeoFeatures();
        var totalArea=0;
        for(var i = 0; i < zones.length; i++){
            totalArea = 0;
            geofeatures[i]["area"] = 0;
            geofeatures[i]["tonsPerHectare"] = 0;
        }
        var totalCompost = 0;
        SharedProperties.setGeoFeatures(geofeatures);

        SharedProperties.setResultsGeoFeatures(0);
        SharedProperties.setTotalCompost(0);
        SharedProperties.setTotalArea(0);
        var i=0;
        vectorLayer = SharedProperties.getVectorLayer();
        if(vectorLayer != null){
            vectorLayer.getSource().forEachFeature(function(feature){
                vectorLayer.getSource().removeFeature(feature);
                i++;
            });
        }
    }



    service.initValues = function( init) {
        geoFeatures = SharedProperties.getGeoFeatures();
        size = geoFeatures.length-1;
        console.log("size is:"+ size);
        zones = SharedProperties.getZones();
        if( init == 0 ){
            zones = SharedProperties.getInitZones();
        }
        else{
            zones = SharedProperties.getZones();

        }
        totalArea=0;
        for(var i = 0; i < geoFeatures.length; i++){
            if( geoFeatures[i] != undefined && geoFeatures[i]["area"] != undefined){

                totalArea = totalArea +geoFeatures[i]["area"];
            }
        }
        SharedProperties.setTotalArea(totalArea);
        totalCompostAmount = SharedProperties.getTotalCompostPerHer() * totalArea;

        zones[0]["amount"] = geoFeatures[0]["area"] * zones[0]['value'];//SharedProperties.getMaxCompostDose();
        zones[size]["amount"] = geoFeatures[size]["area"] *  zones[size]['value']//SharedProperties.getMinCompostDose();

        totalFixed = zones[0]["amount"] + zones[size]["amount"]
        missing = totalCompostAmount - totalFixed;
        for(i =0; i <size; i++){
            if(zones[i]["weight"] == -999){
                zones[i]["extraValue"] =  geoFeatures[i]["area"]
            }
            else{
                zones[i]["extraValue"] =  geoFeatures[i]["area"] * zones[i]["weight"];
            }
        }
        if(zones[1] != undefined && zones[2] != undefined && zones[3] != undefined && zones[4] != undefined){
            zones[4]["extraValue"] = zones[1]["extraValue"] + zones[2]["extraValue"] +zones[3]["extraValue"]
        }
        extraFactor =  missing /  zones[size]["extraValue"];

        sum = 0;
        for(i=1; i <  size; i++){
            zones[i]["amount"] = zones[i]["extraValue"]*extraFactor;
            zones[i]["value"] = service.roundResult(zones[i]["amount"] /(geoFeatures[i]["area"]));
            sum= sum + (zones[i]["amount"] /(geoFeatures[i]["area"]));
        }
        SharedProperties.setTotalCompostAmount(totalCompostAmount);
        SharedProperties.setExtraFactor(extraFactor);
        SharedProperties.setTotalFixed(totalFixed);
        SharedProperties.setMissing(missing);
        SharedProperties.setZones(zones);
    }


	
	service.calculateZones = function( total, zones){
		SharedProperties.setCalculate(1);

		var geofeatures = SharedProperties.getGeoFeatures();
		var totalArea=0;
		totalCompost =0;
		for(var i = 0; i < zones.length; i++){
			totalArea = totalArea +geofeatures[i]["area"];
			geofeatures[i]["area"] = parseFloat(Math.round((geofeatures[i]["area"]) * 100) / 100).toFixed(2);
			geofeatures[i]["tonsPerHectare"] = zones[i]['value'];
			geofeatures[i]["totalCompostZone"] = parseFloat(Math.round((geofeatures[i]["area"] * zones[i].value) * 100) / 100).toFixed(2);
			totalCompost= totalCompost + parseFloat(Math.round((geofeatures[i]["area"] * zones[i].value) * 100) / 100).toFixed(2);

		}
		
		SharedProperties.setGeoFeatures(geofeatures);
		SharedProperties.setResultsGeoFeatures(geofeatures);
		SharedProperties.setTotalCompost(totalCompost);
		SharedProperties.setTotalArea( parseFloat(Math.round((totalArea) * 100) / 100).toFixed(2));

	}


	service.createFeaturesFromGeoJson = function(){
		var geofeatures = SharedProperties.getGeoFeatures();
		var vectorLayer = SharedProperties.getVectorLayer();
		seen=[];
		var i=0;
		var initZones = SharedProperties.getZones();
		var zones =[];
		if(vectorLayer != null){
		    vectorLayer.getSource().forEachFeature(function(feature){
                var properties = feature.getProperties();
                if(geofeatures[i]!== undefined){
                    feature.setProperties({properties ,area:geofeatures[i]["area"], tonsPerHectare:geofeatures[i]["tonsPerHectare"]});
                    zones[i] =initZones[i];
                     geofeatures[i]['geometry'] = feature.getGeometry();

                }
                else{
                    vectorLayer.getSource().removeFeatures(vectorLayer.getSource().getFeatureById(i));
                }

                i++;
            });
            SharedProperties.setVectorLayer(vectorLayer);
            SharedProperties.setGeoFeatures(geofeatures);

		}
		SharedProperties.setZones(zones);

	}

	service.roundResult = function(result) {
        return parseFloat(Math.round((result* 100) / 100)).toFixed(2);
	}


		service.createGeoJson = function(){

            var format = new ol.format.GeoJSON( {dataProjection: 'EPSG:4326',
                                                   featureProjection: 'EPSG:3857'});
		    var geofeatures = SharedProperties.getGeoFeatures();
    		var vectorLayer = SharedProperties.getVectorLayer();
    		var i=0;
    		var geoJson =[];
    		if(vectorLayer != null){
    		    vectorLayer.getSource().forEachFeature(function(feature){
                    //console.log("the feature attributes are:"+ JSON.stringify(geofeatures[i]));
                    var properties = feature.getProperties();
                    if(geofeatures[i]!== undefined){

                        var selectedMultiPolygon = new ol.geom.MultiPolygon();

                        if (feature.getGeometry().getType() == 'Polygon') {

                                selectedMultiPolygon.appendPolygon(feature.getGeometry());
                            }
                        else if(feature.getGeometry().getType() == 'MultiPolygon'){
                            selectedMultiPolygon = feature.getGeometry();
                        }
                        var geom =  feature.getGeometry();
                        if(geom != null && geom != undefined){
                        // geom.transform('EPSG:3857', 'EPSG:4326');
                            var feat= new ol.Feature({
                                      geometry: selectedMultiPolygon
                                    });

                            feat.setProperties({area:geofeatures[i]["area"], tonsPerHec:geofeatures[i]["tonsPerHectare"]});
                            geoJson.push(feat);
                        }
                    }
                    i++;
                });

    		}
    		if( i==0 ){
    		    geoJson= JSON.stringify(geofeatures, function(key, val) {
                       if (val != null && typeof val == "object") {
                            if (seen.indexOf(val) >= 0) {
                                return;
                            }
                            seen.push(val);
                        }
                        return val;
                    });
            		console.log("inside service gjson:"+gjson);
    		}
            var obj = format.writeFeatures(geoJson);
            return obj;

    	}
});
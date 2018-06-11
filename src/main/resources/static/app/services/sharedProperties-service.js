angular.module('PrecisionCompostCalculatorApp')
    .factory('SharedProperties', function () {
        var layerName = 'capsella:ec_measurements_jacob';
        var ecMeasurements;
        var geoFeatures;
        var geoJson;
        var vectorLayer;
        var resultsGeoFeatures;
        var totalCompostPerHer = 20;
        var totalCompost;
        var totalArea;
        var missing;
        var totalFixed;
        var totalCompostAmount = 19000;

        var saveLocal;
        var newCustom;

        var maxCompostDose = 50;
        var minCompostDose = 0;
        var extraFactor;
        var map;

        var eventToggle =true;

        var initZones = [
    		{name: "POOREST", value:50, weight:-999, color:"red", amount:0, extraValue:0},
    		{name: "POOR", value:30, weight:2, color:"orange", amount:0, extraValue:0},
    		{name: "AVERAGE", value:10, weight:1, color:"yellow", amount:0, extraValue:0},
    		{name: "RICHER than Average", value:5, weight:0.5, color:"green", amount:0, extraValue:0},
    		{name: "RICHEST", value:0, weight:-999, color:"dark-green", amount:0, extraValue:0}
    	];

        var zones = [
    		{name: "POOREST", value:50, weight:-999, color:"red", amount:0, extraValue:0},
    		{name: "POOR", value:30, weight:2, color:"orange", amount:0, extraValue:0},
    		{name: "AVERAGE", value:10, weight:1, color:"yellow", amount:0, extraValue:0},
    		{name: "RICHER than Average", value:5, weight:0.5, color:"green", amount:0, extraValue:0},
    		{name: "RICHEST", value:0, weight:-999, color:"dark-green", amount:0, extraValue:0}
    	];

        var calculate = 0;
        
        return {
            getLayerName: function () {
                return layerName;
            },
            setLayerName: function(value) {
            	layerName = value;
            },
            getEcMeasurements: function () {
                return ecMeasurements;
            },
            setEcMeasurements: function(value) {
            	ecMeasurements = value;
            },
            getGeoFeatures: function () {
                return geoFeatures;
            },
            setGeoFeatures: function(value) {
            	geoFeatures = value;
            },
            getGeoJson: function () {
                return geoJson;
            },
            setGeoJson: function(value) {
            	geoJson = value;
            },
            getVectorLayer: function () {
                return vectorLayer;
            },
            setVectorLayer: function(value) {
            	vectorLayer = value;
            },
            getTotalCompost: function () {
                return totalCompost;
            },
            setTotalCompost: function(value) {
            	totalCompost = value;
            },
            getTotalArea: function () {
                return totalArea;
            },
            setTotalArea: function(value) {
            	totalArea = value;
            },
            getTotalCompostPerHer: function () {
                return totalCompostPerHer;
            },
            setTotalCompostPerHer: function(value) {
            	totalCompostPerHer = value;
            },
            getZones: function () {
                return zones;
            },
            setZones: function(value) {
            	zones = value;
            },
            getZones: function () {
                return zones;
            },
            setZones: function(value) {
                zones = value;
            },
            getCalculate: function () {
                return calculate;
            },
            setCalculate: function(value) {
            	calculate = value;
            },
            getResultsGeoFeatures: function () {
                return resultsGeoFeatures;
            },
            setResultsGeoFeatures: function(value) {
            	resultsGeoFeatures = value;
            },
            getSaveLocal: function () {
                return saveLocal;
            },
            setSaveLocal: function(value) {
                saveLocal = value;
            },
            getNewCustom: function () {
                return newCustom;
            },
            setNewCustom: function(value) {
                newCustom = value;
            },
            getMissing: function () {
                return missing;
            },
            setMissing: function(value) {
                missing = value;
            },
            getTotalFixed: function () {
                return totalFixed;
            },
            setTotalFixed: function(value) {
                totalFixed = value;
            },
            getTotalCompostAmount: function () {
                return totalCompostAmount;
            },
            setTotalCompostAmount: function(value) {
                totalCompostAmount = value;
            },
            getMaxCompostDose: function () {
                return maxCompostDose;
            },
            setMaxedCompostDose: function(value) {
                maxCompostDose = value;
            },
            getMinCompostDose: function () {
                return minCompostDose;
            },
            setMinCompostDose: function(value) {
                minCompostDose = value;
            },
            getExtraFactor: function () {
                return extraFactor;
            },
            setExtraFactor: function(value) {
                extraFactor = value;
            },
            getMap: function () {
                return map;
            },
            setMap: function(value) {
                map = value;
            },
            getEventToggle: function () {
             return eventToggle;
            },
            setEventToggle: function(value) {
             eventToggle = value;
            },
            getInitZones: function () {
            return initZones;
            },
            setInitZones: function(value) {
            initZones = value;
            }

        };
    });
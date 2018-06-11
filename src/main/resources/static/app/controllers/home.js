angular.module('PrecisionCompostCalculatorApp')
		// Creating the Angular Controller
    .controller('HomeController',function($http, $scope, SharedProperties, GeoService, $rootScope) {
			
    /****************** styling layers *******************/

    $('.div-loader').hide();

    var blue = new ol.style.Fill({color: '#02872a'});
    var cyan = new ol.style.Fill({color: '#4baf69'});
    var green = new ol.style.Fill({color: '#ffcf4c'});
    var yellow = new ol.style.Fill({color: '#ce6700'});
    var red = new ol.style.Fill({color: '#bc0508'});
    var stroke = new ol.style.Stroke({color: '#4A12ED',width: 1.25});

    /*************** Initializations *******************/
    var features =[];

    $scope.columns;
    $scope.featureData;

    $scope.parcelFeaturesData;
    $scope.parcelFeaturesColumns;
    $scope.ecValueMap = 0;
    var resultMap;
    var resultLayer;
    var layerName = "layer name";
    var polygons={};
    var sphere = new ol.Sphere(6378137);

    var zonesArea =[];
    var container = document.getElementById('information');
    var wmsSource;
    var map;
    var colorMap;

    var wmsLayer = new ol.layer.Tile({
        source : wmsSource
    });
    var view = new ol.View({
        center : ol.proj.fromLonLat([ 5.16140, 51.31119 ]),
        zoom : 15
    });
    var selected_polygon_style = {
        strokeWidth: 5,
        strokeColor: 'purple'
    };
    /******************** TOOLBAR init********************/
    window.app = {};
    var app = window.app;
    //  var drawSource = new ol.source.Vector({wrapX: false});
    var action = 'None';
    var draw =null;
    var featureID = 0;
    var selectedFeatureID;
    var singleClick = null;
    var sourceArray=[];
    var rubber = false, pencil = false;

    var raster = new ol.layer.Tile({
      source: new ol.source.OSM(),

    });

    function initVector(color, s){
        return  new ol.layer.Vector({
            source: s,
            style : new ol.style.Style({
               stroke: stroke,
               fill: color
            })
        });
    }


    app.CustomToolbarControl = function(opt_options) {

        var options = opt_options || {};
        var pencilButton = document.createElement('button');
        pencilButton.classList.add("toolButton");
        pencilButton.innerHTML = '<i class="fas fa-pencil-alt"></i>';

        var eraseButton = document.createElement('button');
        eraseButton.classList.add("toolButton");
        eraseButton.innerHTML = '<i class="fas fa-eraser"></i>';

        var selectList = document.createElement("select");
        selectList.classList.add("toolSelect");

        selectList.id = "mySelect";
        selectList.onchange = function(e){
            if (pencil == false){
                pencil = true;
            }
            else{
                pencil =false;
            }
            if(action == 'None'){
                action ='Polygon';
            }
            else{
                action ='None';
            }
            addDrawInteraction();

        }
        var array = ["Poorest zone","Poor zone","Average zone","Rich zone", "Richest zone"];
        for (var i = 0; i < array.length; i++) {
        var option = document.createElement("option");
            option.value = array[i];
            option.text = array[i];
            selectList.appendChild(option);
        }

        var this_ = this;

        pencilButton.addEventListener('click', addDrawInteraction, false);
        pencilButton.addEventListener('touchstart', addDrawInteraction, false);

        eraseButton.addEventListener('click', addEraseInteraction, false);
        eraseButton.addEventListener('touchstart', addEraseInteraction, false);

        var element = document.createElement('div');
        element.className = 'ol-unselectable ol-mycontrol';
        element.appendChild(pencilButton);
        element.appendChild(eraseButton);
        element.appendChild(selectList);

        ol.control.Control.call(this, {
            element: element,
            target: options.target
        });

        function addSelect() {
            singleClick = new ol.interaction.Select();
            resultMap.addInteraction(singleClick);
            singleClick.getFeatures().on('add', function (event) {
                var properties = event.element.getProperties();
                selectedFeatureID = properties.id;
                console.log("Ive been clicked");
            });
        }

        function removeSelectedFeature(source) {
            var features = source.getFeatures();
            if (features != null && features.length > 0) {

             for (x in features) {
                var properties = features[x].getProperties();
                var id = properties.id;
                if (id == selectedFeatureID) {
                    source.removeFeature(features[x]);
                    break;
                }
              }
            }
            layerTemp = SharedProperties.getVectorLayer();
            layerTemp.getSource().forEachFeature(function(feature){
                if (feature.getProperties().id == selectedFeatureID) {
                    layerTemp.getSource().removeFeature(feature);
                }
            });
            SharedProperties.setVectorLayer(layerTemp);
        }

        eraserCallback = function (event) {
            var properties = event.element.getProperties();
            selectedFeatureID = properties.id;
            removeSelectedFeature(sourceArray[selectedFeatureID]);
        }

        var reader = new jsts.io.WKTReader();

        function addDraw(){
            if(action == 'None'){

                drawS = new ol.source.Vector({wrapX: false});
                draw =  new ol.interaction.Draw({
                    source: drawS,
                    type: 'Polygon',

                });
                var geoFeatures = SharedProperties.getGeoFeatures();
                var selectedId;
                if(selectList.value == "Poorest zone"){
                    resultMap.addLayer(initVector(red,drawS));
                    selectedId = 0;
                }
                else if(selectList.value == "Poor zone"){
                    resultMap.addLayer(initVector(yellow, drawS));
                    selectedId = 1;
                }
                else if(selectList.value == "Average zone"){
                    resultMap.addLayer(initVector(green, drawS));
                    selectedId = 2;
                }
                else if(selectList.value == "Rich zone"){
                   resultMap.addLayer(initVector(cyan, drawS));
                   selectedId = 3;
                }
                else if(selectList.value == "Richest zone"){
                    resultMap.addLayer(initVector(blue, drawS));
                    selectedId = 4;
                }
                draw.on('drawend', function (event) {
                    featureID = featureID + 1;
                    sourceArray[featureID] = drawS;

                    event.feature.setProperties({
                        'fid': selectedId,
                        'id':featureID
                    })

                    i = 0;
                    var parser = new jsts.io.OL3Parser();
                    layer.getSource().forEachFeature(function(feature){

                        if(selectedId == feature.getProperties().fid){
                            var a = parser.read(feature.getProperties().geometry);
                            var b = parser.read(event.feature.getGeometry());
                            var union = a.union(b);

                            feature.setGeometry(parser.write(union));

                            geoFeatures[i]['area'] = ((feature.getProperties().geometry.getArea()/10000)/2.5);

                            feature.setProperties();
                            i++;
                        }
                    });
                    if( i==0 ){
                        getFeatures = [];
                      //  var b = parser.read(event.feature.getGeometry());
                        layer.getSource().addFeature(event.feature);
                        console.log("create new features"+JSON.stringify(event.feature.getGeometry().getCoordinates()));

                        geoFeatures[selectedId]['area'] = (event.feature.getProperties().geometry.getArea()/10000);
                    }
                    SharedProperties.setGeoFeatures(geoFeatures);
                    SharedProperties.setVectorLayer(layer);

                 })

                resultMap.addInteraction(draw);
                action ='Polygon';

            }
            else{
                action = 'None';
                draw = null;
            }
        }



        function addDrawInteraction() {
            if(rubber == true){
                document.getElementById("result-map").classList.remove("rubber-cursor");
                rubber = false;
            }
            if (pencil == false){
                document.getElementById("result-map").classList.add("pencil-cursor");
                pencil = true;
            }
            else{
                document.getElementById("result-map").classList.remove("pencil-cursor");
                pencil =false;
            }
            if( draw != null){
                resultMap.removeInteraction(draw);
            }
            if (singleClick!= null){
                resultMap.removeInteraction(singleClick);

                singleClick.getFeatures().un('add', eraserCallback);
            }
            addDraw();
        };

        function addEraseInteraction(){
            if(rubber == false){
                if (pencil == true){
                    document.getElementById("result-map").classList.remove("pencil-cursor");
                    pencil = false;
                }

                document.getElementById("result-map").classList.add("rubber-cursor");

                resultMap.removeInteraction(draw);
                if(action == 'None'){
                    action ='Polygon';
                }
                else{
                    action = 'None';
                 }
                singleClick = new ol.interaction.Select();
                resultMap.addInteraction(singleClick);

                singleClick.getFeatures().on('add', eraserCallback);
                rubber = true;
            }
            else{
                rubber = false;
                document.getElementById("result-map").classList.remove("rubber-cursor");

            }
        }
    };
    ol.inherits(app.CustomToolbarControl, ol.control.Control);

    /*********************Functions**********************/

    function getFeatureLayers(name){
        $('.div-loader').show();
        if( name != null && name != undefined ){
            $http({
                url : 'layerfeatures',
                method : "GET",
                params : {
                    layername : name,
                }
            }).success(function(res) {
                $scope.parcelFeaturesData = res;
                $scope.parcelFeaturesColumns= res[0];
                SharedProperties.setEcMeasurements(res);
                SharedProperties.setGeoFeatures(res);
                GeoService.initValues(0);
                callFeaturesController();
            });

        }
        else{
            if(resultMap != undefined){
                var oldLayer=resultMap.getLayers().getArray()[1];
                resultMap.removeLayer(oldLayer);
             }else{
                resultMap= SharedProperties.getMap();
                drawMap();
             }
    //        SharedProperties.setVectorLayer(resultMap.getLayers().getArray()[0]);
            $('.div-loader').hide();
        }
    }

    var layer;
    function callFeaturesController(){
     var _geojson_vectorSource;
        if(SharedProperties.getCalculate() == 0){

            $http({
                url : 'map',
                method : "GET",
                params : {
                  layer : layerName,
              },
            }).success( function(res) {

            _geojson_vectorSource = new ol.source.Vector({
                features: (new ol.format.GeoJSON()).readFeatures(res, { featureProjection: 'EPSG:3857' }),
            });
            layer = new ol.layer.Vector({
                source: _geojson_vectorSource,
            });
            drawMap();
            })
            .error( function(res){
                console.log("error");
            });
        }
        else{
            if(SharedProperties.getVectorLayer() !=null ){
                layer = SharedProperties.getVectorLayer();
            }

            drawMap();
        }

     }

     function drawMap(){
        count =0;


        if(layer != null && layer.getSource().getState() === 'ready'){

            layer.getSource().forEachFeature(function(feature){
                if(feature.getProperties().fid == 0 || (feature.getProperties().fid ==undefined && count == 0)){
                     style = new ol.style.Style({
                            stroke: stroke,
                            fill: red
                        });
                        feature.setStyle(style);
                }
                else if(feature.getProperties().fid == 1 || (feature.getProperties().fid ==undefined && count == 1)){
                     style = new ol.style.Style({
                            stroke: stroke,
                            fill: yellow
                        });
                    feature.setStyle(style);
                }
                else if(feature.getProperties().fid == 2 || (feature.getProperties().fid ==undefined && count == 2)){
                     style = new ol.style.Style({
                            stroke: stroke,
                            fill: green
                        });
                    feature.setStyle(style);
                }
                else if(feature.getProperties().fid == 3 || (feature.getProperties().fid ==undefined && count == 3)){
                     style = new ol.style.Style({
                            stroke: stroke,
                            fill: cyan
                        });
                    feature.setStyle(style);
                }
                else if(feature.getProperties().fid == 4 || (feature.getProperties().fid ==undefined && count == 4)){
                     style = new ol.style.Style({
                            stroke: stroke,
                            fill: blue
                        });
                    feature.setStyle(style);
                }

                count++;
            });
        }
        if(SharedProperties.getCalculate() == 0){
            SharedProperties.setVectorLayer(layer);
        }

        GeoService.createFeaturesFromGeoJson();
//
        if(resultMap == undefined || SharedProperties.getCalculate()==3){
            layer = null;

            layer = SharedProperties.getVectorLayer();
            if(SharedProperties.getCalculate() != 1){
                resultMap = new ol.Map({
                     controls: ol.control.defaults({
                       attributionOptions: /** @type {olx.control.AttributionOptions} */ ({
                         collapsible: false
                       })
                     }).extend([
                       new app.CustomToolbarControl()
                     ]),
                    layers: [new ol.layer.Tile({
                        source : new ol.source.BingMaps({
                            key: 'BING MAPS API CODE',
                            imagerySet: 'AerialWithLabels'})
                    }),layer],
                    target: 'result-map',
                });
            }
            else{

                resultMap = new ol.Map({
                    layers: [new ol.layer.Tile({
                        source : new ol.source.BingMaps({
                            key: 'BING MAPS API CODE',
                             imagerySet: 'AerialWithLabels'})
                    }),layer],
                    target: 'result-map',
                    view: new ol.View({
                      center: ol.proj.transform([-1.81185, 52.443141], 'EPSG:4326', 'EPSG:3857'),
                      zoom: 6
                    })
                });
            }

            if( layer != null && layer != undefined  && layer.getSource().getExtent()!= "Infinity,Infinity,-Infinity,-Infinity"){
                resultMap.getView().fit(layer.getSource().getExtent(), resultMap.getSize());

                if(SharedProperties.getCalculate() != 0){
                     setTimeout( function() { resultMap.updateSize();}, 10);
                     resultMap.getView().fit(layer.getSource().getExtent(), resultMap.getSize());
                }
            }
            else{

                resultMap.setView(new ol.View({
                  center: ol.proj.transform([0, 0], 'EPSG:4326', 'EPSG:3857'),
                  zoom: 2
                }));
                if(SharedProperties.getCalculate() !=0 ){
                     setTimeout( function() { resultMap.updateSize();}, 10);
                }
            }
        }
        else{
            var oldLayer=resultMap.getLayers().getArray()[1];
            resultMap.removeLayer(oldLayer);
            var newLayer = SharedProperties.getVectorLayer();
            resultMap.addLayer(newLayer);
            if(layer == null){
                layer = SharedProperties.getVectorLayer();
            }
            resultMap.getView().fit(layer.getSource().getExtent(), resultMap.getSize());

        }
        $('.div-loader').hide();
        SharedProperties.setMap(resultMap);
    }


    $scope.$watch(function(){
        return SharedProperties.getLayerName();
    }, function (newValue) {
        layerName = newValue;

        if(SharedProperties.getCalculate() != 1 ){
            getFeatureLayers(layerName);
        }
        else{
           callFeaturesController();
        }

    });

    var highlight;

    var featureOverlay = new ol.layer.Vector({
        source: new ol.source.Vector(),
        map: resultMap,
        style: function(feature, resolution) {
            var text = resolution < 5000 ? feature.get('fid') : '';
            if (!highlightStyleCache[text]) {
                highlightStyleCache[text] = new ol.style.Style({
                stroke: new ol.style.Stroke({
                color: '#f00',
                width: 1

              }),
              fill: new ol.style.Fill({
                color: 'rgba(255,0,0,0.1)'
              }),
              text: new ol.style.Text({
                font: '12px Calibri,sans-serif',
                text: text,
                fill: new ol.style.Fill({
                  color: '#000'
                }),
                stroke: new ol.style.Stroke({
                  color: '#f00',
                  width: 3
                })
              })
            });
          }
          return highlightStyleCache[text];
        }
      });

					  
					  
    var container = document.getElementById('popup');
    var content = document.getElementById('popup-content');
    var closer = document.getElementById('popup-closer');
    var header = document.getElementById('result-map-header');

					      /**
					       * Create an overlay to anchor the popup to the map.
					       */
    var overlay = new ol.Overlay(/** @type {olx.OverlayOptions} */ ({
        element: container,
        autoPan: true,
        autoPanAnimation: {
          duration: 250
        }
    }));


    /**
    * Add a click handler to hide the popup.
    * @return {boolean} Don't follow the href.
    */
    closer.onclick = function() {
    overlay.setPosition(undefined);
    closer.blur();
    return false;
    };


     $scope.storeShape = function(){

     }

					 
    $scope.calculate = function(){
        GeoService.calculateZones(SharedProperties.getTotalCompostPerHer(),SharedProperties.getZones());
        $scope.resultColumnsNames=["Zone Id","Area in Hectares","EC value", "Total Compost"];
    }

    $rootScope.$on("CallExtractPng", function(event,args){
        if(SharedProperties.getEventToggle()){
           $scope.extractPng(args.message);
           SharedProperties.setEventToggle(false);
        }
    });


    $scope.extractPng = function(name) {
        if(resultMap != undefined){
            resultMap.once('postcompose', function(event) {
                var canvas = event.context.canvas;
                if (navigator.msSaveBlob) {
                    navigator.msSaveBlob(canvas.msToBlob(), name+'.png');
                } else {
                    canvas.toBlob(function(blob) {
                        saveAs(blob, name+'.png');
                    });
                }
            });
            resultMap.renderSync();
        }
    }





});



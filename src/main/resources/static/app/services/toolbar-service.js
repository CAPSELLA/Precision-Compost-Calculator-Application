angular.module('PrecisionCompostCalculatorApp')
// Creating the Angular Service for storing logged user details
.service('ToolbarService', function($rootScope,$window, $http) {

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
        }
    };


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
            map.addInteraction(singleClick);
            console.log("SELECT");
            singleClick.getFeatures().on('add', function (event) {
                var properties = event.element.getProperties();
                selectedFeatureID = properties.id;
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
        }

        eraserCallback = function (event) {
            var properties = event.element.getProperties();
            selectedFeatureID = properties.id;

            removeSelectedFeature(sourceArray[selectedFeatureID]);
            console.log("->"+selectedFeatureID);
        }

        function addDraw(){
            if(action == 'None'){
                //console.log("select:"+resultMap.getLayers().getArray().length);
                //resultMap.removeLayer(resultMap.getLayers().getArray()[resultMap.getLayers().getArray().length-1])
                drawS = new ol.source.Vector({wrapX: false});
                draw =  new ol.interaction.Draw({
                    source: drawS,
                    type: 'Polygon',

                });
                if(selectList.value == "Poorest zone"){
                    resultMap.addLayer(initVector(red,drawS));
                }
                else if(selectList.value == "Poor zone"){
                    resultMap.addLayer(initVector(yellow, drawS));
                }
                else if(selectList.value == "Average zone"){
                    resultMap.addLayer(initVector(green, drawS));
                }
                else if(selectList.value == "Rich zone"){
                   resultMap.addLayer(initVector(cyan, drawS));
                }
                else if(selectList.value == "Richest zone"){
                    resultMap.addLayer(initVector(blue, drawS));
                }

                 draw.on('drawend', function (event) {
                    featureID = featureID + 1;
                    sourceArray[featureID] = drawS;
                    event.feature.setProperties({
                        'id': featureID
                    })
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
});
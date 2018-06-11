package gr.uoa.di.madgik.controller;

import java.io.*;
import java.net.URL;
import java.util.*;

import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.Response;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.org.apache.xpath.internal.SourceTree;
import gr.uoa.di.madgik.model.Person;
import gr.uoa.di.madgik.model.PersonHasShapes;
import gr.uoa.di.madgik.service.UserService;
import gr.uoa.di.madgik.utils.Utils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.opengis.feature.Feature;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.spatial.Intersects;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureStore;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.AreaFunction;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.Identifier;
import org.opengis.filter.spatial.Intersects;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import gr.uoa.di.madgik.model.GeoFeaturesSingleton;
import gr.uoa.di.madgik.model.LayerFeature;
import gr.uoa.di.madgik.service.GeoService;


@RestController
@Component
public class GeoController {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Value("${folder.location}")
    String containerFolder;

    @Value("${geoserver.url}")
    String geoserverUrl;

    @Value("${geoserver.wfs}")
    String geoserverWfs;

    @Value("${geoserver.wms}")
    String geoserverWms;

    @Value("${geoserver.map}")
    String geoserverMap;

    @Value("${geoserver.featuresRequest}")
    String featureRequest;

    @Value("${geoserver.Jacob_parcels}")
    String jacobParcels;

    @Value("${geoserver.workspace}")
    String workspace;

    @Value("${geoserver.username}")
    String username;

    @Value("${geoserver.password}")
    String password;

    @Value("${geoserver.defaultLayer}")
    String defaultLayer;

    @Value("${geoserver.defaultEcField}")
    String ecFieldName;

    @Value("${geoserver.defaultIdField}")
    String idFieldName;

    private GeoService geoservice;
    private UserService userService;


    @Autowired
    public void setGeoservice(GeoService geoservice) {
        this.geoservice = geoservice;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }


    @RequestMapping(value = "/features", method = RequestMethod.GET)
    @ResponseBody
    public String getAllFeatures(@RequestParam("layername") String layername, HttpServletResponse response) throws IOException {

        String plainCreds = username + ":" + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + base64Creds);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("request", featureRequest);
        params.add("outputFormat", "application/json");
        params.add("service", "WFS");
        params.add("version", "1.3.0");
        params.add("typeName", layername);
        ResponseEntity<?> entityResponse;
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(geoserverUrl + geoserverWfs).queryParams(params).build();

        HttpEntity<String> entity = new HttpEntity<String>(headers);
        try {

            entityResponse = restTemplate.postForEntity(uriComponents.toUri(), entity, String.class);

            return entityResponse.getBody().toString();
        } catch (HttpStatusCodeException exception) {

            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR).toString();
        }

    }

    @RequestMapping(value = "/jacobFeatures", method = RequestMethod.GET)
    @ResponseBody
    public String getAllFeatures(HttpServletResponse response) throws IOException {

        if (GeoFeaturesSingleton.instance().getGeoJsonFeatures() != null) {
            return GeoFeaturesSingleton.instance().getGeoJsonFeatures();
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/map", method = RequestMethod.GET)
    @ResponseBody
    public String getLayer(@RequestParam("layer") String layername, HttpServletResponse response) throws IOException {
        geoservice.getAllFeatures(layername);
        return GeoFeaturesSingleton.instance().getGeoJsonFeatures();
    }

    @RequestMapping(value = "/layers", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<?> getAllLayers(@RequestParam("email") String email, @RequestParam("password") String pass, HttpServletResponse response) throws IOException {
        String plainCreds = username + ":" + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + base64Creds);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("request", "getCapabilities");
        params.add("outputFormat", "application/json");
        params.add("service", "WMS");
        params.add("version", "1.3.0");
        ResponseEntity<?> entityResponse;
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(geoserverUrl + workspace + geoserverWms).queryParams(params).build();

        System.out.println(uriComponents.toString());

        HttpEntity<String> entity = new HttpEntity<String>(headers);
        try {
            List<String> layers = new ArrayList<>();
            Optional<Person> user;
            entityResponse = restTemplate.postForEntity(uriComponents.toUri(), entity, String.class);
            if ((user = userService.isUserAuthorized(email, pass)).isPresent()) {
                List<PersonHasShapes> pLayers = userService.findLayersByPerson(user.get());
                layers = pLayers.stream().map(x -> x.getShapeName()).collect(Collectors.toList());
            }
            return new ResponseEntity<Object>(layers, HttpStatus.OK);
        } catch (HttpStatusCodeException exception) {

            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/layerfeatures", method = RequestMethod.GET)
    @ResponseBody
    public List<LayerFeature> getLayerFeatures(@RequestParam("layername") String layername, HttpServletResponse response) throws IOException {
        List<LayerFeature> layerFeatures = new ArrayList<>();
        System.out.println("layername is:" + layername + "-");
        geoservice.getGeofeaturesFromServer(layername);
        return GeoFeaturesSingleton.instance().getLayerFeatures();
    }


    @RequestMapping(value = "/area", method = RequestMethod.GET)
    @ResponseBody
    public String getLayerFeaturesAndArea(@RequestParam("layername") String layername, HttpServletResponse response) throws IOException {

        String plainCreds = username + ":" + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + base64Creds);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("request", featureRequest);
        params.add("outputFormat", "application/json");
        params.add("service", "WFS");
        params.add("version", "1.3.0");
        params.add("typeName", layername);
        ResponseEntity<?> entityResponse;
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(geoserverUrl + geoserverWfs).queryParams(params).build();

        HttpEntity<String> entity = new HttpEntity<String>(headers);
        try {

            entityResponse = restTemplate.postForEntity(uriComponents.toUri(), entity, String.class);


            JSONObject jsnobject = new JSONObject(entityResponse.getBody().toString());
            float sum = 0;
            JSONArray jsonarray = jsnobject.getJSONArray("features");
            for (int i = 0; i < jsonarray.length(); i++) {
                JSONObject jsonobject = jsonarray.getJSONObject(i);
                JSONArray coordinates = jsonobject.getJSONObject("geometry").getJSONArray("coordinates");
                GeometryJSON g = new GeometryJSON();
                Reader reader = new StringReader(jsonobject.getJSONObject("geometry").toString());
                MultiPolygon multi = g.readMultiPolygon(reader);
                Reader readerGeo = new StringReader(jsonobject.toString());
                CoordinateReferenceSystem sourceCRS = CRS.decode("urn:ogc:def:crs:EPSG::4326");
                CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:3857");

                MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
                Geometry geometry = g.read(readerGeo);

                Geometry targetGeometry = JTS.transform(geometry, transform);
                sum += targetGeometry.getArea();
            }

            return entityResponse.getBody().toString();
        } catch (HttpStatusCodeException exception) {

            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR).toString();

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR).toString();

        } catch (NoSuchAuthorityCodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        } catch (FactoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MismatchedDimensionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR).toString();

    }

    protected MultiPolygon buildMultiPolygon(String multip) {
        try {
            WKTReader reader = new WKTReader();
            MultiPolygon mp = (MultiPolygon) reader.read(multip);
            mp.setSRID(4326);
            return mp;
        } catch (ParseException ex) {
            throw new RuntimeException("Unexpected exception: " + ex.getMessage(), ex);
        }
    }


    @RequestMapping(value = "/wfsArea", method = RequestMethod.GET)
    @ResponseBody
    public String getLayerFeaturesArea(@RequestParam("layername") String layername, HttpServletResponse response) throws IOException {
        String plainCreds = username + ":" + password;
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.getEncoder().encode(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Basic " + base64Creds);

        String getCapabilities = base64Creds + "@" + geoserverUrl + "/wfs?REQUEST=GetCapabilities";

        Map<String, String> connectionParameters = new HashMap<>();
        connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL", getCapabilities);

        // Step 2 - connection
        DataStore data = DataStoreFinder.getDataStore(connectionParameters);

        // Step 3 - discouvery
        SimpleFeatureType schema = data.getSchema(layername);
        // Step 4 - target
        SimpleFeatureSource source = data.getFeatureSource(layername);
        // Step 5 - query
        String geomName = schema.getGeometryDescriptor().getLocalName();
        Envelope bbox = new Envelope(-100.0, -70, 25, 40);

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
        Object polygon = JTS.toGeometry(bbox);
        Intersects filter = ff.intersects(ff.property(geomName), ff.literal(polygon));

        Query query = new DefaultQuery(layername, filter, new String[]{geomName});
        SimpleFeatureCollection features = source.getFeatures(query);

        ReferencedEnvelope bounds = new ReferencedEnvelope();
        SimpleFeatureIterator iterator = features.features();
        try {
            while (iterator.hasNext()) {
                Feature feature = (Feature) iterator.next();
                bounds.include(feature.getBounds());
            }
        } finally {
            iterator.close();
        }

        return "";
    }

    @RequestMapping(value = "/storeShape", method = RequestMethod.POST)
    @ResponseBody
    public String storeShape(@RequestBody Map<String, String> request, HttpServletResponse response) throws IOException {
        String shapefileName = "";
        String shapeName = request.get("shapefileName");
        String geoJson = request.get("geoJson");
        String email = request.get("email");
        String privacy = request.get("privacy");
        if (!FilenameUtils.getExtension(shapeName).equals(".shp")) {
            shapefileName = shapeName + ".shp";
        } else {
            shapeName = FilenameUtils.removeExtension(shapeName);
        }
        String zipPath = geoservice.geoJsonToShape(geoJson, shapefileName);
        if (zipPath.equals("failed")) {

            return "not shape material";
        } else {
            ResponseEntity responseEntity = geoservice.storeShapefile(zipPath);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                userService.savePersonHasShapes(email, shapeName, privacy);
            }
            Utils.deleteDir(new File(zipPath));
            return responseEntity.getStatusCode().toString();
        }
    }


    @RequestMapping(value = "/downloadShape", method = RequestMethod.POST)
    @ResponseBody
    public void downloadShape(@RequestBody Map<String, String> request, HttpServletResponse response) throws IOException {
        String shapefileName = request.get("shapefileName");
        String geoJson = request.get("geoJson");
        if (!FilenameUtils.getExtension(shapefileName).equals(".shp")) {
            shapefileName = shapefileName + ".shp";
        }
        String zipPath = geoservice.geoJsonToShape(geoJson, shapefileName);
        File zipFile = new File(zipPath);
        if (!zipPath.equals("failed")) {
            InputStream targetStream = new FileInputStream(zipFile);
            IOUtils.copy(targetStream, response.getOutputStream());
            targetStream.close();
            Utils.deleteDir(new File(zipPath));
        }
    }

    @RequestMapping(value = "/uploadShape", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("license") String license, HttpServletResponse response) throws IOException {
        ResponseEntity responseEntity = geoservice.storeShapefile(file);
        return responseEntity;
    }


    public static JSONArray objectToJSONArray(Object object) {
        Object json = null;
        JSONArray jsonArray = null;
        try {
            json = new JSONTokener(object.toString()).nextValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (json instanceof JSONArray) {
            jsonArray = (JSONArray) json;
        }
        return jsonArray;
    }
}

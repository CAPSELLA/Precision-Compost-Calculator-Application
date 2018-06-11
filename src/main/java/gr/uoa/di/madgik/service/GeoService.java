package gr.uoa.di.madgik.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import gr.uoa.di.madgik.interfaces.CapsellaRepository;
import gr.uoa.di.madgik.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.data.*;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.json.JSONArray;
import org.json.JSONException;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
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
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import gr.uoa.di.madgik.model.GeoFeaturesSingleton;
import gr.uoa.di.madgik.model.LayerFeature;
import gr.uoa.di.madgik.utils.WriteShapefile;

@Service
public class GeoService {

    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    private static final int BUFFER_SIZE = 100000;
    private static final String WFS_CALL = "capsella/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=capsella:ec_measurements_jacob&maxFeatures=50&outputFormat=SHAPE-ZIP";
    private static ObjectMapper mapper = new ObjectMapper();

    @Value("${geoserver.url}")
    String geoserverUrl;

    @Value("${geoserver.wfs}")
    String geoserverWfs;

    @Value("${geoserver.wms}")
    String geoserverWms;

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

    @Value("${shapefile.directory}")
    String folderPath;

    @Value("${folder.location}")
    String containerFolder;

    @Value("${geoserver.defaultEcField}")
    String ecFieldName;

    @Value("${geoserver.defaultIdField}")
    String idFieldName;

    @Value("${compost.defaultTotal}")
    Float defaultTotal;

    @Value("${compost.defaultPoor}")
    Double defaultPoor;

    @Value("${compost.defaultPoorest}")
    Double defaultPoorest;

    @Value("${compost.defaultAverage}")
    Double defaultAverage;

    @Value("${compost.defaultRich}")
    Double defaultRich;

    @Value("${compost.defaultRichest}")
    Double defaultRichest;

    @Value("${geoserver.defaultLayer}")
    String defaultLayer;

    private CapsellaRepository geoserverRepo;

    @Autowired
    public GeoService(CapsellaRepository capsellaRepository) {
        geoserverRepo = capsellaRepository;
    }

    @PostConstruct
    public void readShapefile() {

        Utils.deleteContainDir(containerFolder);

        File folder = new File(containerFolder + File.separator + folderPath);

        try {

            GeoFeaturesSingleton.instance();
            String userpass = username + ":" + password;

            URL url = new URL(
                    geoserverUrl + WFS_CALL);
            String authEncoded = new String(Base64.getEncoder().encode(userpass.getBytes()));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + authEncoded);

            File file = new File("test.zip");

            InputStream in = (InputStream) connection.getInputStream();
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            for (int b; (b = in.read()) != -1; ) {
                out.write(b);
            }
            out.close();
            in.close();
            String shapefileName = "";

            if (!folder.exists()) {
                folder.mkdir();
            }

            // get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
            // get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            byte[] buffer = new byte[BUFFER_SIZE];

            while (ze != null) {

                String fileName = ze.getName();
                if (FilenameUtils.getExtension(fileName).equals("shp")) {
                    Path p = Paths.get(fileName);
                    shapefileName = p.getFileName().toString();

                }
                File newFile = new File(containerFolder + File.separator + folderPath + File.separator + fileName);
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.flush();
                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            File file2 = new File(containerFolder + File.separator + folderPath + File.separator + shapefileName);

            Map<String, Object> map = new HashMap<>();

            map.put("url", file2.toURI().toURL());

            DataStore dataStore = DataStoreFinder.getDataStore(map);
            String typeName = dataStore.getTypeNames()[0];
            GeoFeaturesSingleton.get_instance().setStore(dataStore);
            FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);

            Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM,
            // 10,20,30,40)")

            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);

            GeoFeaturesSingleton.instance().setCollection(collection);

            CoordinateReferenceSystem dataCRS = collection.getSchema().getCoordinateReferenceSystem();
            float sum = 0;
            boolean lenient = true; // allow for some error due to different
            // datums
            List<LayerFeature> layerFeatures = new ArrayList<>();

            try (FeatureIterator<SimpleFeature> features = collection.features()) {
                GeoFeaturesSingleton.instance().setFeatures(features);
                List<Geometry> geometries = new ArrayList<>();
                List<Integer> areas = new ArrayList<>();

                while (features.hasNext()) {
                    SimpleFeature feature = features.next();

                    LayerFeature layerFeature = new LayerFeature();

                    for (Property property : feature.getProperties()) {
                        if (property.getName().toString().equals(ecFieldName)) {
                            layerFeature.setEcValue(Float.valueOf(property.getValue().toString()));
                        } else if (property.getName().toString().equals(idFieldName)) {
                            layerFeature.setId(property.getValue().toString());
                        }
                    }


                    GeometryAttribute geom = feature.getDefaultGeometryProperty();
                    Geometry geo = (Geometry) geom.getValue();

                    CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:5243");

                    MathTransform transform = CRS.findMathTransform(dataCRS, targetCRS);
                    Geometry targetGeometry = JTS.transform(geo, transform);
                    sum += (targetGeometry.getArea() / 10000);
                    geometries.add(targetGeometry);
                    layerFeature.setArea(targetGeometry.getArea() / 10000);
                    layerFeatures.add(layerFeature);
                }
                int count = 0;

                for (LayerFeature layerFeature : layerFeatures) {
                    if (layerFeature.getId() == null) {
                        layerFeature.setId(String.valueOf(count));
                    }
                    count++;
                }
                GeoFeaturesSingleton.instance().setGeometries(geometries);
                GeoFeaturesSingleton.instance().setZoneAreas(areas);
                GeoFeaturesSingleton.instance().setLayerFeatures(layerFeatures);
                GeoFeaturesSingleton.instance().setTotalArea(sum);

                GeoFeaturesSingleton.instance().getLayerFeatures().get(0).setTonsPerHectare(
                        defaultPoorest * GeoFeaturesSingleton.instance().getLayerFeatures().get(0).getArea());
                GeoFeaturesSingleton.instance().getLayerFeatures().get(1).setTonsPerHectare(
                        defaultPoor * GeoFeaturesSingleton.instance().getLayerFeatures().get(1).getArea());
                GeoFeaturesSingleton.instance().getLayerFeatures().get(2).setTonsPerHectare(
                        defaultAverage * GeoFeaturesSingleton.instance().getLayerFeatures().get(2).getArea());
                GeoFeaturesSingleton.instance().getLayerFeatures().get(3).setTonsPerHectare(defaultRich * GeoFeaturesSingleton.instance().getLayerFeatures().get(3).getArea());
                GeoFeaturesSingleton.instance().getLayerFeatures().get(4).setTonsPerHectare(defaultRichest * GeoFeaturesSingleton.instance().getLayerFeatures().get(4).getArea());
                // Remove temp files
                if (file.delete()) {
                    System.out.println("temp file deleted");
                } else {
                    System.out.println("Nothing deleted");
                }


                if (file2.delete()) {
                    System.out.println("tem file2 deleted");
                } else {
                    System.out.println("file2 not deleted");
                }
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
            //	deleteDir(folder);

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
//		deleteDir(folder);

    }

    public void getGeofeaturesFromServer(String layerName) {
        Utils.deleteContainDir(containerFolder);

        File folder = new File(containerFolder + File.separator + folderPath);

        try {

            GeoFeaturesSingleton.instance();
            String userpass = username + ":" + password;

            URL url = new URL(
                    geoserverUrl + "capsella/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=" + layerName + "&maxFeatures=50&outputFormat=SHAPE-ZIP");
            String authEncoded = new String(Base64.getEncoder().encode(userpass.getBytes()));

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + authEncoded);

            File file = new File(containerFolder + File.separator + layerName + ".zip");

            InputStream in = (InputStream) connection.getInputStream();
            OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            for (int b; (b = in.read()) != -1; ) {
                out.write(b);
            }
            out.close();
            in.close();
            String shapefileName = "";

            if (!folder.exists()) {
                folder.mkdir();
            }

            // get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
            // get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            byte[] buffer = new byte[BUFFER_SIZE];

            while (ze != null) {

                String fileName = ze.getName();
                if (FilenameUtils.getExtension(fileName).equals("shp")) {
                    Path p = Paths.get(fileName);
                    shapefileName = p.getFileName().toString();

                }
                File newFile = new File(containerFolder + File.separator + folderPath + File.separator + fileName);

                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.flush();
                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            File file2 = new File(containerFolder + File.separator + folderPath + File.separator + shapefileName);

            Map<String, Object> map = new HashMap<>();

            map.put("url", file2.toURI().toURL());

            DataStore dataStore = DataStoreFinder.getDataStore(map);
            String typeName = dataStore.getTypeNames()[0];
            GeoFeaturesSingleton.instance().setStore(dataStore);
            FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);

            Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM,
            // 10,20,30,40)")

            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
            GeoFeaturesSingleton.instance().setCollection(collection);
            CoordinateReferenceSystem dataCRS = collection.getSchema().getCoordinateReferenceSystem();
            float sum = 0;
            boolean lenient = true; // allow for some error due to different
            // datums
            List<LayerFeature> layerFeatures = new ArrayList<>();
            GeoFeaturesSingleton.instance().setLayerFeatures(layerFeatures);
            try (FeatureIterator<SimpleFeature> features = collection.features()) {
                GeoFeaturesSingleton.instance().setFeatures(features);
                List<Geometry> geometries = new ArrayList<>();
                List<Integer> areas = new ArrayList<>();

                while (features.hasNext()) {
                    SimpleFeature feature = features.next();
                    LayerFeature layerFeature = new LayerFeature();

                    for (Property property : feature.getProperties()) {
                        if (property.getName().toString().equals(ecFieldName)) {
                            layerFeature.setEcValue(Float.valueOf(property.getValue().toString()));
                        } else if (property.getName().toString().equals(idFieldName)) {
                            layerFeature.setId(property.getValue().toString());
                        }
                    }

                    GeometryAttribute geom = feature.getDefaultGeometryProperty();
                    Geometry geo = (Geometry) geom.getValue();
                    Envelope envelope = geo.getEnvelopeInternal();

                    CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:5243");

                    MathTransform transform = CRS.findMathTransform(dataCRS, targetCRS);
                    // create with supplied crs

                    Geometry targetGeometry = JTS.transform(geo, transform);
                    sum += (targetGeometry.getArea() / 10000);
                    geometries.add(targetGeometry);
                    layerFeature.setArea(targetGeometry.getArea() / 10000);
                    layerFeatures.add(layerFeature);

                }

                GeoFeaturesSingleton.instance().setGeometries(geometries);
                GeoFeaturesSingleton.instance().setZoneAreas(areas);
                GeoFeaturesSingleton.instance().setLayerFeatures(layerFeatures);
                GeoFeaturesSingleton.instance().setTotalArea(sum);
                Collections.sort(GeoFeaturesSingleton.instance().getLayerFeatures());

                GeoFeaturesSingleton.instance().getLayerFeatures().get(0).setTonsPerHectare(
                        defaultPoorest * GeoFeaturesSingleton.instance().getLayerFeatures().get(0).getArea());
                if (GeoFeaturesSingleton.instance().getLayerFeatures().size() > 1) {
                    GeoFeaturesSingleton.instance().getLayerFeatures().get(1).setTonsPerHectare(
                            defaultPoor * GeoFeaturesSingleton.instance().getLayerFeatures().get(1).getArea());
                }
                if (GeoFeaturesSingleton.instance().getLayerFeatures().size() > 2) {
                    GeoFeaturesSingleton.instance().getLayerFeatures().get(2).setTonsPerHectare(
                            defaultAverage * GeoFeaturesSingleton.instance().getLayerFeatures().get(2).getArea());
                }
                if (GeoFeaturesSingleton.instance().getLayerFeatures().size() > 3) {
                    GeoFeaturesSingleton.instance().getLayerFeatures().get(3).setTonsPerHectare(defaultRich * GeoFeaturesSingleton.instance().getLayerFeatures().get(3).getArea());

                }

                if (GeoFeaturesSingleton.instance().getLayerFeatures().size() > 4) {
                    GeoFeaturesSingleton.instance().getLayerFeatures().get(4).setTonsPerHectare(defaultRichest * GeoFeaturesSingleton.instance().getLayerFeatures().get(4).getArea());
                }
                // Remove temp files

                if (file.delete()) {
                    System.out.println("temp file deleted");
                } else {
                    System.out.println("Nothing deleted");
                }

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
            //	deleteDir(folder);

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //deleteDir(folder);
    }


    @PostConstruct
    public String getAllFeatures() throws IOException {

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
        params.add("typeName", defaultLayer);
        ResponseEntity<?> entityResponse;
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(geoserverUrl + geoserverWfs).queryParams(params).build();

        System.out.println(uriComponents.toString());

        HttpEntity<String> entity = new HttpEntity<String>(headers);
        try {
            entityResponse = restTemplate.postForEntity(uriComponents.toUri(), entity, String.class);
            GeoFeaturesSingleton.instance().setGeoJsonFeatures(entityResponse.getBody().toString());
            return entityResponse.getBody().toString();
        } catch (HttpStatusCodeException exception) {

            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR).toString();
        }

    }

    public String getAllFeatures(String layerName) throws IOException {

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
        params.add("typeName", layerName);
        ResponseEntity<?> entityResponse;
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(geoserverUrl + geoserverWfs).queryParams(params).build();

        HttpEntity<String> entity = new HttpEntity<String>(headers);
        try {
            entityResponse = restTemplate.postForEntity(uriComponents.toUri(), entity, String.class);
            GeoFeaturesSingleton.instance().setGeoJsonFeatures(entityResponse.getBody().toString());
            return entityResponse.getBody().toString();
        } catch (HttpStatusCodeException exception) {

            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR).toString();
        }

    }

    public static String geoJsonToShape(String geoJson, String shapefileName) throws IOException {


        File shpFolder = new File(FilenameUtils.removeExtension(shapefileName) + "_zip");
        shpFolder.setWritable(true);
        shpFolder.mkdir();
        File shpFile = new File(shpFolder.getAbsolutePath() + "/" + shapefileName);
        shpFile.getParentFile().setWritable(true);
        System.out.println("geoJson:" + geoJson);
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", shpFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);
        InputStream in = IOUtils.toInputStream(geoJson);
        int decimals = 15;
        GeometryJSON gjson = new GeometryJSON(decimals);
        FeatureJSON fjson = new FeatureJSON(gjson);

        FeatureCollection<SimpleFeatureType, SimpleFeature> fc = fjson.readFeatureCollection(in);
        fc.getSchema();

        try {
            WriteShapefile shpWriter = new WriteShapefile(shpFile);

            boolean created = shpWriter.writeFeatures(fc, FilenameUtils.removeExtension(shapefileName));

            if (created != true) {
                //   Utils.deleteDir(shpFolder);
                return "failed";
            }

            WriteShapefile.pack(shpFolder.getAbsolutePath(), FilenameUtils.removeExtension(shapefileName) + ".zip");
            Utils.deleteDir(shpFolder);

            return FilenameUtils.removeExtension(shapefileName) + ".zip";
        } catch (FactoryException e) {
            e.printStackTrace();
            return "failed";
        }
    }

    public String addFeaturesToShape(String filename, String geofeatures) throws IOException {
        URL tempPath = this.getClass().getClassLoader().getResource("application.properties");
        File folder = null;
        System.out.println("the new temp is:" + TMP_DIR);
        try {
            folder = new File(tempPath.toURI());
        } catch (URISyntaxException e) {
            folder = new File(tempPath.getPath());
        }
        System.out.println("the containing path:" + tempPath.getPath());
        String path = folder.getParent() + "/temp/";


        SimpleFeatureSource featureSource =
                GeoFeaturesSingleton.get_instance().getStore().getFeatureSource(GeoFeaturesSingleton.get_instance().getStore().getTypeNames()[0]);
        SimpleFeatureType sft = featureSource.getSchema();


        TypeReference<HashMap<String, String>[]> valueTypeRef = new TypeReference<HashMap<String, String>[]>() {
        };
        Map<String, String>[] map = mapper.readValue(geofeatures, valueTypeRef);
//      Create the new type using the former as a template
        SimpleFeatureTypeBuilder stb = new SimpleFeatureTypeBuilder();
        stb.init(sft);
        stb.setName("newFeatureType");
//       //Add the new attribute 
        stb.add("tonsPerHectare", Float.class);
        SimpleFeatureType newFeatureType = stb.buildFeatureType();
        //Create the collection of new Features
        SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(newFeatureType);
        DefaultFeatureCollection collection = new DefaultFeatureCollection(null, null);
        //SimpleFeatureCollection collection = FeatureCollections.newCollection();
        int i = 0;
        SimpleFeatureIterator it = featureSource.getFeatures().features();
        try {
            while (it.hasNext() && collection.size() <= 10) {
                SimpleFeature sf = it.next();
                sfb.addAll(sf.getAttributes());
                sfb.add(map[i].get("tonsPerHectare"));
                collection.add(sfb.buildFeature(null));
                i++;
            }
        } finally {
            it.close();
        }
        UUID folderUid = UUID.randomUUID();
        File newFolder = new File(path + FilenameUtils.removeExtension(folderUid.toString()) + "/");
        newFolder.mkdir();
        File newFile = new File(newFolder.getPath() + "/" + filename);

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", newFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema(newFeatureType);

        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];

        SimpleFeatureSource newFeatureSource = newDataStore.getFeatureSource(typeName);

        if (newFeatureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) newFeatureSource;

            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();

            } finally {
                transaction.close();

            }
        } else {
            System.exit(1);
        }

        File zipFolder = new File(path + FilenameUtils.removeExtension(folderUid.toString()) + "_zip/");
        zipFolder.mkdir();
        WriteShapefile.pack(newFolder.getPath(), newFolder.getPath() + "_zip/" + FilenameUtils.removeExtension(filename) + ".zip");

        return newFolder.getPath();
    }

    public ResponseEntity<?> storeShapefile(String fileName) {
        return geoserverRepo.storeShapefile(fileName);
    }

    public ResponseEntity<?> storeShapefile(MultipartFile file) {
        return geoserverRepo.storeShapefile(file);
    }

    public static String toShp(String geoJson, String shapefileName) throws IOException {
        File shpFolder = new File(FilenameUtils.removeExtension(shapefileName) + "_zip");
        shpFolder.mkdir();
        File shpFile = new File(shpFolder.getAbsolutePath() + "/" + shapefileName);
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", shpFile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore shpDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);


        InputStream in = IOUtils.toInputStream(geoJson);
        int decimals = 15;
        GeometryJSON gjson = new GeometryJSON(decimals);
        FeatureJSON fjson = new FeatureJSON(gjson);

        FeatureCollection fc = fjson.readFeatureCollection(in);

        SimpleFeatureType type = (SimpleFeatureType) fc.getSchema();
        shpDataStore.createSchema(type);

        Transaction transaction = new DefaultTransaction("create");

        String typeName = shpDataStore.getTypeNames()[0];

        SimpleFeatureSource featureSource = shpDataStore.getFeatureSource(typeName);

        if (featureSource instanceof FeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;

            featureStore.setTransaction(transaction);
            try {

                featureStore.addFeatures(fc);

                transaction.commit();

            } catch (Exception ex) {
                ex.printStackTrace();
                transaction.rollback();
                transaction.close();

            }
        } else {
            System.out.println(typeName + " does not support read/write access");
        }
        MathTransform transform = null;
        try {
            transform = CRS.findMathTransform(CRS.decode("EPSG:3857"), CRS.decode("EPSG:4326"));

            WriteShapefile.changeProjection(transform, FilenameUtils.removeExtension(shapefileName), featureSource.getFeatures(), shpDataStore);
        } catch (FactoryException e) {
            e.printStackTrace();
        } finally {
            transaction.close();
        }

        WriteShapefile.pack(shpFolder.getAbsolutePath(), FilenameUtils.removeExtension(shapefileName) + ".zip");
        Utils.deleteDir(shpFolder);

        return FilenameUtils.removeExtension(shapefileName) + ".zip";
    }
}


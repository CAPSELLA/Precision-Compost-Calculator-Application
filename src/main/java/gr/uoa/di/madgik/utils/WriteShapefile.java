package gr.uoa.di.madgik.utils;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.io.FilenameUtils;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.NameImpl;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeImpl;
import org.geotools.feature.type.GeometryDescriptorImpl;
import org.geotools.feature.type.GeometryTypeImpl;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.identity.FeatureId;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;

import javax.swing.*;


public class WriteShapefile {
    File outfile;
    private ShapefileDataStore shpDataStore;

    public WriteShapefile(File f) {
        outfile = f;

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        try {
            params.put("url", outfile.toURI().toURL());
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        params.put("create spatial index", Boolean.TRUE);

        try {
            shpDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public boolean writeFeatures(FeatureCollection<SimpleFeatureType, SimpleFeature> features, String shapefileName) throws FactoryException {

        if (shpDataStore == null) {
            throw new IllegalStateException("Datastore can not be null when writing");
        }
        SimpleFeatureType schema = features.getSchema();
        GeometryDescriptor geom = schema.getGeometryDescriptor();
        try {

            Transaction transaction = new DefaultTransaction("create");
            String typeName = shpDataStore.getTypeNames()[0];


            List<AttributeDescriptor> attributes = schema
                    .getAttributeDescriptors();
            GeometryType geomType = null;
            List<AttributeDescriptor> attribs = new ArrayList<AttributeDescriptor>();
            for (AttributeDescriptor attrib : attributes) {
                AttributeType type = attrib.getType();
                if (type instanceof GeometryType) {
                    geomType = (GeometryType) type;
                } else {
                    attribs.add(attrib);
                }
            }


            GeometryTypeImpl gt = new GeometryTypeImpl(
                    new NameImpl("the_geom"), geomType.getBinding(),
                    geomType.getCoordinateReferenceSystem(),
                    geomType.isIdentified(), geomType.isAbstract(),
                    geomType.getRestrictions(), geomType.getSuper(),
                    geomType.getDescription());

            GeometryDescriptor geomDesc = new GeometryDescriptorImpl(
                    gt, new NameImpl("the_geom"),
                    geom.getMinOccurs(), geom.getMaxOccurs(),
                    geom.isNillable(), geom.getDefaultValue());

            attribs.add(0, geomDesc);

            SimpleFeatureType shpType = new SimpleFeatureTypeImpl(
                    schema.getName(), attribs, geomDesc,
                    schema.isAbstract(), schema.getRestrictions(),
                    schema.getSuper(), schema.getDescription());


            shpDataStore.createSchema(shpType);
            System.out.println("SHAP2E:" + shpType.getTypeName());

            SimpleFeatureSource featureSource = shpDataStore.getFeatureSource(shpDataStore.getTypeNames()[0]);

            if (featureSource instanceof SimpleFeatureStore) {
                SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
                List<SimpleFeature> feats = new ArrayList<SimpleFeature>();
                FeatureIterator<SimpleFeature> features2 = features.features();

                while (features2.hasNext()) {
                    SimpleFeature f = features2.next();

                    List<Object> attrs = new ArrayList<>();
                    attrs.add(f.getAttributes().get(2));
                    attrs.add(f.getAttributes().get(0));
                    attrs.add(f.getAttributes().get(1));
                    SimpleFeature reType = SimpleFeatureBuilder.build(shpType, attrs, "");
                    feats.add(reType);
                }
                features2.close();
                SimpleFeatureCollection collection = new ListFeatureCollection(shpType, feats);
                featureStore.setTransaction(transaction);
                try {
                    List<FeatureId> ids = featureStore.addFeatures(collection);
                    transaction.commit();
                } catch (Exception problem) {
                    problem.printStackTrace();
                    transaction.rollback();
                } finally {
                    transaction.close();
                }

                shpDataStore.dispose();

                return true;
            } else {
                shpDataStore.dispose();
                System.err.println("ShapefileStore is not writable");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void writeZip(String shapefile) throws IOException {
        byte[] buffer = new byte[1024];

        try {
            String shapeName = FilenameUtils.removeExtension(shapefile);

            FileOutputStream fos = new FileOutputStream(shapeName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            ArrayList<ZipEntry> zipEntries = new ArrayList();

            ZipEntry shp = new ZipEntry(shapeName + ".shp");
            zos.putNextEntry(shp);
            zipEntries.add(shp);

            ZipEntry shx = new ZipEntry(shapeName + ".shx");
            zos.putNextEntry(shx);
            zipEntries.add(shx);

            ZipEntry dbf = new ZipEntry(shapeName + ".dbf");
            zos.putNextEntry(dbf);
            zipEntries.add(dbf);

            ZipEntry prj = new ZipEntry(shapeName + ".prj");
            zos.putNextEntry(prj);
            zipEntries.add(prj);

            for (ZipEntry entry : zipEntries) {
                FileInputStream in =
                        new FileInputStream(entry.getName());

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
            }

            zos.closeEntry();
            //remember close it
            zos.close();

            System.out.println("Done");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void pack(String sourceDirPath, String zipFilePath) throws IOException {
        Path p = Files.createFile(Paths.get(zipFilePath));
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    });
        }
    }


    public static void changeProjection(MathTransform transform, String createdName, SimpleFeatureCollection featureCollection, ShapefileDataStore dataStore) throws IOException {
        Transaction transaction = new DefaultTransaction("Reproject");
        try (FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
                     dataStore.getFeatureWriterAppend(createdName, transaction);
             SimpleFeatureIterator iterator = featureCollection.features()) {
            while (iterator.hasNext()) {
                // copy the contents of each feature and transform the geometry
                SimpleFeature feature = iterator.next();
                SimpleFeature copy = writer.next();
                copy.setAttributes(feature.getAttributes());

                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                //  System.out.println("geometry:" +geometry);
                Geometry geometry2 = JTS.transform(geometry, transform);

                copy.setDefaultGeometry(geometry2);
                writer.write();
            }
            transaction.commit();
        } catch (Exception problem) {
            problem.printStackTrace();
            transaction.rollback();
        } finally {
            transaction.close();

        }
    }

}
package gr.uoa.di.madgik.utils;

import gr.uoa.di.madgik.model.GeoFeaturesSingleton;
import org.geotools.data.FeatureWriter;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;


public class Utils {

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

    public static JSONObject constructMetadataJson(String fileName) {
        JSONObject metadata = new JSONObject();

        return metadata;
    }

    public static void removeFiles(String fileName) {
        File folder = new File(fileName);
        File[] contents = folder.listFiles();
        for (File file : contents) {
            if (file.delete()) {
                System.out.println("deleting file:" + file.getName());
            } else {
                System.out.println("can't delete file:" + file.getName());

            }
        }
    }

    private static void addFeature(SimpleFeature feature,
                                   FeatureWriter<SimpleFeatureType, SimpleFeature> writer)
            throws IOException {

        SimpleFeature toWrite = writer.next();
        for (int i = 0; i < toWrite.getType().getAttributeCount(); i++) {
            String name = toWrite.getType().getDescriptor(i).getLocalName();
            toWrite.setAttribute(name, feature.getAttribute(name));
        }

        // copy over the user data
        if (feature.getUserData().size() > 0) {
            toWrite.getUserData().putAll(feature.getUserData());
        }

        // perform the write
        writer.write();
    }

    private static File getNewShapeFile(File oldFile) {
        String path = oldFile.getAbsolutePath();
        String newPath = path.substring(0, path.length() - 4) + ".shp";

        JFileDataStoreChooser chooser = new JFileDataStoreChooser("shp");
        chooser.setDialogTitle("Save shapefile");
        chooser.setSelectedFile(new File(newPath));

        int returnVal = chooser.showSaveDialog(null);

        if (returnVal != JFileDataStoreChooser.APPROVE_OPTION) {
            // the user canceled the dialog
            System.exit(0);
        }

        File newFile = chooser.getSelectedFile();
        if (newFile.equals(oldFile)) {
            System.out.println("Error: cannot replace " + oldFile);
            System.exit(0);
        }

        return newFile;
    }


    public double findRichZoneCompost() {
        return findRichZoneCompost(defaultTotal, defaultPoorest, defaultPoor);
    }

    public double findRichZoneCompost(double total, double poorest, double poor) {
        double totalTons = defaultTotal * GeoFeaturesSingleton.instance().getTotalArea();
        double tonsA = defaultPoorest * GeoFeaturesSingleton.instance().getLayerFeatures().get(0).getArea();
        double tonsB = defaultPoor * GeoFeaturesSingleton.instance().getLayerFeatures().get(1).getArea();
        double result = 0;
        if ((result = totalTons - tonsA - tonsB) > 0)
            return result;
        return 0;
    }

    public double findRichestZoneCompost() {
        return findRichestZoneCompost(defaultTotal, defaultPoorest, defaultPoor);
    }

    public double findRichestZoneCompost(double total, double poorest, double poor) {
        double totalTons = total * GeoFeaturesSingleton.instance().getTotalArea();
        double tonsA = poorest * GeoFeaturesSingleton.instance().getLayerFeatures().get(0).getArea();
        double tonsB = poor * GeoFeaturesSingleton.instance().getLayerFeatures().get(1).getArea();
        double result = 0;
//        if ((result = totalTons - GeoFeaturesSingleton.instance().getLayerFeatures().get(3).getTonsPerHectare() - tonsA
//                - tonsB) > 0)
//            return result;

        return 0;
    }


    public static void deleteDir(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDir(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();

    }

    public static void deleteContainDir(String cFolder) {
        File folder = new File(cFolder);

        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteDir(f);
                } else {
                    f.delete();
                }
            }
        }
    }

}

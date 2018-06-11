package gr.uoa.di.madgik.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uoa.di.madgik.interfaces.CapsellaRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class GeoserverRepository implements CapsellaRepository {

    @Value("${shapefile.server}")
    String geoserverUrl;


    @Value("${shapefile.delete}")
    String geoserverDelete;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ResponseEntity<?> storeShapefile(String filename) {
        MultiValueMap<String, String> postParams = new LinkedMultiValueMap<>();
        RestTemplate restTemplate = new RestTemplate();

        String datesetName = FilenameUtils.removeExtension(filename);
        String path = filename;
        MultiValueMap<String, Object> multipartMap = new LinkedMultiValueMap<String, Object>();
        File convFile = new File(path);

        multipartMap.add("shapefile", new FileSystemResource(convFile.getPath()));
        multipartMap.add("username", "capsella");
        multipartMap.add("access", "public");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<Object> request = new HttpEntity<Object>(multipartMap, headers);
        restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(
                geoserverUrl,
                request, String.class);
        convFile.delete();

        return response;

    }

    @Override
    public ResponseEntity<?> storeShapefile(MultipartFile file) {
        MultiValueMap<String, Object> multipartMap = new LinkedMultiValueMap<String, Object>();

        URL tempPath = this.getClass().getClassLoader().getResource("application.properties");
        File folder = null;
        try {
            folder = new File(tempPath.toURI());
        } catch (URISyntaxException e) {
            folder = new File(tempPath.getPath());
        }
        String path = folder.getParent() + "/temp/";

        System.out.println(folder.getAbsolutePath());
        File convFile = new File(path + File.separator + file.getOriginalFilename());
        try {
            file.transferTo(convFile);
            multipartMap.add("shapefile", new FileSystemResource(convFile));
            multipartMap.add("access", "public");
            multipartMap.add("username", "capsella");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<Object> request = new HttpEntity<Object>(multipartMap, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    geoserverUrl,
                    request, String.class);

            convFile.delete();
            if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                Map<String, String> responseMap;
                responseMap = mapper.readValue(responseEntity.getBody(), new TypeReference<Map<String, String>>() {
                });
                for (Map.Entry<String, String> entry : responseMap.entrySet()) {
                    if (entry.getKey().equals("wms-service")) {
                        //	metadataService.insertEndpoint(metadata, entry.getValue(),EndpointType.getValue("native").toString());
                    } else if (entry.getKey().equals("wfs-service")) {
                        //	metadataService.insertEndpoint(metadata, entry.getValue(),EndpointType.getValue("native").toString());
                    }
                }
                responseMap.put("name", FilenameUtils.removeExtension(file.getOriginalFilename()));
            }
            return new ResponseEntity<String>(HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

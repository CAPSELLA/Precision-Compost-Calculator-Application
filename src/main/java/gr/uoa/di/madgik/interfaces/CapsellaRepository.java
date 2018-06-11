package gr.uoa.di.madgik.interfaces;


import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface CapsellaRepository {

    public ResponseEntity<?> storeShapefile(String filename);

    public ResponseEntity<?> storeShapefile(MultipartFile file);

}

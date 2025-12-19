package backend.collab.snapshot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class YjsSnapshotClient {
    
    @Value("${snapshot.url}")
    private String snapShotUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sends an http post request to a Y.js service
     * 
     * @param diagramId to take a snapshot
     * @return the binary snapshot state
     */
    public byte[] snapshot(String diagramId) {
        String url = snapShotUrl + "/snapshot/" + diagramId;
        System.out.println(url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response =
                restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    byte[].class
                );

        return response.getBody();
    }
}

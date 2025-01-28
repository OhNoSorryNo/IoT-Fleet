package service;

import model.Communicator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommunicatorServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CommunicatorService communicatorService;

    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor;

    private final String deviceId = "testDeviceId";
    private final String secretKey = "testSecretKey";
    private final String token = "testToken";
    private final String backendUrl = "https://132.231.4.227:8443/agents";

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Use reflection to set private fields
        setPrivateField(communicatorService, "deviceId", deviceId);
        setPrivateField(communicatorService, "secretKey", secretKey);
        setPrivateField(communicatorService, "backendUrl", backendUrl);

        // Initialize communicator with the deviceId and secretKey
        Communicator communicator = new Communicator(deviceId, secretKey);
        setPrivateField(communicatorService, "communicator", communicator);
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    private Object getPrivateField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    @Test
    void testOnApplicationEvent_SuccessfulRegistration() throws Exception {
        when(restTemplate.getForEntity(anyString(), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(false));

        Map<String, String> responseBody = Map.of("token", token);
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        communicatorService.onApplicationEvent(applicationReadyEvent);

        verify(restTemplate).getForEntity(eq(backendUrl + "/" + deviceId + "/exists"), eq(Boolean.class));
        verify(restTemplate).postForEntity(eq(backendUrl + "/register"), any(), eq(Map.class));

        Communicator communicator = (Communicator) getPrivateField(communicatorService, "communicator");
        assertEquals(token, communicator.getJwtToken());

        boolean isRegistered = (boolean) getPrivateField(communicatorService, "isRegistered");
        assertTrue(isRegistered);
    }

    @Test
    void testOnApplicationEvent_DeviceAlreadyRegistered() throws Exception {
        when(restTemplate.getForEntity(anyString(), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(true));

        Map<String, String> responseBody = Map.of("token", token);
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        communicatorService.onApplicationEvent(applicationReadyEvent);

        verify(restTemplate).getForEntity(eq(backendUrl + "/" + deviceId + "/exists"), eq(Boolean.class));
        verify(restTemplate).postForEntity(eq(backendUrl + "/getToken"), any(), eq(Map.class));

        Communicator communicator = (Communicator) getPrivateField(communicatorService, "communicator");
        assertEquals(token, communicator.getJwtToken());

        boolean isRegistered = (boolean) getPrivateField(communicatorService, "isRegistered");
        assertTrue(isRegistered);
    }

    @Test
    void testOnApplicationEvent_RegistrationFails() throws Exception {
        when(restTemplate.getForEntity(anyString(), eq(Boolean.class)))
                .thenThrow(new RuntimeException("Connection error"));

        communicatorService.onApplicationEvent(applicationReadyEvent);

        verify(restTemplate, times(4)).getForEntity(anyString(), eq(Boolean.class));

        boolean isRegistered = (boolean) getPrivateField(communicatorService, "isRegistered");
        assertFalse(isRegistered);
    }

    @Test
    void testSendHeartbeat_Success() throws Exception {
        setPrivateField(communicatorService, "isRegistered", true);
        setPrivateField(communicatorService, "token", token);

        Communicator communicator = (Communicator) getPrivateField(communicatorService, "communicator");
        String deviceId = communicator.getDeviceId();

        communicatorService.sendHeartbeat();

        verify(restTemplate).put(urlCaptor.capture(), entityCaptor.capture());

        assertEquals("https://132.231.4.227:8443/agents/status/testDeviceId", urlCaptor.getValue());

        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertEquals("Bearer " + token, headers.getFirst(HttpHeaders.AUTHORIZATION));

        Map<String, Object> body = entityCaptor.getValue().getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("online"));
    }

    @Test
    void testSendHeartbeat_NotRegistered() throws Exception {
        setPrivateField(communicatorService, "isRegistered", false);

        setPrivateField(communicatorService, "backendUrl", backendUrl);

        when(restTemplate.getForEntity(eq(backendUrl + "/" + deviceId + "/exists"), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(false));

        when(restTemplate.postForEntity(eq(backendUrl + "/register"), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("token", token), HttpStatus.OK));

        communicatorService.sendHeartbeat();

        verify(restTemplate).getForEntity(eq(backendUrl + "/" + deviceId + "/exists"), eq(Boolean.class));
        verify(restTemplate).postForEntity(eq(backendUrl + "/register"), any(), eq(Map.class));

       // verifyNoMoreInteractions(restTemplate);

        boolean isRegistered = (boolean) getPrivateField(communicatorService, "isRegistered");
        assertTrue(isRegistered);

        String fetchedToken = (String) getPrivateField(communicatorService, "token");
        assertEquals(token, fetchedToken);
    }

    @Test
    void testSendHeartbeat_Failure() throws Exception {
        setPrivateField(communicatorService, "isRegistered", true);
        setPrivateField(communicatorService, "token", token);

        doThrow(new RuntimeException("Connection error"))
                .when(restTemplate).put(anyString(), any(HttpEntity.class));

        communicatorService.sendHeartbeat();

        verify(restTemplate).put(anyString(), any(HttpEntity.class));
    }

    @Test
    void testCheckForUpdate_Success() throws Exception {
        setPrivateField(communicatorService, "isRegistered", true);
        setPrivateField(communicatorService, "token", token);

        Map<String, String> responseBody = Map.of(
                "status", "update available",
                "url", "https://example.com/firmware/v2.0.0"
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(responseEntity);

        communicatorService.checkForUpdate();

        verify(restTemplate).getForEntity(urlCaptor.capture(), eq(Map.class));

        String expectedUrl = backendUrl + "/" + deviceId + "/update-check";
        assertEquals(expectedUrl, urlCaptor.getValue());
    }

    @Test
    void testCheckForUpdate_NotRegistered() throws Exception {
        setPrivateField(communicatorService, "isRegistered", false);

        communicatorService.checkForUpdate();

        verifyNoInteractions(restTemplate);
    }

    @Test
    void testSendUpdateRequestToUpdater_Success() throws Exception {
        CommunicatorService spyService = Mockito.spy(communicatorService);

        ResponseEntity<String> successResponse = new ResponseEntity<>("Update triggered", HttpStatus.OK);
        when(restTemplate.exchange(eq(spyService.updaterUrl), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(successResponse);

        spyService.sendUpdateRequestToUpdater("https://example.com/registry", "test-image", "v1.0");

        verify(restTemplate).exchange(eq(spyService.updaterUrl), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class));

        verify(spyService, times(1)).sendUpdateStatus(true);
    }

    @Test
    void testSendUpdateRequestToUpdater_Exception() throws Exception {
        CommunicatorService spyService = Mockito.spy(communicatorService);

        doThrow(new RuntimeException("Connection error"))
                .when(restTemplate)
                .exchange(eq(spyService.updaterUrl), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class));

        spyService.sendUpdateRequestToUpdater("https://example.com/registry", "test-image", "v1.0");

        verify(spyService, times(1)).sendUpdateStatus(false);
    }
}

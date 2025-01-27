package service;

import model.SimulatedDevice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimulatedDeviceServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SimulatedDeviceService simulatedDeviceService;

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
        setPrivateField(simulatedDeviceService, "deviceId", deviceId);
        setPrivateField(simulatedDeviceService, "secretKey", secretKey);
        setPrivateField(simulatedDeviceService, "backendUrl", backendUrl);

        // Initialize simulatedDevice with the deviceId and secretKey
        SimulatedDevice simulatedDevice = new SimulatedDevice(deviceId, secretKey);
        setPrivateField(simulatedDeviceService, "simulatedDevice", simulatedDevice);
    }

    private Object getPrivateField(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    @Test
    void testOnApplicationEvent_SuccessfulRegistration() throws Exception {
        // Mock the check for device existence to return false (device not registered)
        when(restTemplate.getForEntity(anyString(), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(false));

        // Mock the registration response with a token
        Map<String, String> responseBody = Map.of("token", token);
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        // Invoke the method
        simulatedDeviceService.onApplicationEvent(applicationReadyEvent);

        // Verify that the correct endpoints were called
        verify(restTemplate).getForEntity(eq(backendUrl + "/" + deviceId + "/exists"), eq(Boolean.class));
        verify(restTemplate).postForEntity(eq(backendUrl + "/register"), any(), eq(Map.class));

        // Access the private field 'simulatedDevice'
        SimulatedDevice simulatedDevice = (SimulatedDevice) getPrivateField(simulatedDeviceService, "simulatedDevice");

        // Assert that the token is set in the simulatedDevice
        assertEquals(token, simulatedDevice.getJwtToken());

        // Assert that isRegistered is true
        boolean isRegistered = (boolean) getPrivateField(simulatedDeviceService, "isRegistered");
        assertTrue(isRegistered);
    }

    @Test
    void testOnApplicationEvent_DeviceAlreadyRegistered() throws Exception {
        // Mock the check for device existence to return true (device is already registered)
        when(restTemplate.getForEntity(anyString(), eq(Boolean.class)))
                .thenReturn(ResponseEntity.ok(true));

        // Mock the getToken response with a token
        Map<String, String> responseBody = Map.of("token", token);
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenReturn(responseEntity);

        // Invoke the method
        simulatedDeviceService.onApplicationEvent(applicationReadyEvent);

        // Verify that the correct endpoints were called
        verify(restTemplate).getForEntity(eq(backendUrl + "/" + deviceId + "/exists"), eq(Boolean.class));
        verify(restTemplate).postForEntity(eq(backendUrl + "/getToken"), any(), eq(Map.class));

        // Access the private field 'simulatedDevice'
        SimulatedDevice simulatedDevice = (SimulatedDevice) getPrivateField(simulatedDeviceService, "simulatedDevice");

        // Assert that the token is set in the simulatedDevice
        assertEquals(token, simulatedDevice.getJwtToken());

        // Assert that isRegistered is true
        boolean isRegistered = (boolean) getPrivateField(simulatedDeviceService, "isRegistered");
        assertTrue(isRegistered);
    }

    @Test
    void testOnApplicationEvent_RegistrationFails() throws Exception {
        // Mock responses to throw exceptions to simulate failure
        when(restTemplate.getForEntity(anyString(), eq(Boolean.class)))
                .thenThrow(new RuntimeException("Connection error"));

        // Invoke the method
        simulatedDeviceService.onApplicationEvent(applicationReadyEvent);

        // Verify that registration was retried twice
        verify(restTemplate, times(4)).getForEntity(anyString(), eq(Boolean.class));

        // Assert that the device is not registered
        boolean isRegistered = (boolean) getPrivateField(simulatedDeviceService, "isRegistered");
        assertFalse(isRegistered);
    }

    @Test
    void testSendHeartbeat_Success() throws Exception {
        // Set up the service as registered
        setPrivateField(simulatedDeviceService, "isRegistered", true);
        setPrivateField(simulatedDeviceService, "token", token);

        // Access the simulatedDevice to get the deviceId
        SimulatedDevice simulatedDevice = (SimulatedDevice) getPrivateField(simulatedDeviceService, "simulatedDevice");
        String deviceId = simulatedDevice.getDeviceId();

        // Invoke the method
        simulatedDeviceService.sendHeartbeat();

        // Capture the arguments passed to RestTemplate
        verify(restTemplate).put(urlCaptor.capture(), entityCaptor.capture());

        // Verify the URL
        assertEquals("https://132.231.4.227:8443/agents/status/testDeviceId", urlCaptor.getValue());

        // Verify the headers
        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertEquals("Bearer " + token, headers.getFirst(HttpHeaders.AUTHORIZATION));

        // Verify the body
        Map<String, Object> body = entityCaptor.getValue().getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("online"));
    }

    @Test
    void testSendHeartbeat_NotRegistered() throws Exception {
        // Make sure the service thinks it's not yet registered
        setPrivateField(simulatedDeviceService, "isRegistered", false);

        // Force the service to use the same URL it uses in production
        // (If you don't do this, your @BeforeEach might already set it,
        //  but let's be explicit here.)
        setPrivateField(simulatedDeviceService, "backendUrl", "https://132.231.4.227:8443/agents");

        // Mock the GET call that checks existence
        when(restTemplate.getForEntity(
                eq("https://132.231.4.227:8443/agents/testDeviceId/exists"),
                eq(Boolean.class))
        ).thenReturn(ResponseEntity.ok(false));

        // Mock the POST call that registers the device
        when(restTemplate.postForEntity(
                eq("https://132.231.4.227:8443/agents/register"),
                any(), // the request body
                eq(Map.class))
        ).thenReturn(new ResponseEntity<>(Map.of("token", "testToken"), HttpStatus.OK));

        // Invoke the method
        simulatedDeviceService.sendHeartbeat();

        // Verify that the service checked existence, then registered
        verify(restTemplate).getForEntity(
                eq("https://132.231.4.227:8443/agents/testDeviceId/exists"),
                eq(Boolean.class)
        );
        verify(restTemplate).postForEntity(
                eq("https://132.231.4.227:8443/agents/register"),
                any(),
                eq(Map.class)
        );

        // No further interactions (the heartbeat PUT call happens after registration, but you’re specifically checking
        // that we do not send additional heartbeats within this test)
        verifyNoMoreInteractions(restTemplate);

        // Check that we ended up registered
        boolean isRegistered = (boolean) getPrivateField(simulatedDeviceService, "isRegistered");
        assertTrue(isRegistered);

        // Check that the token was set
        String token = (String) getPrivateField(simulatedDeviceService, "token");
        assertEquals("testToken", token);
    }



    @Test
    void testSendHeartbeat_Failure() throws Exception {
        // Set up the service as registered
        setPrivateField(simulatedDeviceService, "isRegistered", true);
        setPrivateField(simulatedDeviceService, "token", token);

        // Mock the PUT request to throw an exception
        doThrow(new RuntimeException("Connection error"))
                .when(restTemplate).put(anyString(), any(HttpEntity.class));

        // Invoke the method
        simulatedDeviceService.sendHeartbeat();

        // Verify that the PUT method was called
        verify(restTemplate).put(anyString(), any(HttpEntity.class));
    }
    @Test
    void testCheckForUpdate_Success() throws Exception {
        // Set up the service as registered
        setPrivateField(simulatedDeviceService, "isRegistered", true);
        setPrivateField(simulatedDeviceService, "token", token);

        // Mock the response from the backend
        Map<String, String> responseBody = Map.of(
                "status", "update available",
                "url", "https://example.com/firmware/v2.0.0"
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(responseEntity);

        // Invoke the method
        simulatedDeviceService.checkForUpdate();

        // Capture the arguments passed to RestTemplate
        verify(restTemplate).getForEntity(urlCaptor.capture(), eq(Map.class));

        // Verify the URL
        String expectedUrl = backendUrl + "/" + deviceId + "/update-check";
        assertEquals(expectedUrl, urlCaptor.getValue());

        // Verify that the log contains the update message
        // You may need to use a logging framework like LogCaptor for this part
        SimulatedDevice simulatedDevice = (SimulatedDevice) getPrivateField(simulatedDeviceService, "simulatedDevice");
        assertNotNull(simulatedDevice);
    }

    @Test
    void testCheckForUpdate_NoUpdateNeeded() throws Exception {
        // Set up the service as registered
        setPrivateField(simulatedDeviceService, "isRegistered", true);
        setPrivateField(simulatedDeviceService, "token", token);

        // Mock the response from the backend
        Map<String, String> responseBody = Map.of(
                "status", "no update needed"
        );
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenReturn(responseEntity);

        // Invoke the method
        simulatedDeviceService.checkForUpdate();

        // Capture the arguments passed to RestTemplate
        verify(restTemplate).getForEntity(urlCaptor.capture(), eq(Map.class));

        // Verify the URL
        String expectedUrl = backendUrl + "/" + deviceId + "/update-check";
        assertEquals(expectedUrl, urlCaptor.getValue());
    }

    @Test
    void testCheckForUpdate_NotRegistered() throws Exception {
        // Set up the service as not registered
        setPrivateField(simulatedDeviceService, "isRegistered", false);

        // Invoke the method
        simulatedDeviceService.checkForUpdate();

        // Verify that no interactions with restTemplate occurred
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testCheckForUpdate_Failure() throws Exception {
        // Set up the service as registered
        setPrivateField(simulatedDeviceService, "isRegistered", true);
        setPrivateField(simulatedDeviceService, "token", token);

        // Mock the GET request to throw an exception
        when(restTemplate.getForEntity(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Connection error"));

        // Invoke the method
        simulatedDeviceService.checkForUpdate();

        // Verify that the GET method was called
        verify(restTemplate).getForEntity(anyString(), eq(Map.class));
    }

}


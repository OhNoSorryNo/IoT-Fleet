package service;

import model.SimulatedDevice;
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
    private final String backendUrl = "https://server-app:8443/agents";

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
        assertEquals("https://server-app:8443/agents/status/testDeviceId", urlCaptor.getValue());

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
        // Set up the service as not registered
        setPrivateField(simulatedDeviceService, "isRegistered", false);

        // Mock the responses for registration flow
        when(restTemplate.getForEntity(eq("https://server-app:8443/agents/testDeviceId/exists"), eq(Boolean.class)))
                .thenReturn(new ResponseEntity<>(false, HttpStatus.OK));

        when(restTemplate.postForEntity(eq("https://server-app:8443/agents/register"), any(), eq(Map.class)))
                .thenReturn(new ResponseEntity<>(Map.of("token", "testToken"), HttpStatus.OK));

        // Invoke the method
        simulatedDeviceService.sendHeartbeat();

        // Verify that the registration process was triggered
        verify(restTemplate).getForEntity(eq("https://server-app:8443/agents/testDeviceId/exists"), eq(Boolean.class));
        verify(restTemplate).postForEntity(eq("https://server-app:8443/agents/register"), any(), eq(Map.class));

        // Ensure no further heartbeat is sent as part of this test (device was initially not registered)
        verifyNoMoreInteractions(restTemplate);

        // Assert that the device is now registered and token is set
        boolean isRegistered = (boolean) getPrivateField(simulatedDeviceService, "isRegistered");
        assertTrue(isRegistered);

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

    @Test
    void testSendUpdateRequestToUpdater_Success() throws Exception {
        // Create a spy for the SimulatedDeviceService to partially mock its behavior
        SimulatedDeviceService spyService = Mockito.spy(simulatedDeviceService);

        // Mock the response from the updater with a 2xx success status
        ResponseEntity<String> successResponse = new ResponseEntity<>("Update triggered", HttpStatus.OK);
        when(restTemplate.exchange(eq(spyService.updaterUrl), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(successResponse);

        // Invoke the method on the spy
        spyService.sendUpdateRequestToUpdater("https://example.com/registry", "test-image", "v1.0");

        // Verify the restTemplate.exchange method was called with the correct arguments
        ArgumentCaptor<HttpEntity<Map<String, String>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(spyService.updaterUrl), eq(HttpMethod.PUT), entityCaptor.capture(), eq(String.class));

        // Verify the request body
        Map<String, String> requestBody = entityCaptor.getValue().getBody();
        assertEquals("https://example.com/registry", requestBody.get("registry_url"));
        assertEquals("test-image", requestBody.get("image_name"));
        assertEquals("v1.0", requestBody.get("tag"));

        // Verify sendUpdateStatus(true) was called on the spy
        verify(spyService, times(1)).sendUpdateStatus(true);
    }

    @Test
    void testSendUpdateRequestToUpdater_Failure_Non2xxStatus() throws Exception {
        // Create a spy for SimulatedDeviceService to partially mock its behavior
        SimulatedDeviceService spyService = Mockito.spy(simulatedDeviceService);

        // Mock the response from the updater with a non-2xx status
        ResponseEntity<String> failureResponse = new ResponseEntity<>("Update failed", HttpStatus.BAD_REQUEST);
        when(restTemplate.exchange(eq(spyService.updaterUrl), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class)))
                .thenReturn(failureResponse);

        // Invoke the method on the spy
        spyService.sendUpdateRequestToUpdater("https://example.com/registry", "test-image", "v1.0");

        // Verify the restTemplate.exchange method was called with the correct arguments
        ArgumentCaptor<HttpEntity<Map<String, String>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(spyService.updaterUrl), eq(HttpMethod.PUT), entityCaptor.capture(), eq(String.class));

        // Verify the request body
        Map<String, String> requestBody = entityCaptor.getValue().getBody();
        assertEquals("https://example.com/registry", requestBody.get("registry_url"));
        assertEquals("test-image", requestBody.get("image_name"));
        assertEquals("v1.0", requestBody.get("tag"));

        // Verify sendUpdateStatus(false) was called on the spy
        verify(spyService, times(1)).sendUpdateStatus(false);
    }

    @Test
    void testSendUpdateRequestToUpdater_Exception() throws Exception {
        // Create a spy for the SimulatedDeviceService to partially mock its behavior
        SimulatedDeviceService spyService = Mockito.spy(simulatedDeviceService);

        // Mock the exchange method in the RestTemplate to throw an exception
        doThrow(new RuntimeException("Connection error"))
                .when(restTemplate)
                .exchange(eq(spyService.updaterUrl), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class));

        // Invoke the method on the spy
        spyService.sendUpdateRequestToUpdater("https://example.com/registry", "test-image", "v1.0");

        // Verify sendUpdateStatus(false) was called on the spy
        verify(spyService, times(1)).sendUpdateStatus(false);

        // Verify that the restTemplate.exchange method was called
        verify(restTemplate, times(1))
                .exchange(eq(spyService.updaterUrl), eq(HttpMethod.PUT), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void testPerformUpdate_Success() throws Exception {
        // Mock the Process object
        Process mockProcess = mock(Process.class);
        when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream("200".getBytes()));
        when(mockProcess.waitFor()).thenReturn(0); // Simulate successful process completion

        // Mock Runtime.getRuntime().exec() to return the mocked Process
        Runtime runtimeMock = mock(Runtime.class);
        when(runtimeMock.exec(anyString())).thenReturn(mockProcess);

        // Use the mocked Runtime in place of the real Runtime
        try (MockedStatic<Runtime> mockedStatic = mockStatic(Runtime.class)) {
            mockedStatic.when(Runtime::getRuntime).thenReturn(runtimeMock);

            // Call the method under test
            boolean result = simulatedDeviceService.performUpdate("https://example.com/update");

            // Assert the result
            assertTrue(result, "The update should be successful");

            // Verify that the command was executed
            String expectedCommand = "curl -w \"%{http_code}\" -o /dev/null -s -X PUT -H \"Content-Type: application/json\" -d '{\"image_name\": \"https://example.com/update\"}' ";
            verify(runtimeMock).exec(eq(expectedCommand));
        }
    }

    @Test
    void testPerformUpdate_Failure() throws Exception {
        // Mock the Process object
        Process mockProcess = mock(Process.class);
        when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream("400".getBytes()));
        when(mockProcess.waitFor()).thenReturn(0); // Simulate successful process completion

        // Mock Runtime.getRuntime().exec() to return the mocked Process
        Runtime runtimeMock = mock(Runtime.class);
        when(runtimeMock.exec(anyString())).thenReturn(mockProcess);

        // Use the mocked Runtime in place of the real Runtime
        try (MockedStatic<Runtime> mockedStatic = mockStatic(Runtime.class)) {
            mockedStatic.when(Runtime::getRuntime).thenReturn(runtimeMock);

            // Call the method under test
            boolean result = simulatedDeviceService.performUpdate("https://example.com/update");

            // Assert the result
            assertFalse(result, "The update should fail for a non-2xx status code");

            // Verify that the command was executed
            String expectedCommand = "curl -w \"%{http_code}\" -o /dev/null -s -X PUT -H \"Content-Type: application/json\" -d '{\"image_name\": \"https://example.com/update\"}' ";
            verify(runtimeMock).exec(eq(expectedCommand));
        }
    }

    @Test
    void testPerformUpdate_Exception() throws Exception {
        // Mock Runtime.getRuntime().exec() to throw an exception
        Runtime runtimeMock = mock(Runtime.class);
        when(runtimeMock.exec(anyString())).thenThrow(new RuntimeException("Command execution failed"));

        // Use the mocked Runtime in place of the real Runtime
        try (MockedStatic<Runtime> mockedStatic = mockStatic(Runtime.class)) {
            mockedStatic.when(Runtime::getRuntime).thenReturn(runtimeMock);

            // Call the method under test
            boolean result = simulatedDeviceService.performUpdate("https://example.com/update");

            // Assert the result
            assertFalse(result, "The update should fail when an exception occurs");

            // Verify that the command was attempted
            String expectedCommand = "curl -w \"%{http_code}\" -o /dev/null -s -X PUT -H \"Content-Type: application/json\" -d '{\"image_name\": \"https://example.com/update\"}' ";
            verify(runtimeMock).exec(eq(expectedCommand));
        }
    }

    @Test
    void testSendUpdateStatus_Success() throws Exception {
        // Mock the SimulatedDevice
        SimulatedDevice mockSimulatedDevice = mock(SimulatedDevice.class);
        when(mockSimulatedDevice.getDeviceId()).thenReturn("testDeviceId");

        // Use reflection to set the private simulatedDevice field
        Field simulatedDeviceField = SimulatedDeviceService.class.getDeclaredField("simulatedDevice");
        simulatedDeviceField.setAccessible(true);
        simulatedDeviceField.set(simulatedDeviceService, mockSimulatedDevice);

        // Mock RestTemplate behavior
        ResponseEntity<String> successResponse = new ResponseEntity<>("Status updated", HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(successResponse);

        // Call the method
        simulatedDeviceService.sendUpdateStatus(true);

        // Verify that the RestTemplate.exchange was called with the correct arguments
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.PUT),
                requestEntityCaptor.capture(),
                eq(String.class)
        );

        // Assert the URL
        String expectedUrl = "https://server-app:8443/agents/testDeviceId/update-status";
        assertEquals(expectedUrl, urlCaptor.getValue());

        // Assert the request body
        Map<String, Object> requestBody = requestEntityCaptor.getValue().getBody();
        assertEquals("success", requestBody.get("status"));
        assertEquals("testDeviceId", requestBody.get("deviceId"));

        // Assert the headers
        HttpHeaders headers = requestEntityCaptor.getValue().getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
    }

    @Test
    void testSendUpdateStatus_Failure() throws Exception {
        // Mock the SimulatedDevice
        SimulatedDevice mockSimulatedDevice = mock(SimulatedDevice.class);
        when(mockSimulatedDevice.getDeviceId()).thenReturn("testDeviceId");

        // Use reflection to set the private simulatedDevice field
        Field simulatedDeviceField = SimulatedDeviceService.class.getDeclaredField("simulatedDevice");
        simulatedDeviceField.setAccessible(true);
        simulatedDeviceField.set(simulatedDeviceService, mockSimulatedDevice);

        // Mock RestTemplate to throw an exception
        doThrow(new RuntimeException("Connection error")).when(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(String.class)
        );

        // Call the method
        simulatedDeviceService.sendUpdateStatus(false);

        // Verify that the RestTemplate.exchange was called with the correct arguments
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.PUT),
                requestEntityCaptor.capture(),
                eq(String.class)
        );

        // Assert the URL
        String expectedUrl = "https://server-app:8443/agents/testDeviceId/update-status";
        assertEquals(expectedUrl, urlCaptor.getValue());

        // Assert the request body
        Map<String, Object> requestBody = requestEntityCaptor.getValue().getBody();
        assertEquals("failure", requestBody.get("status"));
        assertEquals("testDeviceId", requestBody.get("deviceId"));

        // Assert the headers
        HttpHeaders headers = requestEntityCaptor.getValue().getHeaders();
        assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
    }

}


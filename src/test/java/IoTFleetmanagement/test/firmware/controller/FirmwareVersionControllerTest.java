package IoTFleetmanagement.test.firmware.controller;

import IoTFleetManagement.agent.controller.AgentController;
import IoTFleetManagement.firmware.controller.FirmwareVersionController;
import IoTFleetManagement.firmware.model.FirmwareVersion;
import IoTFleetManagement.firmware.service.FirmwareVersionService;
import IoTFleetManagement.user.repository.UserRepository;
import IoTFleetManagement.user.service.CustomUserDetailsService;
import IoTFleetmanagement.test.security.config.TestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FirmwareVersionController.class)
@Import(TestSecurityConfig.class)
@ContextConfiguration(classes = {FirmwareVersionController.class})
public class FirmwareVersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FirmwareVersionService firmwareVersionService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private FirmwareVersion firmwareVersion;

    @BeforeEach
    public void setUp() {
        firmwareVersion = new FirmwareVersion(
                "v1.0",
                "firmware_v1.0",
                "https://dockerhub.com/image_v1.0",
                LocalDate.of(2024, 6, 1)
        );
    }

    @Test
    public void testAddFirmwareVersion() throws Exception {
        // Arrange
        when(firmwareVersionService.addFirmwareVersion(any(FirmwareVersion.class))).thenReturn(firmwareVersion);

        // Act & Assert
        mockMvc.perform(post("/firmware")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(firmwareVersion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("v1.0"))
                .andExpect(jsonPath("$.imageName").value("firmware_v1.0"))
                .andExpect(jsonPath("$.url").value("https://dockerhub.com/image_v1.0"))
                .andExpect(jsonPath("$.releaseDate").value("2024-06-01"));

        verify(firmwareVersionService, times(1)).addFirmwareVersion(any(FirmwareVersion.class));
    }

    @Test
    public void testGetAllFirmwareVersions() throws Exception {
        // Arrange
        when(firmwareVersionService.getAllFirmwareVersions())
                .thenReturn(Arrays.asList(firmwareVersion, new FirmwareVersion(
                        "v2.0",
                        "firmware_v2.0",
                        "https://dockerhub.com/image_v2.0",
                        LocalDate.of(2024, 7, 1)
                )));

        // Act & Assert
        mockMvc.perform(get("/firmware"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].version").value("v1.0"))
                .andExpect(jsonPath("$[1].version").value("v2.0"));

        verify(firmwareVersionService, times(1)).getAllFirmwareVersions();
    }

    @Test
    public void testGetAllFirmwareVersionsEmpty() throws Exception {
        // Arrange
        when(firmwareVersionService.getAllFirmwareVersions()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/firmware"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        verify(firmwareVersionService, times(1)).getAllFirmwareVersions();
    }

    @Test
    public void testGetLatestFirmwareVersion() throws Exception {
        // Arrange
        when(firmwareVersionService.getLatestFirmwareVersion()).thenReturn(firmwareVersion);

        // Act & Assert
        mockMvc.perform(get("/firmware/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("v1.0"))
                .andExpect(jsonPath("$.imageName").value("firmware_v1.0"))
                .andExpect(jsonPath("$.url").value("https://dockerhub.com/image_v1.0"))
                .andExpect(jsonPath("$.releaseDate").value("2024-06-01"));

        verify(firmwareVersionService, times(1)).getLatestFirmwareVersion();
    }
}
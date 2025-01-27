package controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
public class CommunicatorControllerTest {

    @InjectMocks
    private CommunicatorController communicatorController;

    @BeforeEach
    void setUp() {
        communicatorController = new CommunicatorController();
    }

    @Test
    public void testGetStatus() {
        // Act
        ResponseEntity<String> response = communicatorController.getStatus();

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Device is online", response.getBody());
    }
}
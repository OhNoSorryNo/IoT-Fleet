package IoTFleetManagement.controller;

import IoTFleetManagement.exceptions.UsernameAlreadyExistsException;
import IoTFleetManagement.model.User;
import IoTFleetManagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        return userService.authenticate(username, password)
                .map(user -> ResponseEntity.ok("Login successful!"))
                .orElse(new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username, @RequestParam String password, @RequestParam String roleName) {
        System.out.println("Register endpoint hit with username: " + username);
//        if (userService.roleExists(roleName)) {
//            User newUser = userService.registerUser(username, password, roleName);
//            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role not found: " + roleName);
//        }
        // Check if the role exists
        if (!userService.roleExists(roleName)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role not found: " + roleName);
        }

        try {
            // Attempt to register the new user
            User newUser = userService.registerUser(username, password, roleName);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);

        } catch (UsernameAlreadyExistsException e) {
            // Handle the exception if the username is already taken
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username '" + username + "' is already taken.");
        }

    }

}

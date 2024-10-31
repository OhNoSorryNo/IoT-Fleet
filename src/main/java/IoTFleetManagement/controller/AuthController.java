package IoTFleetManagement.controller;

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
        return userService.findUserByUsername(username)
                .filter(user -> user.getPassword().equals(password))
                .map(user -> ResponseEntity.ok("Login successful!"))
                .orElse(new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestParam String username, @RequestParam String password) {
        User newUser = new User(username, password);
        return ResponseEntity.ok(userService.saveUser(newUser));
    }

}

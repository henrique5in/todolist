package br.dev.henrique.todolist.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import at.favre.lib.crypto.bcrypt.BCrypt;

@RestController
@RequestMapping("/users")

public class UserController {
  @Autowired
  private IUserRepository userRepository;

  @PostMapping("/")
  public ResponseEntity create(@RequestBody UserModel user){
    var userFound = this.userRepository.findByUsername(user.getUsername());
    if(userFound != null){
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User already exists");
    }

    var passwordHashed = BCrypt.withDefaults().hashToString(12, user.getPassword().toCharArray());
    
    user.setPassword(passwordHashed);
    
    var userCreated = this.userRepository.save(user);
    return ResponseEntity.status(HttpStatus.CREATED).body(userCreated);
  }

}

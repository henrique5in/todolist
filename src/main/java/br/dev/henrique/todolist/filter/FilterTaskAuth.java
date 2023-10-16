package br.dev.henrique.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.dev.henrique.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



@Component
public class FilterTaskAuth extends OncePerRequestFilter{

  @Autowired
  private IUserRepository userRepository;
  
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
        var servletPath = request.getServletPath();
        if(servletPath.startsWith("/tasks/")){
          
          var authorization = request.getHeader("Authorization");
  
          var authEncoded = authorization.substring("Basic".length()).trim();
          byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
  
          var authString = new String(authDecoded);
  
          var username = authString.split(":")[0];
          var password = authString.split(":")[1];
  
          var user = this.userRepository.findByUsername(username);
          if(user==null){
            response.sendError(401);
          }else{
            var passwordHashed = user.getPassword();
            var passwordDecoded = password.toCharArray();
            var passwordCheck = BCrypt.verifyer().verify(passwordDecoded, passwordHashed);
            if(!passwordCheck.verified){
              response.sendError(401);
            }
            request.setAttribute("userId", user.getId());
            filterChain.doFilter(request, response);
          }
        }else{
          filterChain.doFilter(request, response);
        }
        
      }

}

package in.expensebudget.controller;

import in.expensebudget.dto.ApiDtos;
import in.expensebudget.security.AccountPrincipal;
import in.expensebudget.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AccountService accountService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AccountService accountService, AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository) {
        this.accountService = accountService;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/auth/register")
    public org.springframework.http.ResponseEntity<ApiDtos.MessageResponse> register(
            @Valid @RequestBody ApiDtos.RegisterRequest request) {
        accountService.register(request);
        return org.springframework.http.ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiDtos.MessageResponse("Account created. You can now sign in."));
    }

    @PostMapping("/auth/login")
    public ApiDtos.UserResponse login(@Valid @RequestBody ApiDtos.LoginRequest request,
                                     HttpServletRequest servletRequest, HttpServletResponse response) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.email().trim().toLowerCase(java.util.Locale.ROOT), request.password()));
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, servletRequest, response);
        return ApiDtos.UserResponse.from((AccountPrincipal) authentication.getPrincipal());
    }

    @GetMapping("/auth/me")
    public ApiDtos.UserResponse currentUser(Authentication authentication) {
        return ApiDtos.UserResponse.from((AccountPrincipal) authentication.getPrincipal());
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(org.springframework.security.web.csrf.CsrfToken csrfToken) {
        return Map.of("token", csrfToken.getToken());
    }
}

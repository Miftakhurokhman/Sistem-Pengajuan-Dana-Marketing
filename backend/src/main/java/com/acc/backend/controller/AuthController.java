package com.acc.backend.controller;

import com.acc.backend.domain.dto.req.ReqLogin;
import com.acc.backend.domain.dto.res.BaseResponse;
import com.acc.backend.domain.dto.res.ResLogin;
import com.acc.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*") // Menghindari CORS issue saat dikoneksikan ke Front-End
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<ResLogin>> login(@Valid @RequestBody ReqLogin req) {
        BaseResponse<ResLogin> response = authService.login(req);
        return ResponseEntity.ok(response);
    }
}
package com.lunaris.ansenuza.infrastructure.web.controller;

import com.lunaris.ansenuza.application.usecase.GetPassengerProfileUseCase;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/passengers")
@RequiredArgsConstructor
public class PassengerProfileController {

    private final GetPassengerProfileUseCase getPassengerProfileUseCase;

    @GetMapping({"/me", "/profile"})
    public GetPassengerProfileUseCase.PassengerProfile me(Principal principal) {
        return getPassengerProfileUseCase.execute(principal.getName());
    }
}

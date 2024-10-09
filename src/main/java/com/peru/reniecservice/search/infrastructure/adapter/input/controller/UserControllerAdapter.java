package com.peru.reniecservice.search.infrastructure.adapter.input.controller;


import com.peru.reniecservice.search.application.port.input.service.IUserServicePort;
import com.peru.reniecservice.search.domain.model.entity.TokenEntity;
import com.peru.reniecservice.search.infrastructure.adapter.output.repository.ITokenRepository;
import com.peru.reniecservice.shared.domain.model.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User", description = "User Management API")
public class UserControllerAdapter {

    private final IUserServicePort userServicePort;
    private final ITokenRepository tokenRepository;

    @PostMapping(value = "/start-record/{dniInitial}")
    @Operation(summary = "Start record", description = "Start searching and recording users by DNI initial")
    public ResponseEntity<ResponseDTO> recordUserByDni(@PathVariable Long dniInitial) {
        ResponseDTO responseDTO = userServicePort.saveUserEntityByDni(dniInitial);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping(value = "/net/hash")
    @Operation(summary = "Get hash of token", description = "Get hash of token")
    public ResponseEntity<String> getTokenHash() {
        String tokenOfTm = userServicePort.generateTokenForCreationOfEmail();
        String mailOfTm = userServicePort.createMailOfTm(tokenOfTm);
        String username = userServicePort.generateUsername();
        String nameUnique = userServicePort.generateNameUnique();
        String passwordUnique = userServicePort.generatePassword();

        boolean isRegistered = userServicePort.registerMailToNet(mailOfTm, username, nameUnique, passwordUnique);
        String tokenHash = null;
        try {
            Thread.sleep(1000);
            if (isRegistered) {
                String tokenOfTmAccess = userServicePort.generateTokenOfTmAccess(mailOfTm);
                String accessToken = userServicePort.getTokenOfActivation(tokenOfTmAccess);
                tokenHash = userServicePort.getTokenHash(accessToken);

                TokenEntity tokenEntity = new TokenEntity();
                tokenEntity.setToken(tokenHash);
                tokenRepository.save(tokenEntity);
            }
            else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.status(HttpStatus.OK).body(tokenHash);
    }
}

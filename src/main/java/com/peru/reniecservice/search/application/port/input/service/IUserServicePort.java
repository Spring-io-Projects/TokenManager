package com.peru.reniecservice.search.application.port.input.service;


import com.peru.reniecservice.shared.domain.model.ResponseDTO;

public interface IUserServicePort {
    ResponseDTO saveUserEntityByDni(Long dniInitial);
    String createMailOfTm(String tokenTm);
    boolean registerMailToNet(String emailTm, String username, String nameUnique, String passwordUnique);
    String getTokenHash(String accessToken);
    String getTokenOfActivation(String tokenTm);
    String generateTokenForCreationOfEmail();
    String generateTokenOfTmAccess(String email);
    String generateUsername();
    String generateNameUnique();
    String generatePassword();
}

package com.peru.reniecservice.search.application.usecase;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.peru.reniecservice.search.application.port.input.service.IUserServicePort;
import com.peru.reniecservice.search.application.port.output.persistence.IRecordPersistPort;
import com.peru.reniecservice.search.application.port.output.persistence.IUserPersistPort;
import com.peru.reniecservice.search.domain.model.entity.RecordEntity;
import com.peru.reniecservice.search.domain.model.entity.UserEntity;
import com.peru.reniecservice.search.domain.model.valueObject.ErrorEnum;
import com.peru.reniecservice.search.infrastructure.adapter.input.dto.response.MailTmResponse;
import com.peru.reniecservice.search.infrastructure.adapter.input.dto.response.SignupPeResponse;
import com.peru.reniecservice.search.infrastructure.adapter.input.dto.response.SignupTmResponse;
import com.peru.reniecservice.shared.application.exception.ResourceNotFoundException;
import com.peru.reniecservice.shared.domain.model.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserServicePort {

    @Value("${mail.tm.email}")
    private String tmEmail;

    @Value("${mail.tm.password}")
    private String tmPassword;

    @Value("${mail.tm.domain}")
    private String tmDomain;

    private final IUserPersistPort userPersistPort;
    private final IRecordPersistPort recordPersistPort;

    private final RestTemplate restTemplate;

    private static final String API_URL = "https://api.apis.net.pe/v2/reniec/dni?numero=";
    private static final String TOKEN = "apis-token-10753.BWLWQ6l8pdTHfWF0nsvUwKG8fI5mJ6Vx";
    private static final String BASE_URL_TM = "https://api.mail.tm";
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String PREFIX_USERNAME = "user";
    private static final String PREFIX_NAME = "xazl";
    private static final String PREFIX_CONTRA = "contra_";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    @Override
    public ResponseDTO saveUserEntityByDni(Long dniInitial) {
        long maxDni = Math.min(dniInitial + 999, 89999999);
        int successCount = 0;
        int invalidCount = 0;
        int errorCount = 0;

        for (long dni = dniInitial; dni <= maxDni; dni++) {
            String url = API_URL + dni;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + TOKEN);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            try {
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> responseBody = response.getBody();

                    UserEntity userEntity = null;
                    if (responseBody != null) {
                        userEntity = UserEntity.builder()
                                .name((String) responseBody.get("nombres"))
                                .paternalSurname((String) responseBody.get("apellidoPaterno"))
                                .maternalSurname((String) responseBody.get("apellidoMaterno"))
                                .dni((String) responseBody.get("numeroDocumento"))
                                .checkDigit((String) responseBody.get("digitoVerificador"))
                                .build();
                    }

                    userPersistPort.saveUserEntity(userEntity);

                    RecordEntity recordEntity = RecordEntity.builder()
                            .lastDni(dni)
                            .errorEnum(ErrorEnum.SUCCESS)
                            .build();

                    recordPersistPort.saveRecordEntity(recordEntity);
                    successCount++;
                }
                else {
                    throw new RuntimeException("Unexpected status code: " + response.getStatusCode());
                }
            }
            catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                    RecordEntity recordEntity = RecordEntity.builder()
                            .lastDni(dni)
                            .errorEnum(ErrorEnum.INVALID)
                            .build();

                    recordPersistPort.saveRecordEntity(recordEntity);
                    invalidCount++;
                }
                else {
                    RecordEntity recordEntity = RecordEntity.builder()
                            .lastDni(dni)
                            .errorEnum(ErrorEnum.ERROR)
                            .build();

                    recordPersistPort.saveRecordEntity(recordEntity);
                    errorCount++;
                }
            }
        }
        return new ResponseDTO("Records processed",
                "Last DNI processed: " + dniInitial,
                "Number of DNIs processed successfully: " + successCount,
                "Number of errors: " + errorCount,
                "Number of invalid DNIs: " + invalidCount);
    }

    @Override
    public String createMailOfTm(String tokenTm) {
        String apiTmAccounts = BASE_URL_TM.concat("/accounts");

        String email = generateUsername().concat("@").concat(tmDomain);
        Map<String, String> mailTmRequest = new HashMap<>();
        mailTmRequest.put("address", email);
        mailTmRequest.put("password", tmPassword);

        WebClient webClient = WebClient.builder().build();

        SignupTmResponse response = webClient.post()
                .uri(apiTmAccounts)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenTm)
                .body(Mono.just(mailTmRequest), Map.class)
                .retrieve()
                .bodyToMono(SignupTmResponse.class)
                .block();

        if (response != null && response.address() != null) {
            return response.address();
        }
        else {
            throw new RuntimeException("Unexpected status code: " + response);
        }
    }

    @Override
    public boolean registerMailToNet(String emailTm, String username, String nameUnique, String passwordUnique) {
        Map<String, String> mailNetRequest = new HashMap<>();
        mailNetRequest.put("email", emailTm);
        mailNetRequest.put("name", nameUnique);
        mailNetRequest.put("password", passwordUnique);
        mailNetRequest.put("username", username);

        WebClient webClient = WebClient.builder().build();

        ResponseEntity<SignupPeResponse> responseEntity = webClient.post()
                .uri("https://apis.net.pe/_api/users/signup")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer APIS.NET.EP")
                .body(Mono.just(mailNetRequest), Map.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.CREATED)) {
                        return response.bodyToMono(SignupPeResponse.class)
                                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
                    } else {
                        return response.createException().flatMap(Mono::error);
                    }
                })
                .block();

        if (responseEntity != null && responseEntity.getStatusCode() == HttpStatus.CREATED) {
            return true;
        }
        else {
            throw new RuntimeException("Unexpected status code GET BODY: " + responseEntity.getBody());
        }
    }

    @Override
    public String getTokenHash(String accessToken) {
        Map<String, String> tokenNetRequest = new HashMap<>();
        tokenNetRequest.put("description", "Token created automatically");

        WebClient webClient = WebClient.builder().build();

        Map response = webClient.post()
                .uri("https://apis.net.pe/_api/tokens")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(Mono.just(tokenNetRequest), Map.class)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response != null && response.get("hash") != null) {
            return response.get("hash").toString();
        } else {
            throw new RuntimeException("Unexpected status or missing hash in response");
        }

    }

    @Override
    public String getTokenOfActivation(String tokenTm) {
        String urlMessages = BASE_URL_TM.concat("/messages?page=1");
        String tokenNet = null;

        WebClient webClient = WebClient.builder().build();

        Map response = webClient.get()
                .uri(urlMessages)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenTm)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response != null) {
            List<Map<String, Object>> messagesHydraMember = (List<Map<String, Object>>) response.get("hydra:member");

            for (Map<String, Object> message : messagesHydraMember) {
                String messageId = (String) message.get("id");
                String messageContent = getMessageContent(tokenTm, messageId);
                String activationLink = extractActivationLink(messageContent);

                tokenNet = activateAccountFromLink(activationLink);
            }
        } else {
            throw new RuntimeException("No response or unexpected status");
        }

        return tokenNet;
    }

    @Override
    public String generateTokenForCreationOfEmail() {
        String apiTmToken = BASE_URL_TM.concat("/token");

        Map<String, String> tokenTmRequest = new HashMap<>();
        tokenTmRequest.put("address", tmEmail);
        tokenTmRequest.put("password", tmPassword);

        WebClient webClient = WebClient.builder().build();

        MailTmResponse response = webClient.post()
                .uri(apiTmToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(tokenTmRequest), Map.class)
                .retrieve()
                .bodyToMono(MailTmResponse.class)
                .block();

        if (response != null && response.token() != null) {
            return response.token();
        }
        else {
            throw new RuntimeException("Token not found");
        }
    }

    private String getMessageContent(String tokenTm, String messageId) {
        String urlMessage = BASE_URL_TM.concat("/messages/").concat(messageId);

        WebClient webClient = WebClient.builder().build();

        Map response = webClient.get()
                .uri(urlMessage)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenTm)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response != null && response.containsKey("text")) {
            return response.get("text").toString();
        } else {
            throw new RuntimeException("Unexpected response or missing 'text' field");
        }

    }
    private String extractActivationLink(String content) {
        String linkPattern = "https://[^ ]+";
        Pattern pattern = Pattern.compile(linkPattern);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String activationLink = matcher.group(0);
            if (activationLink.endsWith("]")) {
                activationLink = activationLink.substring(0, activationLink.length() - 1);
            }
            return activationLink;
        } else {
            throw new ResourceNotFoundException("Not found activation link");
        }
    }
    private String activateAccountFromLink(String activationLink) {
        String tokenNet = activationLink.split("token=")[1].strip();
        int endIndex = tokenNet.indexOf(']');
        if (endIndex != -1) {
            tokenNet = tokenNet.substring(0, endIndex);
        }
        WebClient webClient = WebClient.builder().build();

        String response = webClient.post()
                .uri("https://apis.net.pe/_api/users/activate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenNet)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        if (response != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> responseMap = objectMapper.readValue(response, Map.class);

                return responseMap.get("access_token");
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error procesando la respuesta JSON");
            }
        }
        else {
            throw new RuntimeException("Unexpected response or status code");
        }
    }

    @Override
    public String generateTokenOfTmAccess(String email) {
        String apiTmToken = BASE_URL_TM.concat("/token");

        Map<String, String> tokenTmRequest = new HashMap<>();
        tokenTmRequest.put("address", email);
        tokenTmRequest.put("password", tmPassword);

        WebClient webClient = WebClient.builder().build();

        MailTmResponse response = webClient.post()
                .uri(apiTmToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(tokenTmRequest), Map.class)
                .retrieve()
                .bodyToMono(MailTmResponse.class)
                .block();

        if (response != null && response.token() != null) {
            return response.token();
        }
        else {
            throw new RuntimeException("Token not found");
        }
    }


    @Override
    public String generateUsername() {
        int maxLength = 15 - PREFIX_USERNAME.length();
        String randomSuffix = generateRandomSuffix(maxLength);

        return PREFIX_USERNAME + randomSuffix;
    }

    @Override
    public String generateNameUnique() {
        int maxLength = 20 - PREFIX_NAME.length();
        String randomSuffix = generateRandomSuffix(maxLength);

        return PREFIX_NAME + randomSuffix;
    }

    @Override
    public String generatePassword() {
        int maxLength = 15 - PREFIX_CONTRA.length();
        String randomSuffix = generateRandomSuffix(maxLength);

        return PREFIX_CONTRA + randomSuffix;
    }

    private String generateRandomSuffix(int length) {
        StringBuilder randomSuffix = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = RANDOM.nextInt(CHARACTERS.length());
            randomSuffix.append(CHARACTERS.charAt(randomIndex));
        }
        return randomSuffix.toString();
    }
}

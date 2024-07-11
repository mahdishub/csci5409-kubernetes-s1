package com.example.service1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@Slf4j
public class MainController {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorOutput> handleBadRequestException(BadRequestException e) {
        log.info("Error occurred --> {}" , e.getErrorOutput().getError());
        return ResponseEntity.badRequest()
                .body(e.getErrorOutput());
    }

    @Value("${data.directory}")
    private String directory;

    @Value("${service2.url}")
    private String service2Url;

    private static final String FILE_NOT_FOUND = "File not found.";
    private static final String INVALID_JSON_INPUT = "Invalid JSON input.";

    private final RestTemplate restTemplate;


    @PostMapping("/calculate")
    public ResponseEntity<Output> calculate(@RequestBody Input input) {
        validateInput(input);

        return ResponseEntity.ok(getOutputFromService2(input));
    }

    private Output getOutputFromService2(Input input) {
        try {
            return restTemplate.postForObject(service2Url, input, Output.class);

        } catch (HttpClientErrorException e) {
            throw new BadRequestException(e.getResponseBodyAs(ErrorOutput.class));
        } catch (Exception e) {
            throw new RuntimeException("Some error occurred in service2");
        }
    }

    private void validateInput(Input input) {
        if (Objects.isNull(input.getFile())) {
            throw new BadRequestException(
                    new ErrorOutput(null, INVALID_JSON_INPUT)
            );
        }

        validateFileExists(input.getFile());
    }

    private void validateFileExists(String fileName) {
        final var filePath = Paths.get(directory, fileName);
        try {
            final var content = Files.readString(filePath);

        } catch (IOException e) {
            throw new BadRequestException(new ErrorOutput(fileName, FILE_NOT_FOUND));
        }
    }




}

package com.example.service1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@Slf4j
public class MainController {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorOutput> handleBadRequestException(BadRequestException e) {
        log.info("BadRequest occurred --> {}" , e.getErrorOutput().getError());
        return ResponseEntity.badRequest()
                .body(e.getErrorOutput());
    }

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<ErrorOutput> handleServerErrorException(ServerErrorException e) {
        log.info("ServerErrorException occurred --> {}" , e.getErrorOutput());
        return ResponseEntity.internalServerError()
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
        log.info("In calculate api....");
        validateInput(input);

        return ResponseEntity.ok(getOutputFromService2(input));
    }


    @PostMapping("/store-file")
    public ResponseEntity<FileStoreOutput> storeFile(@RequestBody FileDataInput input) {
        validateFileDataInput(input);
        try {
            final var fullFilePath = directory + File.separator + input.getFile();
            createFileIfNotExists(fullFilePath);
            writeStringToFile(fullFilePath, input.getData(), false);
            return ResponseEntity.ok(new FileStoreOutput(input.getFile(), "Success."));

        } catch (IOException e) {
            log.info(e.getMessage());
            throw new ServerErrorException(
                    new ErrorOutput(input.getFile(), "Error while storing the file to the storage."));
        }
    }

    private void validateFileDataInput(FileDataInput input) {
        if (Strings.isBlank(input.getFile())) {
            throw new BadRequestException(
                    new ErrorOutput(input.getFile(), "Invalid JSON input."));
        }
    }

    private static boolean createFileIfNotExists(String pathString) throws IOException {
        Path path = Paths.get(pathString);

        if (Files.notExists(path)) {
            Files.createFile(path);
            return true;
        } else {
            return false;
        }
    }

    private static void writeStringToFile(String pathString, String content, boolean append) throws IOException {
        Path path = Paths.get(pathString);
        if (append) {
            Files.writeString(path, content, StandardOpenOption.APPEND);
        } else {
            Files.writeString(path, content);
        }
    }

    private Output getOutputFromService2(Input input) {
        try {
            return restTemplate.postForObject(service2Url, input, Output.class);

        } catch (HttpClientErrorException e) {
            throw new BadRequestException(e.getResponseBodyAs(ErrorOutput.class));
        } catch (Exception e) {
            log.info(e.getMessage());
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
            Files.readString(filePath);

        } catch (IOException e) {
            throw new BadRequestException(new ErrorOutput(fileName, FILE_NOT_FOUND));
        }
    }




}

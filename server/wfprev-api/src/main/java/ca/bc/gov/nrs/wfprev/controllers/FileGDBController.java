package ca.bc.gov.nrs.wfprev.controllers;

import ca.bc.gov.nrs.wfprev.services.FileGDBService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/gdb")
public class FileGDBController {
    private final FileGDBService fileGDBService;

    public FileGDBController(FileGDBService fileGDBService) {
        this.fileGDBService = fileGDBService;
    }

    @PostMapping("/upload")
    public List<String> extractCoordinates(@RequestParam("file") MultipartFile file) throws IOException {
        return fileGDBService.processGDB(file);
    }
}
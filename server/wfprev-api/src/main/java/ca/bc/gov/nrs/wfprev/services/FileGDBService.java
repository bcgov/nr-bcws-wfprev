package ca.bc.gov.nrs.wfprev.services;

import ca.bc.gov.nrs.wfprev.interfaces.FileGDBLibrary;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.PointerByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class FileGDBService {
    public List<String> processGDB(MultipartFile zipFile) throws IOException {
        String tempDir = Files.createTempDirectory("gdb_extracted_").toString();
        try {
            String gdbPath = unzipGDB(zipFile, tempDir);
            return readFeatureClasses(gdbPath);
        } finally {
            try {
                Files.delete(Paths.get(tempDir));
            } catch (IOException e) {
                log.warn("Could not delete temporary directory", e);
            }
        }
    }

    private List<String> readFeatureClasses(String gdbPath) {
        FileGDBLibrary lib = FileGDBLibrary.INSTANCE;

        // Extensive pre-call diagnostics
        logDiagnosticInfo(gdbPath);

        try {
            // Create memory-backed pointer references
            PointerByReference geodatabasePtr = createSafePointerReference();
            PointerByReference tableNamesPtr = createSafePointerReference();

            // Perform geodatabase operations with extensive error checking
            return performGeodatabaseOperations(lib, gdbPath, geodatabasePtr, tableNamesPtr);

        } catch (Exception e) {
            log.error("Comprehensive error in readFeatureClasses", e);
            return List.of("Error processing geodatabase: " + getDetailedErrorMessage(e));
        }
    }

    private void logDiagnosticInfo(String gdbPath) {
        log.info("=== FileGDB Diagnostic Information ===");
        log.info("Geodatabase Path: {}", gdbPath);
        log.info("JNA Version: {}", Native.VERSION);
        log.info("Native Pointer Size: {}", Native.POINTER_SIZE);
        log.info("Current Thread: {}", Thread.currentThread().getName());
        log.info("Java Version: {}", System.getProperty("java.version"));
        log.info("OS Architecture: {}", System.getProperty("os.arch"));
        log.info("=== End Diagnostic Information ===");
    }

    private PointerByReference createSafePointerReference() {
        // Create a pointer reference with pre-allocated memory
        PointerByReference ptr = new PointerByReference();
        ptr.setValue(new Memory(Native.POINTER_SIZE));
        return ptr;
    }

    private List<String> performGeodatabaseOperations(
            FileGDBLibrary lib,
            String gdbPath,
            PointerByReference geodatabasePtr,
            PointerByReference tableNamesPtr
    ) {
        try {
            // Open geodatabase with detailed logging
            int openResult = openGeodatabaseWithLogging(lib, gdbPath, geodatabasePtr);
            if (openResult != 0) {
                log.error("OpenGeodatabase failed. Result: {}", openResult);
                return List.of("Failed to open geodatabase. Error code: " + openResult);
            }

            // Validate geodatabase pointer
            Pointer dbPointer = validatePointer(geodatabasePtr, "Geodatabase");

            // Get table names
            int tableResult = lib.GetTableNames(dbPointer, tableNamesPtr);
            if (tableResult != 0) {
                log.error("GetTableNames failed. Result: {}", tableResult);
                return List.of("Failed to get table names. Error code: " + tableResult);
            }

            // Extract table names
            Pointer namesPointer = validatePointer(tableNamesPtr, "TableNames");
            return extractTableNames(namesPointer);

        } catch (Exception e) {
            log.error("Error in geodatabase operations", e);
            return List.of("Geodatabase operation error: " + getDetailedErrorMessage(e));
        } finally {
            // Ensure geodatabase is closed
            safeCloseGeodatabase(lib, geodatabasePtr);
        }
    }

    private int openGeodatabaseWithLogging(
            FileGDBLibrary lib,
            String gdbPath,
            PointerByReference geodatabasePtr
    ) {
        log.info("Attempting to open geodatabase: {}", gdbPath);
        WString wGdbPath = new WString(gdbPath);

        try {
            int result = lib.OpenGeodatabase(wGdbPath, geodatabasePtr);
            log.info("OpenGeodatabase result: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Exception in OpenGeodatabase", e);
            throw e;
        }
    }

    private Pointer validatePointer(PointerByReference ptrRef, String pointerType) {
        Pointer ptr = ptrRef.getValue();
        if (ptr == null || ptr == Pointer.NULL) {
            log.error("{} pointer is null or NULL", pointerType);
            throw new IllegalStateException(pointerType + " pointer is invalid");
        }
        return ptr;
    }

    private void safeCloseGeodatabase(FileGDBLibrary lib, PointerByReference geodatabasePtr) {
        try {
            Pointer ptr = geodatabasePtr.getValue();
            if (ptr != null && ptr != Pointer.NULL) {
                int closeResult = lib.CloseGeodatabase(geodatabasePtr);
                log.info("Geodatabase close result: {}", closeResult);
            }
        } catch (Exception e) {
            log.error("Error closing geodatabase", e);
        }
    }

    private List<String> extractTableNames(Pointer namesPointer) {
        List<String> tableNames = new ArrayList<>();
        try {
            int offset = 0;
            while (true) {
                // Safely extract wide string
                String name = namesPointer.getWideString(offset);
                if (name == null || name.isEmpty()) break;

                tableNames.add(name);

                // Move offset to next string
                offset += (name.length() + 1) * Native.WCHAR_SIZE;
            }
            return tableNames;
        } catch (Exception e) {
            log.error("Error extracting table names", e);
            return List.of("Error extracting table names: " + getDetailedErrorMessage(e));
        }
    }

    private String getDetailedErrorMessage(Throwable e) {
        return e.getClass().getSimpleName() + ": " + e.getMessage();
    }

    private String unzipGDB(MultipartFile zipFile, String destDir) throws IOException {
        Path destPath = Paths.get(destDir);
        Files.createDirectories(destPath);

        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newFile = destPath.resolve(entry.getName()).normalize();
                if (!newFile.startsWith(destPath)) {
                    throw new IOException("Bad zip entry: " + entry.getName()); // Prevent Zip Slip attack
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(newFile);
                } else {
                    Files.createDirectories(newFile.getParent());
                    Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);
                }
                zis.closeEntry();
            }
        }

        // 🔍 Locate the extracted .gdb folder
        return findGDBDirectory(destPath);
    }

    private String findGDBDirectory(Path rootDir) throws IOException {
        try (Stream<Path> paths = Files.walk(rootDir)) {
            return paths
                    .filter(Files::isDirectory)
                    .filter(path -> path.toString().endsWith(".gdb"))
                    .map(Path::toString)
                    .findFirst()
                    .orElseThrow(() -> new IOException("No .gdb folder found in extracted files"));
        }
    }
}

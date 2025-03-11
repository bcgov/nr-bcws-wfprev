package ca.bc.gov.nrs.wfprev.entities;

import ca.bc.gov.nrs.wfprev.data.entities.FileAttachmentEntity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class FileAttachmentEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private FileAttachmentEntity createValidFileAttachmentEntity() {
        return FileAttachmentEntity.builder()
                .fileAttachmentGuid(UUID.randomUUID())
                .sourceObjectUniqueId(String.valueOf(UUID.randomUUID()))
                .documentPath("/path/to/document.pdf")
                .attachmentReadOnlyInd(true)
                .revisionCount(1)
                .createUser("tester")
                .createDate(new Date())
                .updateUser("tester")
                .updateDate(new Date())
                .build();
    }

    @Test
    void testSourceObjectUniqueId_IsNull() {
        FileAttachmentEntity entity = createValidFileAttachmentEntity();
        entity.setSourceObjectUniqueId(null);

        Set<ConstraintViolation<FileAttachmentEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("sourceObjectUniqueId") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testDocumentPath_IsNull() {
        FileAttachmentEntity entity = createValidFileAttachmentEntity();
        entity.setDocumentPath(null);

        Set<ConstraintViolation<FileAttachmentEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("documentPath") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testAttachmentReadOnlyInd_IsNull() {
        FileAttachmentEntity entity = createValidFileAttachmentEntity();
        entity.setAttachmentReadOnlyInd(null);

        Set<ConstraintViolation<FileAttachmentEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("attachmentReadOnlyInd") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateUser_IsNull() {
        FileAttachmentEntity entity = createValidFileAttachmentEntity();
        entity.setCreateUser(null);

        Set<ConstraintViolation<FileAttachmentEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("createUser") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testCreateDate_IsNull() {
        FileAttachmentEntity entity = createValidFileAttachmentEntity();
        entity.setCreateDate(null);

        Set<ConstraintViolation<FileAttachmentEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("createDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateUser_IsNull() {
        FileAttachmentEntity entity = createValidFileAttachmentEntity();
        entity.setUpdateUser(null);

        Set<ConstraintViolation<FileAttachmentEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("updateUser") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testUpdateDate_IsNull() {
        FileAttachmentEntity entity = createValidFileAttachmentEntity();
        entity.setUpdateDate(null);

        Set<ConstraintViolation<FileAttachmentEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("updateDate") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testRevisionCount_IsNull() {
        FileAttachmentEntity entity = createValidFileAttachmentEntity();
        entity.setRevisionCount(null);

        Set<ConstraintViolation<FileAttachmentEntity>> violations = validator.validate(entity);

        assertThat(violations)
                .hasSize(1)
                .anyMatch(violation ->
                        violation.getPropertyPath().toString().equals("revisionCount") &&
                                violation.getMessage().equals("must not be null"));
    }

    @Test
    void testAllFields_AreValid() {
        FileAttachmentEntity entity = createValidFileAttachmentEntity();

        Set<ConstraintViolation<FileAttachmentEntity>> violations = validator.validate(entity);

        assertThat(violations).isEmpty();
    }
}
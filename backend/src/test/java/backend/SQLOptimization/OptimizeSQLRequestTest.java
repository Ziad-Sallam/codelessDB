package backend.SQLOptimization;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import backend.SQLOptimization.dto.OptimizeSQLRequest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OptimizeSQLRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRequest() {
        OptimizeSQLRequest request = new OptimizeSQLRequest();
        request.setSqlCode("SELECT * FROM users");

        Set<ConstraintViolation<OptimizeSQLRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testEmptySQL_ValidationFails() {
        OptimizeSQLRequest request = new OptimizeSQLRequest();
        request.setSqlCode("");

        Set<ConstraintViolation<OptimizeSQLRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("cannot be empty"));
    }

    @Test
    void testNullSQL_ValidationFails() {
        OptimizeSQLRequest request = new OptimizeSQLRequest();
        request.setSqlCode(null);

        Set<ConstraintViolation<OptimizeSQLRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }
}


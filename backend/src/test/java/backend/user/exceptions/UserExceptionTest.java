package backend.user.exceptions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserExceptionTest {

    @Test
    void testExceptionInstantiations() {
        // Instantiate the outer class for coverage
        assertNotNull(new UserException());

        UserException.UserNotFoundException e1 = new UserException.UserNotFoundException("msg");
        assertEquals("msg", e1.getMessage());

        UserException.EmailAlreadyExistsException e2 = new UserException.EmailAlreadyExistsException("msg");
        assertEquals("msg", e2.getMessage());

        UserException.InvalidEmailException e3 = new UserException.InvalidEmailException("msg");
        assertEquals("msg", e3.getMessage());

        UserException.UsernameAlreadyExistsException e4 = new UserException.UsernameAlreadyExistsException("msg");
        assertEquals("msg", e4.getMessage());

        UserException.InvalidTokenException e5 = new UserException.InvalidTokenException("msg");
        assertEquals("msg", e5.getMessage());

        UserException.OtpSendFailedException e6 = new UserException.OtpSendFailedException("msg");
        assertEquals("msg", e6.getMessage());

        UserException.QuotaExceededException e7 = new UserException.QuotaExceededException("msg");
        assertEquals("msg", e7.getMessage());
    }
}

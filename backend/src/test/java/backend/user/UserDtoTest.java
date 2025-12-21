package backend.user;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import backend.entities.User;

class UserDtoTest {

    @Test
    void testGettersAndSetters() {
        UserDto dto = new UserDto();
        LocalDateTime now = LocalDateTime.now();
        
        dto.setUsername("user");
        dto.setEmail("email");
        dto.setRawPassword("pwd");
        dto.setPicture("pic");
        dto.setCreatedAt(now);
        dto.setBio("bio");
        dto.setPublicProfile("profile");
        dto.setProfileWebsiteUrl("url");
        dto.setAiQuotaRemaining(10);
        dto.setAiQuotaResetDate(now);

        assertEquals("user", dto.getUsername());
        assertEquals("email", dto.getEmail());
        assertEquals("pwd", dto.getRawPassword());
        assertEquals("pic", dto.getPicture());
        assertEquals(now, dto.getCreatedAt());
        assertEquals("bio", dto.getBio());
        assertEquals("profile", dto.getPublicProfile());
        assertEquals("url", dto.getProfileWebsiteUrl());
        assertEquals(10, dto.getAiQuotaRemaining());
        assertEquals(now, dto.getAiQuotaResetDate());
    }

    @Test
    void testUserConstructor() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername("user");
        user.setEmail("email");
        user.setPicture("pic");
        user.setCreatedAt(now);
        user.setBio("bio");
        user.setPublicProfile("profile");
        user.setProfileWebsiteUrl("url");
        user.setAiQuotaRemaining(10);
        user.setAiQuotaResetDate(now);

        UserDto dto = new UserDto(user);

        assertEquals("user", dto.getUsername());
        assertEquals("email", dto.getEmail());
        assertNull(dto.getRawPassword());
        assertEquals("pic", dto.getPicture());
        assertEquals(now, dto.getCreatedAt());
        assertEquals("bio", dto.getBio());
        assertEquals("profile", dto.getPublicProfile());
        assertEquals("url", dto.getProfileWebsiteUrl());
        assertEquals(10, dto.getAiQuotaRemaining());
        assertEquals(now, dto.getAiQuotaResetDate());
    }

    @Test
    void testEqualsHeaderAndHashcode() {
        UserDto dto1 = new UserDto();
        dto1.setUsername("a");
        UserDto dto2 = new UserDto();
        dto2.setUsername("a");
        UserDto dto3 = new UserDto();
        dto3.setUsername("b");

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
        assertEquals(dto1.toString(), dto2.toString());
    }
}

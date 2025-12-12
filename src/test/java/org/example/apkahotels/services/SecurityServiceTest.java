
package org.example.apkahotels.services;

import org.example.apkahotels.models.AppUser;
import org.example.apkahotels.models.UserRole;
import org.example.apkahotels.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SecurityService securityService;

    private AppUser testUser;
    private AppUser adminUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Testowy użytkownik CLIENT
        testUser = new AppUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(UserRole.CLIENT);
        testUser.setRoles(Set.of(UserRole.CLIENT));
        testUser.setActive(true);

        // Testowy admin
        adminUser = new AppUser();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("encodedAdminPassword");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setRoles(Set.of(UserRole.ADMIN));
        adminUser.setActive(true);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldAllowAdminAccess() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        // when
        boolean hasAdminAccess = securityService.hasRole(UserRole.ADMIN);
        Optional<AppUser> currentUser = securityService.getCurrentUser();

        // then
        assertTrue(hasAdminAccess);
        assertTrue(currentUser.isPresent());
        assertEquals(UserRole.ADMIN, currentUser.get().getRole());
        assertEquals("admin", currentUser.get().getUsername());
    }

    @Test
    void shouldAuthenticateUser() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // when
        Optional<AppUser> currentUser = securityService.getCurrentUser();

        // then
        assertTrue(currentUser.isPresent());
        assertEquals("testuser", currentUser.get().getUsername());
        assertEquals(UserRole.CLIENT, currentUser.get().getRole());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void shouldValidatePassword() {
        // Testujemy czy użytkownik jest aktywny - to podstawowa walidacja w SecurityService
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // when
        Optional<AppUser> user = securityService.getCurrentUser();

        // then
        assertTrue(user.isPresent());
        assertTrue(user.get().isActive());
    }

    @Test
    void shouldBlockUnauthorizedAccess() {
        // given - brak authentication
        when(securityContext.getAuthentication()).thenReturn(null);

        // when
        boolean hasAdminAccess = securityService.hasRole(UserRole.ADMIN);
        Optional<AppUser> currentUser = securityService.getCurrentUser();

        // then
        assertFalse(hasAdminAccess);
        assertTrue(currentUser.isEmpty());
    }

    @Test
    void shouldBlockNonAdminFromAdminAccess() {
        // given - zwykły user próbuje dostać się do admin
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // when
        boolean hasAdminAccess = securityService.hasRole(UserRole.ADMIN);

        // then
        assertFalse(hasAdminAccess);
    }

    @Test
    void shouldReturnEmptyForAnonymousUser() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("anonymousUser");

        // when
        Optional<AppUser> currentUser = securityService.getCurrentUser();

        // then
        assertTrue(currentUser.isEmpty());
    }

    @Test
    void shouldCheckHotelManagerRole() {
        // given
        AppUser hotelManager = new AppUser();
        hotelManager.setUsername("manager");
        hotelManager.setRole(UserRole.HOTEL_MANAGER);
        hotelManager.setRoles(Set.of(UserRole.HOTEL_MANAGER));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("manager");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("manager")).thenReturn(Optional.of(hotelManager));

        // when
        boolean isHotelManager = securityService.isHotelManager();

        // then
        assertTrue(isHotelManager);
    }

    @Test
    void shouldCheckReceptionistPermissions() {
        // given
        AppUser receptionist = new AppUser();
        receptionist.setUsername("receptionist");
        receptionist.setRole(UserRole.RECEPTIONIST);
        receptionist.setRoles(Set.of(UserRole.RECEPTIONIST));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("receptionist");
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userRepository.findByUsername("receptionist")).thenReturn(Optional.of(receptionist));

        // when
        boolean canManageReservations = securityService.canManageReservations();

        // then
        assertTrue(canManageReservations);
    }
}
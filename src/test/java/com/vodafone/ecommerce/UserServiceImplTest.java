package com.vodafone.ecommerce;


import com.vodafone.ecommerce.mapper.UserMapper;
import com.vodafone.ecommerce.model.dto.*;
import com.vodafone.ecommerce.model.entity.*;
import com.vodafone.ecommerce.model.enums.AccountStatus;
import com.vodafone.ecommerce.model.enums.Role;
import com.vodafone.ecommerce.repository.ShoppingCartRepository;
import com.vodafone.ecommerce.repository.UserRepository;
import com.vodafone.ecommerce.security.model.CustomUserDetails;
import com.vodafone.ecommerce.security.repository.RefreshTokenRepository;
import com.vodafone.ecommerce.service.CustomerService;
import com.vodafone.ecommerce.service.impl.UserServiceImpl;
import com.vodafone.ecommerce.validation.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.junit.jupiter.api.AfterEach;
import org.mockito.MockedStatic;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;


import java.util.List;
import java.util.Optional;

import static com.vodafone.ecommerce.model.enums.AccountStatus.DEACTIVATED;
import static com.vodafone.ecommerce.model.enums.Role.ADMIN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private CustomerService customerService;
    @Mock
    private UserValidator userValidator;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private final CustomerProfile customer = new CustomerProfile();
    private final User user = new User();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        customer.setCustomerProfileId(1L);

        user.setUserId(1L);
        user.setRole(ADMIN);
        user.setCustomerProfile(customer);
    }

    @Test
    void registerUser_ShouldCreateAndReturnUser() {
        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setEmail("email@test.com");
        request.setPassword("pass");
        request.setPhoneNumber("123");
        request.setUsername("admin");

        when(userMapper.mapToUserForCreateUser(request)).thenReturn(user);
        when(customerService.registerCustomer(request)).thenReturn(customer);
        when(userRepository.save(any())).thenReturn(user);

        User saved = userService.registerUser(request);

        assertNotNull(saved);
        assertEquals(DEACTIVATED, saved.getAccountStatus());
        verify(userValidator).validateEmail(request.getEmail());
        verify(userValidator).validateUniqueUsername("admin");
        verify(userValidator).validateUniquePhone("123");
    }

    @Test
    void deleteUser_ShouldCascadeDeleteRelatedEntities() {
        ShoppingCart cart = new ShoppingCart();
        when(shoppingCartRepository.findByCustomerProfile(customer)).thenReturn(cart);

        userService.deleteUser(user);

        verify(refreshTokenRepository).deleteByUser(user);
        verify(shoppingCartRepository).delete(cart);
        verify(customerService).deleteCustomerProfile(customer);
        verify(userRepository).delete(user);
    }

    @Test
    void addAdmin_ShouldCreateAdminUser() {
        AdminRequestDTO request = new AdminRequestDTO();
        request.setUsername("admin");
        request.setPassword("pass");
        request.setEmail("admin@test.com");

        User admin = new User();
        when(userMapper.mapToUserAdmin(request)).thenReturn(admin);

        userService.addAdmin(request);

        verify(userValidator).validateEmail("admin@test.com");
        verify(userValidator).validateUniqueUsername("admin");
        verify(userRepository).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertEquals(ADMIN, saved.getRole());
        assertEquals(AccountStatus.ACTIVE, saved.getAccountStatus());
    }

    @Test
    void getAllAdmins_ShouldReturnAdminList() {
        User admin = new User();
        admin.setRole(ADMIN);

        List<User> admins = List.of(admin);
        when(userRepository.findAllByRoleIn(List.of(ADMIN, Role.SUPER_ADMIN))).thenReturn(admins);
        when(userMapper.toAdminResponseDTOList(admins)).thenReturn(List.of(new AdminResponseDTO()));

        List<AdminResponseDTO> result = userService.getAllAdmins();

        assertEquals(1, result.size());
    }

    @Test
    void updateAdmin_ShouldUpdateUserDetails() {
        AdminUpdateRequestDTO dto = new AdminUpdateRequestDTO();
        dto.setUsername("newuser");

        when(userValidator.validateUserExistence(1L)).thenReturn(user);

        userService.updateAdmin(dto, 1L);

        verify(customerService).handleUsername("newuser", user);
        verify(customerService).handlePasswordUpdate(dto, user);
        verify(userRepository).save(user);
    }

    @Test
    void deleteAdmin_ShouldDeleteIfUserIsAdmin() {
        when(userValidator.validateUserExistence(1L)).thenReturn(user);

        userService.deleteAdmin(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteAdmin_NotAdmin_ShouldThrow() {
        user.setRole(Role.CUSTOMER);
        when(userValidator.validateUserExistence(1L)).thenReturn(user);

        assertThrows(IllegalArgumentException.class, () -> userService.deleteAdmin(1L));
    }

    @Test
    void isAdmin_ShouldReturnTrueIfAdmin() {
        user.setRole(ADMIN);
        assertTrue(userService.isAdmin(user));
    }

    @Test
    void isAdmin_ShouldReturnFalseIfNotAdmin() {
        user.setRole(Role.CUSTOMER);
        assertFalse(userService.isAdmin(user));
    }
    @Test
    void getLoggedInUser_ShouldReturnUserFromSecurityContext() {
        User expectedUser = new User();
        expectedUser.setUserId(123L);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUser()).thenReturn(expectedUser);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> contextHolderMock = mockStatic(SecurityContextHolder.class)) {
            contextHolderMock.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            User result = userService.getLoggedInUser();

            assertNotNull(result);
            assertEquals(123L, result.getUserId());
        }
    }
    @Test
    void getAdminProfile_ShouldReturnMappedAdminDTO() {
        // Given
        User mockUser = new User();
        mockUser.setUserId(123L);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUser()).thenReturn(mockUser);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        AdminResponseDTO expectedDTO = new AdminResponseDTO();
        when(userMapper.toAdminResponseDTO(mockUser)).thenReturn(expectedDTO);

        // When
        try (MockedStatic<SecurityContextHolder> contextHolder = mockStatic(SecurityContextHolder.class)) {
            contextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            AdminResponseDTO result = userService.getAdminProfile();

            // Then
            assertNotNull(result);
            assertEquals(expectedDTO, result);
            verify(userMapper).toAdminResponseDTO(mockUser);
        }
    }


}


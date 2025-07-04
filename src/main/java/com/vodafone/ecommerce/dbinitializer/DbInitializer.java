package com.vodafone.ecommerce.dbinitializer;

import com.vodafone.ecommerce.model.entity.*;
import com.vodafone.ecommerce.model.enums.AccountStatus;
import com.vodafone.ecommerce.model.enums.Role;
import com.vodafone.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DbInitializer implements CommandLineRunner {

    private final CustomerProfileRepository customerProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;
    private final ShoppingCartRepository shoppingCartRepository;

    @Override
    public void run(String... args) throws Exception {
        String adminPassword = env.getProperty("admin.password");

        CustomerProfile customer = new CustomerProfile();
        customer.setFirstName("Mariam");
        customer.setLastName("Amr");
        customer.setPhoneNumber("01012345678");
        customer = customerProfileRepository.save(customer);

        User user = new User();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode(adminPassword));
        user.setEmail("mariam.tomoe@gmail.com");
        user.setRole(Role.CUSTOMER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setCustomerProfile(customer);

        CustomerProfile customer2 = new CustomerProfile();
        customer2.setFirstName("Mariam2");
        customer2.setLastName("Amr2");
        customer2.setPhoneNumber("01012345677");
        customer2 = customerProfileRepository.save(customer2);

        User user2 = new User();
        user2.setUsername("admin");
        user2.setPassword(passwordEncoder.encode(adminPassword));
        user2.setEmail("mariam2@example.com");
        user2.setRole(Role.SUPER_ADMIN);
        user2.setAccountStatus(AccountStatus.ACTIVE);
        user2.setCustomerProfile(customer2);



        ShoppingCart cart = new ShoppingCart();
        cart.setCustomerProfile(customer);


        userRepository.save(user);
        userRepository.save(user2);
        shoppingCartRepository.save(cart);

    }
}

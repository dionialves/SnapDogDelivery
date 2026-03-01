package com.dionialves.snapdogdelivery.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.dionialves.snapdogdelivery.domain.admin.customer.Customer;
import com.dionialves.snapdogdelivery.domain.admin.customer.CustomerRepository;
import com.dionialves.snapdogdelivery.domain.admin.customer.State;
import com.dionialves.snapdogdelivery.domain.admin.order.Order;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderRepository;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderStatus;
import com.dionialves.snapdogdelivery.domain.admin.product.Product;
import com.dionialves.snapdogdelivery.domain.admin.product.ProductRepository;
import com.dionialves.snapdogdelivery.domain.admin.settings.CompanySettings;
import com.dionialves.snapdogdelivery.domain.admin.settings.CompanySettingsRepository;
import com.dionialves.snapdogdelivery.domain.admin.user.Role;
import com.dionialves.snapdogdelivery.domain.admin.user.User;
import com.dionialves.snapdogdelivery.domain.admin.user.UserRepository;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CompanySettingsRepository companySettingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsers();
        }

        if (customerRepository.count() == 0) {
            seedCustomers();
        }

        if (productRepository.count() == 0) {
            seedProducts();
        }

        if (orderRepository.count() == 0) {
            seedOrders();
        }

        if (companySettingsRepository.count() == 0) {
            seedCompanySettings();
        }
    }

    private void seedUsers() {
        List<User> users = List.of(
                createUser("Administrador", "admin@snapdog.com", "admin123", Role.ADMIN),
                createUser("Super Admin", "superadmin@snapdog.com", "super123", Role.SUPER_ADMIN),
                createUser("Usuário", "user@snapdog.com", "user123", Role.USER));

        userRepository.saveAll(users);
    }

    private void seedCustomers() {
        List<Customer> customers = List.of(
                createCustomer("João Silva", "(11) 98765-4321", "joao.silva@email.com",
                        "São Paulo", State.SP, "Vila Mariana", "Rua Domingos de Morais", "04010-000", "120", null),
                createCustomer("Maria Oliveira", "(21) 97654-3210", "maria.oliveira@email.com",
                        "Rio de Janeiro", State.RJ, "Copacabana", "Av. Atlântica", "22010-000", "500", "Apto 301"),
                createCustomer("Carlos Santos", "(31) 96543-2109", "carlos.santos@email.com",
                        "Belo Horizonte", State.MG, "Savassi", "Rua Pernambuco", "30130-150", "45", null),
                createCustomer("Ana Costa", "(41) 95432-1098", "ana.costa@email.com",
                        "Curitiba", State.PR, "Batel", "Rua Coronel Dulcídio", "80420-170", "88", "Casa 2"),
                createCustomer("Pedro Souza", "(51) 94321-0987", "pedro.souza@email.com",
                        "Porto Alegre", State.RS, "Moinhos de Vento", "Rua Padre Chagas", "90570-080", "200", null),
                createCustomer("Fernanda Lima", "(85) 93210-9876", "fernanda.lima@email.com",
                        "Fortaleza", State.CE, "Meireles", "Av. Beira Mar", "60165-121", "1500", "Apto 802"),
                createCustomer("Lucas Pereira", "(71) 92109-8765", "lucas.pereira@email.com",
                        "Salvador", State.BA, "Barra", "Av. Oceânica", "40140-130", "310", null),
                createCustomer("Juliana Almeida", "(61) 91098-7654", "juliana.almeida@email.com",
                        "Brasília", State.DF, "Asa Sul", "SQS 308 Bloco A", "70352-010", "12", "Apto 105"),
                createCustomer("Roberto Ferreira", "(81) 90987-6543", "roberto.ferreira@email.com",
                        "Recife", State.PE, "Boa Viagem", "Rua Setúbal", "51030-010", "78", null),
                createCustomer("Camila Rodrigues", "(48) 99876-5432", "camila.rodrigues@email.com",
                        "Florianópolis", State.SC, "Centro", "Rua Felipe Schmidt", "88010-000", "55", "Sala 3"));

        customerRepository.saveAll(customers);
    }

    private void seedProducts() {
        List<Product> products = List.of(
                createProduct("Hot Dog Tradicional", new BigDecimal("12.00"),
                        "Pão, salsicha, molho, mostarda e ketchup"),
                createProduct("Hot Dog Especial", new BigDecimal("16.00"),
                        "Pão, salsicha, purê de batata, milho, ervilha, batata palha e molhos"),
                createProduct("Hot Dog Duplo", new BigDecimal("20.00"),
                        "Pão, duas salsichas, queijo cheddar, bacon e molhos"),
                createProduct("Hot Dog Cheddar Bacon", new BigDecimal("22.00"),
                        "Pão, salsicha, cheddar cremoso, bacon crocante e cebola caramelizada"),
                createProduct("Hot Dog Vegetariano", new BigDecimal("15.00"),
                        "Pão, salsicha de soja, milho, ervilha, purê e molhos"),
                createProduct("Hot Dog Calabresa", new BigDecimal("18.00"),
                        "Pão, salsicha, calabresa ralada, vinagrete e molhos"),
                createProduct("Hot Dog Kids", new BigDecimal("10.00"), "Pão macio, salsicha, ketchup e batata palha"),
                createProduct("Refrigerante Lata", new BigDecimal("6.00"), "Lata 350ml - Coca-Cola, Guaraná ou Fanta"),
                createProduct("Suco Natural", new BigDecimal("8.00"), "Copo 500ml - Laranja, Limão ou Maracujá"),
                createProduct("Água Mineral", new BigDecimal("4.00"), "Garrafa 500ml - com ou sem gás"));

        productRepository.saveAll(products);
    }

    private void seedOrders() {
        List<Customer> customers = customerRepository.findAll();
        List<Product> products = productRepository.findAll();
        Random random = new Random(42);

        OrderStatus[] statuses = OrderStatus.values();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < 50; i++) {
            Customer customer = customers.get(random.nextInt(customers.size()));
            OrderStatus status = statuses[random.nextInt(statuses.length)];

            // Date within last 7 days, with random hour between 10:00 and 22:59
            LocalDateTime createdAt = now
                    .minusDays(random.nextInt(7))
                    .withHour(10 + random.nextInt(13))
                    .withMinute(random.nextInt(60))
                    .withSecond(0)
                    .withNano(0);

            Order order = new Order();
            order.setCustomer(customer);
            order.setStatus(status);
            order.setCreatedAt(createdAt);

            // 1 to 4 products per order
            int productCount = 1 + random.nextInt(4);
            for (int j = 0; j < productCount; j++) {
                Product product = products.get(random.nextInt(products.size()));
                int quantity = 1 + random.nextInt(3);
                order.addProduct(product, quantity, product.getPrice());
            }

            orderRepository.save(order);
        }
    }

    private Customer createCustomer(String name, String phone, String email, String city,
            State state, String neighborhood, String street,
            String zipCode, String number, String complement) {
        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setCity(city);
        customer.setState(state);
        customer.setNeighborhood(neighborhood);
        customer.setStreet(street);
        customer.setZipCode(zipCode);
        customer.setNumber(number);
        customer.setComplement(complement);
        customer.setPassword(passwordEncoder.encode("cliente123"));
        customer.setActive(true);
        return customer;
    }

    private Product createProduct(String name, BigDecimal price, String description) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDescription(description);
        return product;
    }

    private void seedCompanySettings() {
        var settings = new CompanySettings();
        settings.setCompanyName("SnapDog Delivery");
        settings.setEmail("contato@snapdogdelivery.com");
        settings.setPhone("(00) 12345-6789");
        settings.setAddress("Rua Example, 100 — São Paulo, SP");
        settings.setOpeningHours("Seg–Sex: 11h às 22h | Sáb–Dom: 11h às 23h");
        settings.setCopyright("© 2026 SnapDog Delivery. Todos os direitos reservados.");
        companySettingsRepository.save(settings);
    }

    private User createUser(String name, String email, String password, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return user;
    }
}

package com.dionialves.snapdogdelivery.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest
public class ClientRepositoryTest {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void givenNewClient_whenSave_thenCLientIsPesisted() {
        Client newClient = new Client("Everton da Silva", "Rua Bocaneira");
        Client insertedClient = clientRepository.save(newClient);

        assertThat(testEntityManager.find(Client.class, insertedClient.getId())).isEqualTo(newClient);
    }
}

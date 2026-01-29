package com.dionialves.snapdogdelivery.client;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import java.util.List;
import org.hibernate.validator.constraints.Length;

import com.dionialves.snapdogdelivery.order.Order;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Length(min = 2, max = 30, message = "The name length must be between {min} and {max} characters")
    private String name;

    @NotNull
    @Length(min = 2, max = 300, message = "The address length must be betwaan {min} and {max} characters")
    private String address;

    @NonNull
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "client", fetch = FetchType.EAGER)
    private List<Order> orders;

    public Client(String name, String address) {
        this.name = name;
        this.address = address;
    }
}

package com.dionialves.snapdogdelivery.order;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.dionialves.snapdogdelivery.productorder.ProductOrderDTO;
import com.dionialves.snapdogdelivery.client.ClientDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private ClientDTO client;
    private List<ProductOrderDTO> products;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private BigDecimal totalValue;

}

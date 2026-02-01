package com.dionialves.snapdogdelivery.order.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dionialves.snapdogdelivery.productorder.dto.ProductOrderDTO;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Service
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateDTO {

    @NotEmpty(message = "Order must have at least one product")
    @Size(min = 1, max = 50, message = "Order must have between {min} and {max} products")
    @Valid
    private List<ProductOrderDTO> products;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

}

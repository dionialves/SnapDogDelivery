package com.dionialves.snapdogdelivery.domain.storefront.checkout;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dionialves.snapdogdelivery.domain.admin.customer.Customer;
import com.dionialves.snapdogdelivery.domain.admin.order.Order;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderOrigin;
import com.dionialves.snapdogdelivery.domain.admin.order.OrderRepository;
import com.dionialves.snapdogdelivery.domain.admin.order.dto.OrderResponseDTO;
import com.dionialves.snapdogdelivery.domain.admin.product.ProductRepository;
import com.dionialves.snapdogdelivery.domain.admin.productorder.ProductOrder;
import com.dionialves.snapdogdelivery.domain.storefront.cart.Cart;
import com.dionialves.snapdogdelivery.domain.storefront.cart.CartItem;
import com.dionialves.snapdogdelivery.domain.storefront.cart.CartService;
import com.dionialves.snapdogdelivery.exception.BusinessException;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Converte o carrinho de compras em um pedido persistido.
 */
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    /**
     * Cria um pedido a partir do carrinho da sessão.
     *
     * @param session  sessão HTTP com o carrinho
     * @param customer cliente autenticado
     * @return DTO do pedido criado
     * @throws BusinessException se o carrinho estiver vazio
     */
    @Transactional
    public OrderResponseDTO createOrderFromCart(HttpSession session, Customer customer) {
        var cart = cartService.getCart(session);

        if (cart.isEmpty()) {
            throw new BusinessException("Carrinho vazio. Adicione produtos antes de finalizar o pedido.");
        }

        // Monta snapshot do endereço de entrega no momento do pedido
        var deliveryAddress = buildAddressSnapshot(customer);

        // Cria o pedido
        var order = new Order();
        order.setCustomer(customer);
        order.setOrigin(OrderOrigin.ONLINE);
        order.setCreatedAt(LocalDateTime.now());
        order.setDeliveryAddress(deliveryAddress);

        // Converte itens do carrinho em ProductOrder
        addCartItemsToOrder(order, cart);

        var saved = orderRepository.save(order);

        // Limpa o carrinho após criação bem-sucedida
        cartService.clear(session);

        log.info("Pedido online criado: id={}, cliente={}", saved.getId(), customer.getId());
        return OrderResponseDTO.fromEntity(saved);
    }

    private void addCartItemsToOrder(Order order, Cart cart) {
        for (CartItem item : cart.getItemList()) {
            var product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new BusinessException(
                            "Produto não encontrado: " + item.getProductName()));

            var productOrder = new ProductOrder(product, order, item.getQuantity(), item.getUnitPrice());
            order.getProductOrders().add(productOrder);
        }
    }

    private String buildAddressSnapshot(Customer customer) {
        var sb = new StringBuilder();
        sb.append(customer.getStreet()).append(", ").append(customer.getNumber());

        if (customer.getComplement() != null && !customer.getComplement().isBlank()) {
            sb.append(", ").append(customer.getComplement());
        }

        sb.append(" — ").append(customer.getNeighborhood());
        sb.append(", ").append(customer.getCity());
        sb.append("/").append(customer.getState().getCode());
        sb.append(" — CEP: ").append(customer.getZipCode());

        return sb.toString();
    }
}

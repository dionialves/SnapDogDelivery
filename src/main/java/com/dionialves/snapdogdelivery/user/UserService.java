package com.dionialves.snapdogdelivery.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dionialves.snapdogdelivery.exception.BusinessException;
import com.dionialves.snapdogdelivery.exception.NotFoundException;
import com.dionialves.snapdogdelivery.user.dto.UserCreateDTO;
import com.dionialves.snapdogdelivery.user.dto.UserResponseDTO;
import com.dionialves.snapdogdelivery.user.dto.UserUpdateDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private static final int PAGE_SIZE = 10;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Lista todos os usuários de forma paginada, ordenados por nome.
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAll(int page) {
        PageRequest pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "name"));
        return userRepository.findAll(pageable).map(UserResponseDTO::fromEntity);
    }

    /**
     * Busca um usuário pelo ID ou lança NotFoundException.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado com ID: " + id));
        return UserResponseDTO.fromEntity(user);
    }

    /**
     * Cria um novo usuário administrativo.
     * Valida unicidade do e-mail e encoda a senha.
     */
    @Transactional
    public UserResponseDTO create(UserCreateDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("Já existe um usuário com o e-mail: " + dto.getEmail());
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());

        log.info("Criando usuário: {} ({})", dto.getEmail(), dto.getRole());
        return UserResponseDTO.fromEntity(userRepository.save(user));
    }

    /**
     * Atualiza nome, role e opcionalmente a senha de um usuário existente.
     */
    @Transactional
    public UserResponseDTO update(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado com ID: " + id));

        user.setName(dto.getName());
        user.setRole(dto.getRole());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        log.info("Atualizando usuário ID {}: nome={}, role={}", id, dto.getName(), dto.getRole());
        return UserResponseDTO.fromEntity(user);
    }

    /**
     * Remove um usuário. Não permite excluir o próprio usuário autenticado.
     */
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Usuário não encontrado com ID: " + id);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            String emailAtual = auth.getName();
            userRepository.findById(id).ifPresent(user -> {
                if (user.getEmail().equals(emailAtual)) {
                    throw new BusinessException("Não é possível excluir o próprio usuário autenticado");
                }
            });
        }

        log.info("Excluindo usuário ID {}", id);
        userRepository.deleteById(id);
    }
}

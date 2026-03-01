package com.dionialves.snapdogdelivery.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public Object handleNotMapped(Exception ex, HttpServletRequest request) {

        if (isApiRequest(request)) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Recurso não encontrado",
                    LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        String template = isAdminRequest(request) ? "admin/error/404" : "public/error/404";
        ModelAndView modelAndView = new ModelAndView(template);
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        modelAndView.addObject("status", 404);
        modelAndView.addObject("message", null);
        return modelAndView;
    }

    @ExceptionHandler(NotFoundException.class)
    public Object handleNotFound(NotFoundException ex, HttpServletRequest request) {

        if (isApiRequest(request)) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    ex.getMessage(),
                    LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        String template = isAdminRequest(request) ? "admin/error/404" : "public/error/404";
        ModelAndView modelAndView = new ModelAndView(template);
        modelAndView.setStatus(HttpStatus.NOT_FOUND);
        modelAndView.addObject("status", 404);
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(BusinessException.class)
    public Object handleBusiness(BusinessException ex, HttpServletRequest request) {

        if (isApiRequest(request)) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    ex.getMessage(),
                    LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        String template = isAdminRequest(request) ? "admin/error/400" : "public/error/400";
        ModelAndView modelAndView = new ModelAndView(template);
        modelAndView.setStatus(HttpStatus.BAD_REQUEST);
        modelAndView.addObject("status", 400);
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(Exception.class)
    public Object handleGeneric(Exception ex, HttpServletRequest request) {

        if (isApiRequest(request)) {
            ErrorResponse error = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Ocorreu um erro inesperado: " + ex.getMessage(),
                    LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        String template = isAdminRequest(request) ? "admin/error/500" : "public/error/500";
        ModelAndView modelAndView = new ModelAndView(template);
        modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        modelAndView.addObject("status", 500);
        modelAndView.addObject("message", ex.getMessage());
        return modelAndView;

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Erro de validação");

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {

        String message = ex.getMessage();
        String friendlyMessage = "Dados da requisição inválidos";

        // Detecta se é erro de enum
        if (message.contains("Cannot deserialize value of type")) {

            // Tenta extrair o nome do campo
            if (message.contains("State")) {
                friendlyMessage = "Código de estado inválido. Use um dos valores: AC, AL, AP, AM, BA, CE, DF, ES, GO, MA, MT, MS, MG, PA, PB, PR, PE, PI, RJ, RN, RS, RO, RR, SC, SP, SE, TO";
            } else {
                friendlyMessage = "Valor inválido em um dos campos";
            }

        } else if (message.contains("JSON parse error")) {
            friendlyMessage = "Formato JSON inválido";
        }

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                friendlyMessage,
                LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");

        if (path != null && (path.startsWith("/api/") || path.startsWith("/admin/api/"))) {
            return true;
        }

        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            return true;
        }

        return false;
    }

    private boolean isAdminRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && path.startsWith("/admin");
    }
}

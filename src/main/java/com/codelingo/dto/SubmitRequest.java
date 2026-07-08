package com.codelingo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmitRequest {

    @NotBlank(message = "El código no puede estar vacío")
    @Size(max = 10000, message = "El código no puede superar los 10.000 caracteres")
    private String code;

    @NotBlank(message = "El lenguaje es requerido")
    private String language;
}
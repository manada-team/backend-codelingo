package com.codelingo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionRequest {

    @NotBlank(message = "El código no puede estar vacío")
    @Size(max = 10000, message = "El código no puede superar los 10.000 caracteres")
    private String code;

    @NotBlank(message = "El lenguaje es requerido")
    private String language;

    @Size(max = 4000, message = "La entrada no puede superar los 4.000 caracteres")
    private String stdin;
}


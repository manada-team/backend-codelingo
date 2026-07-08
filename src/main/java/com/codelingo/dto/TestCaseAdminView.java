package com.codelingo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// Representacion de un caso de prueba para el panel de Admin: incluye los
// ocultos, a diferencia de TestCaseView que se usa de cara al jugador.
@Data
@AllArgsConstructor
public class TestCaseAdminView {
    private String input;
    private String expectedOutput;
    private boolean hidden;
}
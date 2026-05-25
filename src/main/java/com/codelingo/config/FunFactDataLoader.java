package com.codelingo.config;

import com.codelingo.model.FunFact;
import com.codelingo.repository.FunFactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FunFactDataLoader implements CommandLineRunner {
    private final FunFactRepository funFactRepository;

    @Override
    public void run(String... args) {
        if (funFactRepository.count() > 0) return;
        funFactRepository.saveAll(List.of(
                FunFact.builder().content("El primer 'bug' fue una polilla real encontrada en una computadora Harvard Mark II en 1947.").build(),
                FunFact.builder().content("Python se llama así por el grupo de comedia Monty Python, no por la serpiente.").build(),
                FunFact.builder().content("JavaScript fue creado en solo 10 días por Brendan Eich en 1995.").build(),
                FunFact.builder().content("El término 'algoritmo' viene del matemático persa Al-Juarismi, del siglo IX.").build(),
                FunFact.builder().content("Ada Lovelace es considerada la primera programadora de la historia, en el siglo XIX.").build(),
                FunFact.builder().content("Git fue creado por Linus Torvalds en 2005 en solo unos días para gestionar el kernel de Linux.").build(),
                FunFact.builder().content("El lenguaje C tiene más de 50 años y sigue siendo fundamental en sistemas operativos y hardware.").build(),
                FunFact.builder().content("El 'Hello, World!' como primer programa viene de un libro de C de Brian Kernighan de 1978.").build(),
                FunFact.builder().content("Java fue diseñado originalmente para televisores interactivos, no para computadoras.").build(),
                FunFact.builder().content("El 95% de los sitios web usan JavaScript en el frontend.").build()
        ));
    }
}
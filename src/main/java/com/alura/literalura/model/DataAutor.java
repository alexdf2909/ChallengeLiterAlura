package com.alura.literalura.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public record DataAutor(
    @JsonAlias("name") String nombre,
    @JsonAlias("birth_year") Integer fechaNacimiento,
    @JsonAlias("death_year") Integer fechaFallecimiento){
}

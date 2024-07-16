package com.alura.literalura.principal;

import com.alura.literalura.model.*;
import com.alura.literalura.repository.AutorRepository;
import com.alura.literalura.repository.LibroRepository;
import com.alura.literalura.service.ConsumoAPI;
import com.alura.literalura.service.ConvierteDatos;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {

    private ConsumoAPI consumoApi = new ConsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/?search=";
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);
    private List<Autor> autores;
    private List<Libro> libros;
    private LibroRepository repositoryLibro;
    private AutorRepository repositoryAutor;


    public Principal(LibroRepository repositoryLibro, AutorRepository repositoryAutor) {
        this.repositoryLibro = repositoryLibro;
        this.repositoryAutor = repositoryAutor;
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            System.out.println("*********************************\n");
            System.out.println("1 - Buscar libros por título");
            System.out.println("2 - Mostrar libros registrados");
            System.out.println("3 - Mostrar autores registrados");
            System.out.println("4 - Autores vivos en determinado año");
            System.out.println("5 - Buscar libros por idioma");
            System.out.println("0 - Salir\n");

            while (!teclado.hasNextInt()) {
                System.out.println("Formato inválido, ingrese un número que este disponible en el menú!");
                teclado.nextLine();
            }
            opcion = teclado.nextInt();
            teclado.nextLine();
            switch (opcion) {
                case 1:
                    buscarLibro();
                    break;
                case 2:
                    mostrarLibros();
                    break;
                case 3:
                    mostrarAutores();
                    break;
                case 4:
                    autoresVivosPorAnio();
                    break;
                case 5:
                    buscarLibroPorIdioma();
                    break;
                case 0:
                    System.out.println("Cerrando  la aplicación");
                    break;
                default:
                    System.out.printf("Opción no válida\n");
                    break;
            }
        }
    }

    private DataBusqueda getBusqueda() {
        System.out.println("Escribe el nombre del libro: ");
        var nombreLibro = teclado.nextLine();
        var json = consumoApi.obtenerDatos(URL_BASE + nombreLibro.replace(" ", "%20"));
        DataBusqueda datos = conversor.obtenerDatos(json, DataBusqueda.class);
        return datos;

    }

    private void buscarLibro() {
        DataBusqueda datosBusqueda = getBusqueda();
        if (datosBusqueda != null && !datosBusqueda.resultado().isEmpty()) {
            DataLibro primerLibro = datosBusqueda.resultado().get(0);


            Libro libro = new Libro(primerLibro);
            System.out.println("***** Libro *****");
            System.out.println(libro);
            System.out.println("*****************");

            Optional<Libro> libroExiste = repositoryLibro.findByTitulo(libro.getTitulo());
            if (libroExiste.isPresent()){
                System.out.println("\nMo se puede registrar el mismo libro más de una vez\n");
            }else {

                if (!primerLibro.autor().isEmpty()) {
                    DataAutor autor = primerLibro.autor().get(0);
                    Autor autor1 = new Autor(autor);
                    Optional<Autor> autorOptional = repositoryAutor.findByNombre(autor1.getNombre());

                    if (autorOptional.isPresent()) {
                        Autor autorExiste = autorOptional.get();
                        libro.setAutor(autorExiste);
                        repositoryLibro.save(libro);
                    } else {
                        Autor autorNuevo = repositoryAutor.save(autor1);
                        libro.setAutor(autorNuevo);
                        repositoryLibro.save(libro);
                    }

                    Integer numeroDescargas = libro.getNumero_descargas() != null ? libro.getNumero_descargas() : 0;
                    System.out.println("********** Libro **********");
                    System.out.printf("Titulo: %s%nAutor: %s%nIdioma: %s%nNumero de Descargas: %s%n",
                            libro.getTitulo(), autor1.getNombre(), libro.getLenguaje(), libro.getNumero_descargas());
                    System.out.println("***************************\n");
                } else {
                    System.out.println("Sin autor");
                }
            }
        } else {
            System.out.println("Libro no encontrado");
        }
    }
    private void mostrarLibros() {
        libros = repositoryLibro.findAll();
        libros.stream()
                .forEach(System.out::println);
    }

    private void mostrarAutores() {
        autores = repositoryAutor.findAll();
        autores.stream()
                .forEach(System.out::println);
    }

    private void autoresVivosPorAnio() {
        System.out.println("Ingresa el año vivo de autor(es) que desea buscar: ");
        var anio = teclado.nextInt();
        autores = repositoryAutor.listaAutoresVivosPorAnio(anio);
        autores.stream()
                .forEach(System.out::println);
    }

    private List<Libro> datosBusquedaLenguaje(String idioma){
        var dato = Idioma.fromString(idioma);
        System.out.println("Lenguaje buscado: " + dato);

        List<Libro> libroPorIdioma = repositoryLibro.findByLenguaje(dato);
        return libroPorIdioma;
    }

    private void buscarLibroPorIdioma(){
        System.out.println("Ingrese el idioma para buscar los libros: ");

        var opcion = "";
        while (!opcion.equals(" ")) {
            System.out.println();
            System.out.println("es - español");
            System.out.println("en - ingles");
            System.out.println("fr - francés");
            System.out.println("pt - portugués");

            opcion = teclado.nextLine();

            switch (opcion) {
                case "en":
                    List<Libro> librosEnIngles = datosBusquedaLenguaje("[en]");
                    librosEnIngles.forEach(System.out::println);
                    break;
                case "es":
                    List<Libro> librosEnEspanol = datosBusquedaLenguaje("[es]");
                    librosEnEspanol.forEach(System.out::println);
                    break;
                case "fr":
                    List<Libro> librosEnFrances = datosBusquedaLenguaje("[fr]");
                    librosEnFrances.forEach(System.out::println);
                    break;
                case "pt":
                    List<Libro> librosEnPortugues = datosBusquedaLenguaje("[pt]");
                    librosEnPortugues.forEach(System.out::println);
                    break;
                default:
                    System.out.println("Ningún idioma seleccionado");
                    break;
            }
            opcion = " ";
        }
    }

}

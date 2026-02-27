package com.svalero.fancollector.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
public class ImagenUtil {

    private static final List<String> EXTENSIONES_PERMITIDAS = List.of("jpg", "jpeg", "png", "gif");
    private static final long TAMANIO_MAXIMO = 15_000_000L;

    @Value("C:\\Users\\Nerea\\Desktop\\Proyectos\\FanCollector-Proyecto\\Uploads\\")
    private String rutaUploads;

    public boolean validarImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) return false;
        if (archivo.getSize() > TAMANIO_MAXIMO) return false;

        String nombre = archivo.getOriginalFilename();
        if (nombre == null || !nombre.contains(".")) return false;

        String extension = nombre.substring(nombre.lastIndexOf(".") + 1).toLowerCase();
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) return false;

        try {
            return ImageIO.read(archivo.getInputStream()) != null;
        } catch (IOException e) {
            return false;
        }
    }

    public String procesarImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("La imagen es obligatoria");
        }
        if (!validarImagen(archivo)) {
            throw new IllegalArgumentException("Imagen inválida. Usar jpg, png, gif. Máx 15MB");
        }
        try {
            String nombreUnico = generarNombreUnico(archivo.getOriginalFilename());
            Files.write(Paths.get(rutaUploads + nombreUnico), archivo.getBytes());
            return "/imagenes/" + nombreUnico;
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar la imagen: " + e.getMessage());
        }
    }

    public void eliminarImagen(String urlRelativa) {
        if (urlRelativa == null || urlRelativa.isBlank()) return;
        try {
            String nombreArchivo = urlRelativa.replace("/imagenes/", "");
            Files.deleteIfExists(Paths.get(rutaUploads + nombreArchivo));
        } catch (Exception ignored) {}
    }

    private String generarNombreUnico(String nombreOriginal) {
        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        return System.currentTimeMillis() + extension;
    }
}
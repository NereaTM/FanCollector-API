package com.svalero.fancollector.security.auth;

import com.svalero.fancollector.domain.Coleccion;
import com.svalero.fancollector.domain.Usuario;
import com.svalero.fancollector.domain.UsuarioColeccion;
import com.svalero.fancollector.domain.UsuarioItem;
import com.svalero.fancollector.exception.security.AccesoDenegadoException;

public final class Permisos {

    private Permisos() {}

    public static boolean esElMismoUsuario(Usuario objetivo, String emailUsuario) {
        return objetivo != null
                && objetivo.getEmail() != null
                && emailUsuario != null
                && objetivo.getEmail().equalsIgnoreCase(emailUsuario);
    }

    public static boolean esCreador(Coleccion coleccion, Usuario usuario) {
        if (coleccion == null || usuario == null || coleccion.getCreador() == null) return false;
        if (coleccion.getCreador().getId() != null && usuario.getId() != null) {
            return coleccion.getCreador().getId().equals(usuario.getId());
        }
        return coleccion.getCreador().getEmail() != null
                && usuario.getEmail() != null
                && coleccion.getCreador().getEmail().equalsIgnoreCase(usuario.getEmail());
    }

    public static boolean esDuenoRelacion(UsuarioColeccion uc, Usuario actual) {
        if (uc == null || actual == null || uc.getUsuario() == null) return false;
        if (uc.getUsuario().getId() != null && actual.getId() != null) {
            return uc.getUsuario().getId().equals(actual.getId());
        }
        return uc.getUsuario().getEmail() != null
                && actual.getEmail() != null
                && uc.getUsuario().getEmail().equalsIgnoreCase(actual.getEmail());
    }

    public static boolean esDuenoItem(UsuarioItem ui, Usuario actual) {
        if (ui == null || actual == null || ui.getUsuario() == null) return false;
        if (ui.getUsuario().getId() != null && actual.getId() != null) {
            return ui.getUsuario().getId().equals(actual.getId());
        }
        return ui.getUsuario().getEmail() != null
                && actual.getEmail() != null
                && ui.getUsuario().getEmail().equalsIgnoreCase(actual.getEmail());
    }

    public static boolean puedeVerColeccion(Coleccion coleccion, Usuario usuario, boolean esAdmin) {
        if (coleccion == null) return false;
        if (esAdmin) return true;
        if (esCreador(coleccion, usuario)) return true;
        return coleccion.isEsPublica();
    }

    public static boolean puedeVerUsuarioColeccion(UsuarioColeccion uc, Usuario actual, boolean esAdmin, boolean esMods) {
        if (uc == null) return false;
        if (esAdmin || esMods) return true;
        if (esDuenoRelacion(uc, actual)) return true;
        boolean esVisible = uc.isEsVisible();
        boolean coleccionPublica = uc.getColeccion() != null && uc.getColeccion().isEsPublica();
        return esVisible && coleccionPublica;
    }

    public static boolean puedeVerUsuarioItem(UsuarioItem ui, Usuario actual, boolean esAdmin, boolean esMods) {
        if (ui == null) return false;
        if (esAdmin || esMods) return true;
        if (esDuenoItem(ui, actual)) return true;
        boolean esPublico = ui.isEsVisible();
        boolean coleccionPublica = ui.getColeccion() != null && ui.getColeccion().isEsPublica();
        return esPublico && coleccionPublica;
    }

    public static void checkPuedeVerColeccion(Coleccion coleccion, Usuario usuario, boolean esAdmin) {
        if (coleccion == null) {throw new AccesoDenegadoException();}
        if (esAdmin) return;
        if (esCreador(coleccion, usuario)) return;
        if (coleccion.isEsPublica()) return;
        throw new AccesoDenegadoException();
    }

    public static void checkPuedeVerUsuarioColeccion(UsuarioColeccion uc, Usuario actual, boolean esAdmin, boolean esMods) {
        if (!puedeVerUsuarioColeccion(uc, actual, esAdmin, esMods)) {
            throw new AccesoDenegadoException();
        }
    }

    public static void checkPuedeVerUsuarioItem(UsuarioItem ui, Usuario actual, boolean esAdmin, boolean esMods) {
        if (!puedeVerUsuarioItem(ui, actual, esAdmin, esMods)) {
            throw new AccesoDenegadoException();
        }
    }

    public static void checkPuedeEditarUsuario(Usuario objetivo, String emailUsuario, boolean esAdmin, boolean esMods) {
        if (esAdmin) return;
        if (esMods) return;
        if (esElMismoUsuario(objetivo, emailUsuario)) return;
        throw new AccesoDenegadoException();
    }

    public static void checkPuedeEditarOBorrarColeccion(Coleccion coleccion, Usuario usuario, boolean esAdmin, boolean esMods) {
        if (esAdmin) return;
        if (esCreador(coleccion, usuario)) return;
        if (esMods && coleccion.isEsPublica()) return;
        throw new AccesoDenegadoException();
    }

    public static void checkPuedeEditarOBorrarUsuarioColeccion(UsuarioColeccion uc, Usuario actual, boolean esAdmin, boolean esMods) {
        if (uc == null) {throw new AccesoDenegadoException();}
        if (esAdmin) return;
        if (esMods) return;
        if (esDuenoRelacion(uc, actual)) return;
        throw new AccesoDenegadoException();
    }

    public static void checkPuedeEditarOBorrarUsuarioItem(UsuarioItem ui, Usuario actual, boolean esAdmin, boolean esMods) {
        if (ui == null) {throw new AccesoDenegadoException();}
        if (esAdmin) return;
        if (esMods) return;
        if (esDuenoItem(ui, actual)) return;
        throw new AccesoDenegadoException();
    }
}

package com.serviciotecnico.usuarios.controller;

import com.serviciotecnico.usuarios.dto.UsuarioRequest;
import com.serviciotecnico.usuarios.dto.UsuarioCompletoResponse;
import com.serviciotecnico.usuarios.model.Usuario;
import com.serviciotecnico.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioCompletoResponse> listar() {
        return usuarioService.obtenerTodosCompleto();
    }

    @GetMapping("/{rut}")
    public UsuarioCompletoResponse obtener(@PathVariable String rut) {
        return usuarioService.obtenerUsuarioCompleto(rut.toUpperCase());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crear(@Valid @RequestBody UsuarioRequest request) {
        return usuarioService.crearUsuario(request);
    }

    @PutMapping("/{rut}")
    public Usuario actualizar(@PathVariable String rut, @Valid @RequestBody UsuarioRequest request) {
        return usuarioService.actualizarUsuario(rut.toUpperCase(), request);
    }

    @DeleteMapping("/{rut}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String rut) {
        usuarioService.eliminarUsuario(rut.toUpperCase());
    }
}

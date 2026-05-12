package com.serviciotecnico.usuarios.controller;

import com.serviciotecnico.usuarios.dto.UsuarioRequest;
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
    public List<Usuario> listar() {
        return usuarioService.obtenerTodos();
    }

    @GetMapping("/{rut}")
    public Usuario obtener(@PathVariable String rut) {
        return usuarioService.obtenerPorRut(rut.toUpperCase());
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
}

package com.serviciotecnico.usuarios.service;

import com.serviciotecnico.usuarios.dto.UsuarioRequest;
import com.serviciotecnico.usuarios.dto.external.ComputadorDto;
import com.serviciotecnico.usuarios.dto.external.MantencionDto;
import com.serviciotecnico.usuarios.model.Usuario;
import com.serviciotecnico.usuarios.repository.UsuarioRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RestTemplate restTemplate;

    public UsuarioService(UsuarioRepository usuarioRepository, RestTemplate restTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.restTemplate = restTemplate;
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorRut(String rut) {
        return usuarioRepository.findById(rut)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public Usuario crearUsuario(UsuarioRequest request) {
        String rut = request.getRut().toUpperCase();
        if (!esRutValido(rut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUT inválido");
        }
        if (usuarioRepository.existsById(rut)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya existe");
        }
        Usuario usuario = Usuario.builder()
                .rut(rut)
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .gmail(request.getGmail())
                .telefono(request.getTelefono())
                .build();
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizarUsuario(String rut, UsuarioRequest request) {
        Usuario usuarioExistente = obtenerPorRut(rut);
        if (!request.getRut().equalsIgnoreCase(rut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El RUT de la solicitud no coincide con el RUT de la ruta");
        }
        if (!esRutValido(rut)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUT inválido");
        }
        usuarioExistente.setNombre(request.getNombre());
        usuarioExistente.setApellido(request.getApellido());
        usuarioExistente.setGmail(request.getGmail());
        usuarioExistente.setTelefono(request.getTelefono());
        return usuarioRepository.save(usuarioExistente);
    }

    private boolean esRutValido(String rut) {
        if (rut == null || !rut.matches("^\\d{7,8}-[0-9Kk]$")) {
            return false;
        }
        String[] partes = rut.split("-");
        String cuerpo = partes[0];
        char dv = Character.toUpperCase(partes[1].charAt(0));
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        char dvCalculado = resto == 11 ? '0' : resto == 10 ? 'K' : Character.forDigit(resto, 10);
        return dv == dvCalculado;
    }

    public List<ComputadorDto> obtenerComputadoresPorUsuario(String rut) {
        // Verificar que el usuario existe
        obtenerPorRut(rut);
        String url = "http://localhost:8081/api/computadores/cliente/" + rut;
        ResponseEntity<List<ComputadorDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ComputadorDto>>() {
                });
        return response.getBody();
    }

    public List<MantencionDto> obtenerServiciosPorUsuario(String rut) {
        // Obtener computadores del usuario
        List<ComputadorDto> computadores = obtenerComputadoresPorUsuario(rut);
        List<MantencionDto> servicios = new ArrayList<>();
        for (ComputadorDto comp : computadores) {
            String url = "http://localhost:8082/api/mantenciones/computador/" + comp.getIdPc();
            ResponseEntity<List<MantencionDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<MantencionDto>>() {
                    });
            servicios.addAll(response.getBody());
        }
        return servicios;
    }
}

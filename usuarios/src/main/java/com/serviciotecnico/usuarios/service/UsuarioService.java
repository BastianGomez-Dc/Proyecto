package com.serviciotecnico.usuarios.service;

import com.serviciotecnico.usuarios.dto.UsuarioRequest;
import com.serviciotecnico.usuarios.dto.UsuarioCompletoResponse;
import com.serviciotecnico.usuarios.dto.ComputadorDTO;
import com.serviciotecnico.usuarios.dto.MantencionDTO;
import com.serviciotecnico.usuarios.dto.TipoServicioDTO;
import com.serviciotecnico.usuarios.model.Usuario;
import com.serviciotecnico.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${computador.service.url:http://localhost:8080}")
    private String computadorUrl;

    @Value("${tiposervicio.service.url:http://localhost:8082}")
    private String tiposervicioUrl;

    @Value("${mantencion.service.url:http://localhost:8084}")
    private String mantencionUrl;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioCompletoResponse obtenerUsuarioCompleto(String rut) {
        Usuario usuario = obtenerPorRut(rut);
        List<TipoServicioDTO> tiposServicio = fetchTiposServicio();
        List<ComputadorDTO> computadores = obtenerComputadoresConMantenciones(rut, tiposServicio);
        return toResponse(usuario, computadores);
    }

    public List<UsuarioCompletoResponse> obtenerTodosCompleto() {
        List<TipoServicioDTO> tiposServicio = fetchTiposServicio();
        List<UsuarioCompletoResponse> responses = new ArrayList<>();
        for (Usuario u : usuarioRepository.findAll()) {
            List<ComputadorDTO> computadores = obtenerComputadoresConMantenciones(u.getRut(), tiposServicio);
            responses.add(toResponse(u, computadores));
        }
        return responses;
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
        usuarioRepository.save(usuarioExistente);
        return usuarioExistente;
    }

    public void eliminarUsuario(String rut) {
        obtenerPorRut(rut);
        usuarioRepository.deleteById(rut);
    }

    private List<TipoServicioDTO> fetchTiposServicio() {
        try {
            TipoServicioDTO[] tipos = restTemplate.getForObject(
                    tiposervicioUrl + "/api/tiposervicios", TipoServicioDTO[].class);
            if (tipos != null) return Arrays.asList(tipos);
        } catch (Exception e) {
            logger.error("Error al obtener tipos de servicio: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    private List<ComputadorDTO> obtenerComputadoresConMantenciones(String rut, List<TipoServicioDTO> tiposServicio) {
        List<ComputadorDTO> result = new ArrayList<>();
        try {
            ComputadorDTO[] computadores = restTemplate.getForObject(
                    computadorUrl + "/api/computadores/cliente/" + rut, ComputadorDTO[].class);
            if (computadores == null) return result;
            for (ComputadorDTO pc : computadores) {
                pc.setMantenciones(fetchMantenciones(pc, tiposServicio));
                result.add(pc);
            }
        } catch (Exception e) {
            logger.error("Error al obtener computadores para RUT {}: {}", rut, e.getMessage());
        }
        return result;
    }

    private List<MantencionDTO> fetchMantenciones(ComputadorDTO pc, List<TipoServicioDTO> tiposServicio) {
        List<MantencionDTO> mantenciones = new ArrayList<>();
        try {
            MantencionDTO[] tickets = restTemplate.getForObject(
                    mantencionUrl + "/api/mantenciones/pc/" + pc.getIdPc(), MantencionDTO[].class);
            if (tickets != null) {
                for (MantencionDTO ticket : tickets) {
                    ticket.setTipoServicioDetalle(encontrarDetalleTipoServicio(ticket.getTipoServicio(), tiposServicio));
                    mantenciones.add(ticket);
                }
            }
        } catch (Exception e) {
            logger.error("Error al obtener mantenciones para PC {}: {}", pc.getIdPc(), e.getMessage());
        }
        return mantenciones;
    }

    private UsuarioCompletoResponse toResponse(Usuario u, List<ComputadorDTO> computadores) {
        return UsuarioCompletoResponse.builder()
                .rut(u.getRut())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .gmail(u.getGmail())
                .telefono(u.getTelefono())
                .computadores(computadores)
                .build();
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

    private TipoServicioDTO encontrarDetalleTipoServicio(String enumValue, List<TipoServicioDTO> tiposServicio) {
        if (enumValue == null) return null;
        String mappedDbName = switch (enumValue) {
            case "LIMPIEZA_SUPERFICIAL" -> "Limpieza Superficial";
            case "LIMPIEZA_PROFUNDA" -> "Limpieza Profunda";
            case "REPARACION" -> "Reparación";
            case "MEJORA" -> "Mejora (Upgrade)";
            case "OPTIMIZACION" -> "Optimización";
            default -> enumValue;
        };
        return tiposServicio.stream()
                .filter(t -> t.getNombre() != null && t.getNombre().equalsIgnoreCase(mappedDbName))
                .findFirst()
                .orElse(null);
    }
}

package com.agg.dumbellcheck.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 72)
    private String password;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido1;

    @Column
    private String apellido2;

    @Column(name = "foto_perfil_url")
    private String fotoPerfilUrl;

    @Column
    private String biografia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol;

    @Column(name = "contador_seguidores", nullable = false)
    private Integer contadorSeguidores;

    @Column(name = "contador_seguidos", nullable = false)
    private Integer contadorSeguidos;

    @Column(name = "esta_activo", nullable = false)
    private boolean estaActivo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "ultima_conexion")
    private Instant ultimaConexion;

    @OneToMany(mappedBy = "usuario")
    private List<BaneoEntity> baneos = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<RutinaEntity> rutinas = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<PublicacionEntity> publicaciones = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<ComentarioEntity> comentarios = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<LikeEntity> likes = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<IncidenciaEntity> incidencias = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<NotificacionEntity> notificaciones = new ArrayList<>();

    @OneToMany(mappedBy = "creador")
    private List<ChatEntity> chatsCreados = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<MensajeChatEntity> mensajesChat = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<UsuarioChatEntity> chatsUsuario = new ArrayList<>();

    @OneToMany(mappedBy = "usuario")
    private List<SeguidorEntity> siguiendo = new ArrayList<>();

    @OneToMany(mappedBy = "seguido")
    private List<SeguidorEntity> seguidores = new ArrayList<>();
}

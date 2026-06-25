package ni.edu.uam.bfavocabulario.model;


import lombok.Getter;
import lombok.Setter;
import ni.edu.uam.bfavocabulario.enums.EstadoSesion;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.Required;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class SesionEvaluacion {

    @Id
    @GeneratedValue
    @Hidden
    private Long id;

    @Required
    private String nombreEvaluado;

    @Required
    @Column(length = 100)
    String nombreCompleto;

    @Required
    Integer edad;

    @Required
    @Column(length = 25)
    String cedula;

    @Required
    @Column(length = 25)
    String telefono;

    @Required
    @Column(length = 20)
    String sexo;

    @Required
    @Column(length = 100)
    String colegioProcedencia;

    @Required
    @Column(length = 100)
    String procedencia;

    private LocalDateTime fechaInicio = LocalDateTime.now();

    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    private EstadoSesion estado = EstadoSesion.INICIADA;

    @ManyToMany
    private List<Opcion> opcionesSeleccionadas = new ArrayList<>();

    private Integer puntajeDirecto = 0;

    private Integer percentil = 0;

    public void finalizarSesion() {
        this.fechaFin = LocalDateTime.now();
        this.estado = EstadoSesion.FINALIZADA;
    }

    public boolean estaFinalizada() {
        return EstadoSesion.FINALIZADA.equals(this.estado);
    }
}

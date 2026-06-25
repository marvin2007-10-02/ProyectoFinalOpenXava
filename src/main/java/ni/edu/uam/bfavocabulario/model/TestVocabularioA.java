package ni.edu.uam.bfavocabulario.model;

import lombok.Getter;
import lombok.Setter;
import org.openxava.model.Identifiable;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "TestVocabularioA")
@Getter
@Setter
public class TestVocabularioA extends Identifiable {

    @Column(length = 50, nullable = false)
    private String area = "Vocabulario";

    @Column(length = 10, nullable = false, unique = true)
    private String codigo = "VOC1";

    @Column(length = 100, nullable = false)
    private String nombre = "Test de Vocabulario Forma A";

    @Column(nullable = false)
    private Integer tiempoLimite = 5;

    @Column(length = 50, nullable = false)
    private String tipoTest = "Selección múltiple";

    @Column(nullable = false)
    private Integer totalPreguntas = 37;

    @OneToMany(
            mappedBy = "testVocabularioA",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Pregunta> preguntas;

    public List<Pregunta> obtenerPreguntas() {
        return preguntas;
    }

    public boolean validarCantidadPreguntas() {
        return preguntas != null &&
                preguntas.size() == totalPreguntas;
    }
}

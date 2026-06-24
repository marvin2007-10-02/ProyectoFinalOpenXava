package ni.edu.uam.bfavocabulario.model;

import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.Required;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "TestVocabularioA")
@Getter
@Setter
public class TestVocabularioA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ReadOnly
    @Required
    @Column(nullable = false, length = 50)
    private String area = "Vocabulario";

    @ReadOnly
    @Required
    @Column(nullable = false, unique = true, length = 10)
    private String codigo = "VOC1";

    @Required
    @Column(nullable = false, length = 100)
    private String nombre = "Test de Vocabulario Forma A";

    @Required
    @Column(nullable = false)
    private Integer tiempoLimite = 5;

    @ReadOnly
    @Required
    @Column(nullable = false, length = 50)
    private String tipoTest = "Selección múltiple";

    @ReadOnly
    @Required
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

package ni.edu.uam.bfavocabulario.model;

import lombok.Getter;
import lombok.Setter;
import ni.edu.uam.bfavocabulario.enums.letraOpcion;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.Required;

import javax.persistence.*;

@Entity
@Table(name="opcion")
@Getter
@Setter
public class Opcion {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Hidden
    private Long id;

    @Required
    @Enumerated(EnumType.STRING)
    @Column(nullable= false, length = 1)
    private letraOpcion letra;

    @Required
    @Column(nullable=false, length=100)
    private String texto;

    @Column(name= "es_correcta", nullable=false)
    private boolean esCorrecta;

    @ManyToOne( fetch =FetchType.LAZY)
    @JoinColumn(name = "pregunta_id", nullable = false)
    @DescriptionsList(descriptionProperties = "numero, textoPrincipal")
    private Pregunta pregunta;

    public boolean verificarCorrecta(){
        return esCorrecta;
    }

}

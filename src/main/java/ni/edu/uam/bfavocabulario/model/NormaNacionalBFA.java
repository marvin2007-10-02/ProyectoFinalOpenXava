package ni.edu.uam.bfavocabulario.model;


import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.Required;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class NormaNacionalBFA {

    @Id
    @GeneratedValue
    @Hidden
    private Long id;

    @Required
    @Column(length = 100)
    private String subtest;

    @Required
    private Integer puntajeMinimo;

    @Required
    private Integer puntajeMaximo;

    @Required
    private Integer percentil;


}

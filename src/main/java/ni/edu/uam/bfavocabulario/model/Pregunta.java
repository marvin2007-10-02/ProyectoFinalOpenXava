package ni.edu.uam.bfavocabulario.model;

import lombok.Getter;
import lombok.Setter;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.Required;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="pregunta")
@Getter
@Setter
public class Pregunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Hidden
    private Long id;

    @Required
    @Column(nullable=false, unique=true)
    private Integer numero;

    @Required
    @Column(name = "texto_principal", nullable= false, length=100)
    private String textoPrincipal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_vocabulario_a_id")
    @DescriptionsList(descriptionProperties = "codigo, nombre")
    TestVocabularioA testVocabularioA;

    @OneToMany(mappedBy= "pregunta", cascade= CascadeType.ALL, orphanRemoval = true)
    @ListProperties("letra, texto, esCorrecta")
    private List<Opcion> opciones = new ArrayList<>();

    public Opcion obtenerOpcionCorrecta(){
        if (opciones == null){
            return null;
        }
        for (Opcion opcion : opciones){
            if (opcion.isEsCorrecta()){
                return  opcion;
            }
        }
        return null;
    }

    public boolean tieneCincoOpciones(){
        return opciones != null && opciones.size()==5;
    }
    public boolean tieneUnaOpcionCorrecta(){
        if(opciones == null){
            return false;
        }
        int cantidadCorrectas = 0;
        for (Opcion opcion : opciones) {
            if (opcion.isEsCorrecta()) {
                cantidadCorrectas++;
            }
        }
        return cantidadCorrectas ==1;
    }

}

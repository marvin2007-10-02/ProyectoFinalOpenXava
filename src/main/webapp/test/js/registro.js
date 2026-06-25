(function () {
    const form = document.getElementById("registroForm");
    const mensaje = document.getElementById("mensaje");

    function valor(id) {
        return document.getElementById(id).value.trim();
    }

    form.addEventListener("submit", function (event) {
        event.preventDefault();

        const datosEvaluado = {
            nombreCompleto: valor("nombreCompleto"),
            edad: Number(valor("edad")),
            cedula: valor("cedula"),
            telefono: valor("telefono"),
            sexo: valor("sexo"),
            colegioProcedencia: valor("colegioProcedencia"),
            procedencia: valor("procedencia")
        };

        if (!datosEvaluado.nombreCompleto) {
            mensaje.textContent = "Ingrese el nombre completo.";
            return;
        }

        if (!Number.isInteger(datosEvaluado.edad) || datosEvaluado.edad <= 0) {
            mensaje.textContent = "Ingrese una edad valida.";
            return;
        }

        if (!datosEvaluado.cedula) {
            mensaje.textContent = "Ingrese la cedula.";
            return;
        }

        if (!datosEvaluado.telefono) {
            mensaje.textContent = "Ingrese el telefono.";
            return;
        }

        if (!datosEvaluado.sexo) {
            mensaje.textContent = "Seleccione el sexo.";
            return;
        }

        if (!datosEvaluado.colegioProcedencia) {
            mensaje.textContent = "Ingrese el colegio de procedencia.";
            return;
        }

        if (!datosEvaluado.procedencia) {
            mensaje.textContent = "Ingrese la procedencia.";
            return;
        }

        localStorage.removeItem("bfaSesion");
        localStorage.removeItem("bfaResultado");
        sessionStorage.setItem("bfaDatosEvaluado", JSON.stringify(datosEvaluado));

        window.location.href = "instruccionesA.html";
    });
})();

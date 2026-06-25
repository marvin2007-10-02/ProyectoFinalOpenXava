(function () {
    const nombre = document.getElementById("resultadoNombre");
    const puntajeDirecto = document.getElementById("puntajeDirecto");
    const percentil = document.getElementById("percentil");
    const estado = document.getElementById("estado");
    const mensaje = document.getElementById("mensaje");

    let resultado = null;

    try {
        resultado = JSON.parse(localStorage.getItem("bfaResultado"));
    }
    catch (error) {
        resultado = null;
    }

    if (!resultado) {
        mensaje.textContent = "No hay un resultado disponible.";
        return;
    }

    nombre.textContent = resultado.nombreEvaluado || "Vocabulario Forma A";
    puntajeDirecto.textContent = resultado.puntajeDirecto == null ? "0" : resultado.puntajeDirecto;
    percentil.textContent = resultado.percentil == null ? "0" : resultado.percentil;
    estado.textContent = resultado.estado || "FINALIZADA";
})();

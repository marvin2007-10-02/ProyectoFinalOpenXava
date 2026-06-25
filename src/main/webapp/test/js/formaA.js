(function () {
    const nombreHeading = document.getElementById("nombreEvaluado");
    const timer = document.getElementById("timer");
    const progress = document.getElementById("progress");
    const questionsContainer = document.getElementById("questions");
    const messageBox = document.getElementById("messageBox");
    const finishBtn = document.getElementById("finishBtn");

    const sesion = leerSesion();
    const respuestas = new Map();
    const respuestasPendientes = new Set();
    const respuestasFallidas = new Set();
    let preguntas = [];
    let finalizando = false;
    let timerId = null;

    if (!sesion) {
        window.location.href = "registro.html";
        return;
    }

    nombreHeading.textContent = sesion.nombreEvaluado || "Evaluado";

    function contextPath() {
        const parts = window.location.pathname.split("/");
        return parts.length > 1 && parts[1] ? "/" + parts[1] : "";
    }

    function apiBase() {
        return window.location.origin + contextPath() + "/api";
    }

    function leerSesion() {
        try {
            return JSON.parse(localStorage.getItem("bfaSesion"));
        }
        catch (error) {
            return null;
        }
    }

    function respuestasKey() {
        return "bfaRespuestas_" + sesion.sesionId;
    }

    function cargarRespuestasLocales() {
        try {
            const guardadas = JSON.parse(localStorage.getItem(respuestasKey())) || {};
            Object.keys(guardadas).forEach(function (preguntaId) {
                respuestas.set(Number(preguntaId), Number(guardadas[preguntaId]));
            });
        }
        catch (error) {
            respuestas.clear();
        }
    }

    function guardarRespuestasLocales() {
        const plano = {};
        respuestas.forEach(function (opcionId, preguntaId) {
            plano[preguntaId] = opcionId;
        });
        localStorage.setItem(respuestasKey(), JSON.stringify(plano));
    }

    async function leerRespuesta(response) {
        const texto = await response.text();
        return texto ? JSON.parse(texto) : {};
    }

    function mostrarMensaje(texto, tipo) {
        messageBox.textContent = texto;
        messageBox.classList.remove("hidden", "info");
        if (tipo === "info") {
            messageBox.classList.add("info");
        }
    }

    function ocultarMensaje() {
        messageBox.textContent = "";
        messageBox.classList.add("hidden");
        messageBox.classList.remove("info");
    }

    function actualizarProgreso() {
        progress.textContent = respuestas.size + "/" + preguntas.length;
    }

    function formatearTiempo(totalSegundos) {
        const minutos = Math.floor(totalSegundos / 60).toString().padStart(2, "0");
        const segundos = (totalSegundos % 60).toString().padStart(2, "0");
        return minutos + ":" + segundos;
    }

    function iniciarTimer() {
        if (!sesion.inicioMs) {
            sesion.inicioMs = Date.now();
            localStorage.setItem("bfaSesion", JSON.stringify(sesion));
        }

        const limite = sesion.tiempoLimiteSegundos || 300;

        timerId = window.setInterval(function () {
            const transcurridos = Math.floor((Date.now() - sesion.inicioMs) / 1000);
            const restantes = Math.max(0, limite - transcurridos);
            timer.textContent = formatearTiempo(restantes);

            if (restantes === 0) {
                finalizarTest();
            }
        }, 250);
    }

    async function cargarPreguntas() {
        ocultarMensaje();
        mostrarMensaje("Cargando preguntas...", "info");

        try {
            const response = await fetch(apiBase() + "/test/vocabulario-a/preguntas");
            const data = await leerRespuesta(response);

            if (!response.ok) {
                throw new Error(data.mensaje || "No fue posible cargar las preguntas.");
            }

            preguntas = data.preguntas || [];
            cargarRespuestasLocales();
            renderPreguntas();
            actualizarProgreso();

            if (preguntas.length === 0) {
                finishBtn.disabled = true;
                mostrarMensaje("No hay preguntas cargadas. Un administrador debe registrarlas en OpenXava.", "info");
                return;
            }

            ocultarMensaje();
            finishBtn.disabled = false;
            iniciarTimer();
        }
        catch (error) {
            finishBtn.disabled = true;
            mostrarMensaje(error.message);
        }
    }

    function renderPreguntas() {
        questionsContainer.innerHTML = "";

        preguntas.forEach(function (pregunta) {
            const article = document.createElement("article");
            article.className = "question";

            const title = document.createElement("h2");
            title.className = "question-title";
            title.textContent = pregunta.numero + ". " + pregunta.texto;
            article.appendChild(title);

            const fieldset = document.createElement("fieldset");
            fieldset.className = "options";

            (pregunta.opciones || []).forEach(function (opcion) {
                const label = document.createElement("label");
                label.className = "option";

                const input = document.createElement("input");
                input.type = "radio";
                input.name = "pregunta_" + pregunta.id;
                input.value = opcion.id;
                input.dataset.preguntaId = pregunta.id;

                if (respuestas.get(Number(pregunta.id)) === Number(opcion.id)) {
                    input.checked = true;
                }

                const letra = document.createElement("span");
                letra.className = "option-letter";
                letra.textContent = opcion.letra;

                const texto = document.createElement("span");
                texto.textContent = opcion.texto;

                label.appendChild(input);
                label.appendChild(letra);
                label.appendChild(texto);
                fieldset.appendChild(label);
            });

            article.appendChild(fieldset);
            questionsContainer.appendChild(article);
        });
    }

    questionsContainer.addEventListener("change", async function (event) {
        const input = event.target;
        if (!input.matches("input[type='radio']")) {
            return;
        }

        const preguntaId = Number(input.dataset.preguntaId);
        const opcionId = Number(input.value);
        respuestas.set(preguntaId, opcionId);
        respuestasFallidas.delete(preguntaId);
        guardarRespuestasLocales();
        actualizarProgreso();
        ocultarMensaje();

        const envio = registrarRespuesta(opcionId)
                .catch(function (error) {
                    if (!(error.status === 409 && error.message.toLowerCase().includes("tiempo"))) {
                        respuestasFallidas.add(preguntaId);
                    }
                    mostrarMensaje(error.message);
                })
                .finally(function () {
                    respuestasPendientes.delete(envio);
                });

        respuestasPendientes.add(envio);
    });

    finishBtn.addEventListener("click", finalizarTest);

    async function registrarRespuesta(opcionId) {
        const response = await fetch(apiBase() + "/sesion/responder", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                sesionId: sesion.sesionId,
                opcionId: opcionId
            })
        });

        const data = await leerRespuesta(response);
        if (!response.ok) {
            const error = new Error(data.mensaje || "No fue posible registrar la respuesta.");
            error.status = response.status;
            throw error;
        }
    }

    async function finalizarTest() {
        if (finalizando) {
            return;
        }

        finalizando = true;
        finishBtn.disabled = true;
        if (timerId) {
            window.clearInterval(timerId);
        }

        try {
            if (respuestasPendientes.size > 0) {
                mostrarMensaje("Guardando respuestas...", "info");
                await Promise.all(Array.from(respuestasPendientes));
            }

            if (respuestasFallidas.size > 0) {
                throw new Error("Hay respuestas que no se pudieron registrar. Intente finalizar nuevamente.");
            }

            const response = await fetch(apiBase() + "/sesion/finalizar", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ sesionId: sesion.sesionId })
            });

            const data = await leerRespuesta(response);
            if (!response.ok) {
                throw new Error(data.mensaje || "No fue posible finalizar el test.");
            }

            data.nombreEvaluado = sesion.nombreEvaluado;
            data.sesionId = sesion.sesionId;
            localStorage.setItem("bfaResultado", JSON.stringify(data));
            window.location.href = "resultado.html";
        }
        catch (error) {
            finalizando = false;
            finishBtn.disabled = false;
            iniciarTimer();
            mostrarMensaje(error.message);
        }
    }

    cargarPreguntas();
})();

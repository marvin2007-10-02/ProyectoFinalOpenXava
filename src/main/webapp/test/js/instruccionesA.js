(function () {
    const comenzarBtn = document.getElementById("comenzarBtn");
    const mensaje = document.getElementById("mensaje");

    function contextPath() {
        const parts = window.location.pathname.split("/");
        return parts.length > 1 && parts[1] ? "/" + parts[1] : "";
    }

    function apiBase() {
        return window.location.origin + contextPath() + "/api";
    }

    async function leerRespuesta(response) {
        const texto = await response.text();
        return texto ? JSON.parse(texto) : {};
    }

    function leerDatosEvaluado() {
        try {
            return JSON.parse(sessionStorage.getItem("bfaDatosEvaluado"));
        }
        catch (error) {
            return null;
        }
    }

    comenzarBtn.addEventListener("click", async function () {
        const datosEvaluado = leerDatosEvaluado();
        if (!datosEvaluado) {
            window.location.href = "registro.html";
            return;
        }

        comenzarBtn.disabled = true;
        mensaje.textContent = "";

        try {
            const response = await fetch(apiBase() + "/sesion/iniciar", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(datosEvaluado)
            });

            const data = await leerRespuesta(response);
            if (!response.ok) {
                throw new Error(data.mensaje || "No fue posible iniciar la sesion.");
            }

            data.inicioMs = Date.now();
            localStorage.setItem("bfaSesion", JSON.stringify(data));
            localStorage.removeItem("bfaRespuestas_" + data.sesionId);
            window.location.href = "formaA.html";
        }
        catch (error) {
            comenzarBtn.disabled = false;
            mensaje.textContent = error.message;
        }
    });
})();

document.addEventListener("DOMContentLoaded", function () {
    const searchInput = document.getElementById("clientTableSearch");

    if (searchInput) {
        searchInput.addEventListener("keyup", function () {
            // Pasamos lo que escribió a minúsculas para comparar sin importar las mayúsculas
            const filter = this.value.toLowerCase();

            // Seleccionamos todas las filas dentro del cuerpo de la tabla
            const rows = document.querySelectorAll("tbody tr");

            rows.forEach(row => {
                // El nombre completo del cliente está en la segunda columna (índice 1)
                const clientName = row.cells[1].textContent.toLowerCase();

                // Si el nombre incluye lo que escribió el usuario, mostramos la fila. Si no, la ocultamos.
                if (clientName.includes(filter)) {
                    row.style.display = "";
                } else {
                    row.style.display = "none";
                }
            });
        });
    }
});
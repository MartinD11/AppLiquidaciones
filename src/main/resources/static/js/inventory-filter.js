document.addEventListener("DOMContentLoaded", function () {
    const searchInput = document.getElementById("productTableSearch");

    if (searchInput) {
        searchInput.placeholder = "Buscar por Lote, Nombre, Cliente o Estado...";

        searchInput.addEventListener("keyup", function () {
            const filter = this.value.toLowerCase();
            const rows = document.querySelectorAll("tbody tr");

            rows.forEach(row => {
                const lotNumber = row.cells[1].textContent.toLowerCase();
                const productName = row.cells[2].textContent.toLowerCase();
                const clientName = row.cells[3].textContent.toLowerCase(); // NUEVA: Columna Cliente
                const status = row.cells[5].textContent.toLowerCase();     // El estado ahora es la celda 5

                if (lotNumber.includes(filter) || productName.includes(filter) || clientName.includes(filter) || status.includes(filter)) {
                    row.style.display = "";
                } else {
                    row.style.display = "none";
                }
            });
        });
    }
});
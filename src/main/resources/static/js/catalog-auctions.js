document.addEventListener("DOMContentLoaded", function () {

    // --- Lógica de Checkboxes (Seleccionar todos) ---
    const selectAllCheckbox = document.getElementById("selectAll");
    const productCheckboxes = document.querySelectorAll(".producto-checkbox");

    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener("change", function () {
            productCheckboxes.forEach(checkbox => {
                checkbox.checked = selectAllCheckbox.checked;
            });
        });
    }

    // --- Lógica del Modal de Editar Lote ---
    const editModal = document.getElementById("editLoteModal");
    const editForm = document.getElementById("editLoteForm");

    // IDs del HTML
    const editSalePrice = document.getElementById("editSalePrice");
    const editStatus = document.getElementById("editStatus");
    const editBuyer = document.getElementById("editBuyer"); // Nuevo campo comprador

    document.querySelectorAll(".btn-editar-lote").forEach(btn => {
        btn.addEventListener("click", function () {
            const id = this.getAttribute("data-id");
            const price = this.getAttribute("data-price");
            const status = this.getAttribute("data-status");
            const buyerId = this.getAttribute("data-buyer"); // Capturamos el comprador

            // Asignar valores
            editSalePrice.value = (price && price !== 'null') ? price : '';
            editStatus.value = status;

            // Si el comprador es null o vacío, reseteamos el select
            editBuyer.value = (buyerId && buyerId !== 'null') ? buyerId : '';

            editForm.action = "/products/update-sale/" + id;
            editModal.style.display = "flex";
        });
    });

    const closeBtn = document.getElementById("closeEditModalBtn");
    if (closeBtn) {
        closeBtn.addEventListener("click", () => editModal.style.display = "none");
    }

    // Cerrar si hacen clic fuera del contenido del modal
    window.addEventListener("click", function (event) {
        if (event.target === editModal) {
            editModal.style.display = "none";
        }
    });

    // Al final de tu archivo catalog-auctions.js, agregá esto:

    const newClientModal = document.getElementById("newClientModal");
    const openClientModalBtn = document.getElementById("openClientModalBtn");
    const closeClientModalBtn = document.getElementById("closeClientModalBtn");
    const saveClientBtn = document.getElementById("saveClientBtn");

// Abrir modal de cliente
    openClientModalBtn.addEventListener("click", () => newClientModal.style.display = "flex");
    closeClientModalBtn.addEventListener("click", () => newClientModal.style.display = "none");

// Guardar cliente nuevo (usando AJAX)
    saveClientBtn.addEventListener("click", function() {
        const data = {
            name: document.getElementById("clientName").value,
            lastName: document.getElementById("clientLastName").value
        };

        fetch('/clients/save-ajax', { // Este endpoint lo crearemos abajo
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        })
            .then(response => response.json())
            .then(newClient => {
                // Agregar al select existente
                const select = document.getElementById("editBuyer");
                const option = document.createElement("option");
                option.value = newClient.id;
                option.text = newClient.name + " " + newClient.lastName;
                select.add(option);
                select.value = newClient.id; // Seleccionarlo automáticamente

                newClientModal.style.display = "none";
            });
    });
});
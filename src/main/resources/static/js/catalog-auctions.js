document.addEventListener("DOMContentLoaded", function () {


    //Lógica del Checkbox "Seleccionar Todo"
    const selectAllCheckbox = document.getElementById("selectAll");
    const productCheckboxes = document.querySelectorAll(".producto-checkbox");

    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener("change", function () {
            productCheckboxes.forEach(checkbox => {
                checkbox.checked = selectAllCheckbox.checked;
            });
        });
    }


    //Elementos del Modal de Edicion de Lote
    const editModal = document.getElementById("editLoteModal");
    const editForm = document.getElementById("editLoteForm");
    const editSalePrice = document.getElementById("editSalePrice");
    const editStatus = document.getElementById("editStatus");

    // Elementos del nuevo buscador dinámico
    const editBuyerId = document.getElementById("editBuyerId");
    const clientSearch = document.getElementById("clientSearch");
    const searchResults = document.getElementById("clientSearchResults");

    // Lógica para abrir el modal al tocar el edit
    document.querySelectorAll(".btn-editar-lote").forEach(btn => {
        btn.addEventListener("click", function () {
            const id = this.getAttribute("data-id");
            const price = this.getAttribute("data-price");
            const status = this.getAttribute("data-status");
            const buyerId = this.getAttribute("data-buyer");
            const buyerName = this.getAttribute("data-buyer-name");

            editSalePrice.value = (price && price !== 'null') ? price : '';
            editStatus.value = status;

            // Cargamos los datos del buscador y el ID oculto
            editBuyerId.value = (buyerId && buyerId !== 'null') ? buyerId : '';
            if (clientSearch) {
                clientSearch.value = (buyerName && buyerName !== 'null') ? buyerName : '';
            }
            if (searchResults) {
                searchResults.style.display = 'none';
            }

            editForm.action = "/products/update-sale/" + id;
            editModal.style.display = "flex";
        });
    });

    // Lógica para cerrar el modal de edición
    const closeBtn = document.getElementById("closeEditModalBtn");
    if (closeBtn) {
        closeBtn.addEventListener("click", () => editModal.style.display = "none");
    }

    window.addEventListener("click", function (event) {
        if (event.target === editModal) {
            editModal.style.display = "none";
        }
    });

    //Lógica del Buscador (Autocompletado)

    let searchTimeout;

    if (clientSearch) {
        clientSearch.addEventListener("input", function() {
            clearTimeout(searchTimeout);
            const query = this.value.trim();

            // Si el usuario borra el texto, vaciamos el ID oculto y escondemos la lista
            if (query.length === 0) {
                editBuyerId.value = '';
                searchResults.style.display = 'none';
                return;
            }

            // Esperamos 300ms antes de buscar para no bombardear al servidor
            searchTimeout = setTimeout(() => {
                fetch('/clients/search?q=' + encodeURIComponent(query))
                    .then(response => response.json())
                    .then(clientes => {
                        searchResults.innerHTML = '';
                        if (clientes.length > 0) {
                            clientes.forEach(cliente => {
                                const li = document.createElement('li');
                                li.textContent = cliente.name + ' ' + cliente.lastName;

                                // Al hacer clic en un cliente de la lista
                                li.addEventListener('click', function() {
                                    clientSearch.value = this.textContent;
                                    editBuyerId.value = cliente.id;
                                    searchResults.style.display = 'none';
                                });

                                searchResults.appendChild(li);
                            });
                            searchResults.style.display = 'block';
                        } else {
                            searchResults.style.display = 'none';
                        }
                    })
                    .catch(err => console.error("Error buscando clientes:", err));
            }, 300);
        });
    }

    // Ocultar resultados de búsqueda si hace clic en cualquier lado de la pantalla
    document.addEventListener("click", function(e) {
        if (clientSearch && searchResults && e.target !== clientSearch && e.target !== searchResults) {
            searchResults.style.display = 'none';
        }
    });

    //  Modal para Crear Nuevo Cliente (+ Ajax)
    const newClientModal = document.getElementById("newClientModal");
    const openClientModalBtn = document.getElementById("openClientModalBtn");
    const closeClientModalBtn = document.getElementById("closeClientModalBtn");
    const saveClientBtn = document.getElementById("saveClientBtn");

    if (openClientModalBtn) {
        openClientModalBtn.addEventListener("click", () => newClientModal.style.display = "flex");
    }

    if (closeClientModalBtn) {
        closeClientModalBtn.addEventListener("click", () => newClientModal.style.display = "none");
    }

    if (saveClientBtn) {
        saveClientBtn.addEventListener("click", function() {
            const data = {
                name: document.getElementById("clientName").value,
                lastName: document.getElementById("clientLastName").value
            };

            fetch('/clients/save-ajax', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            })
                .then(response => response.json())
                .then(newClient => {
                    if (clientSearch && editBuyerId) {
                        clientSearch.value = newClient.name + " " + newClient.lastName;
                        editBuyerId.value = newClient.id;
                    }

                    newClientModal.style.display = "none";

                    document.getElementById("clientName").value = '';
                    document.getElementById("clientLastName").value = '';
                })
                .catch(err => console.error("Error al guardar cliente:", err));
        });
    }
});
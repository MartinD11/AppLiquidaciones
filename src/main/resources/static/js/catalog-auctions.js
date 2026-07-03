document.addEventListener("DOMContentLoaded", function () {

    const selectAllCheckbox = document.getElementById("selectAll");
    const productCheckboxes = document.querySelectorAll(".producto-checkbox");

    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener("change", function () {
            productCheckboxes.forEach(checkbox => {
                checkbox.checked = selectAllCheckbox.checked;
            });
        });
    }

    const editModal = document.getElementById("editLoteModal");
    const editForm = document.getElementById("editLoteForm");

    const editSalePrice = document.getElementById("editSalePrice");
    const editStatus = document.getElementById("editStatus");
    const editBuyer = document.getElementById("editBuyer");

    document.querySelectorAll(".btn-editar-lote").forEach(btn => {
        btn.addEventListener("click", function () {
            const id = this.getAttribute("data-id");
            const price = this.getAttribute("data-price");
            const status = this.getAttribute("data-status");
            const buyerId = this.getAttribute("data-buyer");

            editSalePrice.value = (price && price !== 'null') ? price : '';
            editStatus.value = status;

            editBuyer.value = (buyerId && buyerId !== 'null') ? buyerId : '';

            editForm.action = "/products/update-sale/" + id;
            editModal.style.display = "flex";
        });
    });

    const closeBtn = document.getElementById("closeEditModalBtn");
    if (closeBtn) {
        closeBtn.addEventListener("click", () => editModal.style.display = "none");
    }

    window.addEventListener("click", function (event) {
        if (event.target === editModal) {
            editModal.style.display = "none";
        }
    });


    const newClientModal = document.getElementById("newClientModal");
    const openClientModalBtn = document.getElementById("openClientModalBtn");
    const closeClientModalBtn = document.getElementById("closeClientModalBtn");
    const saveClientBtn = document.getElementById("saveClientBtn");


    openClientModalBtn.addEventListener("click", () => newClientModal.style.display = "flex");
    closeClientModalBtn.addEventListener("click", () => newClientModal.style.display = "none");


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
                const select = document.getElementById("editBuyer");
                const option = document.createElement("option");
                option.value = newClient.id;
                option.text = newClient.name + " " + newClient.lastName;
                select.add(option);
                select.value = newClient.id;

                newClientModal.style.display = "none";
            });
    });
});
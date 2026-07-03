document.addEventListener("DOMContentLoaded", function () {

    const editModal = document.getElementById("editModal");
    const closeEditModalBtn = document.getElementById("closeModalBtn");
    const editForm = document.getElementById("editForm");

    const editName = document.getElementById("editName");
    const editLot = document.getElementById("editLot");
    const editPrice = document.getElementById("editPrice");

    const botonesEditar = document.querySelectorAll(".btn-editar");

    botonesEditar.forEach(boton => {
        boton.addEventListener("click", function () {
            const id = this.getAttribute("data-id");
            const name = this.getAttribute("data-name");
            const lot = this.getAttribute("data-lot");
            const price = this.getAttribute("data-price");

            editName.value = name;
            editLot.value = (lot && lot !== 'null') ? lot : '';
            editPrice.value = (price && price !== 'null') ? price : '';

            editForm.action = "/products/update/" + id;
            editModal.style.display = "flex";
        });
    });

    closeEditModalBtn.addEventListener("click", function () {
        editModal.style.display = "none";
    });

    const deleteModal = document.getElementById("deleteModal");
    const closeDeleteModalBtn = document.getElementById("closeDeleteModalBtn");
    const confirmDeleteBtn = document.getElementById("confirmDeleteBtn");
    const deleteProductName = document.getElementById("deleteProductName");

    const botonesEliminar = document.querySelectorAll(".btn-delete-modal");

    botonesEliminar.forEach(boton => {
        boton.addEventListener("click", function (event) {
            event.preventDefault();

            const id = this.getAttribute("data-id");
            const name = this.getAttribute("data-name");

            deleteProductName.textContent = name;

            confirmDeleteBtn.href = "/products/delete/" + id;

            deleteModal.style.display = "flex";
        });
    });

    closeDeleteModalBtn.addEventListener("click", function () {
        deleteModal.style.display = "none";
    });


    window.addEventListener("click", function (event) {
        if (event.target === editModal) {
            editModal.style.display = "none";
        }
        if (event.target === deleteModal) {
            deleteModal.style.display = "none";
        }
    });
});
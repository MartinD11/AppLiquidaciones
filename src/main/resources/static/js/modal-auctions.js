document.addEventListener("DOMContentLoaded", function () {

    // --- MODAL EDITAR ---
    const editModal = document.getElementById("editModal");
    const closeEditModalBtn = document.getElementById("closeModalBtn");
    const editForm = document.getElementById("editForm");
    const editDate = document.getElementById("editDate");

    document.querySelectorAll(".btn-editar").forEach(boton => {
        boton.addEventListener("click", function () {
            const id = this.getAttribute("data-id");
            const date = this.getAttribute("data-date");

            editDate.value = date;
            editForm.action = "/auctions/update/" + id;
            editModal.style.display = "flex";
        });
    });

    closeEditModalBtn.addEventListener("click", () => editModal.style.display = "none");

    // --- MODAL ELIMINAR ---
    const deleteModal = document.getElementById("deleteModal");
    const closeDeleteModalBtn = document.getElementById("closeDeleteModalBtn");
    const confirmDeleteBtn = document.getElementById("confirmDeleteBtn");
    const deleteAuctionDate = document.getElementById("deleteAuctionDate");

    document.querySelectorAll(".btn-delete-modal").forEach(boton => {
        boton.addEventListener("click", function (event) {
            event.preventDefault();
            const id = this.getAttribute("data-id");
            const date = this.getAttribute("data-date");

            deleteAuctionDate.textContent = date;
            confirmDeleteBtn.href = "/auctions/delete/" + id;
            deleteModal.style.display = "flex";
        });
    });

    closeDeleteModalBtn.addEventListener("click", () => deleteModal.style.display = "none");

    // Cerrar al hacer clic fuera
    window.addEventListener("click", function (event) {
        if (event.target === editModal) editModal.style.display = "none";
        if (event.target === deleteModal) deleteModal.style.display = "none";
    });
});
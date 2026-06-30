document.addEventListener('DOMContentLoaded', () => {
    const modal = document.getElementById('editModal');
    const form = document.getElementById('editForm');
    const closeBtn = document.getElementById('closeModalBtn');
    const table = document.querySelector('table');

    table.addEventListener('click', (event) => {
        const btn = event.target.closest('.btn-editar');
        if (btn) {
            const id = btn.getAttribute('data-id');
            const name = btn.getAttribute('data-name');
            const lastName = btn.getAttribute('data-lastname');

            document.getElementById('editName').value = name;
            document.getElementById('editLastName').value = lastName;
            form.action = `/clients/update/${id}`;
            modal.style.display = 'flex';
        }
    });


    closeBtn.addEventListener('click', () => modal.style.display = 'none');

    window.addEventListener('click', (event) => {
        if (event.target === modal) modal.style.display = 'none';
    });

    //delete modal

    const deleteModal = document.getElementById('deleteModal');
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    const cancelDeleteBtn = document.getElementById('cancelDeleteBtn');

    document.querySelector('table').addEventListener('click', (event) => {
        // Si hicieron clic en el botón de eliminar (o en el icono dentro del botón)
        const deleteBtn = event.target.closest('.btn-eliminar');
        if (deleteBtn) {
            event.preventDefault(); // Evitamos que navegue directo
            confirmDeleteBtn.href = deleteBtn.getAttribute('href'); // Pasamos el link
            deleteModal.style.display = 'flex'; // Abrimos el modal
        }
    });

    cancelDeleteBtn.addEventListener('click', () => {
        deleteModal.style.display = 'none';
    });

});
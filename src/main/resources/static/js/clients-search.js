document.addEventListener("DOMContentLoaded", function () {
    const searchInput = document.getElementById("clientTableSearch");

    if (searchInput) {
        searchInput.addEventListener("keyup", function () {
            const filter = this.value.toLowerCase();

            const rows = document.querySelectorAll("tbody tr");

            rows.forEach(row => {
                const clientName = row.cells[1].textContent.toLowerCase();

                if (clientName.includes(filter)) {
                    row.style.display = "";
                } else {
                    row.style.display = "none";
                }
            });
        });
    }
});
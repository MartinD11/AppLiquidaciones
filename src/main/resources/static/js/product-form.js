document.addEventListener("DOMContentLoaded", function () {
    const clientSearch = document.getElementById("clientSearch");
    const sellerId = document.getElementById("sellerId");
    const searchResults = document.getElementById("clientSearchResults");
    let searchTimeout;

    if (clientSearch) {
        clientSearch.addEventListener("input", function() {
            clearTimeout(searchTimeout);
            const query = this.value.trim();

            if (query.length === 0) {
                sellerId.value = '';
                searchResults.style.display = 'none';
                return;
            }

            searchTimeout = setTimeout(() => {
                fetch('/clients/search?q=' + encodeURIComponent(query))
                    .then(response => response.json())
                    .then(clientes => {
                        searchResults.innerHTML = '';
                        if (clientes.length > 0) {
                            clientes.forEach(cliente => {
                                const li = document.createElement('li');
                                li.textContent = cliente.name + ' ' + cliente.lastName;

                                li.addEventListener('click', function() {
                                    clientSearch.value = this.textContent;
                                    sellerId.value = cliente.id;
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

    document.addEventListener("click", function(e) {
        if (clientSearch && searchResults && e.target !== clientSearch && e.target !== searchResults) {
            searchResults.style.display = 'none';
        }
    });
});
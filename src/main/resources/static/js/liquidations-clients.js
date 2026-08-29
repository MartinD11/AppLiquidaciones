document.addEventListener("DOMContentLoaded", function () {
    const clientSearch = document.getElementById("clientSearch");
    const searchClientId = document.getElementById("searchClientId");
    const clientSearchResults = document.getElementById("clientSearchResults");
    const form = document.getElementById("liquidationForm");
    const preselectedName = document.getElementById("selectedClientName");

    let searchTimeout;

    if (preselectedName && clientSearch) {
        clientSearch.value = preselectedName.value;
    }

    if (clientSearch) {
        clientSearch.addEventListener("input", function() {
            clearTimeout(searchTimeout);
            const query = this.value.trim();

            if (query.length === 0) {
                searchClientId.value = '';
                clientSearchResults.style.display = 'none';
                form.submit();
                return;
            }

            searchTimeout = setTimeout(() => {
                fetch('/clients/search-historical?q=' + encodeURIComponent(query))
                    .then(response => response.json())
                    .then(clientes => {
                        clientSearchResults.innerHTML = '';

                        const clearLi = document.createElement('li');
                        clearLi.innerHTML = '<em><i class="fa-solid fa-times-circle"></i> Limpiar filtro (Ver todos)</em>';
                        clearLi.style.color = '#64748b';
                        clearLi.addEventListener('click', function() {
                            clientSearch.value = '';
                            searchClientId.value = '';
                            clientSearchResults.style.display = 'none';
                            form.submit();
                        });
                        clientSearchResults.appendChild(clearLi);

                        if (clientes.length > 0) {
                            clientes.forEach(cliente => {
                                const li = document.createElement('li');
                                li.textContent = cliente.name + ' ' + cliente.lastName;

                                li.addEventListener('click', function() {
                                    clientSearch.value = this.textContent;
                                    searchClientId.value = cliente.id;
                                    clientSearchResults.style.display = 'none';

                                    form.submit();
                                });

                                clientSearchResults.appendChild(li);
                            });
                        }
                        clientSearchResults.style.display = 'block';
                    })
                    .catch(err => console.error("Error buscando clientes:", err));
            }, 300);
        });
    }

    document.addEventListener("click", function(e) {
        if (clientSearch && clientSearchResults && e.target !== clientSearch && e.target !== clientSearchResults) {
            clientSearchResults.style.display = 'none';
        }
    });
});
document.addEventListener("DOMContentLoaded", function () {


    // autocompletado de los clientes

    const clientSearch = document.getElementById("clientSearch");
    const searchClientId = document.getElementById("searchClientId");
    const clientSearchResults = document.getElementById("clientSearchResults");
    let clientTimeout;

    if (clientSearch) {
        clientSearch.addEventListener("input", function() {
            clearTimeout(clientTimeout);
            const query = this.value.trim();

            if (query.length === 0) {
                searchClientId.value = '';
                clientSearchResults.style.display = 'none';
                return;
            }

            clientTimeout = setTimeout(() => {
                fetch('/clients/search?q=' + encodeURIComponent(query))
                    .then(response => response.json())
                    .then(clientes => {
                        clientSearchResults.innerHTML = '';
                        if (clientes.length > 0) {
                            clientes.forEach(cliente => {
                                const li = document.createElement('li');
                                li.textContent = cliente.name + ' ' + cliente.lastName;
                                li.addEventListener('click', function() {
                                    clientSearch.value = this.textContent;
                                    searchClientId.value = cliente.id;
                                    clientSearchResults.style.display = 'none';
                                });
                                clientSearchResults.appendChild(li);
                            });
                            clientSearchResults.style.display = 'block';
                        } else {
                            clientSearchResults.style.display = 'none';
                        }
                    })
                    .catch(err => console.error("Error buscando clientes:", err));
            }, 300);
        });
    }


    // autocompletado de productos(lotes)

    const productSearch = document.getElementById("productSearch");
    const productSearchResults = document.getElementById("productSearchResults");
    let productTimeout;

    if (productSearch) {
        productSearch.addEventListener("input", function() {
            clearTimeout(productTimeout);
            const query = this.value.trim();

            if (query.length === 0) {
                productSearchResults.style.display = 'none';
                return;
            }

            productTimeout = setTimeout(() => {
                fetch('/products/search-ajax?q=' + encodeURIComponent(query))
                    .then(response => response.json())
                    .then(productos => {
                        productSearchResults.innerHTML = '';
                        if (productos.length > 0) {
                            productos.forEach(producto => {
                                const li = document.createElement('li');
                                // Mostramos el nombre y el lote si lo tiene para más contexto
                                li.textContent = producto.name + (producto.lotNumber ? ' (Lote: ' + producto.lotNumber + ')' : '');

                                li.addEventListener('click', function() {
                                    productSearch.value = producto.name;
                                    productSearchResults.style.display = 'none';
                                });
                                productSearchResults.appendChild(li);
                            });
                            productSearchResults.style.display = 'block';
                        } else {
                            productSearchResults.style.display = 'none';
                        }
                    })
                    .catch(err => console.error("Error buscando productos:", err));
            }, 300);
        });
    }


    // con esto oculto las listas al hacer clcik fuera del rango
    document.addEventListener("click", function(e) {
        if (clientSearch && clientSearchResults && e.target !== clientSearch && e.target !== clientSearchResults) {
            clientSearchResults.style.display = 'none';
        }
        if (productSearch && productSearchResults && e.target !== productSearch && e.target !== productSearchResults) {
            productSearchResults.style.display = 'none';
        }
    });
});
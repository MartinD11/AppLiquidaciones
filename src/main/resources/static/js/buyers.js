
function abrirModalDetalle(id) {
    const modal = document.getElementById('modal-' + id);
    if (modal) {
        modal.style.display = 'flex';
    }
}

function cerrarModalDetalle(id) {
    const modal = document.getElementById('modal-' + id);
    if (modal) {
        modal.style.display = 'none';
    }
}

function abrirModalPago(liquidationId, auctionId) {
    const modal = document.getElementById('modalPago');
    const form = document.getElementById('formModalPago'); // Asegurate de que el form tenga este ID en tu HTML
    const inputAuction = document.getElementById('inputModalAuctionId'); // Asegurate de que el input hidden tenga este ID

    if (modal && form && inputAuction) {
        form.action = '/buyers/pay/' + liquidationId;
        inputAuction.value = auctionId;
        modal.style.display = 'flex';
    }
}

function cerrarModalPago() {
    const modal = document.getElementById('modalPago');
    if (modal) {
        modal.style.display = 'none';
    }
}

window.addEventListener('click', function(event) {
    if (event.target.classList.contains('modal-overlay')) {
        event.target.style.display = "none";
    }
});
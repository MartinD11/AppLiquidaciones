function abrirModalPago(liquidationId, auctionId) {
    const modal = document.getElementById('modalPago');
    const form = document.getElementById('formModalPago');
    const inputAuction = document.getElementById('inputModalAuctionId');

    if (modal && form && inputAuction) {
        form.action = '/liquidations/pay/' + liquidationId;

        inputAuction.value = auctionId;

        modal.style.display = 'flex';
    }
}

function cerrarModalPago() {
    const modal = document.getElementById('modalPago');
    const form = document.getElementById('formModalPago');

    if (modal && form) {
        modal.style.display = 'none';
        form.action = '';
    }
}

window.onclick = function(event) {
    const modal = document.getElementById('modalPago');
    if (event.target === modal) {
        cerrarModalPago();
    }
}
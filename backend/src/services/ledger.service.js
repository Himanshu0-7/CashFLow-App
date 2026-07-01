const ledgerGateway = require("../socket/ledger.gateway")

exports.create = async (ledgerData) => {
    return ledgerGateway.createLedger(ledgerData);
}
exports.alter = async (ledgerData) => {
    return ledgerGateway.alterLedger(ledgerData);
}
exports.delete = async (ledgerData) => {
    return ledgerGateway.deleteLedger(ledgerData);
}
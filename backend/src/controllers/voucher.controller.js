const voucherService = require("../services/voucher.service")

exports.createLedger = async (req, res) =>{
    const response = await voucherService.create(req.body);
    res.status(200).json(response)
}
exports.alterLedger = async (req, res) =>{
    const response = await voucherService.alter(req.body);
    res.status(200).json(response)
}
exports.deleteLedger = async (req, res) =>{
    const response = await voucherService.delete(req.body);
    res.status(200).json(response)
}
const legderService = require("../services/ledger.service")

exports.create = async (req, res) =>{
    const response = await legderService.create(req.body);
    res.status(200).json(response)
}
exports.alter = async (req, res) =>{
    const response = await legderService.alter(req.body);
    res.status(200).json(response)
}
exports.delete = async (req, res) =>{
    const response = await legderService.delete(req.body);
    res.status(200).json(response)
}
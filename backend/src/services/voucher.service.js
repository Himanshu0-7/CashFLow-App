const voucherGateway = require("../socket/voucher.gateway")

exports.create = async (voucherData) => {
    return voucherGateway.create(voucherData);
}
exports.alter = async (voucherData) => {
    return voucherGateway.alter(voucherData);
}
exports.delete = async (voucherData) => {
    return voucherGateway.delete(voucherData);
}
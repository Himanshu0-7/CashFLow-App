const agentManager = require("./agentManager");

function emitToAgent(event, data) {
    const socket = agentManager.getAgent();

    if (!socket) {
        throw new Error("Python Agent is not connected.");
    }

    socket.emit(event, data);
}

exports.create = (data) => emitToAgent("create-voucher", data);
exports.alter = (data) => emitToAgent("alter-voucher", data);
exports.delete = (data) => emitToAgent("delete-voucher", data);
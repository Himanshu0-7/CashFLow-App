const agentManager = require("./agentManager");

function emit(event, data) {
    const socket = agentManager.getAgent();

    if (!socket) {
        throw new Error("Python Agent is not connected.");
    }

    socket.emit(event, data);
}

exports.load = (data) => {
    emit("load-company", data);
};

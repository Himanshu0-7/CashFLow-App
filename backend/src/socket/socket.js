const agentManger = require("./agentManager")

module.exports = (io) => {

    io.on("connection", (socket) => {


        socket.emits("register-agent", (data)=>{
            agentManger.setAgent(data.agentId, socket)
        })
        console.log("Client Connected");

        socket.on("disconnect", () => {

            console.log("Client Disconnected");

        });

    });

};
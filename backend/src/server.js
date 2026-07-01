const http = require("http");
const { Server } = require("socket.io");

const app = require("./app");
const registerSocket = require("./socket/socket");

const server = http.createServer(app);

const io = new Server(server);

registerSocket(io);

server.listen(3000, () => {
    console.log("Server Running");
});
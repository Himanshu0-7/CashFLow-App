const  agents = new Map()

exports.setAgent = (agentId, socket) =>{
    agents.set(agentId, socket)
}
exports.getAgent = ()=>{
    return agents.get(agentId)
}
exports.removeAgent = (agentId) => {
    agents.delete(agentId);
};
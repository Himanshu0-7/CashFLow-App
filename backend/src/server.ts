import Fastify from "fastify";
const app = Fastify({ logger: true });

app.get("/", async () => {
  return { ok: true };
});

app.listen({ port: 3000 }).then(() => {
  console.log("Server running on http://localhost:3000");
});

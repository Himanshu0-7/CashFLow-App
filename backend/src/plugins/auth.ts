import { FastifyReply, FastifyRequest } from "fastify";
import jwt from "jsonwebtoken";
export async function auth(request: FastifyRequest, reply: FastifyReply) {
  const header = request.headers.authorization;

  if (!header) {
    return reply.code(401).send({ message: "No token" });
  }
  try {
    const decoded = jwt.verify(
      header,
      process.env.JWT_SECRET || "secret",
    ) as AuthPayload;

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (request as any).user = decoded;
  } catch {
    return reply.code(401).send({ message: "Invalid or expired token" });
  }
}

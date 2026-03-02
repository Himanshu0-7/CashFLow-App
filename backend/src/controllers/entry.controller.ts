import { FastifyReply, FastifyRequest } from "fastify";
import { prisma } from "../utils/prisma.js";

export async function updateEntry(
  req: FastifyRequest<{
    Params: { id: string };
    Body: { encryptedData: string };
  }>,
  reply: FastifyReply,
) {
  const userId = req.user.id;
  const { id } = req.params;
  const { encryptedData } = req.body;

  await prisma.entry.updateMany({
    where: {
      id,
      userId,
    },
    data: {
      encryptedData,
    },
  });
  reply.send({ message: "Updated Successfully" });
}
export async function deleteEntry(
  req: FastifyRequest<{
    Params: { id: string };
  }>,
  reply: FastifyReply,
) {
  const userId = req.user.id;
  const { id } = req.params;

  await prisma.entry.deleteMany({
    where: {
      id,
      userId,
    },
  });
  reply.send({ message: "Deleted Successfully" });
}

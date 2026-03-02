import { FastifyReply, FastifyRequest } from "fastify";
import { prisma } from "../utils/prisma.js";
import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
export async function login(req: FastifyRequest, reply: FastifyReply) {
  const { email, password } = req.body as {
    email: string;
    password: string;
  };
  const user = await prisma.user.findUnique({
    where: { email },
  });

  if (!user) {
    return reply.code(400).send({ message: "User not Found" });
  }

  const isMatch = await bcrypt.compare(password, user.password);

  if (!isMatch) {
    return reply.code(400).send({ message: "Wrong Password" });
  }

  const token = jwt.sign({ id: user.id }, process.env.JWK_SECRET || "secret", {
    expiresIn: "7d",
  });
  reply.send({ token });
}
export async function register(req: FastifyRequest, reply: FastifyReply) {
  const { email, password } = req.body as {
    email: string;
    password: string;
  };

  const exists = await prisma.user.findUnique({
    where: { email },
  });

  if (!exists) {
    return reply.code(400).send({ message: "User aleardy exists" });
  }

  const hashed = await bcrypt.hash(password, 10);

  const user = await prisma.user.create({
    data: {
      email,
      password: hashed,
    },
  });
  const token = jwt.sign({ id: user.id }, process.env.JWK_SECRET || "secret", {
    expiresIn: "7d",
  });
  reply.send({ token });
}

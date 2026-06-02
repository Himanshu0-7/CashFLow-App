import { Request, Response } from "express";
import prisma from "../utils/prisma";

export const getBooks = async (req: Request, res: Response) => {
  const books = await prisma.book.findMany();
  res.json(books);
};

export const createBook = async (req: Request, res: Response) => {
  const { name, type, balance } = req.body;

  const book = await prisma.book.create({
    data: { name, type, balance },
  });

  res.json(book);
};

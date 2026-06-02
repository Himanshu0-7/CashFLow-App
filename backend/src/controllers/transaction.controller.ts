import { Request, Response } from "express";
import prisma from "../utils/prisma";

export const getTransactions = async (req: Request, res: Response) => {
  const data = await prisma.transaction.findMany();
  res.json(data);
};

export const createTransaction = async (req: Request, res: Response) => {
  const { title, amount, category, location, date, type, bookId } = req.body;

  const transaction = await prisma.transaction.create({
    data: {
      title,
      amount,
      category,
      location,
      date: new Date(date),
      type,
      bookId,
    },
  });

  res.json(transaction);
};

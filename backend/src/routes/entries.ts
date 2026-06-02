import express from "express";
import {
  getTransactions,
  createTransaction,
} from "../controllers/transaction.controller";
import { getBooks, createBook } from "../controllers/book.controller";

const router = express.Router();

router.get("/transactions", getTransactions);
router.post("/transactions", createTransaction);

router.get("/books", getBooks);
router.post("/books", createBook);

export default router;

import express from "express";
import "dotenv/config";
import cors from "cors";
import routes from "./routes/index";

const app = express();

app.use(cors());
app.use(express.json());

app.use("/api", routes);

app.listen(3000, "0.0.0.0", () => {
  console.log("Server running on http://localhost:3000");
});

const express = require("express");
const router = express.Router();
const {register, login, profile} = require("../controllers/auth.controller");
const { authenticate } = require("../middleware/auth.middleware");
router.get("/", (req, res) => {
    res.status(200).json({
        message: "Auth Route Works!"
    });
});

router.post("/register", register);
router.post("/login", login);
router.get("/profile", authenticate, profile)
module.exports = router;
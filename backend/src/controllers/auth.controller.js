const authService = require("../services/auth.service");

exports.register = async (req, res, next) => {
    try {
        const response = await authService.register(req.body);

        res.status(201).json(response);
    } catch (err) {
        next(err)
    }
};

exports.login = async (req, res, next) => {
    try {
        const response = await authService.login(req.body);

        res.status(200).json(response);
    } catch (err) {
        next(err);
    }
};
exports.profile = async (req, res) =>{
    res.status(200).json({
        message:"Profile fetched Successfully",
        user: req.user
    })
}
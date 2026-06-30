const prisma = require("../lib/prisma");
const bcrypt = require('bcrypt')
const jwt = require("jsonwebtoken");

exports.register = async (userData) => {
    console.log(userData);
    const existingUser = await prisma.user.findUnique({
        where: {
            email: userData.email
        }
    })
    if (existingUser) throw new Error("Email already exists");
    const hashedPassword = await bcrypt.hash(userData.password, 10);

    const user = await prisma.user.create({
        data: {
            name: userData.name,
            email: userData.email,
            password: hashedPassword
        }
    })
    const { password, ...userWithoutPassword } = user;

    return userWithoutPassword;

};

exports.login = async (userData) => {
    const user = await prisma.user.findUnique({
        where: {
            email: userData.email
        }
    })
    if (!user) throw new AppError("Invalid email or password",401);

    const isMatch = await bcrypt.compare(
        userData.password,
        user.password
    )
    if (!isMatch) throw new AppError("Invalid email or password",401);

    const token = jwt.sign(
        {
            userId: user.id,
            email: user.email
        },
        process.env.JWT_SECRET,
        {
            expiresIn: "7d"
        }
    )
    return {
        token,
        user: {
            id: user.id,
            name: user.name,
            email: user.email
        }
    }
};
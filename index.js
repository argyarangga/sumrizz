const express = require("express")
const translate = require("./module/translate/route")

const router = express.Router()

router.use('/translate', translate)

module.exports = router


const express = require("express")
const controller = require('./controller')
const router = express.Router()

// router.post('/url', controller.inputUrl)
router.post('/text', controller.inputText)
router.post('/url', controller.inputUrl)
router.get('/sukses', controller.sukses)

module.exports = router